package app.vcampus.client.scene.SubScene.FinanceScene;

import app.vcampus.client.viewmodel.ManageFinanceViewModel;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * 财务管理场景控制器。
 * 负责将财务管理界面的UI组件与 `ManageFinanceViewModel` 进行数据绑定和事件处理。
 */
public class ManageFinanceController implements Initializable {

    @FXML
    private JFXTextField searchTextField;
    @FXML
    private JFXButton searchButton;
    @FXML
    private Label statusLabel;
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

    private final ManageFinanceViewModel viewModel = new ManageFinanceViewModel();

    /**
     * 初始化方法，在FXML文件加载完成后自动调用。
     *
     * @param location  URL定位资源。
     * @param resources 资源包。
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchTextField.textProperty().bindBidirectional(viewModel.searchCardNumber);
        rechargeTextField.textProperty().bindBidirectional(viewModel.rechargeAmount);

        resultPane.visibleProperty().bind(viewModel.searchResultVisible);
        resultPane.managedProperty().bind(viewModel.searchResultVisible);
        cardInfoLabel.textProperty().bind(viewModel.cardInfoText);

        statusLabel.textProperty().bind(viewModel.statusMessage);
        statusLabel.styleProperty().bind(viewModel.statusMessageStyle);

        freezeButton.textProperty().bind(viewModel.freezeButtonText);

        searchButton.setOnAction(event -> viewModel.search());
        rechargeButton.setOnAction(event -> viewModel.recharge());
        freezeButton.setOnAction(event -> viewModel.toggleFreezeState());
    }
}