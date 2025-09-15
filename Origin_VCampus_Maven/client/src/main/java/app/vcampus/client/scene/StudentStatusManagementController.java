package app.vcampus.client.scene;

import app.vcampus.client.scene.components.SearchStudentCell;
import app.vcampus.client.viewmodel.StudentStatusViewModel;
import app.vcampus.server.entity.Student;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class StudentStatusManagementController implements Initializable {

    @FXML private JFXTextField searchField;
    @FXML private JFXButton searchBtn;
    @FXML private ListView<Student> searchResultsList;

    private final StudentStatusViewModel viewModel = new StudentStatusViewModel();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 禁用空输入时的搜索按钮
        searchBtn.disableProperty().bind(searchField.textProperty().isEmpty());

        // 按回车触发搜索
        searchField.setOnAction(e -> {
            if (!searchField.getText().isBlank()) {
                searchBtn.fire();
            }
        });

        // 搜索动作
        searchBtn.setOnAction(e -> {
            String kw = searchField.getText();
            viewModel.searchStudent(kw == null ? "" : kw.trim());
        });

        // ListView 占位提示
        searchResultsList.setPlaceholder(new Label("尚未搜索或无匹配结果"));

        // 关键配置：启用可变高度
        searchResultsList.setFixedCellSize(-1); // 禁用固定单元格大小
        searchResultsList.setFocusTraversable(false); // 避免焦点边框问题

        // 绑定数据源并自定义 Cell
        searchResultsList.setItems(viewModel.getSearchedStudents());
        searchResultsList.setCellFactory(lv -> new SearchStudentCell(viewModel, true));

        // 双击条目进入编辑对话框
        searchResultsList.setOnMouseClicked(evt -> {
            if (evt.getClickCount() == 2) {
                Student sel = searchResultsList.getSelectionModel().getSelectedItem();
                if (sel != null) openEditDialog(sel);
            }
        });

        // 可选：在初始化时预加载空关键字
        // viewModel.searchStudent("");
    }

    /**
     * 使用 javafx Dialog 实现一个简单的编辑窗，编辑后调用 viewModel.updateStudent(...)
     */
    private void openEditDialog(Student student) {
        // 这里保持原有的对话框实现逻辑不变
        // ... existing code ...
    }
}