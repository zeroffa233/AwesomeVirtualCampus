package app.vcampus.server.utility;

import app.vcampus.server.entity.StoreItem;
import java.util.ArrayList;
import java.util.List;
import lombok.Data; // <-- 导入
import lombok.NoArgsConstructor; // <-- 导入


@Data // <-- 【核心】添加@Data，它会自动生成 getter, setter, toString, equals, hashCode
@NoArgsConstructor
/**
 * 商店交易记录类。
 * 用于封装一次商店交易的详细信息。
 */
public class ShopTransactionRecord {
public class ShopTransactionRecord {
    /**
     * 交易发生时的时间戳。
     */
    private final long timestamp;
    /**
     * 交易所包含的商品列表。
     */
    private final List<StoreItem> items;
    /**
     * 交易的总价。
     */
    private final double totalPrice;

    /**
     * 构造一个新的商店交易记录。
     *
     * @param items      交易的商品列表。
     * @param totalPrice 交易的总价。
     */
    public ShopTransactionRecord(List<StoreItem> items, double totalPrice) {
        this.timestamp = System.currentTimeMillis();
        this.items = new ArrayList<>(items); // 创建副本以确保不可变性
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
