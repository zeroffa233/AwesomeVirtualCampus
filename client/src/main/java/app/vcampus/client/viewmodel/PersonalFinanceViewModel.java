package app.vcampus.client.viewmodel;

import app.vcampus.client.gateway.FinanceClient;
import app.vcampus.server.utility.DisplayableTransaction;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * 个人财务视图模型。
 * 负责处理个人余额和交易记录的获取与展示。
 */
public class PersonalFinanceViewModel {

    private final DoubleProperty balance = new SimpleDoubleProperty();
    private final ObservableList<DisplayableTransaction> transactionHistory = FXCollections.observableArrayList();

    /**
     * 获取余额。
     *
     * @return 余额值。
     */
    public double getBalance() {
        return balance.get();
    }

    /**
     * 获取余额属性。
     *
     * @return 余额的 DoubleProperty。
     */
    public DoubleProperty balanceProperty() {
        return balance;
    }

    /**
     * 获取交易记录列表。
     *
     * @return 交易记录的 ObservableList。
     */
    public ObservableList<DisplayableTransaction> getTransactionHistory() {
        return transactionHistory;
    }

    /**
     * 从服务器异步加载个人财务数据（余额和交易记录）。
     */
    public void loadData() {
        new Thread(() -> {
            try {
                double fetchedBalance = FinanceClient.getBalance();
                var fetchedTransactions = FinanceClient.getTransactionHistory();

                // 在JavaFX应用线程上更新UI组件
                Platform.runLater(() -> {
                    balance.set(fetchedBalance);
                    transactionHistory.setAll(fetchedTransactions);
                });
            } catch (Exception e) {
                // 在此处理异常，例如记录日志或显示错误提示
                e.printStackTrace();
            }
        }).start();
    }
}