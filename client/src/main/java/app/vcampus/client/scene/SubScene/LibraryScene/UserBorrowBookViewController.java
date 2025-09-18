package app.vcampus.client.scene.SubScene.LibraryScene;

import app.vcampus.client.gateway.LibraryClient;
import app.vcampus.client.repository.FakeRepository;
import app.vcampus.server.entity.LibraryBook;
import app.vcampus.server.enums.BookStatus;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户借书视图控制器。
 * 负责处理用户搜索可借阅图书并执行借阅操作的界面逻辑。
 */
public class UserBorrowBookViewController {

    @FXML
    private JFXTextField bookNameField;

    @FXML
    private JFXButton searchButton;

    @FXML
    private VBox resultsVBox;

    /**
     * 初始化方法，在FXML文件加载完成后自动调用。
     */
    @FXML
    public void initialize() {
        searchButton.setOnAction(event -> searchAvailableBooks());
    }

    private void searchAvailableBooks() {
        String bookName = bookNameField.getText();
        if (bookName == null || bookName.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "请输入书籍名称");
            return;
        }

        new Thread(() -> {
            try {
                Map<String, List<LibraryBook>> booksMap = LibraryClient.searchBook(FakeRepository.handler, bookName);
                List<LibraryBook> availableBooks = booksMap.values().stream()
                        .flatMap(List::stream)
                        .filter(book -> book.getBookStatus() == BookStatus.available)
                        .collect(Collectors.toList());

                Platform.runLater(() -> displayResults(availableBooks));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "搜索失败，请检查网络或联系管理员。"));
            }
        }).start();
    }

    private void displayResults(List<LibraryBook> books) {
        resultsVBox.getChildren().clear();
        if (books.isEmpty()) {
            resultsVBox.getChildren().add(new Label("未找到该书的可借阅副本。"));
            return;
        }

        for (LibraryBook book : books) {
            resultsVBox.getChildren().add(createBookDisplayNode(book));
        }
    }

    private GridPane createBookDisplayNode(LibraryBook book) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.setStyle("-fx-border-color: #dcdcdc; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #F8F9FA;");

        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setHalignment(HPos.RIGHT);
        ColumnConstraints contentCol = new ColumnConstraints();
        contentCol.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(labelCol, contentCol, labelCol, contentCol);

        Label bookNameTitle = new Label("书名:");
        bookNameTitle.setStyle("-fx-font-size: 14px;");
        Label bookNameContent = new Label(book.getName());
        bookNameContent.setStyle("-fx-font-size: 14px;");

        Label callNumberTitle = new Label("索书号:");
        callNumberTitle.setStyle("-fx-font-size: 14px;");
        Label callNumberContent = new Label(book.getCallNumber());
        callNumberContent.setStyle("-fx-font-size: 14px;");

        Label authorTitle = new Label("作者:");
        authorTitle.setStyle("-fx-font-size: 14px;");
        Label authorContent = new Label(book.getAuthor());
        authorContent.setStyle("-fx-font-size: 14px;");

        Label placeTitle = new Label("馆藏地:");
        placeTitle.setStyle("-fx-font-size: 14px;");
        Label placeContent = new Label(book.getPlace());
        placeContent.setStyle("-fx-font-size: 14px;");

        grid.add(bookNameTitle, 0, 0);
        grid.add(bookNameContent, 1, 0);
        grid.add(callNumberTitle, 2, 0);
        grid.add(callNumberContent, 3, 0);

        grid.add(authorTitle, 0, 1);
        grid.add(authorContent, 1, 1);
        grid.add(placeTitle, 2, 1);
        grid.add(placeContent, 3, 1);

        JFXButton borrowButton = new JFXButton("确定借阅该副本");
        borrowButton.setStyle("-fx-background-color: #607830DE; -fx-text-fill: white;");
        borrowButton.setButtonType(JFXButton.ButtonType.RAISED);
        Label feedbackLabel = new Label();
        HBox buttonBox = new HBox(10, borrowButton, feedbackLabel);
        grid.add(buttonBox, 0, 2, 4, 1);

        borrowButton.setOnAction(event -> {
            if (FakeRepository.user == null || FakeRepository.user.getCardNum() == null) {
                showAlert(Alert.AlertType.ERROR, "无法获取当前用户信息，请重新登录");
                return;
            }
            String cardNumber = FakeRepository.user.getCardNum().toString();

            borrowButton.setDisable(true);
            new Thread(() -> {
                boolean success = LibraryClient.borrowBook(FakeRepository.handler, book.getUuid().toString(), cardNumber);
                Platform.runLater(() -> {
                    if (success) {
                        feedbackLabel.setText("借阅成功!");
                        feedbackLabel.setStyle("-fx-text-fill: green;");
                    } else {
                        feedbackLabel.setText("借阅失败");
                        feedbackLabel.setStyle("-fx-text-fill: red;");
                        borrowButton.setDisable(false);
                    }
                });
            }).start();
        });

        return grid;
    }

    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}