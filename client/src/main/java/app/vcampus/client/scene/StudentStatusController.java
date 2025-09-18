package app.vcampus.client.scene;

import app.vcampus.client.viewmodel.StudentStatusViewModel;
import app.vcampus.server.entity.Student;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * 学籍信息场景控制器。
 * 负责展示学生本人的学籍信息。
 */
public class StudentStatusController implements Initializable {

    @FXML private JFXTextField familyNameField;
    /**
     * 名输入框。
     */
    @FXML private JFXTextField givenNameField;
    /**
     * 性别输入框。
     */
    @FXML private JFXTextField genderField;
    /**
     * 出生日期输入框。
     */
    @FXML private JFXTextField birthDateField;
    /**
     * 出生地输入框。
     */
    @FXML private JFXTextField birthPlaceField;
    /**
     * 政治面貌输入框。
     */
    @FXML private JFXTextField politicalStatusField;
    /**
     * 学籍状态输入框。
     */
    @FXML private JFXTextField statusField;
    /**
     * 专业输入框。
     */
    @FXML private JFXTextField majorField;
    /**
     * 学院输入框。
     */
    @FXML private JFXTextField schoolField;
    /**
     * 学号输入框。
     */
    @FXML private JFXTextField studentNumberField;
    /**
     * 卡号输入框。
     */
    @FXML private JFXTextField cardNumberField;

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
        viewModel.currentStudentProperty().addListener((obs, oldS, newS) -> {
            if (newS != null) {
                loadStudentInfo(newS);
            }
        });

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

    /**
     * 处理空字符串。
     *
     * @param s 字符串。
     * @return 如果字符串为空，则返回空字符串，否则返回原字符串。
     */
    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}