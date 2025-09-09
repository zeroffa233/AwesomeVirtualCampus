package app.vcampus.client.scene;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShopController {

    // FXML Injected Fields
    @FXML private AnchorPane rootPane;
    @FXML private GridPane itemsGrid;
    @FXML private JFXTextField searchField;
    @FXML private JFXButton searchButton;
    @FXML private HBox bottomBar;
    @FXML private JFXButton toggleCartButton;
    @FXML private Label itemCountLabel;
    @FXML private Label totalPriceLabel;
    @FXML private VBox cartView;
    @FXML private Label cartTotalPriceLabel;
    @FXML private Label cartItemCountLabel;
    @FXML private VBox cartItemsContainer;
    @FXML private StackPane overlayPane; // 新增: 遮罩窗格


    // ViewModel / State Properties
    private final ObservableList<ShopItem> allItems = FXCollections.observableArrayList();
    private final ObservableList<ShopItem> displayedItems = FXCollections.observableArrayList();

    private final ObservableList<ShopItem> chosenItems = FXCollections.observableArrayList();
    private final IntegerProperty chosenItemsCount = new SimpleIntegerProperty(0);
    private final DoubleProperty chosenItemsPrice = new SimpleDoubleProperty(0.0);

    // --- 全新的动画逻辑，灵感来自你的代码 ---
    private boolean isCartVisible = false;
    private static final Duration ANIMATION_SPEED = Duration.millis(300); // 稍微放慢一点点，效果更优雅
    private static final Interpolator CUSTOM_EASING = Interpolator.SPLINE(0.4, 0.1, 0.2, 1.0);
    private ParallelTransition cartAnimation;
    // --- 动画逻辑结束 ---

    // Initialization
    @FXML
    public void initialize() {
        loadData(); // Data(ShopItem) -> allItems(private final ObservableList<ShopItem>)
        displayedItems.setAll(allItems);
        setupBindings();
        setupListeners();
        populateItemsGrid();

        // 初始时将购物车视图完全移出屏幕下方
        // 使用一个监听器确保即使窗口大小改变，其位置也正确
        rootPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (!isCartVisible) {
                cartView.setTranslateY(newVal.doubleValue());
            }
        });

        // 首次加载时设置初始位置
        cartView.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                cartView.setTranslateY(rootPane.getHeight());
            }
        });

    }

    // --- Data Loading ---
    private void loadData() {
        // Simulating the items from the image
        allItems.add(new ShopItem("正版 黑暗之魂官方艺术设定集 全套1-2-3册 DARK SOULS", 249.00, "/images/500.png"));
        allItems.add(new ShopItem("Sony/索尼 ECM-G1 枪型麦克风 大尺寸收音单元 清晰人声", 999.00, "/images/500.png"));
        allItems.add(new ShopItem("Apple/苹果 13 英寸 MacBook Air Apple M2 芯片 8 核中央处理器", 12699.00, "/images/500.png"));
        // Add more items to see the grid wrap
        allItems.add(new ShopItem("C++ Primer Plus (第6版)", 89.50, "/images/500.png"));
        allItems.add(new ShopItem("小米便携风扇", 59.00, "/images/500.png"));
        allItems.add(new ShopItem("原神 | 刻晴手办", 888.00, "/images/500.png"));
        allItems.add(new ShopItem("原神 | 刻晴手办", 888.00, "/images/500.png"));
        allItems.add(new ShopItem("原神 | 刻晴手办", 888.00, "/images/500.png"));
        allItems.add(new ShopItem("原神 | 刻晴手办", 888.00, "/images/500.png"));
        allItems.add(new ShopItem("原神 | 刻晴手办", 888.00, "/images/500.png"));
        allItems.add(new ShopItem("原神 | 刻晴手办", 888.00, "/images/500.png"));
        allItems.add(new ShopItem("原神 | 刻晴手办", 888.00, "/images/500.png"));
        allItems.add(new ShopItem("原神 | 刻晴手办", 888.00, "/images/500.png"));
        allItems.add(new ShopItem("原神 | 刻晴手办", 888.00, "/images/500.png"));
        allItems.add(new ShopItem("原神 | 刻晴手办", 888.00, "/images/500.png"));
        allItems.add(new ShopItem("原神 | 刻晴手办", 888.00, "/images/500.png"));
        allItems.add(new ShopItem("原神 | 刻晴手办", 888.00, "/images/500.png"));
        allItems.add(new ShopItem("原神 | 刻晴手办", 888.00, "/images/500.png"));
        allItems.add(new ShopItem("原神 | 刻晴手办", 888.00, "/images/500.png"));
        allItems.add(new ShopItem("原神 | 刻晴手办", 888.00, "/images/500.png"));
    }

    // --- 动画和事件处理 ---
    @FXML
    private void toggleCart() {
        // 1. 如果动画正在进行，先停止它，实现可打断效果
        if (cartAnimation != null) {
            cartAnimation.stop();
        }

        // 决定是显示还是隐藏
        boolean show = !isCartVisible;

        // 2. 创建并行动画组，包含购物车的平移动画和遮罩的淡入淡出动画
        TranslateTransition cartTransition = new TranslateTransition(ANIMATION_SPEED, cartView);
        FadeTransition overlayFade = new FadeTransition(ANIMATION_SPEED, overlayPane);

        // 3. 应用你提供的非线性插值器，让动画更具物理感
        cartTransition.setInterpolator(CUSTOM_EASING);
        overlayFade.setInterpolator(CUSTOM_EASING);

        if (show) {
            // --- 显示购物车 ---
            overlayPane.setVisible(true); // 动画开始前，让遮罩可见
            cartTransition.setToY(0); // 目标：移动到屏幕内
            overlayFade.setToValue(0.6); // 目标：遮罩淡入到60%不透明度
            toggleCartButton.setText("返回");
        } else {
            // --- 隐藏购物车 ---
            cartTransition.setToY(rootPane.getHeight()); // 目标：移出屏幕
            overlayFade.setToValue(0.0); // 目标：遮罩完全淡出
            toggleCartButton.setText("结算");
        }

        // 4. 将两个动画组合成一个并行动画
        cartAnimation = new ParallelTransition(cartTransition, overlayFade);

        // 5. 关键：在隐藏动画结束后，再将遮罩设为不可见，以释放鼠标事件
        if (!show) {
            cartAnimation.setOnFinished(event -> overlayPane.setVisible(false));
        }

        cartAnimation.play();
        isCartVisible = show; // 更新状态
    }

    // --- UI Population ---
    private void populateItemsGrid() {
        itemsGrid.getChildren().clear();
        int col = 0;
        int row = 0;
        for (ShopItem item : displayedItems) {
            Node itemCard = createShopItemCard(item);
            itemsGrid.add(itemCard, col, row);
            col++;
            if (col == 3) {
                col = 0;
                row++;
            }
        }
    }

    private Node createShopItemCard(ShopItem item) {
        // 1. 调整主卡片VBox的对齐方式为顶部居中
        VBox card = new VBox(15); // 稍微增加一点间距
        card.setPrefWidth(250);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        card.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.1)));
        card.setPadding(new Insets(10));
        card.setAlignment(Pos.TOP_CENTER); // 从 CENTER_LEFT 改为 TOP_CENTER，这将使图片框水平居中

        // --- 全新的、更健壮的图片处理逻辑 ---

        // 2. 严格限定图片容器的尺寸，使其成为一个真正的“框”
        double viewportSize = 220.0;
        StackPane imageContainer = new StackPane();
        // 通过同时设置min, pref, max尺寸，我们强制这个“框”的布局边界固定为220x220
        imageContainer.setMinSize(viewportSize, viewportSize);
        imageContainer.setPrefSize(viewportSize, viewportSize);
        imageContainer.setMaxSize(viewportSize, viewportSize);
        imageContainer.setStyle("-fx-background-color: #F0F0F0; -fx-background-radius: 4;"); // 添加背景色和圆角

        // 3. 移除 setClip()，因为StackPane本身就会裁剪超出其边界的内容
        // imageContainer.setClip(...); // <<-- 这行代码被删除了！

        try {
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(item.getImagePath())));
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true); // 保持宽高比是必须的

            // 核心缩放逻辑不变：确保图片能填满“框”的一边
            if (image.getWidth() < image.getHeight()) {
                // 图片是纵向的 (高 > 宽)，让宽度填满框
                imageView.setFitWidth(viewportSize);
            } else {
                // 图片是横向或方形的 (宽 >= 高)，让高度填满框
                imageView.setFitHeight(viewportSize);
            }

            imageContainer.getChildren().add(imageView);

        } catch (Exception e) {
            System.err.println("Could not load image: " + item.getImagePath());
        }

        // --- 文本和按钮的处理 ---

        // 4. 创建一个单独的VBox来控制文本的左对齐
        VBox textContent = new VBox(10);
        textContent.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(item.getName());
        nameLabel.setWrapText(true);

        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        nameLabel.setTextFill(Color.web("#212121")); // 颜色设置为一个非常深的灰色，比纯黑更柔和

        Label priceLabel = new Label("¥ " + String.format("%.2f", item.getPrice()));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        priceLabel.setTextFill(Color.valueOf("#d32f2f"));

        JFXButton addButton = new JFXButton("+");
        addButton.setStyle("-fx-background-color: #7C4DFF; -fx-text-fill: white; -fx-background-radius: 50; -fx-font-size: 18px;");
        addButton.setButtonType(JFXButton.ButtonType.RAISED);

        addButton.setOnAction(event -> chosenItems.add(item));

        HBox priceAndAddBox = new HBox(priceLabel, new Region(), addButton);
        HBox.setHgrow(priceAndAddBox.getChildren().get(1), Priority.ALWAYS);
        priceAndAddBox.setAlignment(Pos.CENTER_LEFT);

        // 将标签和价格/按钮行添加到专用的文本容器中
        textContent.getChildren().addAll(nameLabel, priceAndAddBox);

        // 5. 将图片框和文本容器添加到主卡片中
        card.getChildren().addAll(imageContainer, textContent);
        return card;
    }

    private void updateCartItemsList() {
        cartItemsContainer.getChildren().clear();
        for (ShopItem item : chosenItems) {
            cartItemsContainer.getChildren().add(createCartListItem(item));
        }
    }

    private Node createCartListItem(ShopItem item) {
        HBox listItem = new HBox(15);
        listItem.setAlignment(Pos.CENTER_LEFT);
        listItem.setPadding(new Insets(10));
        listItem.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0;");

        Label name = new Label(item.getName());
        name.setPrefWidth(300);

        Label price = new Label(String.format("¥%.2f", item.getPrice()));

        JFXButton removeButton = new JFXButton("移除");
        removeButton.setTextFill(Color.RED);
        removeButton.setOnAction(e -> chosenItems.remove(item));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        listItem.getChildren().addAll(name, spacer, price, removeButton);
        return listItem;
    }


    // --- Bindings and Listeners ---
    private void setupBindings() {
        // Bind bottom bar labels to properties
        itemCountLabel.textProperty().bind(
                Bindings.createStringBinding(() -> "已选择 " + chosenItemsCount.get() + " 项商品", chosenItemsCount)
        );
        totalPriceLabel.textProperty().bind(
                Bindings.createStringBinding(() -> "共 " + String.format("%.2f", chosenItemsPrice.get()) + " 元", chosenItemsPrice)
        );

        // Bind cart view labels to properties
        cartItemCountLabel.textProperty().bind(
                Bindings.createStringBinding(() -> "已选择 " + chosenItemsCount.get() + " 件商品", chosenItemsCount)
        );
        cartTotalPriceLabel.textProperty().bind(
                Bindings.createStringBinding(() -> "共 " + String.format("%.2f", chosenItemsPrice.get()) + " 元", chosenItemsPrice)
        );
    }

    private void setupListeners() {
        // Listen to changes in the chosen items list
        chosenItems.addListener((ListChangeListener<ShopItem>) c -> {
            // Recalculate total price and count
            chosenItemsCount.set(chosenItems.size());
            chosenItemsPrice.set(chosenItems.stream().mapToDouble(ShopItem::getPrice).sum());
            // Update the visual list in the cart
            updateCartItemsList();
        });

        // Search button logic
        searchButton.setOnAction(event -> performSearch());
        searchField.setOnAction(event -> performSearch());
    }

    private void performSearch() {
        String keyword = searchField.getText().toLowerCase().trim();
        if (keyword.isEmpty()) {
            displayedItems.setAll(allItems);
        } else {
            List<ShopItem> filteredList = new ArrayList<>();
            for (ShopItem item : allItems) {
                if (item.getName().toLowerCase().contains(keyword)) {
                    filteredList.add(item);
                }
            }
            displayedItems.setAll(filteredList);
        }
        populateItemsGrid();
    }

    // --- Animations and Event Handlers ---
    @FXML
    private void toggleCartView() {
        isCartVisible = !isCartVisible;

        TranslateTransition tt = new TranslateTransition(Duration.millis(350), cartView);

        if (isCartVisible) {
            cartView.setVisible(true);
            tt.setToY(0);
            tt.play();
            toggleCartButton.setText("下滑以浏览");
        } else {
            tt.setToY(cartView.getHeight() > 0 ? cartView.getHeight() : 800); // Hide below
            tt.setOnFinished(e -> cartView.setVisible(false));
            tt.play();
            toggleCartButton.setText("上滑以结算");
        }
    }

    // --- Data Model Class ---

    private static class ShopItem {
        private final String name;
        private final double price;
        private final String imagePath;

        public ShopItem(String name, double price, String imagePath) {
            this.name = name;
            this.price = price;
            this.imagePath = imagePath;
        }

        public String getName() { return name; }
        public double getPrice() { return price; }
        public String getImagePath() { return imagePath; }
    }
}
