package app.vcampus.client.viewmodel;

import app.vcampus.client.gateway.FinanceClient;
import app.vcampus.server.utility.CardInfo;
import javafx.application.Platform;
import javafx.beans.property.*;

public class ManageFinanceViewModel {
    // Input properties
    public final StringProperty searchCardNumber = new SimpleStringProperty("");
    public final StringProperty rechargeAmount = new SimpleStringProperty("");

    // Output/State properties
    private final ObjectProperty<CardInfo> foundCard = new SimpleObjectProperty<>();
    public final StringProperty cardInfoText = new SimpleStringProperty("");
    public final BooleanProperty searchResultVisible = new SimpleBooleanProperty(false);

    // Properties for the status message (replaces alerts)
    public final StringProperty statusMessage = new SimpleStringProperty("");
    public final StringProperty statusMessageStyle = new SimpleStringProperty("");

    // Properties for the toggle button
    public final StringProperty freezeButtonText = new SimpleStringProperty("冻结");
    public final BooleanProperty freezeButtonDisabled = new SimpleBooleanProperty(true);


    public ManageFinanceViewModel() {
        // Listener to update UI when a new card is found
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

    private void updateCardInfoText(CardInfo card) {
        cardInfoText.set(String.format("卡号: %s    卡片状态: %s    余额: %.2f 元",
                card.getCardNumber(), card.getStatus(), card.getBalance()));
    }

    private void updateFreezeButtonState(String status) {
        if ("正常".equals(status)) {
            freezeButtonText.set("冻结");
            freezeButtonDisabled.set(false);
        } else if ("已冻结".equals(status)) {
            freezeButtonText.set("解冻");
            freezeButtonDisabled.set(false);
        } else {
            // Disable button for other statuses like "已挂失"
            freezeButtonText.set("冻结");
            freezeButtonDisabled.set(true);
        }
    }

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

    private void showStatusMessage(String message, boolean isError) {
        statusMessage.set(message);
        if (isError) {
            statusMessageStyle.set("-fx-text-fill: #D32F2F;"); // Red color for errors
        } else {
            statusMessageStyle.set("-fx-text-fill: #388E3C;"); // Green color for success
        }
    }

    public void search() {
        String cardNumber = searchCardNumber.get().trim();
        if (cardNumber.isEmpty()) {
            showStatusMessage("请输入一卡通号", true);
            return;
        }

        statusMessage.set(""); // Clear previous message
        runAsyncTask(() -> {
            var cardOpt = FinanceClient.findCardInfo(cardNumber,null);
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

    public void recharge() {
        try {
            double amount = Double.parseDouble(rechargeAmount.get().trim());
            if (amount <= 0) {
                showStatusMessage("充值金额必须大于0", true);
                return;
            }

            runAsyncTask(() -> {
                boolean success = FinanceClient.recharge(foundCard.get().getCardNumber(), amount,null);
                Platform.runLater(() -> {
                    if (success) {
                        showStatusMessage("充值 " + String.format("%.2f", amount) + " 元成功！", false);
                        // Manually update the balance in the ViewModel to refresh UI
                        CardInfo currentCard = foundCard.get();
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

    public void toggleFreezeState() {
        CardInfo currentCard = foundCard.get();
        if (currentCard == null) return;

        String currentStatus = currentCard.getStatus();
        String newStatus;
        String actionName;

        if ("正常".equals(currentStatus)) {
            newStatus = "已冻结";
            actionName = "冻结";
        } else if ("已冻结".equals(currentStatus)) {
            newStatus = "正常";
            actionName = "解冻";
        } else {
            // Should not happen if button is disabled, but as a safeguard
            return;
        }

        runAsyncTask(() -> {
            boolean success = FinanceClient.updateCardStatus(currentCard.getCardNumber(), newStatus,null);
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