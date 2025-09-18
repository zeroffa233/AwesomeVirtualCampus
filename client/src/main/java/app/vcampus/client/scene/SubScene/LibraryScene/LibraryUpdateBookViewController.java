package app.vcampus.client.scene.SubScene.LibraryScene;

import app.vcampus.client.gateway.LibraryClient;
import app.vcampus.client.repository.FakeRepository;
import app.vcampus.server.entity.LibraryBook;
import app.vcampus.server.enums.BookStatus;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;

/**
 * 图书信息更新视图控制器。
 * 负责处理图书管理员搜索和更新图书信息的界面逻辑。
 */
public class LibraryUpdateBookViewController {

    @FXML
    private JFXTextField searchTextField;

    @FXML
    private JFXButton searchButton;

    @FXML
    private Label countLabel;

    @FXML
    private VBox resultsVBox;

    /**
     * 初始化方法，在FXML文件加载完成后自动调用。
     */
    @FXML
    public void initialize() {
        searchButton.setOnAction(event -> searchBooks());
    }

    private void searchBooks() {
        String keyword = searchTextField.getText();
        if (keyword == null || keyword.trim().isEmpty()) {
            countLabel.setText("请输入搜索关键词。");
            return;
        }

        new Thread(() -> {
            try {
                Map<String, List<LibraryBook>> booksMap = LibraryClient.searchBook(FakeRepository.handler, keyword);
                Platform.runLater(() -> displayResults(booksMap));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> countLabel.setText("搜索失败，请检查网络或联系管理员。"));
            }
        }).start();
    }

    private void displayResults(Map<String, List<LibraryBook>> booksMap) {
        resultsVBox.getChildren().clear();
        if (booksMap == null || booksMap.isEmpty()) {
            countLabel.setText("未找到相关书籍。");
            return;
        }

        int totalCount = 0;
        for (List<LibraryBook> bookList : booksMap.values()) {
            totalCount += bookList.size();
        }
        countLabel.setText("该书在数据库之中目前有 " + totalCount + " 本");

        for (List<LibraryBook> bookList : booksMap.values()) {
            for (LibraryBook book : bookList) {
                resultsVBox.getChildren().add(createBookEditor(book));
            }
        }
    }

    private GridPane createBookEditor(LibraryBook book) {
        GridPane grid = new GridPane();
        grid.setVgap(20);
        grid.setPadding(new Insets(30));
        grid.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #BDBDBD; -fx-border-width: 1; -fx-border-radius: 8; -fx-font-size: 14px;");

        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setHgrow(Priority.NEVER);
        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(labelCol, fieldCol, labelCol, fieldCol, labelCol, fieldCol);

        JFXTextField nameField = createStyledTextField(book.getName());
        JFXTextField authorField = createStyledTextField(book.getAuthor());
        JFXTextField pressField = createStyledTextField(book.getPress());
        grid.addRow(0, new Label("书名:"), nameField, new Label("作者:"), authorField, new Label("出版社:"), pressField);

        JFXTextField coverField = createStyledTextField(book.getCover());
        JFXTextField isbnField = createStyledTextField(book.getIsbn());
        isbnField.setEditable(false);
        grid.add(new Label("封面链接:"), 0, 1);
        grid.add(coverField, 1, 1, 3, 1);
        grid.add(new Label("ISBN:"), 4, 1);
        grid.add(isbnField, 5, 1);

        JFXTextArea descriptionArea = new JFXTextArea(book.getDescription());
        descriptionArea.setWrapText(true);
        grid.add(new Label("简介:"), 0, 2);
        grid.add(descriptionArea, 1, 2, 5, 1);

        JFXTextField callNumberField = createStyledTextField(book.getCallNumber());
        JFXTextField placeField = createStyledTextField(book.getPlace());
        JFXComboBox<BookStatus> statusComboBox = new JFXComboBox<>();
        statusComboBox.getItems().setAll(BookStatus.values());
        statusComboBox.setValue(book.getBookStatus());
        statusComboBox.setPromptText("书籍状态");
        grid.addRow(3, new Label("索书号:"), callNumberField, new Label("馆藏地:"), placeField, new Label("状态:"), statusComboBox);

        JFXButton saveButton = new JFXButton("保存此副本修改");
        saveButton.setButtonType(JFXButton.ButtonType.RAISED);
        saveButton.setStyle("-fx-background-color: #607830; -fx-text-fill: white;");
        Label feedbackLabel = new Label();
        HBox buttonBox = new HBox(10, saveButton, feedbackLabel);
        grid.add(buttonBox, 0, 4, 6, 1);

        grid.getChildren().forEach(node -> {
            if (node instanceof Label) {
                node.setStyle("-fx-font-weight: bold;");
            }
        });

        saveButton.setOnAction(event -> {
            book.setName(nameField.getText());
            book.setAuthor(authorField.getText());
            book.setPress(pressField.getText());
            book.setCover(coverField.getText());
            book.setDescription(descriptionArea.getText());
            book.setCallNumber(callNumberField.getText());
            book.setPlace(placeField.getText());
            book.setBookStatus(statusComboBox.getValue());

            new Thread(() -> {
                boolean success = LibraryClient.updateBook(FakeRepository.handler, book);
                Platform.runLater(() -> {
                    if (success) {
                        feedbackLabel.setText("保存成功!");
                        feedbackLabel.setStyle("-fx-text-fill: green;");
                    } else {
                        feedbackLabel.setText("保存失败。");
                        feedbackLabel.setStyle("-fx-text-fill: red;");
                    }
                });
            }).start();
        });

        return grid;
    }

    private JFXTextField createStyledTextField(String text) {
        JFXTextField textField = new JFXTextField(text);

        textField.promptTextProperty().addListener((obs, oldVal, newVal) -> {
            if (textField.lookup(".prompt-text") instanceof Label) {
                Label promptText = (Label) textField.lookup(".prompt-text");
                promptText.opacityProperty().bind(
                        Bindings.when(textField.textProperty().isEmpty())
                                .then(1.0)
                                .otherwise(0.0)
                );
            }
        });

        return textField;
    }
}