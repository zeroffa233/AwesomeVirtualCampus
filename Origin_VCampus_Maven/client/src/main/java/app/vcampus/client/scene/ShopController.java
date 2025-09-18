package app.vcampus.client.scene;

import app.vcampus.client.gateway.FinanceClient;
import app.vcampus.client.util.ImageCache;
import com.google.gson.Gson;
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
import javafx.application.Platform;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import javafx.scene.text.Text;

import app.vcampus.server.utility.ShopTransactionRecord;

import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath; // 确保导入
import javafx.animation.Timeline; // 确保导入

import app.vcampus.client.gateway.StoreClient; // 【重要】导入 StoreClient
import app.vcampus.client.repository.FakeRepository; // 【重要】导入 FakeRepository
import app.vcampus.server.entity.StoreItem; // 【重要】导入服务端的实体

//TODO : 模糊搜索优化
//TODO : 去掉 Cart 的自动换行

public class ShopController {

    // FXML Injected Fields
    @FXML private AnchorPane rootPane;
    @FXML private GridPane itemsGrid;
    @FXML private JFXTextField searchField;
    @FXML private JFXButton searchButton;
    @FXML private HBox bottomBar;
    @FXML private Label itemCountLabel;
    @FXML private Label totalPriceLabel;
    @FXML private VBox cartView;
    @FXML private Label cartTotalPriceLabel;
    @FXML private Label cartItemCountLabel;
    @FXML private VBox cartItemsContainer;
    @FXML private StackPane overlayPane;
    @FXML private JFXButton payButton;
    @FXML private StackPane successOverlay;
    @FXML private StackPane cartContainer;
    @FXML private SVGPath swipeHintIcon;

    private Timeline swipeHintAnimation;
    private double lastKnownPrice = 0.0; // <<--- 在这里新增这一行

    // ViewModel / State Properties
    private final ObservableList<StoreItem> allItems = FXCollections.observableArrayList();
    private final ObservableList<StoreItem> displayedItems = FXCollections.observableArrayList();
    private final ObservableList<StoreItem> chosenItems = FXCollections.observableArrayList();

    private final IntegerProperty chosenItemsCount = new SimpleIntegerProperty(0);
    private final DoubleProperty chosenItemsPrice = new SimpleDoubleProperty(0.0);

    // --- Animation Properties ---
    private boolean isCartVisible = false;
    private static final Duration ANIMATION_SPEED = Duration.millis(400);
    private static final Interpolator CUSTOM_EASING = Interpolator.SPLINE(0.23, 1.0, 0.32, 1.0);
    private ParallelTransition cartAnimation;

    public static final List<ShopTransactionRecord> transactionHistory = new ArrayList<>();

    private static ShopController instance;

    // 【已重构】
    @FXML
    public void initialize() {
        instance = this;
        refreshData();

        setupBindings();
        setupListeners();
        createAndPlaySwipeHintAnimation();

        payButton.disableProperty().bind(Bindings.isEmpty(chosenItems));

        initialize_overlayPane_and_bottomBar();

        // Ensure that the initial position of cartContainer is outside the screen and can respond to changes in window size.
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

        cartContainer.setMouseTransparent(true);
        cartContainer.setCache(true);
        cartContainer.setCacheHint(javafx.scene.CacheHint.SPEED);
    }

    private void initialize_overlayPane_and_bottomBar() {
        overlayPane.setVisible(false);
        overlayPane.setMouseTransparent(true);

        overlayPane.setOnMouseClicked(event -> { if (isCartVisible) toggleCart(); });
        bottomBar.setOnMouseClicked(event -> { toggleCart(); });
    }

    @FXML
    private void toggleCart() {
        if (cartAnimation != null) cartAnimation.stop();
        isCartVisible = !isCartVisible;

        TranslateTransition cartTransition = new TranslateTransition(ANIMATION_SPEED, cartContainer);
        FadeTransition overlayFade = new FadeTransition(ANIMATION_SPEED, overlayPane);
        cartTransition.setInterpolator(CUSTOM_EASING);
        overlayFade.setInterpolator(CUSTOM_EASING);

        if (isCartVisible) { // 【打开购物车】
            swipeHintAnimation.pause();
            swipeHintIcon.setVisible(false);

            cartContainer.setMouseTransparent(false);
            updateCartItemsList();

            overlayPane.setVisible(true);

            overlayPane.setMouseTransparent(false); // 让遮罩层可以拦截点击

            double targetY = Math.max(60, rootPane.getHeight() - cartContainer.getHeight());
            cartTransition.setToY(targetY);
            overlayFade.setToValue(0.6);

        } else { // 【关闭购物车】
            swipeHintAnimation.play();
            swipeHintIcon.setVisible(true);

            cartTransition.setToY(rootPane.getHeight());
            overlayFade.setToValue(0.0);

            double currentPriceInFen = chosenItems.stream()
                    .mapToDouble(item -> item.price.doubleValue())
                    .sum();
            double finalPrice = currentPriceInFen / 100.0;
            totalPriceLabel.setText(String.format("共 %.2f 元", finalPrice));
            lastKnownPrice = finalPrice;
        }

        // 将动画的创建和播放逻辑统一到方法末尾
        cartAnimation = new ParallelTransition(cartTransition, overlayFade);

        if (!isCartVisible) { // 如果是关闭购物车，则在动画结束后执行清理
            cartAnimation.setOnFinished(event -> {
                overlayPane.setVisible(false);
                overlayPane.setMouseTransparent(true); // 允许鼠标穿透
                cartContainer.setMouseTransparent(true);
            });
        } else { // 如果是打开购物车，则不需要结束回调
            cartAnimation.setOnFinished(null);
        }

        cartAnimation.play();
    }

    private void populateItemsGrid() {
        itemsGrid.getChildren().clear();
        int col = 0;
        int row = 0;
        for (StoreItem item : displayedItems) {
            Node itemCard = createShopItemCard(item);
            itemsGrid.add(itemCard, col, row);
            col++;
            if (col == 3) {
                col = 0;
                row++;
            }
        }
    }

    private Node createShopItemCard(StoreItem item) {
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
            // 步骤 1: 尝试从缓存获取理想的图片
            Image image = ImageCache.getInstance().getImage(item.getPictureLink());

            // 检查图片数据本身是否损坏
            if (image.isError()) {
                throw new Exception("Image data is corrupted for path: " + item.getPictureLink());
            }

            // 如果一切顺利，创建并显示这张理想的图片
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            if (image.getWidth() < image.getHeight()) imageView.setFitWidth(VIEWPORT_SIZE);
            else imageView.setFitHeight(VIEWPORT_SIZE);
            imageContainer.getChildren().add(imageView);

        } catch (Exception e) {
            // 步骤 2: 【核心修改】如果 try 块失败，就在这里执行我们的“备用方案”
            System.err.println("无法从缓存加载图片 '" + item.getPictureLink() + "', 正在使用默认占位图。原因: " + e.getMessage());

            try {
                // a. 尝试从本地资源加载我们的备用图片
                Image fallbackImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/500.png")));

                // b. 显示这张备用图片
                ImageView fallbackImageView = new ImageView(fallbackImage);
                fallbackImageView.setPreserveRatio(true);
                if (fallbackImage.getWidth() < fallbackImage.getHeight()) fallbackImageView.setFitWidth(VIEWPORT_SIZE);
                else fallbackImageView.setFitHeight(VIEWPORT_SIZE);
                imageContainer.getChildren().add(fallbackImageView);

            } catch (Exception fallbackEx) {
                // c. 【终极备用方案】如果连备用图片都加载失败了...
                System.err.println("致命错误：默认占位图 /images/500.png 也无法加载！");
                fallbackEx.printStackTrace();

                // ...我们还是显示一个红色的 "X" 作为最后的提示
                Label errorLabel = new Label("X");
                errorLabel.setFont(Font.font("System", FontWeight.BOLD, 48));
                errorLabel.setTextFill(Color.RED);
                imageContainer.getChildren().add(errorLabel);
            }
        }

        // --- 后面的代码保持完全不变 ---
        VBox textContent = new VBox(10);
        textContent.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(item.getItemName());
        nameLabel.setWrapText(true);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        nameLabel.setTextFill(Color.web("#212121"));

        Label priceLabel = new Label("¥ " + String.format("%.2f", item.getPrice().doubleValue() / 100.0));
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
        for (StoreItem item : chosenItems) {
            cartItemsContainer.getChildren().add(createCartListItem(item));
        }
    }

    private Node createCartListItem(StoreItem item) {
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
            // 步骤 1: 尝试从缓存获取理想的图片
            Image image = ImageCache.getInstance().getImage(item.getPictureLink());
            if (image.isError()) {
                throw new Exception("Image data is corrupted for path: " + item.getPictureLink());
            }
            imageView.setImage(image);

        } catch (Exception e) {
            // 步骤 2: 如果失败，执行“备用方案”
            System.err.println("无法加载购物车图片 '" + item.getPictureLink() + "', 正在使用默认占位图。原因: " + e.getMessage());
            try {
                // a. 尝试从本地资源加载我们的备用图片
                Image fallbackImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/500.png")));
                imageView.setImage(fallbackImage);

            } catch (Exception fallbackEx) {
                // b. 如果连备用图片都加载失败了，imageView 将保持空白，
                //    在小尺寸的购物车视图里，这比显示一个 "X" 可能更不突兀。
                System.err.println("致命错误：购物车的默认占位图 /images/500.png 也无法加载！");
            }
        }
        // --- 修改结束 ---

        // --- 【微调参数】在这里调整图片大小 (保持不变) ---
        imageView.setFitHeight(60); // 设置图片高度为 60px
        imageView.setFitWidth(60);  // 设置图片宽度为 60px
        imageView.setPreserveRatio(true); // 保持宽高比


        // --- B. 创建图片右侧的商品信息 VBox (保持不变) ---
        VBox nameAndPriceContainer = new VBox(4);
        nameAndPriceContainer.setAlignment(Pos.CENTER_LEFT);

        Text nameText = new Text(item.getItemName());
        nameText.setFont(Font.font("System", FontWeight.NORMAL, 16));
        nameText.setFill(Color.web("#212121"));
        nameText.wrappingWidthProperty().bind(nameAndPriceContainer.widthProperty());

        Label priceLabel = new Label(String.format("¥%.2f", item.getPrice().doubleValue() / 100.0));
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


        // Bind cart view labels to properties
        cartItemCountLabel.textProperty().bind(
                Bindings.createStringBinding(() -> "已选择 " + chosenItemsCount.get() + " 件商品", chosenItemsCount)
        );
        cartTotalPriceLabel.textProperty().bind(
                Bindings.createStringBinding(() -> {
                    double totalPriceInFen = chosenItems.stream()
                            .mapToDouble(item -> item.price.doubleValue()) // 使用 item.price
                            .sum();
                    return "共 " + String.format("%.2f", totalPriceInFen / 100.0) + " 元";
                }, chosenItems)
        );
    }

    private void setupListeners() {
        chosenItems.addListener((ListChangeListener<StoreItem>) c -> {
            // 【核心修正】
            double newPriceInFen = chosenItems.stream()
                    .mapToDouble(item -> item.price.doubleValue()) // 使用 item.price
                    .sum();

            chosenItemsCount.set(chosenItems.size());
            chosenItemsPrice.set(newPriceInFen); // chosenItemsPrice 存储的是分

            playPriceScrollAnimation(newPriceInFen / 100.0); // 传递“元”

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
            List<StoreItem> filteredList = new ArrayList<>();
            for (StoreItem item : allItems) {
                if (item.getItemName().toLowerCase().contains(keyword) || item.getDescription().toLowerCase().contains(keyword))
                    filteredList.add(item);
            }
            displayedItems.setAll(filteredList);
        }
        populateItemsGrid();
    }

    @FXML
    private void handlePayment() {
        if (chosenItems.isEmpty() || successOverlay.isVisible()) return;
        ShopTransactionRecord record = new ShopTransactionRecord(chosenItems, chosenItemsPrice.get()/100.0);
        transactionHistory.add(record);
        System.out.println("新交易已记录: " + record);
        finance_process_credit(record);   // 财务模块处理扣款

        play_payment_animation();
    }

    private void finance_process_credit(ShopTransactionRecord record) {
        Integer userCardNum = FakeRepository.user.getCardNum();
        String credit_json = credit_json_maker(record);
        System.out.println(credit_json);
        String description = "消费:" + credit_json;
        FinanceClient.credit(userCardNum.toString(), record.getTotalPrice() , description);
    }

    /**
     * @param record 商店交易记录
     * @return 描述消费详情的JSON字符串
     */
    private String credit_json_maker(ShopTransactionRecord record) {
        List<StoreItem> items = record.getItems();

        Map<String, Long> itemCounts = items.stream()
                .collect(Collectors.groupingBy(StoreItem::getItemName, Collectors.counting()));

        // 3. 为了获取每种商品的单价，我们创建一个去重后的商品Map
        Map<String, StoreItem> uniqueItems = new HashMap<>();
        for (StoreItem item : items) {
            uniqueItems.putIfAbsent(item.getItemName(), item);
        }

        // 4. 构建用于生成最终JSON的数据结构
        List<Map<String, Object>> transactionDetails = new ArrayList<>();

        // 遍历统计结果
        for (Map.Entry<String, Long> entry : itemCounts.entrySet()) {
            String itemName = entry.getKey();
            Long quantity = entry.getValue();
            StoreItem itemInfo = uniqueItems.get(itemName);

            // 创建一个小Map，代表JSON数组中的一个对象
            Map<String, Object> detail = new HashMap<>();
            detail.put("商品名称", itemName);
            detail.put("数量", quantity);

            double priceInYuan = itemInfo.getPrice().doubleValue() / 100.0;
            detail.put("单价(元)", priceInYuan); // 键名也更新为“单价(元)”

            transactionDetails.add(detail);
        }

        // 5. 使用 Gson 库将数据结构转换为JSON字符串并返回
        return new Gson().toJson(transactionDetails);
    }

    private void play_payment_animation() {
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

    public void refreshData() {
        System.out.println("ShopController: 正在启动数据刷新...");
        new Thread(() -> {
            List<StoreItem> itemsFromServer = StoreClient.getAll(FakeRepository.handler);
            Platform.runLater(() -> {
                if (itemsFromServer != null) {
                    allItems.setAll(itemsFromServer);
                    System.out.println("ShopController: 数据刷新成功，共加载 " + allItems.size() + " 件商品。");
                } else {
                    System.err.println("ShopController: 数据刷新失败。");
                    allItems.clear();
                }
                displayedItems.setAll(allItems);
                populateItemsGrid();
            });
        }).start();
    }
    public static ShopController getInstance() {
        return instance;
    }
}