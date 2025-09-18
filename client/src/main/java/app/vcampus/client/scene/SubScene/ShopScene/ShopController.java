package app.vcampus.client.scene.SubScene.ShopScene;

import app.vcampus.client.gateway.FinanceClient;
import app.vcampus.client.gateway.HistoryClient;
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

import java.util.*;
import java.util.stream.Collectors;

import javafx.scene.text.Text;

import app.vcampus.server.utility.ShopTransactionRecord;

import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.animation.Timeline;

import app.vcampus.client.gateway.StoreClient;
import app.vcampus.client.repository.FakeRepository;
import app.vcampus.server.entity.StoreItem;

/**
 * 商店场景控制器。
 * 负责处理商店主界面的所有逻辑，包括商品展示、搜索、购物车管理和支付流程。
 */
public class ShopController {

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
    private double lastKnownPrice = 0.0;

    private final ObservableList<StoreItem> allItems = FXCollections.observableArrayList();
    private final ObservableList<StoreItem> displayedItems = FXCollections.observableArrayList();
    private final ObservableList<StoreItem> chosenItems = FXCollections.observableArrayList();

    private final IntegerProperty chosenItemsCount = new SimpleIntegerProperty(0);
    private final DoubleProperty chosenItemsPrice = new SimpleDoubleProperty(0.0);

    private boolean isCartVisible = false;
    private static final Duration ANIMATION_SPEED = Duration.millis(400);
    private static final Interpolator CUSTOM_EASING = Interpolator.SPLINE(0.23, 1.0, 0.32, 1.0);
    private ParallelTransition cartAnimation;

    /**
     * 交易历史记录。
     */
    public static final List<ShopTransactionRecord> transactionHistory = new ArrayList<>();

    private static ShopController instance;

    /**
     * 初始化方法，在FXML文件加载完成后自动调用。
     */
    @FXML
    public void initialize() {
        instance = this;
        refreshData();
        loadTransactionHistory();
        setupBindings();
        setupListeners();
        createAndPlaySwipeHintAnimation();

        payButton.disableProperty().bind(Bindings.isEmpty(chosenItems));

        initialize_overlayPane_and_bottomBar();

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

    /**
     * 刷新商品数据。
     */
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

    /**
     * 获取 ShopController 的单例实例。
     *
     * @return ShopController 实例。
     */
    public static ShopController getInstance() {
        return instance;
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

        if (isCartVisible) {
            swipeHintAnimation.pause();
            swipeHintIcon.setVisible(false);

            cartContainer.setMouseTransparent(false);
            updateCartItemsList();

            overlayPane.setVisible(true);

            overlayPane.setMouseTransparent(false);

            double targetY = Math.max(60, rootPane.getHeight() - cartContainer.getHeight());
            cartTransition.setToY(targetY);
            overlayFade.setToValue(0.6);

        } else {
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

        cartAnimation = new ParallelTransition(cartTransition, overlayFade);

        if (!isCartVisible) {
            cartAnimation.setOnFinished(event -> {
                overlayPane.setVisible(false);
                overlayPane.setMouseTransparent(true);
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
        imageContainer.setStyle("-fx-background-color: #F0F0F0;");

        Rectangle clip = new Rectangle(VIEWPORT_SIZE, VIEWPORT_SIZE);

        double cornerRadius = 30.0;
        clip.setArcWidth(cornerRadius);
        clip.setArcHeight(cornerRadius);

        imageContainer.setClip(clip);

        load_image_from_cache(item.getPictureLink(), VIEWPORT_SIZE, imageContainer);

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

    private void load_image_from_cache(String pictureLink, double VIEWPORT_SIZE, StackPane imageContainer) {
        try {
            Image image = ImageCache.getInstance().getImage(pictureLink);

            if (image.isError()) throw new Exception("Image data is corrupted for path: " + pictureLink);

            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            if (image.getWidth() < image.getHeight()) imageView.setFitWidth(VIEWPORT_SIZE);
            else imageView.setFitHeight(VIEWPORT_SIZE);
            imageContainer.getChildren().add(imageView);

        } catch (Exception e) {
            System.err.println("无法从缓存加载图片 '" + pictureLink + "', 正在使用默认占位图。原因: " + e.getMessage());

            try {
                Image fallbackImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/500.png")));

                ImageView fallbackImageView = new ImageView(fallbackImage);
                fallbackImageView.setPreserveRatio(true);
                if (fallbackImage.getWidth() < fallbackImage.getHeight()) fallbackImageView.setFitWidth(VIEWPORT_SIZE);
                else fallbackImageView.setFitHeight(VIEWPORT_SIZE);
                imageContainer.getChildren().add(fallbackImageView);

            } catch (Exception fallbackEx) {
                System.err.println("致命错误：默认占位图 /images/500.png 也无法加载！");
                fallbackEx.printStackTrace();

                Label errorLabel = new Label("X");
                errorLabel.setFont(Font.font("System", FontWeight.BOLD, 48));
                errorLabel.setTextFill(Color.RED);
                imageContainer.getChildren().add(errorLabel);
            }
        }
    }

    private void updateCartItemsList() {
        cartItemsContainer.getChildren().clear();
        for (StoreItem item : chosenItems) {
            cartItemsContainer.getChildren().add(createCartListItem(item));
        }
    }

    private Node createCartListItem(StoreItem item) {
        GridPane listItem = new GridPane();
        listItem.setPadding(new Insets(16, 10, 16, 10));
        listItem.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0;");

        ColumnConstraints infoColumn = new ColumnConstraints();
        infoColumn.setHgrow(Priority.ALWAYS);
        ColumnConstraints buttonColumn = new ColumnConstraints();
        buttonColumn.setHgrow(Priority.NEVER);
        listItem.getColumnConstraints().addAll(infoColumn, buttonColumn);

        ImageView imageView = new ImageView();

        load_image_then_config_ImageView(item, imageView);

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

        HBox imageAndInfoContainer = new HBox(15);
        imageAndInfoContainer.setAlignment(Pos.CENTER_LEFT);
        imageAndInfoContainer.getChildren().addAll(imageView, nameAndPriceContainer);

        JFXButton removeButton = new JFXButton("移除");
        removeButton.setTextFill(Color.RED);
        removeButton.setOnAction(e -> chosenItems.remove(item));

        listItem.add(imageAndInfoContainer, 0, 0);
        listItem.add(removeButton, 1, 0);
        GridPane.setHalignment(removeButton, javafx.geometry.HPos.RIGHT);

        return listItem;
    }

    private void load_image_then_config_ImageView(StoreItem item, ImageView imageView) {
        try {
            Image image = ImageCache.getInstance().getImage(item.getPictureLink());
            if (image.isError()) throw new Exception("Image data is corrupted for path: " + item.getPictureLink());
            imageView.setImage(image);

        } catch (Exception e) {
            System.err.println("无法加载购物车图片 '" + item.getPictureLink() + "', 正在使用默认占位图。原因: " + e.getMessage());
            try {
                Image fallbackImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/500.png")));
                imageView.setImage(fallbackImage);

            } catch (Exception fallbackEx) {
                System.err.println("致命错误：购物车的默认占位图 /images/500.png 也无法加载！");
            }
        }
        imageView.setFitHeight(60);
        imageView.setFitWidth(60);
        imageView.setPreserveRatio(true);
    }

    private void setupBindings() {
        itemCountLabel.textProperty().bind(
                Bindings.createStringBinding(() -> "已选择 " + chosenItemsCount.get() + " 项商品", chosenItemsCount)
        );

        cartItemCountLabel.textProperty().bind(
                Bindings.createStringBinding(() -> "已选择 " + chosenItemsCount.get() + " 件商品", chosenItemsCount)
        );
        cartTotalPriceLabel.textProperty().bind(
                Bindings.createStringBinding(() -> {
                    double totalPriceInFen = chosenItems.stream()
                            .mapToDouble(item -> item.price.doubleValue())
                            .sum();
                    return "共 " + String.format("%.2f", totalPriceInFen / 100.0) + " 元";
                }, chosenItems)
        );
    }

    private void setupListeners() {
        chosenItems.addListener((ListChangeListener<StoreItem>) c -> {
            double newPriceInFen = chosenItems.stream()
                    .mapToDouble(item -> item.price.doubleValue())
                    .sum();

            chosenItemsCount.set(chosenItems.size());
            chosenItemsPrice.set(newPriceInFen);

            playPriceScrollAnimation(newPriceInFen / 100.0);

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

        // 【核心】在支付流程中，启动一个后台线程来更新服务器上的历史记录
        new Thread(() -> {
            boolean success = HistoryClient.updateHistory(FakeRepository.handler, transactionHistory);
            if (success) {
                System.out.println("交易历史已成功同步到服务器。");
            } else {
                System.err.println("警告：交易历史同步到服务器失败！");
            }
        }).start();

        finance_process_credit(record);   // 财务模块处理扣款
        finance_process_debit(record);
        play_payment_animation();
    }

    private void finance_process_credit(ShopTransactionRecord record) {
        Integer userCardNum = FakeRepository.user.getCardNum();
        String credit_json = credit_json_maker(record);
        System.out.println(credit_json);
        String description = "消费:" + credit_json;
        FinanceClient.credit(userCardNum.toString(), record.getTotalPrice() , description);
    }

    private void finance_process_debit(ShopTransactionRecord record) {
        List<StoreItem> allItems = record.getItems();

        Map<String, List<StoreItem>> itemsByBarcode = allItems.stream().collect(Collectors.groupingBy(StoreItem::getBarcode));

        for (Map.Entry<String, List<StoreItem>> bossEntry : itemsByBarcode.entrySet()) {
            String barcode = bossEntry.getKey();
            List<StoreItem> itemsForThisBoss = bossEntry.getValue();

            // --- 从这里开始，逻辑几乎与 credit_json_maker 完全相同，只是处理的数据范围是 itemsForThisBoss ---

            // 4. 【第二次分组】：聚合当前卖家的商品，统计每种商品的数量
            Map<String, Long> itemCounts = itemsForThisBoss.stream()
                    .collect(Collectors.groupingBy(StoreItem::getItemName, Collectors.counting()));

            // 5. 为获取商品元信息（如描述、图片链接等），创建去重后的Map
            Map<String, StoreItem> uniqueItems = new HashMap<>();
            for (StoreItem item : itemsForThisBoss) {
                uniqueItems.putIfAbsent(item.getItemName(), item);
            }

            // 6. 构建用于生成JSON的数据结构
            List<Map<String, Object>> transactionDetails = new ArrayList<>();
            int totalAmountForBoss = 0; // 用于计算该卖家的总收款

            for (Map.Entry<String, Long> entry : itemCounts.entrySet()) {
                String itemNameValue = entry.getKey();
                Long quantityValue = entry.getValue();
                StoreItem itemInfo = uniqueItems.get(itemNameValue);

                Map<String, Object> detail = new HashMap<>();
                detail.put("itemName", itemNameValue);
                detail.put("price", itemInfo.getPrice());
                detail.put("description", itemInfo.getDescription());
                detail.put("stock", quantityValue); // 复用 stock 字段存储购买数量
                detail.put("pictureLink", itemInfo.getPictureLink());
                detail.put("uuid", itemInfo.getUuid());
                transactionDetails.add(detail);

                // 累加总金额 (单价 * 数量)
                totalAmountForBoss += itemInfo.getPrice() * quantityValue;
            }

            String debitJson = new Gson().toJson(transactionDetails);
            String description = "卖出货款：" + debitJson;

            try {
                // 假设 debit 方法需要 (收款人卡号, 金额(分), 描述)
                boolean success = FinanceClient.debit(barcode, totalAmountForBoss/100.0, description);
                if (success) {
                    System.out.println("成功为卖家 [" + barcode + "] 生成收款记录，金额: " + totalAmountForBoss/100.0 + "元");
                } else {
                    System.err.println("为卖家 [" + barcode + "] 生成收款记录失败！");
                }
            } catch (Exception e) {
                System.err.println("为卖家 [" + barcode + "] 生成收款记录时发生异常: " + e.getMessage());
            }
        }
    }

    /**
     * @param record 商店交易记录
     * @return 描述消费详情的JSON字符串
     */
    private String credit_json_maker(ShopTransactionRecord record) {
        List<StoreItem> items = record.getItems();

        Map<String, Long> itemCounts = items.stream()
                .collect(Collectors.groupingBy(StoreItem::getItemName, Collectors.counting()));

        Map<String, StoreItem> uniqueItems = new HashMap<>();
        for (StoreItem item : items) {
            uniqueItems.putIfAbsent(item.getItemName(), item);
        }

        List<Map<String, Object>> transactionDetails = new ArrayList<>();

        for (Map.Entry<String, Long> entry : itemCounts.entrySet()) {
            String itemNameValue = entry.getKey();
            Long quantityValue = entry.getValue();
            StoreItem itemInfo = uniqueItems.get(itemNameValue);

            Map<String, Object> detail = new HashMap<>();

            detail.put("itemName", itemNameValue);
            detail.put("price", itemInfo.getPrice());
            detail.put("description", itemInfo.getDescription());
            detail.put("stock", quantityValue);
            detail.put("pictureLink", itemInfo.getPictureLink());
            detail.put("uuid", itemInfo.getUuid());

            transactionDetails.add(detail);
        }

        return new Gson().toJson(transactionDetails);
    }

    private void play_payment_animation() {
        FadeTransition cartFadeOut = new FadeTransition(Duration.millis(300), cartView);
        cartFadeOut.setToValue(0);

        FadeTransition successFadeIn = new FadeTransition(Duration.millis(300), successOverlay);
        successFadeIn.setFromValue(0);
        successFadeIn.setToValue(1);

        successFadeIn.setOnFinished(event -> {
            chosenItems.clear();

            PauseTransition delay = new PauseTransition(Duration.millis(500));
            delay.setOnFinished(e -> {
                toggleCart();
            });
            delay.play();
        });

        successOverlay.setVisible(true);
        cartFadeOut.play();
        successFadeIn.play();
    }

    private void createAndPlaySwipeHintAnimation() {
        swipeHintAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(swipeHintIcon.translateYProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(swipeHintIcon.opacityProperty(), 0.5, Interpolator.EASE_BOTH)
                ),
                new KeyFrame(Duration.seconds(1.2),
                        new KeyValue(swipeHintIcon.translateYProperty(), -8, Interpolator.EASE_BOTH),
                        new KeyValue(swipeHintIcon.opacityProperty(), 1.0, Interpolator.EASE_BOTH)
                )
        );

        swipeHintAnimation.setAutoReverse(true);
        swipeHintAnimation.setCycleCount(Timeline.INDEFINITE);
        swipeHintAnimation.play();
    }
    private void playPriceScrollAnimation(double newPrice) {
        DoubleProperty animatedPrice = new SimpleDoubleProperty(lastKnownPrice);

        Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.millis(300),
                        new KeyValue(animatedPrice, newPrice, Interpolator.EASE_OUT)
                )
        );

        animatedPrice.addListener((obs, oldVal, newVal) -> {
            totalPriceLabel.setText(String.format("共 %.2f 元", newVal.doubleValue()));
        });

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

    private void loadTransactionHistory() {
        new Thread(() -> {
            List<ShopTransactionRecord> historyFromServer = HistoryClient.getHistory(FakeRepository.handler);
            Platform.runLater(() -> {
                if (historyFromServer != null) {
                    transactionHistory.clear();
                    transactionHistory.addAll(historyFromServer);
                    System.out.println("ShopController: 交易历史加载成功，共 " + transactionHistory.size() + " 条记录。");
                } else {
                    System.err.println("ShopController: 交易历史加载失败。");
                }
            });
        }).start();
    }
}
