package app.vcampus.client.util;

import app.vcampus.client.util.ShopItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * A wrapper class to represent any transaction record in the UI.
 * It can be created from a ShopTransactionRecord or for other types like recharges.
 */
public class DisplayableTransaction {
    private final String date;
    private final String type;
    private final double amount;
    private final List<ShopItem> items; // Only for shop transactions

    // Static formatter for date conversion
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");

    /**
     * Constructor for non-shop transactions like "Recharge".
     *
     * @param timestamp The timestamp of the transaction.
     * @param type      The type of the transaction (e.g., "充值").
     * @param amount    The transaction amount.
     */
    public DisplayableTransaction(long timestamp, String type, double amount) {
        this.date = DATE_FORMAT.format(new Date(timestamp));
        this.type = type;
        this.amount = amount;
        this.items = null; // No items for this type
    }

    /**
     * Constructor to create a displayable record from a ShopTransactionRecord.
     *
     * @param record The shop transaction record from the backend.
     */
    public DisplayableTransaction(ShopTransactionRecord record) {
        this.date = DATE_FORMAT.format(new Date(record.getTimestamp()));
        this.type = "商店消费";
        this.amount = record.getTotalPrice();
        this.items = record.getItems();
    }

    // --- Getters ---
    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public List<ShopItem> getItems() {
        return items;
    }
}