package app.vcampus.server.utility;

import app.vcampus.server.entity.StoreItem;
import java.util.ArrayList;
import java.util.List;

public class ShopTransactionRecord {
    private final long timestamp;
    private final List<StoreItem> items;
    private final double totalPrice;

    public ShopTransactionRecord(List<StoreItem> items, double totalPrice) {
        this.timestamp = System.currentTimeMillis();
        this.items = new ArrayList<>(items); // 创建副本
        this.totalPrice = totalPrice;
    }

    // --- Getters ---
    public long getTimestamp() { return timestamp; }
    public List<StoreItem> getItems() { return items; }
    public double getTotalPrice() { return totalPrice; }

    @Override
    public String toString() {
        return "ShopTransactionRecord{" +
                "timestamp=" + timestamp +
                ", items=" + items.size() + " pcs" +
                ", totalPrice=" + totalPrice +
                '}';
    }
}