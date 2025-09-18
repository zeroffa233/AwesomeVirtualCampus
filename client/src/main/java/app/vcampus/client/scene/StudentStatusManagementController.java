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

/**
 * 学籍管理场景控制器。
 * 负责处理教职工管理学生学籍信息的界面逻辑，主要是搜索和展示学生信息。
 */
public class StudentStatusManagementController implements Initializable {

        @FXML private JFXTextField searchField;
    /**
     * 搜索按钮。
     */
    @FXML private JFXButton searchBtn;
    /**
     * 搜索结果容器。
     */
    @FXML private VBox searchResultsContainer;

    /**
     * 学生学籍视图模型。
     */
    private final StudentStatusViewModel viewModel = new StudentStatusViewModel();

    /**
     * 初始化方法，在FXML文件加载完成后自动调用。
     *
     * @param location  URL定位资源。
     * @param resources 资源包。
     */
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

    /**
     * 打开编辑对话框。
     *
     * @param student 要编辑的学生对象。
     */
    private void openEditDialog(Student student) {
    }
}