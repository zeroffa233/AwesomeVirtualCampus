package app.vcampus.client.scene.SubScene.FinanceScene;

import app.vcampus.server.entity.StoreItem;
import app.vcampus.server.utility.DisplayableTransaction;
import app.vcampus.client.viewmodel.PersonalFinanceViewModel;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * 个人财务场景控制器。
 * 负责展示用户的余额和交易历史记录。
 */
public class PersonalFinanceController implements Initializable {

    @FXML
    private Label balanceLabel;

    @FXML
    private JFXListView<DisplayableTransaction> transactionsListView;

    private final PersonalFinanceViewModel viewModel = new PersonalFinanceViewModel();

    /**
     * 初始化方法，在FXML文件加载完成后自动调用。
     *
     * @param location  URL定位资源。
     * @param resources 资源包。
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        balanceLabel.textProperty().bind(viewModel.balanceProperty().asString("%.2f 元"));

        final String inlineCss = """
            .jfx-list-cell:selected {
                -fx-background-color: #B2C926B2;
            }
            .jfx-list-cell:selected .label {
                -fx-text-fill: black;
            }
            .jfx-list-cell .jfx-rippler {
                -jfx-rippler-fill: #728748;
            }
        """;

        transactionsListView.getStylesheets().add("data:text/css," + inlineCss);

        transactionsListView.setCellFactory(param -> new TransactionListCell());
        transactionsListView.setItems(viewModel.getTransactionHistory());
        viewModel.loadData();
    }

    /**
     * 自定义 ListCell 用于显示 DisplayableTransaction 对象。
     * 支持带动画效果的商店交易记录展开视图。
     */
    private static class TransactionListCell extends JFXListCell<DisplayableTransaction> {
        private final VBox layout = new VBox();
        private final HBox summaryContent = new HBox();
        private final VBox details = new VBox();
        private final Label typeLabel = new Label();
        private final Label dateLabel = new Label();
        private final Label amountLabel = new Label();
        private final Region spacer = new Region();
        private final Button expandButton = new Button("▼");

        private final VBox itemsContainer = new VBox();

        private record GroupedItem(String name, int quantity, double unitPrice) {}

        public TransactionListCell() {
            super();
            typeLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            dateLabel.setStyle("-fx-text-fill: #888888;");
            amountLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            expandButton.setStyle("-fx-background-color: transparent; -fx-padding: 0 8;");

            details.getChildren().addAll(typeLabel, dateLabel);
            HBox.setHgrow(spacer, Priority.ALWAYS);

            summaryContent.getChildren().addAll(details, spacer, amountLabel, expandButton);
            summaryContent.setAlignment(Pos.CENTER_LEFT);
            summaryContent.setSpacing(10);

            itemsContainer.setVisible(false);
            itemsContainer.setManaged(false);
            itemsContainer.setPadding(new Insets(10, 0, 5, 20));
            itemsContainer.setSpacing(5);

            layout.getChildren().addAll(summaryContent, itemsContainer);

            expandButton.setOnAction(event -> toggleDetails(true));

            selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                if (isNowSelected) {
                    setStyle("-fx-background-color: #B2C926B2;");
                } else {
                    setStyle("-fx-background-color: transparent;");
                }
            });
        }

        private void toggleDetails(boolean animated) {
            boolean isExpanding = !itemsContainer.isManaged();

            if (!animated) {
                itemsContainer.setVisible(isExpanding);
                itemsContainer.setManaged(isExpanding);
                expandButton.setText(isExpanding ? "▲" : "▼");
                if (!isExpanding) {
                    itemsContainer.setOpacity(1.0);
                }
                return;
            }

            if (isExpanding) {
                itemsContainer.setOpacity(0.0);
                itemsContainer.setVisible(true);
                itemsContainer.setManaged(true);
                FadeTransition ft = new FadeTransition(Duration.millis(300), itemsContainer);
                ft.setFromValue(0.0);
                ft.setToValue(1.0);
                ft.play();
                expandButton.setText("▲");
            } else {
                FadeTransition ft = new FadeTransition(Duration.millis(300), itemsContainer);
                ft.setFromValue(1.0);
                ft.setToValue(0.0);
                ft.setOnFinished(event -> {
                    itemsContainer.setVisible(false);
                    itemsContainer.setManaged(false);
                });
                ft.play();
                expandButton.setText("▼");
            }
        }

        @Override
        protected void updateItem(DisplayableTransaction item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                return;
            }

            // Instantly collapse the cell if it's being recycled by the list view
            if (itemsContainer.isManaged()) {
                toggleDetails(false);
            }
            itemsContainer.getChildren().clear();

            // === 【核心修改逻辑】 ===
            // 1. 使用 DisplayableTransaction 解析好的数据
            typeLabel.setText(item.getType()); // 直接获取类型，如 "商品售出"
            dateLabel.setText(item.getDate());

            // 2. 检查是否存在可供展示的商品列表
            List<StoreItem> items = item.getItems();
            boolean hasExpandableItems = items != null && !items.isEmpty();

            // 3. 根据是否存在商品列表，决定是否显示展开按钮并填充内容
            expandButton.setVisible(hasExpandableItems);
            expandButton.setManaged(hasExpandableItems);
            if (hasExpandableItems) {
                populateItemsContainer(items);
            }

            // 4. 根据原始交易类型（payment/deposit）设置金额颜色和符号
            if ("payment".equals(item.getRawType())) {
                amountLabel.setText(String.format("-%.2f", item.getAmount()));
                amountLabel.setStyle("-fx-text-fill: red;");
            } else { // 涵盖 "deposit" (包括普通充值和商品售出)
                amountLabel.setText(String.format("+%.2f", item.getAmount()));
                amountLabel.setStyle("-fx-text-fill: #4CAF50;"); // 绿色代表收入
            }

            setGraphic(layout);
            setText(null);
        }


        private void populateItemsContainer(List<StoreItem> items) {
            // 【修复】不再需要客户端进行分组，因为从服务器获取的数据已经包含了正确的商品种类和对应的数量 (存在stock字段)
            // 直接遍历items列表，为每个StoreItem（代表一类商品）创建对应的UI行
            for (StoreItem item : items) {
                // 从StoreItem中直接获取信息：
                // 1. 商品名称
                String name = item.getItemName();
                // 2. 商品数量 (存储在复用的stock字段中)
                // 注意：需要确认StoreItem中的stock字段类型，JSON中是整数，假设getter返回long或int
                int quantity = item.getStock().intValue();
                // 3. 商品单价 (服务器存储的是分，需要转换为元)
                double unitPrice = item.getPrice().doubleValue() / 100.0;

                // 使用获取到的正确信息创建GroupedItem记录
                GroupedItem groupedItem = new GroupedItem(name, quantity, unitPrice);

                // 调用现有的方法创建UI并添加到容器中
                itemsContainer.getChildren().add(createItemRow(groupedItem));
            }
        }

        private Node createItemRow(GroupedItem item) {
            HBox itemRow = new HBox(4);
            itemRow.setAlignment(Pos.CENTER_LEFT);

            Label itemName = new Label(item.name());

            Region itemSpacer = new Region();
            HBox.setHgrow(itemSpacer, Priority.ALWAYS);

            Label quantityLabel = new Label(String.format("%d个，共", item.quantity()));
            quantityLabel.setStyle("-fx-text-fill: #616161;");

            Label itemPrice = new Label(String.format("¥%.2f", item.unitPrice() * item.quantity()));
            itemPrice.setFont(Font.font("System", FontWeight.MEDIUM, 14));

            itemRow.getChildren().addAll(itemName, itemSpacer, quantityLabel, itemPrice);

            return itemRow;
        }
    }
}
