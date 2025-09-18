package app.vcampus.client.scene.SubScene.CourseScene;

import app.vcampus.client.viewmodel.TeachingAffairsViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

/**
 * 添加课程控制器。
 * 负责处理管理员添加新课程的界面逻辑。
 */
public class AddCourseController {
    @FXML private TextField courseIdField;
    /**
     * 课程名称输入框。
     */
    @FXML private TextField courseNameField;
    /**
     * 开课学院输入框。
     */
    @FXML private TextField schoolField;
    /**
     * 学分输入框。
     */
    @FXML private TextField creditField;

    /**
     * 教务视图模型。
     */
    private TeachingAffairsViewModel viewModel;

    /**
     * 初始化方法，在FXML文件加载完成后自动调用。
     */
    public void initialize() {
    }

    /**
     * 设置视图模型。
     *
     * @param viewModel 教务视图模型。
     */
    public void setViewModel(TeachingAffairsViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @FXML
    private void handleAddCourse() {
        try {
            if (viewModel == null) {
                showAlert(AlertType.ERROR, "错误", "ViewModel未初始化");
                return;
            }

            String courseId = courseIdField.getText().trim();
            String courseName = courseNameField.getText().trim();
            String school = schoolField.getText().trim();
            float credit = Float.parseFloat(creditField.getText().trim());
            if (courseId.isEmpty() || courseName.isEmpty() || school.isEmpty()) {
                showAlert(AlertType.ERROR, "错误", "请填写所有必填字段");
                return;
            }

            viewModel.adminTools.addCourse(courseId, courseName, school, credit)
                    .thenAccept(success -> {
                        if (success) {
                            showAlert(AlertType.INFORMATION, "成功", "课程添加成功");
                            clearFields();
                        } else {
                            showAlert(AlertType.ERROR, "错误", "课程添加失败");
                        }
                    });
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "错误", "学分必须是数字");
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "错误", "添加课程时发生错误");
            e.printStackTrace();
        }
    }

    /**
     * 显示警告框。
     *
     * @param type 警告类型。
     * @param title 标题。
     * @param message 消息。
     */
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * 清空所有输入字段。
     */
    private void clearFields() {
        courseIdField.clear();
        courseNameField.clear();
        schoolField.clear();
        creditField.clear();
    }
}