package app.vcampus.client.scene;

import app.vcampus.client.scene.components.*;
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

        // 按回车触发搜索（更友好）
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

        // 绑定数据源并自定义 Cell（你已有 SearchStudentCell）
        searchResultsList.setItems(viewModel.getSearchedStudents());
        searchResultsList.setCellFactory(lv -> new SearchStudentCell(viewModel, true));


        // 双击条目进入编辑对话框
        searchResultsList.setOnMouseClicked(evt -> {
            if (evt.getClickCount() == 2) {
                Student sel = searchResultsList.getSelectionModel().getSelectedItem();
                if (sel != null) openEditDialog(sel);
            }
        });

        // 可选：在初始化时预加载空关键字（或不加载）
        // viewModel.searchStudent("");
    }

    /**
     * 使用 javafx Dialog 实现一个简单的编辑窗，编辑后调用 viewModel.updateStudent(...)
     */
    private void openEditDialog(Student s) {
        Dialog<Student> dlg = new Dialog<>();
        dlg.setTitle("编辑学籍 - " + (s.getStudentNumber() == null ? "" : s.getStudentNumber()));
        dlg.setHeaderText("编辑学生信息后点击保存");

        ButtonType saveBtnType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        // 表单
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        JFXTextField familyField = new JFXTextField();
        familyField.setPromptText("姓");
        familyField.setText(s.getFamilyName());

        JFXTextField givenField = new JFXTextField();
        givenField.setPromptText("名");
        givenField.setText(s.getGivenName());

        JFXTextField majorField = new JFXTextField();
        majorField.setPromptText("专业");
        majorField.setText(s.getMajor());

        JFXTextField schoolField = new JFXTextField();
        schoolField.setPromptText("学院");
        schoolField.setText(s.getSchool());

        JFXTextField studentNumberField = new JFXTextField();
        studentNumberField.setPromptText("学号");
        studentNumberField.setText(s.getStudentNumber());

        grid.add(new Label("姓"), 0, 0);
        grid.add(familyField, 1, 0);
        grid.add(new Label("名"), 0, 1);
        grid.add(givenField, 1, 1);
        grid.add(new Label("专业"), 0, 2);
        grid.add(majorField, 1, 2);
        grid.add(new Label("学院"), 0, 3);
        grid.add(schoolField, 1, 3);
        grid.add(new Label("学号"), 0, 4);
        grid.add(studentNumberField, 1, 4);

        dlg.getDialogPane().setContent(grid);


        // 将结果封装为 Student 返回
        dlg.setResultConverter(button -> {
            if (button == saveBtnType) {
                // 将原 Student 更新（浅修改），或新建副本并返回
                Student updated = new Student();
                // 保留 uuid/cardNumber 等关键字段（若 Student 有这些 setter）
                updated.setCardNumber(s.getCardNumber());
                updated.setStudentNumber(studentNumberField.getText());
                updated.setFamilyName(familyField.getText());
                updated.setGivenName(givenField.getText());
                updated.setMajor(majorField.getText());
                updated.setSchool(schoolField.getText());
                // 你可以设置更多字段
                return updated;
            }
            return null;
        });

        Optional<Student> res = dlg.showAndWait();
        res.ifPresent(updatedStudent -> {
            // 调用 viewModel.updateStudent 并展示结果
            viewModel.updateStudent(updatedStudent,
                    () -> Platform.runLater(() -> {
                        // 成功：刷新搜索列表（重新搜索当前关键字）
                        viewModel.searchStudent(searchField.getText());
                        Alert a = new Alert(Alert.AlertType.INFORMATION, "保存成功", ButtonType.OK);
                        a.showAndWait();
                    }),
                    () -> Platform.runLater(() -> {
                        Alert a = new Alert(Alert.AlertType.ERROR, "保存失败", ButtonType.OK);
                        a.showAndWait();
                    })
            );
        });
    }
}
