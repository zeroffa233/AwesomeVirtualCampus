// FinanceViewModel.java
package app.vcampus.client.viewmodel;

import app.vcampus.client.gateway.FinanceClient;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.image.Image;

public class FinanceViewModel {
    // 网关实例，用于所有网络请求
    private final FinanceClient financeClient = new FinanceClient();

    private final DoubleProperty balance = new SimpleDoubleProperty(0.00);
    private final StringProperty rechargeAmount = new SimpleStringProperty("");
    private final ObjectProperty<Image> qrCodeImage = new SimpleObjectProperty<>();
    private final StringProperty statusMessage = new SimpleStringProperty("");

    // Getters and Setters (与原代码相同)
    public DoubleProperty balanceProperty() { return balance; }
    public double getBalance() { return balance.get(); }
    public void setBalance(double balance) { this.balance.set(balance); }
    public StringProperty rechargeAmountProperty() { return rechargeAmount; }
    public String getRechargeAmount() { return rechargeAmount.get(); }
    public void setRechargeAmount(String rechargeAmount) { this.rechargeAmount.set(rechargeAmount); }
    public ObjectProperty<Image> qrCodeImageProperty() { return qrCodeImage; }
    public Image getQrCodeImage() { return qrCodeImage.get(); }
    public void setQrCodeImage(Image qrCodeImage) { this.qrCodeImage.set(qrCodeImage); }
    public StringProperty statusMessageProperty() { return statusMessage; }
    public String getStatusMessage() { return statusMessage.get(); }
    public void setStatusMessage(String statusMessage) { this.statusMessage.set(statusMessage); }

    /**
     * 初始化ViewModel，从网关获取初始数据。
     */
    public void init() {
        updateBalance();
    }

    /**
     * 从网关异步获取并更新余额。
     */
    public void updateBalance() {
        financeClient.getBalance().thenAccept(newBalance -> {
            // 确保UI更新在JavaFX应用线程上执行
            Platform.runLater(() -> setBalance(newBalance));
        }).exceptionally(e -> {
            // 异常处理
            Platform.runLater(() -> setStatusMessage("获取余额失败: " + e.getMessage()));
            return null;
        });
    }

    /**
     * 处理充值逻辑。
     * 通过网关发起异步充值请求，并根据结果更新UI。
     */
    public void performRecharge() {
        try {
            double amount = Double.parseDouble(rechargeAmount.get());
            if (amount <= 0) {
                // 前端快速验证
                setStatusMessage("充值金额必须大于0！");
                return;
            }

            // 调用网关进行充值，并处理返回的Future
            financeClient.recharge(amount).thenAccept(newBalance -> {
                // 成功回调
                Platform.runLater(() -> {
                    setBalance(newBalance);
                    setStatusMessage("充值成功！");
                    setRechargeAmount(""); // 清空输入框
                });
            }).exceptionally(e -> {
                // 失败回调
                Platform.runLater(() -> {
                    setStatusMessage("充值失败: " + e.getCause().getMessage());
                });
                return null;
            });

        } catch (NumberFormatException e) {
            setStatusMessage("请输入有效的充值金额！");
        }
    }
}