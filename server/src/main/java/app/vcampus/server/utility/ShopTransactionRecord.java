package app.vcampus.server.utility;

import app.vcampus.server.entity.StoreItem;
import java.util.ArrayList;
import java.util.List;
import lombok.Data; // <-- 导入
import lombok.NoArgsConstructor; // <-- 导入


@Data // <-- 【核心】添加@Data，它会自动生成 getter, setter, toString, equals, hashCode
@NoArgsConstructor
public class ShopTransactionRecord {
    private long timestamp;
    private List<StoreItem> items;
    private double totalPrice;

    public ShopTransactionRecord(List<StoreItem> items, double totalPrice) {
        this.timestamp = System.currentTimeMillis();
        this.items = new ArrayList<>(items); // 创建副本
        this.totalPrice = totalPrice;
    }

    @Override
    public String toString() {
        return "ShopTransactionRecord{" +
                "timestamp=" + timestamp +
                ", items=" + items.size() + " pcs" +
                ", totalPrice=" + totalPrice +
                '}';
    }
}