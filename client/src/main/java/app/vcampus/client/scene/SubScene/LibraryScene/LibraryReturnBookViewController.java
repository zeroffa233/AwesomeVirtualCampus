package app.vcampus.client.scene.SubScene.LibraryScene;

import app.vcampus.client.gateway.LibraryClient;
import app.vcampus.client.repository.FakeRepository;
import app.vcampus.server.entity.LibraryTransaction;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

public class LibraryReturnBookViewController {

    @FXML
    private JFXTextField cardNumberField;

    @FXML
    private JFXButton searchButton;

    @FXML
    private VBox resultsVBox;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        searchButton.setOnAction(event -> searchUserTransactions());
    }

    private void searchUserTransactions() {
        String cardNumber = cardNumberField.getText();
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "请输入一卡通号");
            return;
        }

        new Thread(() -> {
            try {
                List<LibraryTransaction> transactions = LibraryClient.staffGetRecords(FakeRepository.handler, cardNumber);
                // Filter for books that are not yet returned
                List<LibraryTransaction> activeTransactions = transactions.stream()
                        .filter(t -> t.getReturnTime() == null)
                        .collect(Collectors.toList());

                Platform.runLater(() -> displayResults(activeTransactions));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "搜索失败，请检查网络或一卡通号。"));
            }
        }).start();
    }

    private void displayResults(List<LibraryTransaction> transactions) {
        resultsVBox.getChildren().clear();
        if (transactions.isEmpty()) {
            resultsVBox.getChildren().add(new Label("该用户没有未归还的书籍。"));
            return;
        }

        for (LibraryTransaction transaction : transactions) {
            resultsVBox.getChildren().add(createTransactionNode(transaction));
        }
    }

    private GridPane createTransactionNode(LibraryTransaction transaction) {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-border-color: #dcdcdc; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #F8F9FA; -fx-font-size: 14px;");

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHalignment(HPos.RIGHT);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setHalignment(HPos.RIGHT);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setHgrow(Priority.ALWAYS);
        ColumnConstraints col5 = new ColumnConstraints();
        col5.setPrefWidth(100);
        grid.getColumnConstraints().addAll(col1, col2, col3, col4, col5);

        // Row 1
        Label nameLabel = new Label("书名:");
        nameLabel.setStyle("-fx-font-weight: bold;");
        Label nameContent = new Label(transaction.getBook().getName());
        grid.add(nameLabel, 0, 0);
        grid.add(nameContent, 1, 0);

        Label placeLabel = new Label("馆藏地:");
        placeLabel.setStyle("-fx-font-weight: bold;");
        Label placeContent = new Label(transaction.getBook().getPlace());
        grid.add(placeLabel, 2, 0);
        grid.add(placeContent, 3, 0);

        // Row 2
        Label borrowLabel = new Label("借阅时间:");
        borrowLabel.setStyle("-fx-font-weight: bold;");
        Label borrowContent = new Label(dateFormat.format(transaction.getBorrowTime()));
        grid.add(borrowLabel, 0, 1);
        grid.add(borrowContent, 1, 1);

        Label dueLabel = new Label("应还时间:");
        dueLabel.setStyle("-fx-font-weight: bold;");
        Label dueContent = new Label(dateFormat.format(transaction.getDueTime()));
        grid.add(dueLabel, 2, 1);
        grid.add(dueContent, 3, 1);

        // Action Buttons
        JFXButton renewButton = new JFXButton("续借");
        renewButton.setStyle("-fx-background-color: #FFC107; -fx-text-fill: white;");
        renewButton.setButtonType(JFXButton.ButtonType.RAISED);
        renewButton.setPrefWidth(100);

        JFXButton returnButton = new JFXButton("还书");
        returnButton.setStyle("-fx-background-color: #607830; -fx-text-fill: white;");
        returnButton.setButtonType(JFXButton.ButtonType.RAISED);
        returnButton.setPrefWidth(100);

        VBox actionBox = new VBox(10, renewButton, returnButton);
        actionBox.setAlignment(Pos.CENTER);
        grid.add(actionBox, 4, 0, 1, 2);

        Label feedbackLabel = new Label();
        grid.add(feedbackLabel, 0, 2, 4, 1);

        // Button Actions
        renewButton.setOnAction(event -> handleRenew(transaction, feedbackLabel, renewButton, returnButton));
        returnButton.setOnAction(event -> handleReturn(transaction, feedbackLabel, renewButton, returnButton));

        return grid;
    }

    private void handleRenew(LibraryTransaction transaction, Label feedbackLabel, JFXButton renewButton, JFXButton returnButton) {
        renewButton.setDisable(true);
        returnButton.setDisable(true);
        new Thread(() -> {
            boolean success = LibraryClient.staffRenewBook(FakeRepository.handler, transaction.getUuid());
            Platform.runLater(() -> {
                if (success) {
                    feedbackLabel.setText("续借成功！请重新搜索以刷新应还时间。");
                    feedbackLabel.setStyle("-fx-text-fill: green;");
                } else {
                    feedbackLabel.setText("续借失败。");
                    feedbackLabel.setStyle("-fx-text-fill: red;");
                    renewButton.setDisable(false);
                    returnButton.setDisable(false);
                }
            });
        }).start();
    }

    private void handleReturn(LibraryTransaction transaction, Label feedbackLabel, JFXButton renewButton, JFXButton returnButton) {
        renewButton.setDisable(true);
        returnButton.setDisable(true);
        new Thread(() -> {
            boolean success = LibraryClient.returnBook(FakeRepository.handler, transaction.getUuid());
            Platform.runLater(() -> {
                if (success) {
                    feedbackLabel.setText("还书成功!");
                    feedbackLabel.setStyle("-fx-text-fill: green;");
                } else {
                    feedbackLabel.setText("还书失败。");
                    feedbackLabel.setStyle("-fx-text-fill: red;");
                    renewButton.setDisable(false);
                    returnButton.setDisable(false);
                }
            });
        }).start();
    }

    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}