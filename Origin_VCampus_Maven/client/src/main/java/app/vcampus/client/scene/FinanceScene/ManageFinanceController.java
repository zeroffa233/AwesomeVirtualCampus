package app.vcampus.client.scene.FinanceScene;

import app.vcampus.client.viewmodel.ManageFinanceViewModel;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class ManageFinanceController implements Initializable {

    @FXML
    private JFXTextField searchTextField;
    @FXML
    private JFXButton searchButton;
    @FXML
    private Label statusLabel; // Added for status messages
    @FXML
    private VBox resultPane;
    @FXML
    private Label cardInfoLabel;
    @FXML
    private JFXButton freezeButton;
    @FXML
    private JFXTextField rechargeTextField;
    @FXML
    private JFXButton rechargeButton;
    // The reportLossButton field has been removed

    private final ManageFinanceViewModel viewModel = new ManageFinanceViewModel();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Bind inputs
        searchTextField.textProperty().bindBidirectional(viewModel.searchCardNumber);
        rechargeTextField.textProperty().bindBidirectional(viewModel.rechargeAmount);

        // Bind visibility and display texts
        resultPane.visibleProperty().bind(viewModel.searchResultVisible);
        resultPane.managedProperty().bind(viewModel.searchResultVisible);
        cardInfoLabel.textProperty().bind(viewModel.cardInfoText);

        // Bind status message label
        statusLabel.textProperty().bind(viewModel.statusMessage);
        statusLabel.styleProperty().bind(viewModel.statusMessageStyle);

        // Bind freeze/unfreeze toggle button
        freezeButton.textProperty().bind(viewModel.freezeButtonText);


        // Set button actions
        searchButton.setOnAction(event -> viewModel.search());
        rechargeButton.setOnAction(event -> viewModel.recharge());
        freezeButton.setOnAction(event -> viewModel.toggleFreezeState());
    }
}