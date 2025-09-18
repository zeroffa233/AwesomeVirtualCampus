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

@Slf4j
public class FinanceClient extends BaseClient {
    private static final NettyHandler handler = FakeRepository.handler;

    /**
     * Finds card information by card number.
     *
     * @param cardNumber The card number to search for.
     * @return An Optional containing the CardInfo if found, otherwise empty.
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
            log.warn("Failed to find card info for card: " + cardNumber, e);
            Thread.currentThread().interrupt();
        }
        return Optional.empty();
    }

    /**
     * Recharges a card with a specified amount and description.
     *
     * @param cardNumber The card number to recharge.
     * @param amount The amount to add.
     * @param description The description of the transaction.
     * @return true if successful, false otherwise.
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
            log.warn("Failed to recharge card: " + cardNumber, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Spends from a card with a specified amount and description.
     *
     * @param cardNumber The card number to spend from.
     * @param amount The amount to subtract.
     * @param description The description of the transaction.
     * @return true if successful, false otherwise.
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
            log.warn("Failed to credit card: " + cardNumber, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }


    /**
     * Updates the status of a card (e.g., "冻结", "挂失").
     *
     * @param cardNumber The card number to update.
     * @param newStatus The new status.
     * @return true if successful, false otherwise.
     */
    public static boolean updateCardStatus(String cardNumber, String newStatus) {
        Request request = new Request();
        request.setUri("finance/updateStatus");
        request.setParams(Map.of("cardNumber", cardNumber, "newStatus", newStatus));

        try {
            Response response = BaseClient.sendRequest(handler, request);
            return response.getStatus().equals("success");
        } catch (InterruptedException e) {
            log.warn("Failed to update status for card: " + cardNumber, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Gets the balance of the current user's card.
     * @return the balance, or 0.0 if failed.
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
            log.warn("Failed to get balance", e);
            Thread.currentThread().interrupt();
        }
        return 0.0;
    }

    /**
     * Gets the transaction history of the current user.
     * @return A list of DisplayableTransaction, or empty list on failure.
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
            log.warn("Failed to get transaction history", e);
            Thread.currentThread().interrupt();
        }
        return List.of(); // Return empty list on failure
    }
}