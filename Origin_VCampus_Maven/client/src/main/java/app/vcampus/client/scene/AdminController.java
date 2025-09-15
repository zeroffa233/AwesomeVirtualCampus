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

public class AdminController implements Initializable {

    @FXML
    private JFXButton refreshButton;
    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, Number> cardNumCol;
    @FXML
    private TableColumn<User, String> nameCol;
    @FXML
    private TableColumn<User, String> rolesCol;
    @FXML
    private TableColumn<User, String> genderCol;
    @FXML
    private TableColumn<User, String> emailCol;
    @FXML
    private TableColumn<User, String> phoneCol;
    @FXML
    private VBox formPane;
    @FXML
    private JFXTextField cardNumField;
    @FXML
    private JFXTextField nameField;
    @FXML
    private JFXTextField rolesField;
    @FXML
    private JFXTextField genderField;
    @FXML
    private JFXTextField emailField;
    @FXML
    private JFXTextField phoneField;
    @FXML
    private JFXPasswordField passwordField;
    @FXML
    private JFXButton saveButton;
    @FXML
    private JFXButton deleteButton;
    @FXML
    private JFXButton clearButton;
    @FXML
    private Label errorLabel;

    private final AdminViewModel viewModel = new AdminViewModel();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Bind table columns
        cardNumCol.setCellValueFactory(cellData -> cellData.getValue().cardNumProperty());
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        rolesCol.setCellValueFactory(cellData -> cellData.getValue().roleStrProperty());
        genderCol.setCellValueFactory(cellData -> cellData.getValue().genderProperty());
        emailCol.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        phoneCol.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());

        userTable.setItems(viewModel.users);

        // Bind form fields
        Bindings.bindBidirectional(cardNumField.textProperty(), viewModel.cardNum, new NumberStringConverter());
        viewModel.name.bindBidirectional(nameField.textProperty());
        viewModel.roles.bindBidirectional(rolesField.textProperty());
        viewModel.gender.bindBidirectional(genderField.textProperty());
        viewModel.email.bindBidirectional(emailField.textProperty());
        viewModel.phone.bindBidirectional(phoneField.textProperty());
        viewModel.password.bindBidirectional(passwordField.textProperty());

        errorLabel.textProperty().bind(viewModel.errorMessage);

        // Listen for table selection
        userTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    viewModel.selectedUser.set(newValue);
                    viewModel.setupForm(newValue);
                });

        // Initial data load
        viewModel.fetchUsers();
    }

    @FXML
    private void handleRefresh() {
        viewModel.fetchUsers();
    }

    @FXML
    private void handleSave() {
        viewModel.saveUser();
    }

    @FXML
    private void handleDelete() {
        viewModel.deleteUser();
    }

    @FXML
    private void handleClear() {
        userTable.getSelectionModel().clearSelection();
        viewModel.clearForm();
    }
}