package app.vcampus.client.scene.SubScene.LibraryScene;

import app.vcampus.client.gateway.LibraryClient;
import app.vcampus.client.repository.FakeRepository;
import app.vcampus.server.entity.LibraryTransaction;
import com.jfoenix.controls.JFXButton;
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

/**
 * 图书馆借阅历史视图控制器。
 * 负责显示用户当前的借阅记录，并提供还书功能。
 */
public class LibraryHistoryViewController {

    @FXML
    private VBox resultsVBox;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 初始化方法，在FXML文件加载完成后自动调用。
     */
    @FXML
    public void initialize() {
        loadUserTransactions();
    }

    private void loadUserTransactions() {
        if (FakeRepository.user == null) {
            Platform.runLater(() -> showAlert(Alert.AlertType.WARNING, "无法获取用户信息，请重新登录"));
            return;
        }

        new Thread(() -> {
            try {
                List<LibraryTransaction> transactions = LibraryClient.getMyRecords(FakeRepository.handler);
                List<LibraryTransaction> activeTransactions = transactions.stream()
                        .filter(t -> t.getReturnTime() == null)
                        .collect(Collectors.toList());

                Platform.runLater(() -> displayResults(activeTransactions));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "加载借阅列表失败，请检查网络连接。"));
            }
        }).start();
    }

    private void displayResults(List<LibraryTransaction> transactions) {
        resultsVBox.getChildren().clear();
        if (transactions.isEmpty()) {
            Label emptyLabel = new Label("您当前没有借阅中的书籍。");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #888888;");
            resultsVBox.getChildren().add(emptyLabel);
            resultsVBox.setAlignment(Pos.CENTER);
            return;
        }

        resultsVBox.setAlignment(Pos.TOP_CENTER);
        for (LibraryTransaction transaction : transactions) {
            resultsVBox.getChildren().add(createTransactionNode(transaction));
        }
    }

    private GridPane createTransactionNode(LibraryTransaction transaction) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));
        grid.setStyle("-fx-border-color: #dcdcdc; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-color: #F8F9FA; -fx-background-radius: 8;");

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHalignment(HPos.RIGHT);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setHalignment(HPos.RIGHT);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2, col3, col4);

        Label nameLabel = new Label("书名:");
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label nameContent = new Label(transaction.getBook().getName());
        nameContent.setStyle("-fx-font-size: 14px;");
        grid.add(nameLabel, 0, 0);
        grid.add(nameContent, 1, 0);

        Label placeLabel = new Label("馆藏地:");
        placeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label placeContent = new Label(transaction.getBook().getPlace());
        placeContent.setStyle("-fx-font-size: 14px;");
        grid.add(placeLabel, 2, 0);
        grid.add(placeContent, 3, 0);

        Label borrowLabel = new Label("借阅时间:");
        borrowLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label borrowContent = new Label(dateFormat.format(transaction.getBorrowTime()));
        borrowContent.setStyle("-fx-font-size: 14px;");
        grid.add(borrowLabel, 0, 1);
        grid.add(borrowContent, 1, 1);

        Label dueLabel = new Label("应还时间:");
        dueLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label dueContent = new Label(dateFormat.format(transaction.getDueTime()));
        dueContent.setStyle("-fx-font-size: 14px;");
        grid.add(dueLabel, 2, 1);
        grid.add(dueContent, 3, 1);

        JFXButton returnButton = new JFXButton("还书");
        returnButton.setStyle("-fx-background-color: #607830DE; -fx-text-fill: white;");
        returnButton.setButtonType(JFXButton.ButtonType.RAISED);
        returnButton.setPrefSize(100, 35);

        HBox actionBox = new HBox(10, returnButton);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        grid.add(actionBox, 0, 2, 4, 1);

        returnButton.setOnAction(event -> handleReturn(transaction, returnButton));

        return grid;
    }

    private void handleReturn(LibraryTransaction transaction, JFXButton returnButton) {
        returnButton.setDisable(true);
        new Thread(() -> {
            boolean success = LibraryClient.returnBook(FakeRepository.handler, transaction.getUuid());
            Platform.runLater(() -> {
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "还书成功!");
                    loadUserTransactions();
                } else {
                    showAlert(Alert.AlertType.ERROR, "还书失败，请稍后再试。");
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