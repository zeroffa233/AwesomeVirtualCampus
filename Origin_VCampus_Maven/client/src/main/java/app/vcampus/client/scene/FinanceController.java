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

    //@FXML
    //private Label statusLabel; // 用于显示状态消息

    private FinanceViewModel viewModel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        viewModel = new FinanceViewModel();

        // 将视图组件绑定到视图模型属性
        balanceLabel.textProperty().bind(viewModel.balanceProperty().asString("%.2f"));
        rechargeAmountField.textProperty().bindBidirectional(viewModel.rechargeAmountProperty());
        qrCodeImageView.imageProperty().bind(viewModel.qrCodeImageProperty());
        // statusLabel.textProperty().bind(viewModel.statusMessageProperty());

        // 初始更新（如果需要）
        // viewModel.updateBalance(); // 可以在这里调用方法从后端获取最新余额
    }

    @FXML
    private void handleRecharge(ActionEvent event) {
        viewModel.performRecharge();
    }
}