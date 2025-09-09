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
import javafx.scene.text.Text;

//TODO : 模糊搜索优化
//TODO : 去掉Cart的自动换行
public class ShopController {

    // FXML Injected Fields
    @FXML private AnchorPane rootPane;
    @FXML private GridPane itemsGrid;
    @FXML private JFXTextField searchField;
    @FXML private JFXButton searchButton;
    @FXML private HBox bottomBar;
    // @FXML private JFXButton toggleCartButton; // 【修改点 1】此控件已在 FXML 中移除，此处需删除
    @FXML private Label itemCountLabel;
    @FXML private Label totalPriceLabel;
    @FXML private VBox cartView;
    @FXML private Label cartTotalPriceLabel;
    @FXML private Label cartItemCountLabel;
    @FXML private VBox cartItemsContainer;
    @FXML private StackPane overlayPane;


    // ViewModel / State Properties
    private final ObservableList<ShopItem> allItems = FXCollections.observableArrayList();
    private final ObservableList<ShopItem> displayedItems = FXCollections.observableArrayList();

    private final ObservableList<ShopItem> chosenItems = FXCollections.observableArrayList();
    private final IntegerProperty chosenItemsCount = new SimpleIntegerProperty(0);
    private final DoubleProperty chosenItemsPrice = new SimpleDoubleProperty(0.0);

    // --- Animation Properties ---
    private boolean isCartVisible = false;
    private static final Duration ANIMATION_SPEED = Duration.millis(400);
    private static final Interpolator CUSTOM_EASING = Interpolator.SPLINE(0.23, 1.0, 0.32, 1.0);
    private ParallelTransition cartAnimation;

    // Initialization
// 【已重构】
    @FXML
    public void initialize() {
        loadData();
        displayedItems.setAll(allItems);
        setupBindings();
        setupListeners();
        populateItemsGrid();

        // 【关键】我们不再控制 topAnchor，而是给 cartView 设置一个最大高度的限制。
        // 这个限制与窗口高度动态绑定。
        cartView.maxHeightProperty().bind(
                rootPane.heightProperty().subtract(60) // 最大高度 = 窗口高度 - 60
        );

        // 确保初始位置在屏幕外 (这部分逻辑保持不变)
        rootPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (!isCartVisible) {
                cartView.setTranslateY(newVal.doubleValue());
            }
        });
        cartView.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                cartView.setTranslateY(rootPane.getHeight());
            }
        });

        cartView.setCache(true);
        cartView.setCacheHint(javafx.scene.CacheHint.SPEED);
    }
    // --- Data Loading ---
    private void loadData() {
        // Simulating the items from the image
        allItems.add(new ShopItem("正版 黑暗之魂官方艺术设定集 全套1-2-3册 DARK SOULS", 249.00, "/images/500.png"));
        allItems.add(new ShopItem("Sony/索尼 ECM-G1 枪型麦克风 大尺寸收音单元 清晰人声", 999.00, "/images/500.png"));
        allItems.add(new ShopItem("Apple/苹果 13 英寸 MacBook Air Apple M2 芯片 8 核中央处理器", 12699.00, "/images/500.png"));
        allItems.add(new ShopItem("C++ Primer Plus (第6版)", 89.50, "/images/500.png"));
        allItems.add(new ShopItem("小米便携风扇", 59.00, "/images/500.png"));
        allItems.add(new ShopItem("原神 | 刻晴手办", 888.00, "/images/500.png"));
        allItems.add(new ShopItem("原神 | 可莉手办", 888.00, "/images/500.png"));
        allItems.add(new ShopItem("原神 | 胡桃手办", 888.00, "/images/500.png"));
        allItems.add(new ShopItem("原神 | 雷电将军手办", 888.00, "/images/500.png"));
        allItems.add(new ShopItem("原神 | 甘雨手办", 888.00, "/images/500.png"));
    }


    @FXML
    private void toggleCart() {
        if (cartAnimation != null) {
            cartAnimation.stop();
        }

        isCartVisible = !isCartVisible;

        TranslateTransition cartTransition = new TranslateTransition(ANIMATION_SPEED, cartView);
        FadeTransition overlayFade = new FadeTransition(ANIMATION_SPEED, overlayPane);

        cartTransition.setInterpolator(CUSTOM_EASING);
        overlayFade.setInterpolator(CUSTOM_EASING);

        if (isCartVisible) {
            // 在显示之前，刷新一次列表
            updateCartItemsList();

            overlayPane.setVisible(true);
            cartTransition.setToY(0);
            overlayFade.setToValue(0.6);
        } else {
            cartTransition.setToY(rootPane.getHeight());
            overlayFade.setToValue(0.0);
        }

        cartAnimation = new ParallelTransition(cartTransition, overlayFade);

        if (!isCartVisible) {
            cartAnimation.setOnFinished(event -> overlayPane.setVisible(false));
        } else {
            cartAnimation.setOnFinished(null);
        }

        cartAnimation.play();
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
        VBox card = new VBox(15);
        card.setPrefWidth(250);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        card.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.1)));
        card.setPadding(new Insets(10));
        card.setAlignment(Pos.TOP_CENTER);

        final double VIEWPORT_SIZE = 220.0;
        StackPane imageContainer = new StackPane();
        imageContainer.setMinSize(VIEWPORT_SIZE, VIEWPORT_SIZE);
        imageContainer.setPrefSize(VIEWPORT_SIZE, VIEWPORT_SIZE);
        imageContainer.setMaxSize(VIEWPORT_SIZE, VIEWPORT_SIZE);
        imageContainer.setStyle("-fx-background-color: #F0F0F0; -fx-background-radius: 4;");

        try {
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(item.getImagePath())));
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);

            if (image.getWidth() < image.getHeight()) {
                imageView.setFitWidth(VIEWPORT_SIZE);
            } else {
                imageView.setFitHeight(VIEWPORT_SIZE);
            }

            imageContainer.getChildren().add(imageView);

        } catch (Exception e) {
            System.err.println("Could not load image: " + item.getImagePath());
        }

        VBox textContent = new VBox(10);
        textContent.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(item.getName());
        nameLabel.setWrapText(true);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        nameLabel.setTextFill(Color.web("#212121"));

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

        textContent.getChildren().addAll(nameLabel, priceAndAddBox);

        card.getChildren().addAll(imageContainer, textContent);
        return card;
    }

    private void updateCartItemsList() {
        cartItemsContainer.getChildren().clear();
        for (ShopItem item : chosenItems) {
            cartItemsContainer.getChildren().add(createCartListItem(item));
        }
    }

    // 【终极武器】使用纯粹的 Text 节点替换 Label，绕开所有可能的控件渲染 Bug
    private Node createCartListItem(ShopItem item) {
        // --- 1. 创建 GridPane 根布局 (保持不变) ---
        GridPane listItem = new GridPane();
        listItem.setPadding(new Insets(16, 10, 16, 10));
        listItem.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0;");

        ColumnConstraints infoColumn = new ColumnConstraints();
        infoColumn.setHgrow(Priority.ALWAYS);
        ColumnConstraints buttonColumn = new ColumnConstraints();
        buttonColumn.setHgrow(Priority.NEVER);
        listItem.getColumnConstraints().addAll(infoColumn, buttonColumn);

        // --- 2. 创建左侧的商品信息 VBox (保持不变) ---
        VBox nameAndPriceContainer = new VBox(4);
        nameAndPriceContainer.setAlignment(Pos.CENTER_LEFT);

        // --- 3. 【核心修改】使用 Text 节点来显示商品名 ---
        Text nameText = new Text(item.getName());
        nameText.setFont(Font.font("System", FontWeight.NORMAL, 16));
        // Text 节点使用 setFill 来设置颜色，而不是 setTextFill
        nameText.setFill(Color.web("#212121"));

        // 【关键】为了实现自动换行，我们需要将 Text 节点的换行宽度绑定到其父容器的宽度上
        // 这会让文本在容器宽度不够时自动换行
        nameText.wrappingWidthProperty().bind(nameAndPriceContainer.widthProperty());

        // 我们保留 priceLabel 作为 Label，用于对比
        Label priceLabel = new Label(String.format("¥%.2f", item.getPrice()));
        priceLabel.setTextFill(Color.web("#616161"));
        priceLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));

        // 将 Text 节点和 Label 节点一同加入 VBox
        nameAndPriceContainer.getChildren().addAll(nameText, priceLabel);

        // --- 4. 创建右侧的移除按钮 (保持不变) ---
        JFXButton removeButton = new JFXButton("移除");
        removeButton.setTextFill(Color.RED);
        removeButton.setOnAction(e -> chosenItems.remove(item));

        // --- 5. 将 VBox 和 Button 添加到 GridPane (保持不变) ---
        listItem.add(nameAndPriceContainer, 0, 0);
        listItem.add(removeButton, 1, 0);
        GridPane.setHalignment(removeButton, javafx.geometry.HPos.RIGHT);

        return listItem;
    }

    // --- Bindings and Listeners ---
    private void setupBindings() {
        itemCountLabel.textProperty().bind(
                Bindings.createStringBinding(() -> "已选择 " + chosenItemsCount.get() + " 项商品", chosenItemsCount)
        );
        totalPriceLabel.textProperty().bind(
                Bindings.createStringBinding(() -> "共 " + String.format("%.2f", chosenItemsPrice.get()) + " 元", chosenItemsPrice)
        );
        cartItemCountLabel.textProperty().bind(
                Bindings.createStringBinding(() -> "已选择 " + chosenItemsCount.get() + " 件商品", chosenItemsCount)
        );
        cartTotalPriceLabel.textProperty().bind(
                Bindings.createStringBinding(() -> "共 " + String.format("%.2f", chosenItemsPrice.get()) + " 元", chosenItemsPrice)
        );
    }

    // 【已重构】
    private void setupListeners() {
        chosenItems.addListener((ListChangeListener<ShopItem>) c -> {
            // 数据变化时，只更新数据属性
            chosenItemsCount.set(chosenItems.size());
            chosenItemsPrice.set(chosenItems.stream().mapToDouble(ShopItem::getPrice).sum());

            // 【关键】如果购物车当前是可见的，就刷新它的列表
            // 高度会由 JavaFX 根据 maxHeight 自动计算，我们不再需要手动干预
            if (isCartVisible) {
                updateCartItemsList();
            }
        });

        searchButton.setOnAction(event -> performSearch());
        searchField.setOnAction(event -> performSearch());
    }
    // 【新增】一个辅助方法，专门负责根据购物车状态更新其外观

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