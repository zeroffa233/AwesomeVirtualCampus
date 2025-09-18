package app.vcampus.server.utility;

import app.vcampus.server.entity.StoreItem;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 一个重构后的、统一的交易记录展示类。
 * 它直接从服务器的原始数据构造，并在内部处理所有解析逻辑。
 */
public class DisplayableTransaction {
    private final long time;
    private final String rawType; // 从服务器接收的原始类型, e.g., "deposit" or "payment"
    private final String description;
    private final double amount;
    private final List<StoreItem> items; // 解析后的商品列表，仅用于商店消费

    // 使用静态实例以提高性能
    private static final Gson gson = new Gson();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    /**
     * 唯一的、统一的构造函数。
     *
     * @param time        交易时间戳 (long)
     * @param rawType     从服务器接收的原始交易类型 ("deposit", "payment")
     * @param description 交易描述 (可能包含商品列表的JSON)
     * @param amount      交易金额
     */
    public DisplayableTransaction(long time, String rawType, String description, double amount) {
        this.time = time;
        this.rawType = rawType;
        this.description = description;
        this.amount = amount;
        // 构造时立即尝试解析商品列表
        this.items = parseItemsFromDescription();
    }

    /**
     * 内部私有方法，负责从 description 字段中解析商品列表。
     * 这是此类的核心逻辑。
     * @return 解析出的商品列表，如果不是商店消费或解析失败，则返回空列表。
     */
    private List<StoreItem> parseItemsFromDescription() {
        // 检查是否为可能包含商品列表的商店消费记录
        if ("payment".equals(rawType) && description != null && description.startsWith("消费:")) {
            try {
                // 提取 "消费:" 后面的JSON字符串部分
                String jsonPart = description.substring("消费:".length());

                // 定义需要转换的类型 (List<StoreItem>)
                Type listType = new TypeToken<List<StoreItem>>() {}.getType();

                // 使用Gson进行解析
                List<StoreItem> parsedItems = gson.fromJson(jsonPart, listType);

                // 如果解析结果不为null，则返回结果，否则返回空列表以防万一
                return parsedItems != null ? parsedItems : Collections.emptyList();
            } catch (JsonSyntaxException e) {
                // 如果JSON格式错误，打印错误信息并返回空列表，保证程序健壮性
                System.err.println("解析交易记录中的商品JSON失败: " + e.getMessage());
                return Collections.emptyList();
            }
        }
        // 如果不是商店消费，或者描述不符合格式，直接返回空列表
        return Collections.emptyList();
    }

    /**
     * 获取用于UI展示的、翻译后的交易类型。
     * @return "商店消费", "充值", 或 "未知类型".
     */
    public String getType() {
        return switch (rawType) {
            case "payment" -> "商店消费";
            case "deposit" -> "充值";
            default -> "未知类型";
        };
    }

    /**
     * 获取从服务器传来的原始类型字符串。
     * UI逻辑应使用此方法来判断交易的具体种类。
     * @return "payment" 或 "deposit".
     */
    public String getRawType() {
        return rawType;
    }

    /**
     * 获取格式化后的日期字符串。
     * @return "yyyy-MM-dd HH:mm:ss" 格式的日期。
     */
    public String getDate() {
        return dateFormat.format(new Date(time));
    }

    // --- 其他简单的 Getters ---

    public double getAmount() {
        return amount;
    }

    public List<StoreItem> getItems() {
        return items;
    }
}