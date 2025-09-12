package app.vcampus.client.scene;

import app.vcampus.client.util.ImageCache;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.scene.text.Text;
import app.vcampus.server.utility.ShopItem;
import app.vcampus.server.utility.ShopTransactionRecord;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath; // 确保导入
import javafx.animation.Timeline; // 确保导入
//TODO : 模糊搜索优化
//TODO : 去掉 Cart 的自动换行

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

    @FXML private JFXButton payButton;
    @FXML private StackPane successOverlay;
    @FXML private StackPane cartContainer; // 【新增】
    @FXML private SVGPath swipeHintIcon; // <-- 新增：注入我们的 V 形箭头图标

    private Timeline swipeHintAnimation; // <-- 新增：用于存储我们创建的动画
    private double lastKnownPrice = 0.0; // <<--- 在这里新增这一行

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

    public static final List<ShopTransactionRecord> transactionHistory = new ArrayList<>();

    // 【已重构】
    @FXML
    public void initialize() {
        loadData();
        displayedItems.setAll(allItems);
        setupBindings();
        setupListeners();
        populateItemsGrid();

        // 【修复问题1】将支付按钮的 disable 属性与购物车是否为空进行绑定
        payButton.disableProperty().bind(Bindings.isEmpty(chosenItems));

        // 【修复问题2】不再绑定 cartView 的 maxHeight，让其自由生长
        // 我们改为直接控制 cartContainer 的动画

        // 确保 cartContainer 初始位置在屏幕外，且能响应窗口大小变化
        rootPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (!isCartVisible) {
                cartContainer.setTranslateY(newVal.doubleValue());
            }
        });
        cartContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                cartContainer.setTranslateY(rootPane.getHeight());
            }
        });

        // 默认让弹窗“鼠标透明”，不拦截事件
        cartContainer.setMouseTransparent(true);
        cartContainer.setCache(true);
        cartContainer.setCacheHint(javafx.scene.CacheHint.SPEED);

        createAndPlaySwipeHintAnimation();
    }

    private void loadData() {
        // Cache initialize
        ImageCache.getInstance().addImage("/images/DARKSOULS.png" , new Image(getClass().getResourceAsStream("/images/500.png")));
        ImageCache.getInstance().addImage("/images/500.png" , new Image(getClass().getResourceAsStream("/images/DARKSOULS.png")));

        // Simulating the items from the image
        allItems.add(new ShopItem("正版 黑暗之魂官方艺术设定集 全套1-2-3册 DARK SOULS", 249.00, "/images/DARKSOULS.png"));
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
        if (cartAnimation != null) cartAnimation.stop();

        isCartVisible = !isCartVisible;

        // 【核心修改】所有平移动画都只针对 cartContainer 这个“弹窗”
        TranslateTransition cartTransition = new TranslateTransition(ANIMATION_SPEED, cartContainer);
        FadeTransition overlayFade = new FadeTransition(ANIMATION_SPEED, overlayPane);
        cartTransition.setInterpolator(CUSTOM_EASING);
        overlayFade.setInterpolator(CUSTOM_EASING);

        if (isCartVisible) {
            swipeHintAnimation.pause();
            swipeHintIcon.setVisible(false);

            cartContainer.setMouseTransparent(false);
            updateCartItemsList();

            overlayPane.setVisible(true);
            // 计算弹窗滑入的目标位置。
            // 我们不能直接设为 0，因为 cartContainer 的顶部还有 60px 的边距
            // 同时，我们还要确保它不会超出屏幕
            double targetY = Math.max(60, rootPane.getHeight() - cartContainer.getHeight());
            cartTransition.setToY(targetY);

            overlayFade.setToValue(0.6);
        } else {
            swipeHintAnimation.play();
            swipeHintIcon.setVisible(true);

            double currentPrice = chosenItems.stream().mapToDouble(ShopItem::getPrice).sum();
            playPriceScrollAnimation(currentPrice);

            cartTransition.setToY(rootPane.getHeight());
            overlayFade.setToValue(0.0);
        }

        cartAnimation = new ParallelTransition(cartTransition, overlayFade);

        if (!isCartVisible) {
            cartAnimation.setOnFinished(event -> {
                overlayPane.setVisible(false);
                cartView.setOpacity(1);
                successOverlay.setVisible(false);
                cartContainer.setMouseTransparent(true);
            });
        } else {
            cartAnimation.setOnFinished(null);
        }
        cartAnimation.play();
    }

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

        final double VIEWPORT_SIZE = 270.0;
        StackPane imageContainer = new StackPane();
        imageContainer.setMinSize(VIEWPORT_SIZE, VIEWPORT_SIZE);
        imageContainer.setPrefSize(VIEWPORT_SIZE, VIEWPORT_SIZE);
        imageContainer.setMaxSize(VIEWPORT_SIZE, VIEWPORT_SIZE);
        // 【优化】我们不再需要背景圆角了，因为剪裁会处理它
        imageContainer.setStyle("-fx-background-color: #F0F0F0;");

        // --- 【关键修正】将剪裁应用到“相框”而不是“照片” ---
        // 1. 创建一个和图片视口一样大的矩形，作为我们的“剪刀”
        Rectangle clip = new Rectangle(VIEWPORT_SIZE, VIEWPORT_SIZE);

        // 2. 设置矩形的圆角半径，这个值可以随你调整
        double cornerRadius = 30.0;
        clip.setArcWidth(cornerRadius);
        clip.setArcHeight(cornerRadius);

        // 3. 将这个圆角矩形“剪刀”应用到我们的 imageContainer (相框) 上
        imageContainer.setClip(clip);
        // --- 修正结束 ---

        try {
            Image image = ImageCache.getInstance().getImage(item.getImagePath());

            if (!image.isError()) {
                ImageView imageView = new ImageView(image);
                imageView.setPreserveRatio(true);

                if (image.getWidth() < image.getHeight()) imageView.setFitWidth(VIEWPORT_SIZE);
                else imageView.setFitHeight(VIEWPORT_SIZE);

                imageContainer.getChildren().add(imageView);
            } else {
                throw new Exception("Image data is corrupted for path: " + item.getImagePath());
            }

        } catch (Exception e) {
            System.err.println(e.getMessage()); // 在控制台打印错误信息，便于调试

            Label errorLabel = new Label("X");
            errorLabel.setFont(Font.font("System", FontWeight.BOLD, 48));
            errorLabel.setTextFill(Color.RED);
            imageContainer.getChildren().add(errorLabel);
        }

        // --- 后面的代码保持完全不变 ---
        VBox textContent = new VBox(10);
        textContent.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(item.getName());
        nameLabel.setWrapText(true);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        nameLabel.setTextFill(Color.web("#212121"));

        Label priceLabel = new Label("¥ " + String.format("%.2f", item.getPrice()));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        priceLabel.setTextFill(Color.valueOf("#212121"));

        JFXButton addButton = new JFXButton("+");
        addButton.setStyle("-fx-background-color: #B2C926B2; -fx-text-fill: white; -fx-background-radius: 50; -fx-font-size: 18px;");
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

        // --- 【核心修改】用我们统一的 ImageCache 逻辑替换旧的加载方式 ---
        ImageView imageView = new ImageView();
        try {
            // 1. 统一地、高效地从缓存中获取图片
            Image image = ImageCache.getInstance().getImage(item.getImagePath());
            imageView.setImage(image);

        } catch (Exception e) {
            // 2. 如果图片在预加载时失败，这里会捕获异常
            System.err.println("Could not get cart image from cache: " + e.getMessage());
            // imageView 将保持空白，或者你可以在这里设置一个错误占位符
        }
        // --- 修改结束 ---

        // --- 【微调参数】在这里调整图片大小 (保持不变) ---
        imageView.setFitHeight(60); // 设置图片高度为 60px
        imageView.setFitWidth(60);  // 设置图片宽度为 60px
        imageView.setPreserveRatio(true); // 保持宽高比


        // --- B. 创建图片右侧的商品信息 VBox (保持不变) ---
        VBox nameAndPriceContainer = new VBox(4);
        nameAndPriceContainer.setAlignment(Pos.CENTER_LEFT);

        Text nameText = new Text(item.getName());
        nameText.setFont(Font.font("System", FontWeight.NORMAL, 16));
        nameText.setFill(Color.web("#212121"));
        nameText.wrappingWidthProperty().bind(nameAndPriceContainer.widthProperty());

        Label priceLabel = new Label(String.format("¥%.2f", item.getPrice()));
        priceLabel.setTextFill(Color.web("#616161"));
        priceLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));

        nameAndPriceContainer.getChildren().addAll(nameText, priceLabel);


        // --- C. 【新增】创建一个 HBox 来包裹图片和商品信息 (保持不变) ---
        HBox imageAndInfoContainer = new HBox(15); // 15px 的水平间距
        imageAndInfoContainer.setAlignment(Pos.CENTER_LEFT);
        imageAndInfoContainer.getChildren().addAll(imageView, nameAndPriceContainer);


        // --- D. 创建右侧的移除按钮 (保持不变) ---
        JFXButton removeButton = new JFXButton("移除");
        removeButton.setTextFill(Color.RED);
        removeButton.setOnAction(e -> chosenItems.remove(item));


        // --- E. 【修改】将新的 HBox 和 Button 添加到 GridPane 中 (保持不变) ---
        listItem.add(imageAndInfoContainer, 0, 0); // 将 HBox 作为一个整体放入第一列
        listItem.add(removeButton, 1, 0);
        GridPane.setHalignment(removeButton, javafx.geometry.HPos.RIGHT);

        return listItem;
    }

    private void setupBindings() {
        // Bind bottom bar labels to properties
        itemCountLabel.textProperty().bind(
                Bindings.createStringBinding(() -> "已选择 " + chosenItemsCount.get() + " 项商品", chosenItemsCount)
        );

        // 【核心修复】将下面这整个绑定语句删除或注释掉
    /*
    totalPriceLabel.textProperty().bind(
        Bindings.createStringBinding(() -> "共 " + String.format("%.2f", chosenItemsPrice.get()) + " 元", chosenItemsPrice)
    );
    */

        // Bind cart view labels to properties
        cartItemCountLabel.textProperty().bind(
                Bindings.createStringBinding(() -> "已选择 " + chosenItemsCount.get() + " 件商品", chosenItemsCount)
        );
        cartTotalPriceLabel.textProperty().bind(
                // 注意：这里的绑定我们可能仍然需要，除非你也想让它滚动
                // 暂时保留它，因为它控制的是购物车内部的标签
                Bindings.createStringBinding(() -> "共 " + String.format("%.2f", chosenItems.stream().mapToDouble(ShopItem::getPrice).sum()) + " 元", chosenItemsCount)
        );
    }

    private void setupListeners() {
        chosenItems.addListener((ListChangeListener<ShopItem>) c -> {
            double newPrice = chosenItems.stream().mapToDouble(ShopItem::getPrice).sum();

            chosenItemsCount.set(chosenItems.size());
            chosenItemsPrice.set(chosenItems.stream().mapToDouble(ShopItem::getPrice).sum());

            if (!chosenItems.isEmpty()) {
                playPriceScrollAnimation(newPrice);
            }

            // 只有当弹窗可见时，才刷新列表内容
            if (isCartVisible) {
                updateCartItemsList();
            }
        });

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

    @FXML
    private void handlePayment() {
        // --- 0. 状态检查 (保持不变) ---
        if (chosenItems.isEmpty() || successOverlay.isVisible()) {
            return;
        }

        // --- 1. 记录交易 (保持不变) ---
        ShopTransactionRecord record = new ShopTransactionRecord(chosenItems, chosenItemsPrice.get());
        transactionHistory.add(record);
        System.out.println("新交易已记录: " + record);

        // --- 2. 准备动画 (已修改) ---
        // cartView 淡出
        FadeTransition cartFadeOut = new FadeTransition(Duration.millis(300), cartView);
        cartFadeOut.setToValue(0);

        // successOverlay 淡入
        FadeTransition successFadeIn = new FadeTransition(Duration.millis(300), successOverlay);
        successFadeIn.setFromValue(0);
        successFadeIn.setToValue(1);

        // --- 3. 执行动画 (已修改) ---
        // 当 successOverlay 淡入完成后，再清空数据并收回弹窗
        successFadeIn.setOnFinished(event -> {
            chosenItems.clear(); // 在用户看到“已下单”后才清空数据

            PauseTransition delay = new PauseTransition(Duration.millis(500));
            delay.setOnFinished(e -> {
                toggleCart(); // 调用统一的收回方法
            });
            delay.play();
        });

        // 启动动画：先显示 successOverlay，然后同时播放两个淡入淡出动画
        successOverlay.setVisible(true);
        cartFadeOut.play();
        successFadeIn.play();
    }
    private void createAndPlaySwipeHintAnimation() {
        // 创建一个从起点到终点的时间轴动画
        swipeHintAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(swipeHintIcon.translateYProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(swipeHintIcon.opacityProperty(), 0.5, Interpolator.EASE_BOTH)
                ),
                new KeyFrame(Duration.seconds(1.2), // 动画单程时间
                        new KeyValue(swipeHintIcon.translateYProperty(), -8, Interpolator.EASE_BOTH),
                        new KeyValue(swipeHintIcon.opacityProperty(), 1.0, Interpolator.EASE_BOTH)
                )
        );

        swipeHintAnimation.setAutoReverse(true); // 【魔法在这里】让动画自动往返播放
        swipeHintAnimation.setCycleCount(Timeline.INDEFINITE); // 无限循环
        swipeHintAnimation.play();
    }
    private void playPriceScrollAnimation(double newPrice) {
        // 使用 DoubleProperty 来驱动动画
        DoubleProperty animatedPrice = new SimpleDoubleProperty(lastKnownPrice);

        // 创建一个从旧价格到新价格的 Timeline 动画
        Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.millis(300), // 动画持续时间，600毫秒感觉很平滑
                        new KeyValue(animatedPrice, newPrice, Interpolator.EASE_OUT) // 使用缓出插值器
                )
        );

        // 添加监听器：当 animatedPrice 的值在动画过程中变化时，实时更新UI
        animatedPrice.addListener((obs, oldVal, newVal) -> {
            totalPriceLabel.setText(String.format("共 %.2f 元", newVal.doubleValue()));
        });

        // 动画结束后，更新“上一次的价格”记录
        timeline.setOnFinished(event -> lastKnownPrice = newPrice);

        timeline.play();
    }

}