package app.vcampus.client.scene.SubScene.LibraryScene;

import app.vcampus.client.gateway.LibraryClient;
import app.vcampus.client.repository.FakeRepository;
import app.vcampus.server.entity.LibraryBook;
import app.vcampus.server.enums.BookStatus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LibraryBorrowBookViewController {

    @FXML
    private JFXTextField cardNumberField;

    @FXML
    private JFXTextField bookNameField;

    @FXML
    private JFXButton searchButton;

    @FXML
    private VBox resultsVBox;

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
            resultsVBox.getChildren().add(new Label("未找到可供借阅的副本。"));
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

        // Set column constraints for adaptive layout
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setHalignment(HPos.RIGHT);
        ColumnConstraints contentCol = new ColumnConstraints();
        contentCol.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(labelCol, contentCol, labelCol, contentCol, labelCol, contentCol);

        // Row 1: Book Name, Author, Press
        Label nameLabel = new Label("书名:");
        nameLabel.setStyle("-fx-font-weight: bold;");
        Label nameContent = new Label(book.getName());
        nameContent.setStyle("-fx-underline: true;");

        Label authorLabel = new Label("作者:");
        authorLabel.setStyle("-fx-font-weight: bold;");
        Label authorContent = new Label(book.getAuthor());
        authorContent.setStyle("-fx-underline: true;");

        Label pressLabel = new Label("出版社:");
        pressLabel.setStyle("-fx-font-weight: bold;");
        Label pressContent = new Label(book.getPress());
        pressContent.setStyle("-fx-underline: true;");

        grid.addRow(0, nameLabel, nameContent, authorLabel, authorContent, pressLabel, pressContent);

        // Row 2: Description
        Label descLabel = new Label("简介:");
        descLabel.setStyle("-fx-font-weight: bold;");
        Label descriptionContent = new Label(book.getDescription());
        descriptionContent.setWrapText(true);
        grid.add(descLabel, 0, 1);
        grid.add(descriptionContent, 1, 1, 5, 1);

        // Row 3: Call Number, Place
        Label callNumLabel = new Label("索书号:");
        callNumLabel.setStyle("-fx-font-weight: bold;");
        Label callNumContent = new Label(book.getCallNumber());
        callNumContent.setStyle("-fx-underline: true;");

        Label placeLabel = new Label("馆藏地:");
        placeLabel.setStyle("-fx-font-weight: bold;");
        Label placeContent = new Label(book.getPlace());
        placeContent.setStyle("-fx-underline: true;");

        grid.addRow(2, callNumLabel, callNumContent, placeLabel, placeContent);

        // Borrow button
        JFXButton borrowButton = new JFXButton("确定借阅该副本");
        borrowButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        borrowButton.setButtonType(JFXButton.ButtonType.RAISED);
        Label feedbackLabel = new Label();
        HBox buttonBox = new HBox(10, borrowButton, feedbackLabel);
        grid.add(buttonBox, 0, 3, 6, 1);

        borrowButton.setOnAction(event -> {
            String cardNumber = cardNumberField.getText();
            if (cardNumber == null || cardNumber.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "请输入一卡通号");
                return;
            }

            borrowButton.setDisable(true);
            new Thread(() -> {
                boolean success = LibraryClient.borrowBook(FakeRepository.handler, book.getUuid().toString(), cardNumber);
                Platform.runLater(() -> {
                    if (success) {
                        feedbackLabel.setText("借阅成功!");
                        feedbackLabel.setStyle("-fx-text-fill: green;");
                    } else {
                        feedbackLabel.setText("借阅失败，请检查一卡通号或该书状态。");
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