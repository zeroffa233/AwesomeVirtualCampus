package app.vcampus.client.scene;

import app.vcampus.client.scene.SubScene.ShopScene.ShopController;
import app.vcampus.client.util.ImageCache;
import app.vcampus.server.entity.StoreItem;
import app.vcampus.server.utility.ShopTransactionRecord;
import javafx.fxml.FXML;
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

/**
 * 订单场景控制器。
 * 负责展示用户的订单历史记录，按天分组显示。
 */
public class OrderController {

    @FXML
    private VBox ordersListContainer;

    /**
     * 日期格式化器。
     */
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 初始化方法，在FXML文件加载完成后自动调用。
     */
    @FXML
    public void initialize() {
        List<ShopTransactionRecord> allTransactions = ShopController.transactionHistory;

        if (allTransactions.isEmpty()) {
            ordersListContainer.getChildren().add(new Label("暂无订单记录"));
            return;
        }

        Map<LocalDate, List<ShopTransactionRecord>> groupedByDay = allTransactions.stream()
                .collect(Collectors.groupingBy(record ->
                        Instant.ofEpochMilli(record.getTimestamp()).atZone(ZoneId.systemDefault()).toLocalDate()
                ));

        List<LocalDate> sortedDates = new ArrayList<>(groupedByDay.keySet());
        sortedDates.sort(Comparator.reverseOrder());

        for (LocalDate date : sortedDates) {
            Node dayGroupNode = createDayGroup(date, groupedByDay.get(date));
            ordersListContainer.getChildren().add(dayGroupNode);
        }
    }

    private Node createDayGroup(LocalDate date, List<ShopTransactionRecord> transactions) {
        VBox dayVBox = new VBox(20);

        Label dateLabel = new Label(date.format(dateFormatter));
        dateLabel.setFont(Font.font("System", FontWeight.BOLD, 22));

        dayVBox.getChildren().add(dateLabel);

        for (ShopTransactionRecord transaction : transactions) {
            Node transactionNode = createTransactionCard(transaction);
            dayVBox.getChildren().add(transactionNode);
        }

        return dayVBox;
    }

    /**
     * 创建交易卡片。
     *
     * @param transaction 交易记录。
     * @return 交易卡片节点。
     */
    private Node createTransactionCard(ShopTransactionRecord transaction) {
        VBox cardVBox = new VBox(10);
        cardVBox.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-background-color: white; -fx-background-radius: 8;");
        cardVBox.setPadding(new Insets(20));

        Label totalLabel = new Label("共 " + String.format("%.2f", transaction.getTotalPrice()) + " 元");
        totalLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        HBox totalHBox = new HBox(totalLabel);
        totalHBox.setAlignment(Pos.CENTER_RIGHT);
        cardVBox.getChildren().add(totalHBox);

        Map<StoreItem, Long> itemCounts = transaction.getItems().stream()
                .collect(Collectors.groupingBy(item -> item, Collectors.counting()));

        for (Map.Entry<StoreItem, Long> entry : itemCounts.entrySet()) {
            Node itemRow = createOrderItemRow(entry.getKey(), entry.getValue().intValue());
            cardVBox.getChildren().add(itemRow);
        }

        return cardVBox;
    }

    /**
     * 创建订单商品行。
     *
     * @param item 商品。
     * @param quantity 数量。
     * @return 订单商品行节点。
     */
    private Node createOrderItemRow(StoreItem item, int quantity) {
        HBox itemHBox = new HBox(15);
        itemHBox.setAlignment(Pos.CENTER_LEFT);
        itemHBox.setPadding(new Insets(10, 0, 10, 0));
        itemHBox.setStyle("-fx-border-color: #F0F0F0; -fx-border-width: 1 0 0 0;");

        ImageView imageView = new ImageView();
        try {
            Image image = ImageCache.getInstance().getImage(item.getPictureLink());
            if (image.isError()) throw new Exception("Corrupted image data");
            imageView.setImage(image);
        } catch (Exception e) {
            System.err.println("无法加载订单图片 '" + item.getPictureLink() + "', 使用默认图。");
            try {
                imageView.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/500.png"))));
            } catch (Exception fallbackEx) {
            }
        }
        imageView.setFitHeight(80);
        imageView.setFitWidth(80);
        imageView.setPreserveRatio(true);

        VBox nameAndPriceVBox = new VBox(5);
        Text nameText = new Text(item.getItemName());
        nameText.setFont(Font.font(16));
        Label priceLabel = new Label(String.format("¥%.2f", item.getPrice().doubleValue() / 100.0));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        nameAndPriceVBox.getChildren().addAll(nameText, priceLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

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