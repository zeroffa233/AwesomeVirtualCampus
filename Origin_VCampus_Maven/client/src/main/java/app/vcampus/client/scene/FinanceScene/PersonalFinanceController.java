package app.vcampus.client.scene.FinanceScene;

import app.vcampus.client.util.ShopItem;
import app.vcampus.client.util.DisplayableTransaction;
import app.vcampus.client.viewmodel.PersonalFinanceViewModel;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;
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

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class PersonalFinanceController implements Initializable {

    @FXML
    private Label balanceLabel;

    @FXML
    private JFXListView<DisplayableTransaction> transactionsListView;

    private final PersonalFinanceViewModel viewModel = new PersonalFinanceViewModel();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        balanceLabel.textProperty().bind(viewModel.balanceProperty().asString("%.2f 元"));
        transactionsListView.setCellFactory(param -> new TransactionListCell());
        transactionsListView.setItems(viewModel.getTransactionHistory());
        viewModel.loadData();
    }

    /**
     * Custom ListCell to display DisplayableTransaction objects.
     * Supports expandable view for shop transactions.
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

        // A private record to hold grouped item data
        private record GroupedItem(String name, int quantity, double totalPrice) {}

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
            expandButton.setOnAction(event -> toggleDetails());
        }

        private void toggleDetails() {
            boolean isExpanded = !itemsContainer.isManaged();
            itemsContainer.setVisible(isExpanded);
            itemsContainer.setManaged(isExpanded);
            expandButton.setText(isExpanded ? "▲" : "▼");
        }

        @Override
        protected void updateItem(DisplayableTransaction item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                if (itemsContainer.isManaged()) {
                    toggleDetails();
                }
                itemsContainer.getChildren().clear();

                typeLabel.setText(item.getType());
                dateLabel.setText(item.getDate());

                if ("商店消费".equals(item.getType())) {
                    amountLabel.setText(String.format("-%.2f", item.getAmount()));
                    amountLabel.setStyle("-fx-text-fill: red;"); // Black font for total price

                    if (item.getItems() != null && !item.getItems().isEmpty()) {
                        expandButton.setVisible(true);
                        expandButton.setManaged(true);
                        populateItemsContainer(item.getItems());
                    } else {
                        expandButton.setVisible(false);
                        expandButton.setManaged(false);
                    }
                } else { // For "Recharge" and other types
                    expandButton.setVisible(false);
                    expandButton.setManaged(false);
                    amountLabel.setText(String.format("+%.2f", item.getAmount()));
                    amountLabel.setStyle("-fx-text-fill: #4CAF50;"); // Green for recharge
                }

                setGraphic(layout);
                setText(null);
            }
        }

        private void populateItemsContainer(List<ShopItem> items) {
            Map<String, GroupedItem> groupedItems = new LinkedHashMap<>();
            for (ShopItem shopItem : items) {
                groupedItems.compute(shopItem.getName(), (name, grouped) -> {
                    if (grouped == null) {
                        return new GroupedItem(name, 1, shopItem.getPrice());
                    } else {
                        return new GroupedItem(name, grouped.quantity + 1, grouped.totalPrice + shopItem.getPrice());
                    }
                });
            }

            for (GroupedItem groupedItem : groupedItems.values()) {
                itemsContainer.getChildren().add(createItemRow(groupedItem));
            }
        }

        private Node createItemRow(GroupedItem item) {
            HBox itemRow = new HBox(10);
            itemRow.setAlignment(Pos.CENTER_LEFT);
            Label itemName = new Label(item.name() + " x" + item.quantity());
            Region itemSpacer = new Region();
            HBox.setHgrow(itemSpacer, Priority.ALWAYS);
            Label itemPrice = new Label(String.format("%.2f", item.totalPrice()));
            itemRow.getChildren().addAll(itemName, itemSpacer, itemPrice);
            return itemRow;
        }
    }
}