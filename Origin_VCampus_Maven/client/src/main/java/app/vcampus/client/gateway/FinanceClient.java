package app.vcampus.client.gateway;

import app.vcampus.client.util.CardInfo;
import app.vcampus.client.util.ShopItem;
import app.vcampus.client.util.DisplayableTransaction;
import app.vcampus.client.util.ShopTransactionRecord;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//TODO make it a real net io

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
     * @return An Optional containing the CardInfo if found, otherwise empty.
     */
    public static Optional<CardInfo> findCardInfo(String cardNumber) {
        // Simulate network delay
        try {
            Thread.sleep(500);
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
     * @return true if successful, false otherwise.
     */
    public static boolean recharge(String cardNumber, double amount) {
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
     * @return true if successful, false otherwise.
     */
    public static boolean updateCardStatus(String cardNumber, String newStatus) {
        CardInfo card = cardDatabase.get(cardNumber);
        if (card != null) {
            card.setStatus(newStatus);
            return true;
        }
        return false;
    }


    // --- Methods from previous implementation (Personal Finance) ---
    public static double getBalance() {
        return 13221836.50;
    }

    public static List<DisplayableTransaction> getTransactionHistory() {
        ShopItem laptop = new ShopItem("笔记本电脑", 11000.00, "/images/laptop.png");
        ShopItem mouse = new ShopItem("鼠标", 350.00, "/images/mouse.png");
        // ... (rest of the method is unchanged)
        return Arrays.asList(
                new DisplayableTransaction(1725936059000L, "充值", 12345678.00),
                new DisplayableTransaction(new ShopTransactionRecord(Arrays.asList(laptop, mouse), 11350.00))
        );
    }
}