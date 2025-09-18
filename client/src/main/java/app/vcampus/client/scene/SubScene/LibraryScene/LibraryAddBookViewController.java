package app.vcampus.client.scene.SubScene.LibraryScene;

import app.vcampus.client.gateway.LibraryClient;
import app.vcampus.client.repository.FakeRepository;
import app.vcampus.server.entity.LibraryBook;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * 图书馆添加书籍视图控制器。
 * 负责处理添加新书界面的逻辑。
 */
public class LibraryAddBookViewController {
    @FXML
    private TextField authorField;
    @FXML
    private TextField callNumberField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField coverField;
    @FXML
    private TextField isbnField;
    @FXML
    private TextField placeField;
    @FXML
    private TextField pressField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private JFXButton confirmButton;
    @FXML
    private Label infoLabel;

    /**
     * 初始化方法，在FXML文件加载完成后自动调用。
     */
    @FXML
    public void initialize() {
        confirmButton.setOnAction(event -> {
            LibraryBook newBook = new LibraryBook();
            newBook.setName(nameField.getText());
            newBook.setAuthor(authorField.getText());
            newBook.setIsbn(isbnField.getText());
            newBook.setPress(pressField.getText());
            newBook.setDescription(descriptionArea.getText());
            newBook.setPlace(placeField.getText());
            newBook.setCover(coverField.getText());
            newBook.setCallNumber(callNumberField.getText());

            new Thread(() -> {
                boolean result = LibraryClient.addBook(FakeRepository.handler, newBook);
                Platform.runLater(() -> {
                    if (result) {
                        infoLabel.setText("添加成功");
                        infoLabel.setStyle("-fx-text-fill: green;");
                        clearFields();
                    } else {
                        infoLabel.setText("添加失败，请检查输入信息或联系管理员");
                        infoLabel.setStyle("-fx-text-fill: red;");
                    }
                });
            }).start();
        });
    }

    private void clearFields() {
        nameField.clear();
        authorField.clear();
        isbnField.clear();
        pressField.clear();
        descriptionArea.clear();
        placeField.clear();
        coverField.clear();
        callNumberField.clear();
    }
}