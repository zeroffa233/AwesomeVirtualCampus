package app.vcampus.client.scene;

import app.vcampus.client.scene.components.SearchStudentCell;
import app.vcampus.client.viewmodel.StudentStatusViewModel;
import app.vcampus.server.entity.Student;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class StudentStatusController implements Initializable {

    @FXML private StackPane contentPane;
    @FXML private AnchorPane studentInfoPane;
    @FXML private AnchorPane modifyPane;

    // student info fields (JFoenix)
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

    // modify pane controls
    @FXML private JFXTextField searchField;
    @FXML private JFXButton searchBtn;
    @FXML private ListView<Student> searchResultsList;

    // 可选：增加切换按钮，如果想通过按钮切换两个 Pane
    @FXML private JFXButton showInfoBtn;
    @FXML private JFXButton showModifyBtn;

    private final StudentStatusViewModel viewModel = new StudentStatusViewModel();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // 默认显示 studentInfoPane
        showStudentInfo();

        // 切换按钮事件
        if (showInfoBtn != null) {
            showInfoBtn.setOnAction(e -> showStudentInfo());
        }
        if (showModifyBtn != null) {
            showModifyBtn.setOnAction(e -> showModifyPane());
        }

        // 绑定 viewModel -> UI（当 currentStudent 改变时刷新界面）
        viewModel.currentStudentProperty().addListener((obs, oldS, newS) -> {
            if (newS == null) return;
            familyNameField.setText(nullSafe(newS.getFamilyName()));
            givenNameField.setText(nullSafe(newS.getGivenName()));
            genderField.setText(newS.getGender() == null ? "" : newS.getGender().getLabel());
            birthDateField.setText(newS.getBirthDate() == null ? "" : app.vcampus.server.utility.DateUtility.fromDate(newS.getBirthDate()));
            birthPlaceField.setText(nullSafe(newS.getBirthPlace()));
            politicalStatusField.setText(newS.getPoliticalStatus() == null ? "" : newS.getPoliticalStatus().getLabel());
            statusField.setText(newS.getStatus() == null ? "" : newS.getStatus().getLabel());
            majorField.setText(nullSafe(newS.getMajor()));
            schoolField.setText(nullSafe(newS.getSchool()));
            studentNumberField.setText(nullSafe(newS.getStudentNumber()));
            cardNumberField.setText(String.valueOf(newS.getCardNumber() == null ? 0 : newS.getCardNumber()));
        });

        // 搜索按钮
        searchBtn.setOnAction(e -> viewModel.searchStudent(searchField.getText()));

        // 搜索结果 ListView：直接绑定 viewModel 的 ObservableList，并用自定义 Cell
        searchResultsList.setItems(viewModel.getSearchedStudents());
        searchResultsList.setCellFactory(lv -> new SearchStudentCell(viewModel, true));

        // 初始加载当前用户学籍
        viewModel.getStudentStatus();
    }

    private void showStudentInfo() {
        studentInfoPane.setVisible(true);
        modifyPane.setVisible(false);
    }

    private void showModifyPane() {
        studentInfoPane.setVisible(false);
        modifyPane.setVisible(true);
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}