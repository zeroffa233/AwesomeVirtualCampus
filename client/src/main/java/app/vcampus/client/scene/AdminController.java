package app.vcampus.client.scene;

import app.vcampus.server.utility.User;
import app.vcampus.client.viewmodel.AdminViewModel;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.util.converter.NumberStringConverter;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * 管理员场景控制器。
 * 负责处理用户管理的界面逻辑，包括显示用户列表、编辑用户信息等。
 */
public class AdminController implements Initializable {

    /**
     * 刷新按钮。
     */
    @FXML
    private JFXButton refreshButton;
    /**
     * 用户信息表格。
     */
    @FXML
    private TableView<User> userTable;
    /**
     * 卡号列。
     */
    @FXML
    private TableColumn<User, Number> cardNumCol;
    /**
     * 姓名列。
     */
    @FXML
    private TableColumn<User, String> nameCol;
    /**
     * 角色列。
     */
    @FXML
    private TableColumn<User, String> rolesCol;
    /**
     * 性别列。
     */
    @FXML
    private TableColumn<User, String> genderCol;
    /**
     * 邮箱列。
     */
    @FXML
    private TableColumn<User, String> emailCol;
    /**
     * 电话列。
     */
    @FXML
    private TableColumn<User, String> phoneCol;
    /**
     * 表单面板。
     */
    @FXML
    private VBox formPane;
    /**
     * 卡号输入框。
     */
    @FXML
    private JFXTextField cardNumField;
    /**
     * 姓名输入框。
     */
    @FXML
    private JFXTextField nameField;
    /**
     * 角色输入框。
     */
    @FXML
    private JFXTextField rolesField;
    /**
     * 性别输入框。
     */
    @FXML
    private JFXTextField genderField;
    /**
     * 邮箱输入框。
     */
    @FXML
    private JFXTextField emailField;
    /**
     * 电话输入框。
     */
    @FXML
    private JFXTextField phoneField;
    /**
     * 密码输入框。
     */
    @FXML
    private JFXPasswordField passwordField;
    /**
     * 保存按钮。
     */
    @FXML
    private JFXButton saveButton;
    /**
     * 删除按钮。
     */
    @FXML
    private JFXButton deleteButton;
    /**
     * 清除按钮。
     */
    @FXML
    private JFXButton clearButton;
    /**
     * 错误信息标签。
     */
    @FXML
    private Label errorLabel;

    /**
     * 管理员视图模型。
     */
    private final AdminViewModel viewModel = new AdminViewModel();

    /**
     * 初始化方法，在FXML文件加载完成后自动调用。
     *
     * @param location  URL定位资源。
     * @param resources 资源包。
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cardNumCol.setCellValueFactory(cellData -> cellData.getValue().cardNumProperty());
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        rolesCol.setCellValueFactory(cellData -> cellData.getValue().roleStrProperty());
        genderCol.setCellValueFactory(cellData -> cellData.getValue().genderProperty());
        emailCol.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        phoneCol.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());

        userTable.setItems(viewModel.users);

        Bindings.bindBidirectional(cardNumField.textProperty(), viewModel.cardNum, new NumberStringConverter());
        viewModel.name.bindBidirectional(nameField.textProperty());
        viewModel.roles.bindBidirectional(rolesField.textProperty());
        viewModel.gender.bindBidirectional(genderField.textProperty());
        viewModel.email.bindBidirectional(emailField.textProperty());
        viewModel.phone.bindBidirectional(phoneField.textProperty());
        viewModel.password.bindBidirectional(passwordField.textProperty());

        errorLabel.textProperty().bind(viewModel.errorMessage);

        userTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    viewModel.selectedUser.set(newValue);
                    viewModel.setupForm(newValue);
                });

        viewModel.fetchUsers();
    }

    /**
     * 处理刷新操作。
     * 调用ViewModel刷新用户列表。
     */
    @FXML
    private void handleRefresh() {
        viewModel.fetchUsers();
    }

    /**
     * 处理保存操作。
     * 调用ViewModel保存用户信息。
     */
    @FXML
    private void handleSave() {
        viewModel.saveUser();
    }

    /**
     * 处理删除操作。
     * 调用ViewModel删除用户。
     */
    @FXML
    private void handleDelete() {
        viewModel.deleteUser();
    }

    /**
     * 处理清除操作。
     * 清除用户选择并重置表单。
     */
    @FXML
    private void handleClear() {
        userTable.getSelectionModel().clearSelection();
        viewModel.clearForm();
    }
}