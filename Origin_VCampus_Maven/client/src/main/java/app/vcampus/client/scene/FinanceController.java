// FinanceController.java
package app.vcampus.client.scene;

import app.vcampus.client.viewmodel.FinanceViewModel;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class FinanceController implements Initializable {
    @FXML
    private Label balanceLabel;
    @FXML
    private ImageView qrCodeImageView;
    @FXML
    private JFXTextField rechargeAmountField;
    @FXML
    private JFXButton rechargeButton;

    // private Label statusLabel; // 状态标签可以根据需要添加回来

    private FinanceViewModel viewModel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        viewModel = new FinanceViewModel();

        // 绑定UI组件到ViewModel属性 (与原代码相同)
        balanceLabel.textProperty().bind(viewModel.balanceProperty().asString("¥ %.2f")); // 格式化得更美观
        rechargeAmountField.textProperty().bindBidirectional(viewModel.rechargeAmountProperty());
        qrCodeImageView.imageProperty().bind(viewModel.qrCodeImageProperty());
        // statusLabel.textProperty().bind(viewModel.statusMessageProperty());

        // **新增：初始化ViewModel，获取初始数据**
        viewModel.init();
    }

    @FXML
    private void handleRecharge(ActionEvent event) {
        viewModel.performRecharge();
    }
}