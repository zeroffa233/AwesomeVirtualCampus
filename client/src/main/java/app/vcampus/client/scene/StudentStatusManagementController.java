// File: StudentStatusManagementController.java (small adjustments)
package app.vcampus.client.scene;

import app.vcampus.client.scene.components.SearchStudentCell;
import app.vcampus.client.viewmodel.StudentStatusViewModel;
import app.vcampus.server.entity.Student;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class StudentStatusManagementController implements Initializable {

    @FXML private JFXTextField searchField;
    @FXML private JFXButton searchBtn;
    @FXML private VBox searchResultsContainer;

    private final StudentStatusViewModel viewModel = new StudentStatusViewModel();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchBtn.disableProperty().bind(searchField.textProperty().isEmpty());

        searchField.setOnAction(e -> { if (!searchField.getText().isBlank()) searchBtn.fire(); });

        searchBtn.setOnAction(e -> {
            String kw = searchField.getText();
            viewModel.searchStudent(kw == null ? "" : kw.trim());
        });

        searchResultsContainer.getChildren().clear();
        Label placeholder = new Label("尚未搜索或无匹配结果");
        placeholder.setStyle("-fx-font-size: 14px; -fx-text-fill: #999; -fx-padding: 20px;");
        searchResultsContainer.getChildren().add(placeholder);

        viewModel.getSearchedStudents().addListener((javafx.collections.ListChangeListener<Student>) change -> updateSearchResults());
    }

    private void updateSearchResults() {
        Platform.runLater(() -> {
            searchResultsContainer.getChildren().clear();

            if (viewModel.getSearchedStudents().isEmpty()) {
                Label placeholder = new Label("尚未搜索或无匹配结果");
                placeholder.setStyle("-fx-font-size: 14px; -fx-text-fill: #999; -fx-padding: 20px;");
                searchResultsContainer.getChildren().add(placeholder);
            } else {
                for (Student student : viewModel.getSearchedStudents()) {
                    SearchStudentCell cell = new SearchStudentCell(viewModel, true);
                    cell.updateItem(student, false);

                    // 让卡片宽度自适应容器宽度
                    cell.getRoot().prefWidthProperty().bind(searchResultsContainer.widthProperty().subtract(8));
                    VBox.setVgrow(cell.getRoot(), Priority.NEVER);

                    cell.getRoot().setOnMouseClicked(evt -> {
                        if (evt.getClickCount() == 2) {
                            openEditDialog(student);
                        }
                    });

                    searchResultsContainer.getChildren().add(cell.getRoot());
                }
            }
        });
    }

    private void openEditDialog(Student student) {
        // 保持原实现
    }
}
