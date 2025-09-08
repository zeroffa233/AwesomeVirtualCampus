package app.vcampus.client.viewmodel;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.scene.image.Image;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class FinanceViewModel {
    private final DoubleProperty balance = new SimpleDoubleProperty(1000.00); // 示例余额
    private final StringProperty rechargeAmount = new SimpleStringProperty("");
    private final ObjectProperty<Image> qrCodeImage = new SimpleObjectProperty<>();
    private final StringProperty statusMessage = new SimpleStringProperty(""); // 用于显示操作结果或错误信息

    public DoubleProperty balanceProperty() {
        return balance;
    }

    public double getBalance() {
        return balance.get();
    }

    public void setBalance(double balance) {
        this.balance.set(balance);
    }

    public StringProperty rechargeAmountProperty() {
        return rechargeAmount;
    }

    public String getRechargeAmount() {
        return rechargeAmount.get();
    }

    public void setRechargeAmount(String rechargeAmount) {
        this.rechargeAmount.set(rechargeAmount);
    }

    public ObjectProperty<Image> qrCodeImageProperty() {
        return qrCodeImage;
    }

    public Image getQrCodeImage() {
        return qrCodeImage.get();
    }

    public void setQrCodeImage(Image qrCodeImage) {
        this.qrCodeImage.set(qrCodeImage);
    }

    public StringProperty statusMessageProperty() {
        return statusMessage;
    }

    public String getStatusMessage() {
        return statusMessage.get();
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage.set(statusMessage);
    }

    /**
     * 处理充值逻辑。
     * 实际应用中，这里会调用服务层进行充值操作，并根据结果更新 balance 和 statusMessage。
     */
    public void performRecharge() {
        try {
            double amount = Double.parseDouble(rechargeAmount.get());
            if (amount <= 0) {
                setStatusMessage("充值金额必须大于0！");
                return;
            }
            // 模拟异步操作
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // 模拟网络延迟
                    // TODO: 真正的充值请求并等待结果
                    // 假设充值成功
                    Platform.runLater(() -> {
                        setBalance(getBalance() + amount);
                        setStatusMessage("充值成功！");
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    setStatusMessage("充值中断！");
                } catch (NumberFormatException e) {
                    setStatusMessage("请输入有效的充值金额！");
                } catch (Exception e) {
                    setStatusMessage("充值失败: " + e.getMessage());
                } finally {
                    setRechargeAmount(""); // 清空输入框
                }
            }).start();

        } catch (NumberFormatException e) {
            setStatusMessage("请输入有效的充值金额！");
        }
    }

}