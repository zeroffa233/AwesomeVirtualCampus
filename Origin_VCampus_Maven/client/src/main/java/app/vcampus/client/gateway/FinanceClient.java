// FinanceClient.java
package app.vcampus.client.gateway;

import java.util.concurrent.CompletableFuture;

/**
 * FinanceClient负责处理所有与财务相关的网络请求。
 * 当前使用模拟数据和延迟来模拟真实的网络通信。
 */
public class FinanceClient {

    // 模拟的服务器端余额
    private static double mockRemoteBalance = 1000.00;

    /**
     * 模拟异步获取当前用户余额。
     *
     * @return 一个CompletableFuture，当操作完成时，它将包含最新的余额。
     */
    public CompletableFuture<Double> getBalance() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 模拟100ms的网络延迟
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                // 在实际应用中，这里应该有更健壮的异常处理
                throw new RuntimeException("获取余额时中断", e);
            }
            return mockRemoteBalance;
        });
    }

    /**
     * 模拟异步处理充值请求。
     *
     * @param amount 充值的金额
     * @return 一个CompletableFuture，当操作完成时，它将包含充值后的新余额。
     *         如果充值失败，Future将以异常结束。
     */
    public CompletableFuture<Double> recharge(double amount) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 模拟800ms的网络延迟和处理时间
                Thread.sleep(800);

                if (amount <= 0) {
                    // 模拟服务器端验证失败
                    throw new IllegalArgumentException("充值金额必须为正数");
                }

                // 模拟充值成功
                mockRemoteBalance += amount;
                return mockRemoteBalance;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("充值操作中断", e);
            }
        });
    }
}