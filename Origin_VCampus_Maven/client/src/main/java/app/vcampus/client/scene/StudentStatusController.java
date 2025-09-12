package app.vcampus.client.scene;

import app.vcampus.client.viewmodel.StudentStatusViewModel;
import app.vcampus.server.entity.Student;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class StudentStatusController implements Initializable {

    // 学籍信息字段
    @FXML private JFXTextField familyNameField;
    @FXML private JFXTextField givenNameField;
    @FXML private JFXTextField genderField;
    @FXML private JFXTextField birthDateField;
    @FXML private JFXTextField birthPlaceField;
    @FXML private JFXTextField politicalStatusField;
    @FXML private JFXTextField statusField;
    @FXML private JFXTextField majorField;
    @FXML private JFXTextField schoolField;
    @FXML private JFXTextField studentNumberField;
    @FXML private JFXTextField cardNumberField;

    private final StudentStatusViewModel viewModel = new StudentStatusViewModel();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 当 ViewModel 的 currentStudent 改变时刷新界面
        viewModel.currentStudentProperty().addListener((obs, oldS, newS) -> {
            if (newS != null) {
                loadStudentInfo(newS);
            }
        });

        // 初始加载当前用户学籍
        viewModel.getStudentStatus();
    }

    private void loadStudentInfo(Student s) {
        familyNameField.setText(nullSafe(s.getFamilyName()));
        givenNameField.setText(nullSafe(s.getGivenName()));
        genderField.setText(s.getGender() == null ? "" : s.getGender().getLabel());
        birthDateField.setText(s.getBirthDate() == null ? "" :
                app.vcampus.server.utility.DateUtility.fromDate(s.getBirthDate()));
        birthPlaceField.setText(nullSafe(s.getBirthPlace()));
        politicalStatusField.setText(s.getPoliticalStatus() == null ? "" : s.getPoliticalStatus().getLabel());
        statusField.setText(s.getStatus() == null ? "" : s.getStatus().getLabel());
        majorField.setText(nullSafe(s.getMajor()));
        schoolField.setText(nullSafe(s.getSchool()));
        studentNumberField.setText(nullSafe(s.getStudentNumber()));
        cardNumberField.setText(String.valueOf(s.getCardNumber() == null ? 0 : s.getCardNumber()));
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
