package app.vcampus.client.gateway;

import app.vcampus.client.net.NettyHandler;
import app.vcampus.server.utility.CardInfo;
import app.vcampus.server.utility.ShopItem;
import app.vcampus.server.utility.DisplayableTransaction;
import app.vcampus.server.utility.ShopTransactionRecord;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class FinanceClient extends BaseClient {

    // --- Mock Database for Card Management ---
    private static final Map<String, CardInfo> cardDatabase = new ConcurrentHashMap<>();

    static {
        // Populate with some mock data
        cardDatabase.put("123456", new CardInfo("123456", "正常", 13221836.50));
        cardDatabase.put("654321", new CardInfo("654321", "正常", 888.88));
        cardDatabase.put("100000", new CardInfo("100000", "已冻结", 123.00));
        cardDatabase.put("200000", new CardInfo("200000", "已冻结", 0.00));
    }

    /**
     * Finds card information by card number.
     *
     * @param cardNumber The card number to search for.
     * @param handler The network handler for communication.
     * @return An Optional containing the CardInfo if found, otherwise empty.
     */
    public static Optional<CardInfo> findCardInfo(String cardNumber, NettyHandler handler) {
        // TODO: Real IO implementation
        /*
        Gson gson = new Gson();
        Request request = new Request();
        request.setUri("finance/info");
        request.setParams(Map.of("cardNumber", cardNumber));

        try {
            Response response = BaseClient.sendRequest(handler, request);
            if (response.getStatus().equals("success")) {
                CardInfo cardInfo = gson.fromJson(gson.toJson(response.getData()), CardInfo.class);
                return Optional.ofNullable(cardInfo);
            }
        } catch (InterruptedException e) {
            log.warn("Failed to find card info for card: " + cardNumber, e);
        }
        return Optional.empty();
        */

        // Mock for now
        try {
            Thread.sleep(500); // Simulate network delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return Optional.ofNullable(cardDatabase.get(cardNumber));
    }

    /**
     * Recharges a card with a specified amount.
     *
     * @param cardNumber The card number to recharge.
     * @param amount The amount to add.
     * @param handler The network handler for communication.
     * @return true if successful, false otherwise.
     */
    public static boolean recharge(String cardNumber, double amount, NettyHandler handler) {
        // TODO: Real IO implementation
        /*
        Request request = new Request();
        request.setUri("finance/recharge");
        request.setParams(Map.of("cardNumber", cardNumber, "amount", String.valueOf(amount)));

        try {
            Response response = BaseClient.sendRequest(handler, request);
            return response.getStatus().equals("success");
        } catch (InterruptedException e) {
            log.warn("Failed to recharge card: " + cardNumber, e);
            return false;
        }
        */

        // Mock for now
        CardInfo card = cardDatabase.get(cardNumber);
        if (card != null && amount > 0 && !"已冻结".equals(card.getStatus())) {
            card.setBalance(card.getBalance() + amount);
            return true;
        }
        return false;
    }

    /**
     * Updates the status of a card (e.g., "冻结", "挂失").
     *
     * @param cardNumber The card number to update.
     * @param newStatus The new status.
     * @param handler The network handler for communication.
     * @return true if successful, false otherwise.
     */
    public static boolean updateCardStatus(String cardNumber, String newStatus, NettyHandler handler) {
        // TODO: Real IO implementation
        /*
        Request request = new Request();
        request.setUri("finance/updateCard");
        request.setParams(Map.of("cardNumber", cardNumber, "newStatus", newStatus));

        try {
            Response response = BaseClient.sendRequest(handler, request);
            return response.getStatus().equals("success");
        } catch (InterruptedException e) {
            log.warn("Failed to update status for card: " + cardNumber, e);
            return false;
        }
        */

        // Mock for now
        CardInfo card = cardDatabase.get(cardNumber);
        if (card != null) {
            card.setStatus(newStatus);
            return true;
        }
        return false;
    }


    // --- Methods from previous implementation (Personal Finance) ---
    public static double getBalance(NettyHandler handler) {
        // TODO: Real IO implementation
        /*
        Request request = new Request();
        request.setUri("finance/balance");
        request.setParams(Map.of());

        try {
            Response response = BaseClient.sendRequest(handler, request);
            if (response.getStatus().equals("success") && response.getData() instanceof Map) {
                Map<String, Object> data = (Map<String, Object>) response.getData();
                Object balanceObj = data.get("balance");
                if (balanceObj instanceof Number) {
                    return ((Number) balanceObj).doubleValue();
                }
            }
        } catch (InterruptedException e) {
            log.warn("Failed to get balance", e);
        }
        return 0.0;
        */
        // Mock for now
        return 13221836.50;
    }

    public static List<DisplayableTransaction> getTransactionHistory(NettyHandler handler) {
        // TODO: Real IO implementation
        /*
        Gson gson = new Gson();
        Request request = new Request();
        request.setUri("finance/transactions");
        request.setParams(Map.of());

        try {
            Response response = BaseClient.sendRequest(handler, request);
            if (response.getStatus().equals("success")) {
                String json = gson.toJson(response.getData());
                Type listType = new TypeToken<List<DisplayableTransaction>>() {}.getType();
                return gson.fromJson(json, listType);
            }
        } catch (InterruptedException e) {
            log.warn("Failed to get transaction history", e);
        }
        return List.of(); // Return empty list on failure
        */

        // Mock for now
        ShopItem laptop = new ShopItem("笔记本电脑", 11000.00, "/images/laptop.png");
        ShopItem mouse = new ShopItem("鼠标", 350.00, "/images/mouse.png");
        return Arrays.asList(
                new DisplayableTransaction(1725936059000L, "充值", 12345678.00),
                new DisplayableTransaction(new ShopTransactionRecord(Arrays.asList(laptop, mouse, laptop), 11350.00)),
                new DisplayableTransaction(1725937059000L, "充值", 13.00)
        );
    }
}