package app.vcampus.client.scene.SubScene.LibraryScene;

import app.vcampus.client.gateway.LibraryClient;
import app.vcampus.client.repository.FakeRepository;
import app.vcampus.server.entity.LibraryBook;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import app.vcampus.client.util.ImageCache;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 图书馆视图控制器。
 * 负责处理图书馆主界面的逻辑，包括图书搜索、结果展示和详细信息显示。
 */
public class LibraryViewController {

    @FXML
    private JFXTextField searchTextField;
    @FXML
    private JFXButton searchButton;
    @FXML
    private VBox emptyStateContainer;
    @FXML
    private ImageView emptyStateImageView;
    @FXML
    private Label emptyStateLabel;
    @FXML
    private VBox resultsContainer;
    @FXML
    private Label resultCountLabel;
    @FXML
    private HBox detailsBox;
    @FXML
    private Label bookNameLabel;
    @FXML
    private Label authorLabel;
    @FXML
    private Label pressLabel;
    @FXML
    private Label isbnLabel;
    @FXML
    private Label descriptionArea;
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
    private TableColumn<LibraryBook, String> bookNameColumn;
    @FXML
    private TableColumn<LibraryBook, String> pressColumn;

    /**
     * 初始化方法，在FXML文件加载完成后自动调用。
     */
    @FXML
    public void initialize() {
        bookNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        pressColumn.setCellValueFactory(new PropertyValueFactory<>("press"));
        callNumberColumn.setCellValueFactory(new PropertyValueFactory<>("callNumber"));
        placeColumn.setCellValueFactory(new PropertyValueFactory<>("place"));
        bookStatusColumn.setCellValueFactory(new PropertyValueFactory<>("bookStatus"));

        searchButton.setOnAction(event -> searchBooks());

        loadAllBooks();
    }

    private void loadAllBooks() {
        new Thread(() -> {
            List<LibraryBook> allBooks = LibraryClient.getAllBooks(FakeRepository.handler);
            Platform.runLater(() -> processAndDisplayResults(allBooks, true));
        }).start();
    }

    private void showEmptyState() {
        emptyStateImageView.setVisible(true);
        emptyStateImageView.setManaged(true);
        emptyStateLabel.setText("输入关键词，开始探索图书馆的宝藏");
        resultsContainer.setVisible(false);
        resultsContainer.setManaged(false);
        emptyStateContainer.setVisible(true);
        emptyStateContainer.setManaged(true);
    }

    private void showNotFoundState() {
        emptyStateImageView.setVisible(false);
        emptyStateImageView.setManaged(false);
        emptyStateLabel.setText("没有找到您所搜寻的内容，请重试");
        resultsContainer.setVisible(false);
        resultsContainer.setManaged(false);
        emptyStateContainer.setVisible(true);
        emptyStateContainer.setManaged(true);
    }

    private void showResults() {
        emptyStateContainer.setVisible(false);
        emptyStateContainer.setManaged(false);
        resultsContainer.setVisible(true);
        resultsContainer.setManaged(true);
    }

    private void processAndDisplayResults(List<LibraryBook> allCopies, boolean isAllBooks) {
        if (allCopies == null || allCopies.isEmpty()) {
            if (isAllBooks) {
                showEmptyState();
            } else {
                showNotFoundState();
            }
            return;
        }

        showResults();

        if (isAllBooks) {
            detailsBox.setVisible(false);
            detailsBox.setManaged(false);
        } else {
            detailsBox.setVisible(true);
            detailsBox.setManaged(true);

            resultCountLabel.setText("共检索到 " + allCopies.size() + " 个结果");

            LibraryBook firstBook = allCopies.get(0);
            bookNameLabel.setText(firstBook.getName());
            authorLabel.setText(firstBook.getAuthor());
            pressLabel.setText(firstBook.getPress());
            isbnLabel.setText(firstBook.getIsbn());
            descriptionArea.setText(firstBook.getDescription());

            if (firstBook.getCover() != null && !firstBook.getCover().isEmpty()) {
                try {
                    Image image = ImageCache.getInstance().getImage(firstBook.getCover());
                    coverImageView.setImage(image);
                } catch (Exception e) {
                    System.err.println("Cannot load image from cache: " + firstBook.getCover());
                    coverImageView.setImage(null);
                }
            } else {
                coverImageView.setImage(null);
            }

            long availableCount = allCopies.stream()
                    .filter(book -> book.getBookStatus() == app.vcampus.server.enums.BookStatus.available)
                    .count();
            totalCopiesLabel.setText("馆藏副本: " + allCopies.size());
            availableCopiesLabel.setText("可借副本: " + availableCount);
        }

        copiesTableView.getItems().setAll(allCopies);
    }

    private void searchBooks() {
        String keyword = searchTextField.getText();

        if (keyword == null || keyword.trim().isEmpty()) {
            loadAllBooks();
        } else {
            new Thread(() -> {
                Map<String, List<LibraryBook>> booksMap = LibraryClient.searchBook(FakeRepository.handler, keyword);
                List<LibraryBook> allCopies = (booksMap == null) ? Collections.emptyList() :
                        booksMap.values().stream()
                                .flatMap(List::stream)
                                .collect(Collectors.toList());
                Platform.runLater(() -> processAndDisplayResults(allCopies, false));
            }).start();
        }
    }
}