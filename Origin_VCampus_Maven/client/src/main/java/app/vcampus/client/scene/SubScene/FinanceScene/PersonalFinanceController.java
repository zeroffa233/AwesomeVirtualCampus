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

public class PersonalFinanceController implements Initializable {

    @FXML
    private Label balanceLabel;

    @FXML
    private JFXListView<DisplayableTransaction> transactionsListView;

    private final PersonalFinanceViewModel viewModel = new PersonalFinanceViewModel();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        balanceLabel.textProperty().bind(viewModel.balanceProperty().asString("%.2f 元"));

        // --- 开始修改 ---
        // 1. 将CSS规则定义为一个字符串
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

        // 2. 创建Data URL并添加到ListView的样式表中
        transactionsListView.getStylesheets().add("data:text/css," + inlineCss);
        // --- 结束修改 ---

        transactionsListView.setCellFactory(param -> new TransactionListCell());
        transactionsListView.setItems(viewModel.getTransactionHistory());
        viewModel.loadData();
    }

    /**
     * Custom ListCell to display DisplayableTransaction objects.
     * Supports expandable view for shop transactions with animation.
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

            // Handle expansion/collapse with animation on button click
            expandButton.setOnAction(event -> toggleDetails(true));

            // Add a listener to change background color on selection
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
                    itemsContainer.setOpacity(1.0); // Reset opacity for future animations
                }
                return;
            }

            if (isExpanding) {
                // Expand with fade-in animation
                itemsContainer.setOpacity(0.0);
                itemsContainer.setVisible(true);
                itemsContainer.setManaged(true);
                FadeTransition ft = new FadeTransition(Duration.millis(300), itemsContainer);
                ft.setFromValue(0.0);
                ft.setToValue(1.0);
                ft.play();
                expandButton.setText("▲");
            } else {
                // Collapse with fade-out animation
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
            } else {
                // Instantly collapse the cell if it's being recycled by the list view
                if (itemsContainer.isManaged()) {
                    toggleDetails(false);
                }
                itemsContainer.getChildren().clear();

                typeLabel.setText(item.getType()); // Use translated type for display
                dateLabel.setText(item.getDate());

                // **CORE LOGIC CHANGE**: Check the rawType from the server
                if ("payment".equals(item.getRawType())) {
                    amountLabel.setText(String.format("-%.2f", item.getAmount()));
                    amountLabel.setStyle("-fx-text-fill: red;");

                    // The item list is now pre-parsed in DisplayableTransaction
                    List<StoreItem> items = item.getItems();
                    if (items != null && !items.isEmpty()) {
                        expandButton.setVisible(true);
                        expandButton.setManaged(true);
                        populateItemsContainer(items);
                    } else {
                        expandButton.setVisible(false);
                        expandButton.setManaged(false);
                    }
                } else { // For "deposit" and other types
                    expandButton.setVisible(false);
                    expandButton.setManaged(false);
                    amountLabel.setText(String.format("+%.2f", item.getAmount()));
                    amountLabel.setStyle("-fx-text-fill: #4CAF50;"); // Green for recharge
                }

                setGraphic(layout);
                setText(null);
            }
        }

        private void populateItemsContainer(List<StoreItem> items) {
            Map<String, GroupedItem> groupedItems = new LinkedHashMap<>();
            // Group items by name to count quantity
            for (StoreItem shopItem : items) {
                groupedItems.compute(shopItem.getItemName(), (name, grouped) -> {
                    if (grouped == null) {
                        return new GroupedItem(name, 1, shopItem.getPrice().doubleValue() / 100.0);
                    } else {
                        return new GroupedItem(name, grouped.quantity + 1, grouped.unitPrice);
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
            // Calculate total price for the group
            Label itemPrice = new Label(String.format("%.2f", item.unitPrice() * item.quantity()));
            itemRow.getChildren().addAll(itemName, itemSpacer, itemPrice);
            return itemRow;
        }
    }
}