package app.vcampus.client.viewmodel;

import app.vcampus.client.gateway.FinanceClient;
import app.vcampus.server.utility.CardInfo;
import javafx.application.Platform;
import javafx.beans.property.*;

/**
 * 财务管理视图模型。
 * 负责处理财务管理界面的逻辑，如搜索一卡通、充值、冻结/解冻卡片等。
 */
public class ManageFinanceViewModel {
    /**
     * 搜索输入框的卡号属性。
     */
    public final StringProperty searchCardNumber = new SimpleStringProperty("");
    /**
     * 充值输入框的金额属性。
     */
    public final StringProperty rechargeAmount = new SimpleStringProperty("");

    private final ObjectProperty<CardInfo> foundCard = new SimpleObjectProperty<>();
    /**
     * 显示卡片信息的文本属性。
     */
    public final StringProperty cardInfoText = new SimpleStringProperty("");
    /**
     * 控制搜索结果是否可见的布尔属性。
     */
    public final BooleanProperty searchResultVisible = new SimpleBooleanProperty(false);

    /**
     * 显示状态消息的文本属性。
     */
    public final StringProperty statusMessage = new SimpleStringProperty("");
    /**
     * 状态消息文本的样式属性。
     */
    public final StringProperty statusMessageStyle = new SimpleStringProperty("");

    /**
     * 冻结/解冻按钮的文本属性。
     */
    public final StringProperty freezeButtonText = new SimpleStringProperty("冻结");

    /**
     * 构造函数。
     * 初始化监听器，当找到新的卡片时更新UI。
     */
    public ManageFinanceViewModel() {
        foundCard.addListener((obs, oldCard, newCard) -> {
            Platform.runLater(() -> {
                if (newCard != null) {
                    updateCardInfoText(newCard);
                    updateFreezeButtonState(newCard.getStatus());
                } else {
                    searchResultVisible.set(false);
                }
            });
        });
    }

    /**
     * 更新显示的卡片信息文本。
     *
     * @param card 卡片信息对象。
     */
    private void updateCardInfoText(CardInfo card) {
        cardInfoText.set(String.format("卡号: %s    卡片状态: %s    余额: %.2f 元",
                card.getCardNumber(), card.getStatus(), card.getBalance()));
    }

    /**
     * 根据卡片状态更新冻结/解冻按钮的文本。
     *
     * @param status 卡片状态字符串。
     */
    private void updateFreezeButtonState(String status) {
        if ("正常".equals(status)) {
            freezeButtonText.set("冻结");
        } else if ("冻结".equals(status)) {
            freezeButtonText.set("解冻");
        } else {
            freezeButtonText.set("冻结");
        }
    }

    /**
     * 在后台线程中执行任务。
     *
     * @param task 要执行的任务。
     */
    private void runAsyncTask(Runnable task) {
        new Thread(() -> {
            try {
                task.run();
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showStatusMessage("发生未知错误", true));
            }
        }).start();
    }

    /**
     * 显示状态消息。
     *
     * @param message 消息内容。
     * @param isError 是否为错误消息。
     */
    private void showStatusMessage(String message, boolean isError) {
        statusMessage.set(message);
        if (isError) {
            statusMessageStyle.set("-fx-text-fill: #D32F2F;");
        } else {
            statusMessageStyle.set("-fx-text-fill: #388E3C;");
        }
    }

    /**
     * 搜索一卡通。
     */
    public void search() {
        String cardNumber = searchCardNumber.get().trim();
        if (cardNumber.isEmpty()) {
            showStatusMessage("请输入一卡通号", true);
            return;
        }

        statusMessage.set("");
        runAsyncTask(() -> {
            var cardOpt = FinanceClient.findCardInfo(cardNumber);
            Platform.runLater(() -> {
                if (cardOpt.isPresent()) {
                    foundCard.set(cardOpt.get());
                    searchResultVisible.set(true);
                } else {
                    showStatusMessage("未找到卡号为 " + cardNumber + " 的一卡通", true);
                    searchResultVisible.set(false);
                    foundCard.set(null);
                }
            });
        });
    }

    /**
     * 为一卡通充值。
     */
    public void recharge() {
        try {
            double amount = Double.parseDouble(rechargeAmount.get().trim());
            if (amount <= 0) {
                showStatusMessage("充值金额必须大于0", true);
                return;
            }

            runAsyncTask(() -> {
                boolean success = FinanceClient.debit(foundCard.get().getCardNumber(), amount, "一卡通充值");
                Platform.runLater(() -> {
                    if (success) {
                        showStatusMessage("充值 " + String.format("%.2f", amount) + " 元成功！", false);
                        CardInfo currentCard = foundCard.get();
                        double newBalance = currentCard.getBalance() + amount;
                        currentCard.setBalance(newBalance);
                        updateCardInfoText(currentCard);
                        rechargeAmount.set("");
                    } else {
                        showStatusMessage("充值失败，请重试", true);
                    }
                });
            });
        } catch (NumberFormatException e) {
            showStatusMessage("请输入有效的充值金额", true);
        }
    }

    /**
     * 切换一卡通的冻结/解冻状态。
     */
    public void toggleFreezeState() {
        CardInfo currentCard = foundCard.get();
        if (currentCard == null) return;

        String currentStatus = currentCard.getStatus();
        String newStatus;
        String actionName;
        if ("正常".equals(currentStatus)) {
            newStatus = "冻结";
            actionName = "冻结";
        } else if ("冻结".equals(currentStatus)) {
            newStatus = "正常";
            actionName = "解冻";
        } else {
            return;
        }

        runAsyncTask(() -> {
            boolean success = FinanceClient.updateCardStatus(currentCard.getCardNumber(), newStatus);
            Platform.runLater(() -> {
                if (success) {
                    showStatusMessage("卡片已成功" + actionName + "！", false);
                    currentCard.setStatus(newStatus);
                    updateCardInfoText(currentCard);
                    updateFreezeButtonState(newStatus);
                } else {
                    showStatusMessage(actionName + "操作失败，请重试", true);
                }
            });
        });
    }
}