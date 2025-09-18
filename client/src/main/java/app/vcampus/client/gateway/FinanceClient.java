package app.vcampus.client.gateway;

import app.vcampus.client.net.NettyHandler;
import app.vcampus.client.repository.FakeRepository;
import app.vcampus.server.utility.CardInfo;
import app.vcampus.server.utility.DisplayableTransaction;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 财务客户端，提供与财务系统交互的功能，例如查询卡片信息、充值、消费、更新卡片状态和查询交易历史。
 */
@Slf4j
public class FinanceClient extends BaseClient {
    /**
     * Netty处理器，用于发送请求。
     */
    private static final NettyHandler handler = FakeRepository.handler;

    /**
     * 根据卡号查找卡片信息。
     *
     * @param cardNumber 要搜索的卡号。
     * @return 包含CardInfo的Optional对象，如果找到则包含信息，否则为空。
     */
    public static Optional<CardInfo> findCardInfo(String cardNumber) {
        Gson gson = new Gson();
        Request request = new Request();
        request.setUri("finance/info");
        request.setParams(Map.of("cardNumber", cardNumber));

        try {
            Response response = BaseClient.sendRequest(handler, request);
            if (response.getStatus().equals("success")) {
                String json = gson.toJson(response.getData());
                CardInfo cardInfo = gson.fromJson(json, CardInfo.class);
                return Optional.ofNullable(cardInfo);
            }
        } catch (InterruptedException e) {
            log.warn("查找卡号 {} 的卡片信息失败", cardNumber, e);
            Thread.currentThread().interrupt();
        }
        return Optional.empty();
    }

    /**
     * 为指定卡号充值。
     *
     * @param cardNumber 要充值的卡号。
     * @param amount 充值金额。
     * @param description 交易描述。
     * @return 如果成功则返回true，否则返回false。
     */
    public static boolean debit(String cardNumber, double amount, String description) {
        Request request = new Request();
        request.setUri("finance/debit");
        request.setParams(Map.of(
                "cardNumber", cardNumber,
                "amount", String.valueOf(amount),
                "description", description
        ));

        try {
            Response response = BaseClient.sendRequest(handler, request);
            return response.getStatus().equals("success");
        } catch (InterruptedException e) {
            log.warn("卡号 {} 充值失败", cardNumber, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 从指定卡号消费。
     *
     * @param cardNumber 要消费的卡号。
     * @param amount 消费金额。
     * @param description 交易描述。
     * @return 如果成功则返回true，否则返回false。
     */
    public static boolean credit(String cardNumber, double amount, String description) {
        Request request = new Request();
        request.setUri("finance/credit");
        request.setParams(Map.of(
                "cardNumber", cardNumber,
                "amount", String.valueOf(amount),
                "description", description
        ));

        try {
            Response response = BaseClient.sendRequest(handler, request);
            return response.getStatus().equals("success");
        } catch (InterruptedException e) {
            log.warn("卡号 {} 消费失败", cardNumber, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }


    /**
     * 更新卡片状态（例如，“冻结”，“挂失”）。
     *
     * @param cardNumber 要更新的卡号。
     * @param newStatus 新的状态。
     * @return 如果成功则返回true，否则返回false。
     */
    public static boolean updateCardStatus(String cardNumber, String newStatus) {
        Request request = new Request();
        request.setUri("finance/updateStatus");
        request.setParams(Map.of("cardNumber", cardNumber, "newStatus", newStatus));

        try {
            Response response = BaseClient.sendRequest(handler, request);
            return response.getStatus().equals("success");
        } catch (InterruptedException e) {
            log.warn("更新卡号 {} 状态失败", cardNumber, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 获取当前用户卡片的余额。
     * @return 余额，如果失败则返回0.0。
     */
    public static double getBalance() {
        Request request = new Request();
        request.setUri("finance/balance");

        try {
            Response response = BaseClient.sendRequest(handler, request);
            if (response.getStatus().equals("success") && response.getData() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.getData();
                Object balanceObj = data.get("balance");
                if (balanceObj instanceof Number) {
                    return ((Number) balanceObj).doubleValue();
                }
            }
        } catch (InterruptedException e) {
            log.warn("获取余额失败", e);
            Thread.currentThread().interrupt();
        }
        return 0.0;
    }

    /**
     * 获取当前用户的交易历史记录。
     * @return DisplayableTransaction列表，失败时返回空列表。
     */
    public static List<DisplayableTransaction> getTransactionHistory() {
        Gson gson = new Gson();
        Request request = new Request();
        request.setUri("finance/transactions");

        try {
            Response response = BaseClient.sendRequest(handler, request);
            if (response.getStatus().equals("success")) {
                String json = gson.toJson(response.getData());
                Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
                List<Map<String, Object>> rawTransactions = gson.fromJson(json, listType);

                // New logic: Map raw data to DisplayableTransaction objects
                // The parsing of the description is now handled inside the DisplayableTransaction constructor
                return rawTransactions.stream().map(txMap -> {
                    long time = ((Number) txMap.get("time")).longValue();
                    String type = (String) txMap.get("type");
                    double amount = ((Number) txMap.get("amount")).doubleValue();
                    String description = (String) txMap.get("description");

                    // The constructor of DisplayableTransaction will handle the logic
                    // of parsing items from the description if the type is "payment".
                    return new DisplayableTransaction(time, type, description, amount);
                }).collect(Collectors.toList());
            }
        } catch (InterruptedException e) {
            log.warn("获取交易历史失败", e);
            Thread.currentThread().interrupt();
        }
        return List.of(); // Return empty list on failure
    }
}