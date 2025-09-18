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
    /**
     * 交易时间戳。
     */
    private final long time;
    /**
     * 从服务器接收的原始类型, e.g., "deposit" or "payment"。
     */
    private final String rawType;
    /**
     * 交易描述，可能包含商品列表的JSON。
     */
    private final String description;
    /**
     * 交易金额。
     */
    private final double amount;
    /**
     * 解析后的商品列表，仅用于商店消费。
     */
    private final List<StoreItem> items;

    /**
     * Gson 实例，用于 JSON 解析，设为静态以提高性能。
     */
    private static final Gson gson = new Gson();
    /**
     * 日期格式化实例，设为静态以提高性能。
     */
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    /**
     * 唯一的、统一的构造函数。
     *
     * @param time        交易时间戳 (long)。
     * @param rawType     从服务器接收的原始交易类型 ("deposit", "payment")。
     * @param description 交易描述 (可能包含商品列表的JSON)。
     * @param amount      交易金额。
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
     * @return 解析出的商品列表，如果不是商店消费/售出或解析失败，则返回空列表。
     */
    private List<StoreItem> parseItemsFromDescription() {
        String jsonPart = null;

        // 【新增逻辑】检查是否为商家收款记录
        if ("deposit".equals(rawType) && description != null && description.startsWith("卖出货款：")) {
            jsonPart = description.substring("卖出货款：".length());
        }
        // 检查是否为用户消费记录
        else if ("payment".equals(rawType) && description != null && description.startsWith("消费:")) {
            jsonPart = description.substring("消费:".length());
        }

        // 如果成功提取了JSON部分，则进行解析
        if (jsonPart != null) {
            try {
                Type listType = new TypeToken<List<StoreItem>>() {}.getType();
                List<StoreItem> parsedItems = gson.fromJson(jsonPart, listType);
                return parsedItems != null ? parsedItems : Collections.emptyList();
            } catch (JsonSyntaxException e) {
                System.err.println("解析交易记录中的商品JSON失败: " + e.getMessage());
                return Collections.emptyList();
            }
        }
        // 如果不符合任何已知格式，返回空列表
        return Collections.emptyList();
    }


    /**
     * 获取用于UI展示的、翻译后的交易类型。
     * @return "商店消费", "商品售出", "充值", 或 "未知类型".
     */
    public String getType() {
        return switch (rawType) {
            case "payment" -> "商店消费";
            case "deposit" -> {
                // 【新增逻辑】根据描述区分是“商品售出”还是普通“充值”
                if (description != null && description.startsWith("卖出货款：")) {
                    yield "商品售出";
                } else {
                    yield "充值";
                }
            }
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

    /**
     * 获取交易金额。
     * @return 交易金额。
     */
    public double getAmount() {
        return amount;
    }

    /**
     * 获取与此交易关联的商品列表。
     * @return 商品列表；如果不是商店交易，则为空列表。
     */
    public List<StoreItem> getItems() {
        return items;
    }
}
