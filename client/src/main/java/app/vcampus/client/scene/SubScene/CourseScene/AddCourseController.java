package app.vcampus.client.scene.SubScene.CourseScene;

import app.vcampus.client.viewmodel.TeachingAffairsViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import lombok.Setter;

// ... existing code ...
public class AddCourseController {
    @FXML private TextField courseIdField;
    @FXML private TextField courseNameField;
    @FXML private TextField schoolField;
    @FXML private TextField creditField;

    // 添加setViewModel方法，允许MainSceneController注入共享的ViewModel
    private TeachingAffairsViewModel viewModel;

    // 不要在initialize中创建新的ViewModel，而是使用注入的
    public void initialize() {
        // 移除：viewModel = new TeachingAffairsViewModel();
    }
    public void setViewModel(TeachingAffairsViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @FXML
    private void handleAddCourse() {
        try {
            // 确保viewModel不为null
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


    
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void clearFields() {
        courseIdField.clear();
        courseNameField.clear();
        schoolField.clear();
        creditField.clear();
    }
}
