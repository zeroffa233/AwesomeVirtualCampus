package app.vcampus.client.viewmodel;

import app.vcampus.client.gateway.FinanceClient;
import app.vcampus.server.utility.DisplayableTransaction;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PersonalFinanceViewModel {

    private final DoubleProperty balance = new SimpleDoubleProperty();
    // Updated to use the DisplayableTransaction wrapper
    private final ObservableList<DisplayableTransaction> transactionHistory = FXCollections.observableArrayList();

    public double getBalance() {
        return balance.get();
    }

    public DoubleProperty balanceProperty() {
        return balance;
    }

    public ObservableList<DisplayableTransaction> getTransactionHistory() {
        return transactionHistory;
    }

    /**
     * 从服务器加载数据
     */
    public void loadData() {
        // 建议在后台线程中执行网络请求
        new Thread(() -> {
            try {
                double fetchedBalance = FinanceClient.getBalance(null);
                var fetchedTransactions = FinanceClient.getTransactionHistory(null);

                // 更新UI需要在JavaFX应用线程中执行
                Platform.runLater(() -> {
                    balance.set(fetchedBalance);
                    transactionHistory.setAll(fetchedTransactions);
                });
            } catch (Exception e) {
                // 处理异常，例如显示错误消息
                e.printStackTrace();
            }
        }).start();
    }
}