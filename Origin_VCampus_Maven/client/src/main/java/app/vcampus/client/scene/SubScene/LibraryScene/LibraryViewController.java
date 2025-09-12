package app.vcampus.client.scene.SubScene.LibraryScene;

import app.vcampus.client.gateway.LibraryClient;
import app.vcampus.client.repository.FakeRepository;
import app.vcampus.server.entity.LibraryBook;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LibraryViewController {

    @FXML
    private JFXTextField searchTextField;
    @FXML
    private JFXButton searchButton;
    @FXML
    private Label resultCountLabel;
    @FXML
    private HBox detailsBox;
    @FXML
    private Label bookNameLabel;
    @FXML
    private Label pressLabel;
    @FXML
    private Label isbnLabel;
    @FXML
    private JFXTextArea descriptionArea;
    @FXML
    private Label totalCopiesLabel;
    @FXML
    private Label availableCopiesLabel;
    @FXML
    private ImageView coverImageView;
    @FXML
    private TableView<LibraryBook> copiesTableView;
    @FXML
    private TableColumn<LibraryBook, String> callNumberColumn;
    @FXML
    private TableColumn<LibraryBook, String> placeColumn;
    @FXML
    private TableColumn<LibraryBook, String> bookStatusColumn;

    @FXML
    public void initialize() {
        // Initialize TableView columns
        callNumberColumn.setCellValueFactory(new PropertyValueFactory<>("callNumber"));
        placeColumn.setCellValueFactory(new PropertyValueFactory<>("place"));
        bookStatusColumn.setCellValueFactory(new PropertyValueFactory<>("bookStatus"));

        searchButton.setOnAction(event -> searchBooks());

        // Initially hide the details and table view
        clearAndHideDetails();
    }

    private void clearAndHideDetails() {
        resultCountLabel.setText("");
        detailsBox.setVisible(false);
        detailsBox.setManaged(false);
        copiesTableView.setVisible(false);
        copiesTableView.setManaged(false);

        // Clear content
        bookNameLabel.setText("书名:");
        pressLabel.setText("出版社:");
        isbnLabel.setText("ISBN:");
        descriptionArea.clear();
        totalCopiesLabel.setText("馆藏副本:");
        availableCopiesLabel.setText("可借副本:");
        coverImageView.setImage(null);
        copiesTableView.getItems().clear();
    }

    private void searchBooks() {
        String keyword = searchTextField.getText();
        if (keyword == null || keyword.trim().isEmpty()) {
            clearAndHideDetails();
            resultCountLabel.setText("请输入搜索关键词。");
            return;
        }

        new Thread(() -> {
            Map<String, List<LibraryBook>> booksMap = LibraryClient.searchBook(FakeRepository.handler, keyword);
            Platform.runLater(() -> {
                clearAndHideDetails();
                if (booksMap == null || booksMap.isEmpty()) {
                    resultCountLabel.setText("共检索到 0 个结果");
                    return;
                }

                List<LibraryBook> allCopies = booksMap.values().stream()
                        .flatMap(List::stream)
                        .collect(Collectors.toList());

                if (allCopies.isEmpty()) {
                    resultCountLabel.setText("共检索到 0 个结果");
                    return;
                }

                // Show the details and table view
                detailsBox.setVisible(true);
                detailsBox.setManaged(true);
                copiesTableView.setVisible(true);
                copiesTableView.setManaged(true);

                resultCountLabel.setText("共检索到 " + allCopies.size() + " 个结果");

                LibraryBook firstBook = allCopies.get(0);
                bookNameLabel.setText("书名: " + firstBook.getName());
                pressLabel.setText("出版社: " + firstBook.getPress());
                isbnLabel.setText("ISBN: " + firstBook.getIsbn());
                descriptionArea.setText(firstBook.getDescription());

                if (firstBook.getCover() != null && !firstBook.getCover().isEmpty()) {
                    try {
                        coverImageView.setImage(new Image(firstBook.getCover(), true));
                    } catch (Exception e) {
                        coverImageView.setImage(null);
                    }
                }

                long availableCount = allCopies.stream()
                        .filter(book -> book.getBookStatus() == app.vcampus.server.enums.BookStatus.available)
                        .count();
                totalCopiesLabel.setText("馆藏副本: " + allCopies.size());
                availableCopiesLabel.setText("可借副本: " + availableCount);

                copiesTableView.getItems().setAll(allCopies);
            });
        }).start();
    }
}
