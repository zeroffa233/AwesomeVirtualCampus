package app.vcampus.client.scene;

import app.vcampus.client.util.ShopItem;
import app.vcampus.client.util.ShopTransactionRecord;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class OrderController {

    @FXML
    private VBox ordersListContainer;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        // 1. 从 ShopController 获取全局的交易历史记录
        List<ShopTransactionRecord> allTransactions = ShopController.transactionHistory;

        if (allTransactions.isEmpty()) {
            ordersListContainer.getChildren().add(new Label("暂无订单记录"));
            return;
        }

        // 2. 将所有交易按“天”进行分组
        Map<LocalDate, List<ShopTransactionRecord>> groupedByDay = allTransactions.stream()
                .collect(Collectors.groupingBy(record ->
                        Instant.ofEpochMilli(record.getTimestamp()).atZone(ZoneId.systemDefault()).toLocalDate()
                ));

        // 3. 按日期倒序排序
        List<LocalDate> sortedDates = new ArrayList<>(groupedByDay.keySet());
        sortedDates.sort(Comparator.reverseOrder());

        // 4. 遍历排序后的日期，为每一天动态创建UI
        for (LocalDate date : sortedDates) {
            Node dayGroupNode = createDayGroup(date, groupedByDay.get(date));
            ordersListContainer.getChildren().add(dayGroupNode);
        }
    }

    // 辅助方法：为一整天的数据创建UI
    private Node createDayGroup(LocalDate date, List<ShopTransactionRecord> transactions) {
        VBox dayVBox = new VBox(20); // 天分组的垂直容器

        // 创建日期标签
        Label dateLabel = new Label(date.format(dateFormatter));
        dateLabel.setFont(Font.font("System", FontWeight.BOLD, 22));

        dayVBox.getChildren().add(dateLabel);

        // 遍历这一天的每一笔交易
        for (ShopTransactionRecord transaction : transactions) {
            Node transactionNode = createTransactionCard(transaction);
            dayVBox.getChildren().add(transactionNode);
        }

        return dayVBox;
    }

    // 辅助方法：为单笔交易创建卡片
    private Node createTransactionCard(ShopTransactionRecord transaction) {
        VBox cardVBox = new VBox(10);
        cardVBox.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-background-color: white; -fx-background-radius: 8;");
        cardVBox.setPadding(new Insets(20));

        // --- 交易总额标题 ---
        Label totalLabel = new Label("共 " + String.format("%.2f", transaction.getTotalPrice()) + " 元");
        totalLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        HBox totalHBox = new HBox(totalLabel);
        totalHBox.setAlignment(Pos.CENTER_RIGHT);
        cardVBox.getChildren().add(totalHBox);

        // --- 商品列表 ---
        // 为了正确显示数量，我们需要对商品列表进行分组计数
        Map<ShopItem, Long> itemCounts = transaction.getItems().stream()
                .collect(Collectors.groupingBy(item -> item, Collectors.counting()));

        for (Map.Entry<ShopItem, Long> entry : itemCounts.entrySet()) {
            Node itemRow = createOrderItemRow(entry.getKey(), entry.getValue().intValue());
            cardVBox.getChildren().add(itemRow);
        }

        return cardVBox;
    }

    // 辅助方法：为单个商品行创建UI（带数量）
    private Node createOrderItemRow(ShopItem item, int quantity) {
        HBox itemHBox = new HBox(15);
        itemHBox.setAlignment(Pos.CENTER_LEFT);
        itemHBox.setPadding(new Insets(10, 0, 10, 0));
        itemHBox.setStyle("-fx-border-color: #F0F0F0; -fx-border-width: 1 0 0 0;"); // 分割线

        // 图片
        ImageView imageView = new ImageView();
        try {
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(item.getImagePath())));
            imageView.setImage(image);
        } catch (Exception e) {
            System.err.println("Could not load order image: " + item.getImagePath());
        }
        imageView.setFitHeight(80);
        imageView.setFitWidth(80);
        imageView.setPreserveRatio(true);

        // 名称和价格
        VBox nameAndPriceVBox = new VBox(5);
        Text nameText = new Text(item.getName());
        nameText.setFont(Font.font(16));
        Label priceLabel = new Label("¥ " + String.format("%.2f", item.getPrice()));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        nameAndPriceVBox.getChildren().addAll(nameText, priceLabel);

        // 伸缩弹簧
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 数量
        Label quantityLabel = new Label(String.valueOf(quantity));
        quantityLabel.setFont(Font.font(14));
        quantityLabel.setTextFill(Color.web("#616161"));
        StackPane quantityPane = new StackPane(quantityLabel);
        quantityPane.setPadding(new Insets(5, 10, 5, 10));
        quantityPane.setStyle("-fx-background-color: #F5F5F5; -fx-background-radius: 4;");

        itemHBox.getChildren().addAll(imageView, nameAndPriceVBox, spacer, quantityPane);
        return itemHBox;
    }
}