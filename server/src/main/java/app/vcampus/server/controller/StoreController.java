package app.vcampus.server.controller;

import app.vcampus.server.entity.*;
import app.vcampus.server.enums.CardStatus;
import app.vcampus.server.enums.TransactionType;
import app.vcampus.server.utility.*;
import app.vcampus.server.utility.router.RouteMapping;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Transaction;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商店控制器。
 * 处理与商店相关的操作，如购买、查询、添加和更新商品等。
 */
@Slf4j
public class StoreController {
    /**
     * 用户购买商品。
     *
     * @param request  包含购买商品列表的请求。
     * @param database 数据库会话。
     * @return 操作结果的响应。
     */
    @RouteMapping(uri = "store/buy")
    public Response buy(Request request, org.hibernate.Session database) {
        try {
            FinanceCard financeCard = database.get(FinanceCard.class, request.getSession().getCardNum());
            if (financeCard == null)
                return Response.Common.error("No such finance card");

            if (financeCard.status != CardStatus.normal)
                return Response.Common.error("Card is not normal");

            Type type = new TypeToken<List<Pair<UUID, Integer>>>() {
            }.getType();
            List<Pair<UUID, Integer>> items = new Gson().fromJson(request.getParams().get("items"), type);
            if (items == null)
                return Response.Common.error("Items cannot be empty");

            int totalPrice = 0;
            Transaction tx = database.beginTransaction();

            for (Pair<UUID, Integer> item : items) {
                StoreItem storeItem = database.get(StoreItem.class, item.getFirst());
                if (storeItem == null)
                    return Response.Common.error("No such item");
                totalPrice += storeItem.getPrice() * item.getSecond();
                if (storeItem.getStock() < item.getSecond())
                    return Response.Common.error("Stock cannot be less than amount");
                storeItem.setStock(storeItem.getStock() - item.getSecond());
                storeItem.setSalesVolume(storeItem.getSalesVolume() + item.getSecond());
                database.persist(storeItem);

                StoreTransaction storeTransaction = new StoreTransaction();
                storeTransaction.setUuid(UUID.randomUUID());
                storeTransaction.setItemUUID(storeItem.getUuid());
                storeTransaction.setItemPrice(storeItem.getPrice());
                storeTransaction.setAmount(item.getSecond());
                storeTransaction.setCardNumber(financeCard.getCardNumber());
                storeTransaction.setTime(new Date());
                storeTransaction.setRemark("");
                database.persist(storeTransaction);
            }
            if (financeCard.getBalance() < totalPrice)
                return Response.Common.error("Balance is not enough");
            financeCard.setBalance(financeCard.getBalance() - totalPrice);
            database.persist(financeCard);

            CardTransaction cardTransaction = new CardTransaction();
            cardTransaction.setUuid(UUID.randomUUID());
            cardTransaction.setCardNumber(financeCard.getCardNumber());
            cardTransaction.setAmount(totalPrice);
            cardTransaction.setTime(new Date());
            cardTransaction.setDescription("商店消费");
            cardTransaction.setType(TransactionType.payment);
            database.persist(cardTransaction);

            tx.commit();

            return Response.Common.ok();
        } catch (Exception e) {
            return Response.Common.error("Failed to buy item");
        }
    }

    /**
     * 获取所有商店交易记录。
     *
     * @param request  请求对象。
     * @param database 数据库会话。
     * @return 包含所有交易记录列表的响应。
     */
    @RouteMapping(uri = "store/user/getAllTransactions")
    public Response getAllTransactions(Request request, org.hibernate.Session database) {
        try {
            List<StoreTransaction> allTransactions = Database.loadAllData(StoreTransaction.class, database);
            return Response.Common.ok(allTransactions.stream().map(StoreTransaction::toJson).collect(Collectors.toList()));
        } catch (Exception e) {
            log.warn("Failed to get transaction records", e);
            return Response.Common.error("Failed to get transaction records");
        }
    }

    /**
     * 获取当日销售额。
     *
     * @param request  请求对象。
     * @param database 数据库会话。
     * @return 包含当日销售额的响应。
     */
    @RouteMapping(uri = "store/staff/getTodaySales")
    public Response getTodaySales(Request request, org.hibernate.Session database) {
        try {
            List<StoreTransaction> allTransactions = Database.loadAllData(StoreTransaction.class, database);
            int salesVolume = 0;
            for (StoreTransaction storeTransaction : allTransactions) {
                if (DateUtility.fromDate(storeTransaction.getTime()).equals(DateUtility.fromDate(new Date())))
                    salesVolume += storeTransaction.getAmount() * storeTransaction.getItemPrice();
            }

            return Response.Common.ok(Map.of("salesVolume", Integer.toString(salesVolume)));
        } catch (Exception e) {
            log.warn("Failed to get transaction records", e);
            return Response.Common.error("Failed to get transaction records");
        }
    }

    /**
     * 根据关键词搜索商品。
     *
     * @param request  包含搜索关键词的请求。
     * @param database 数据库会话。
     * @return 包含匹配商品列表的响应。
     */
    @RouteMapping(uri = "storeItem/searchItem")
    public Response searchItem(Request request, org.hibernate.Session database) {
        try {
            String keyword = request.getParams().get("keyword");
            if (keyword == null)
                return Response.Common.error("Keyword cannot be empty");
            List<StoreItem> items = Database.likeQuery(StoreItem.class,
                    new String[]{"uuid", "itemName", "price", "pictureLink", "barcode", "description"}, keyword, database);
            return Response.Common.ok(Map.of("items", items.stream().map(StoreItem::toJson).collect(Collectors.toList())));
        } catch (Exception e) {
            return Response.Common.error("Failed to search item");
        }
    }

    /**
     * 根据商品ID搜索商品。
     *
     * @param request  包含商品UUID的请求。
     * @param database 数据库会话。
     * @return 包含商品信息的响应。
     */
    @RouteMapping(uri = "storeItem/searchId")
    public Response searchId(Request request, org.hibernate.Session database) {
        try {
            String uuid = request.getParams().get("uuid");
            if (uuid == null)
                return Response.Common.error("UUID cannot be empty");
            UUID id = UUID.fromString(uuid);
            StoreItem storeItem = database.get(StoreItem.class, id);
            if (storeItem == null) {
                return Response.Common.error("missing item information");
            }
            return Response.Common.ok(storeItem.toJson());
        } catch (Exception e) {
            return Response.Common.error("Failed to search item");
        }
    }

    /**
     * 获取所有商品列表。
     *
     * @param request  请求对象。
     * @param database 数据库会话。
     * @return 包含所有商品列表的响应。
     */
    @RouteMapping(uri = "store/filter")
    public Response filter(Request request, org.hibernate.Session database) {
        try {
            List<StoreItem> allItems;
            allItems = Database.loadAllData(StoreItem.class, database);
            return Response.Common.ok(allItems.stream().map(StoreItem::toJson).collect(Collectors.toList()));
        } catch (Exception e) {
            log.warn("Failed to filter store items", e);
            return Response.Common.error("Failed to filter store items");
        }
    }

    /**
     * 添加新商品。
     *
     * @param request  包含新商品信息的请求。
     * @param database 数据库会话。
     * @return 操作结果的响应。
     */
    @RouteMapping(uri = "storeItem/addItem")
    public Response addItem(Request request, org.hibernate.Session database) {
        String storeItemJson = request.getParams().get("item");
        if (storeItemJson == null) {
            return Response.Common.badRequest();
        }

        StoreItem newStoreItem = IEntity.fromJson(storeItemJson, StoreItem.class);

        if (newStoreItem == null) {
            return Response.Common.badRequest();
        }

        if (Objects.equals(newStoreItem.itemName, "")) {
            return Response.Common.badRequest();
        }
        if (newStoreItem.price <= 0) {
            return Response.Common.badRequest();
        }
        if (newStoreItem.stock <= 0) {
            return Response.Common.badRequest();
        }
        if (Objects.equals(newStoreItem.pictureLink, "")) {
            return Response.Common.badRequest();
        }

        Transaction tx = database.beginTransaction();
        database.persist(newStoreItem);
        tx.commit();

        return Response.Common.ok();
    }

    /**
     * 获取当前用户的交易记录。
     *
     * @param request  请求对象。
     * @param database 数据库会话。
     * @return 按日期分组的交易记录列表的响应。
     */
    @RouteMapping(uri = "storeTransaction/getRecords")
    public Response getRecords(Request request, org.hibernate.Session database) {
        try {
            List<StoreTransaction> allRecords = Database.getWhereString(StoreTransaction.class, "cardNumber", Integer.toString(request.getSession().getCardNum()), database);
            allRecords = allRecords.stream().peek(w -> {
                StoreItem storeItem = database.get(StoreItem.class, w.getItemUUID());
                w.setItem(storeItem);
            }).collect(Collectors.toList());
            allRecords.sort((o1, o2) -> o2.getTime().compareTo(o1.getTime()));
            return Response.Common.ok(allRecords.stream().collect(Collectors.groupingBy(w -> DateUtility.fromDate(w.time))).entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().stream().map(StoreTransaction::toJson).collect(Collectors.toList()))));
        } catch (Exception e) {
            log.warn("Failed to get transaction records", e);
            return Response.Common.error("Failed to get transaction records");
        }
    }

    /**
     * 根据关键词搜索交易记录。
     *
     * @param request  包含搜索关键词的请求。
     * @param database 数据库会话。
     * @return 按UUID分组的交易记录列表的响应。
     */
    @RouteMapping(uri = "storeTransaction/searchTransaction")
    public Response searchTransaction(Request request, org.hibernate.Session database) {
        try {
            String keyword = request.getParams().get("keyword");
            if (keyword == null)
                return Response.Common.error("Keyword cannot be empty");
            List<StoreTransaction> transactions = Database.likeQuery(StoreTransaction.class,
                    new String[]{"itemName", "itemPrice", "amount", "remark", "time"}, keyword, database);
            return Response.Common.ok(transactions.stream().collect(Collectors.groupingBy(w -> w.uuid)).entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().stream().map(StoreTransaction::toJson).collect(Collectors.toList())
            )));
        } catch (Exception e) {
            return Response.Common.error("Failed to search transaction record");
        }
    }

    /**
     * 更新商品信息。
     *
     * @param request  包含更新后商品信息的请求。
     * @param database 数据库会话。
     * @return 操作结果的响应。
     */
    @RouteMapping(uri = "storeItem/updateItem")
    public Response updateItem(Request request, org.hibernate.Session database) {
        StoreItem newItem = IEntity.fromJson(request.getParams().get("storeItem"), StoreItem.class);

        Transaction tx = database.beginTransaction();
        database.merge(newItem);
        tx.commit();

        return Response.Common.ok();
    }

    /**
     * 【新增】批量更新商品的库存和销量。
     * 在一次成功的支付后由客户端调用，以反映本次交易的结果。
     *
     * @param request  需要包含一个 "updates" 的参数，其值为一个JSON字符串，
     *                 格式为: Map<String, Integer>，其中 key 是商品UUID的字符串形式，value 是本次购买的数量。
     * @param database Hibernate Session
     * @return 操作成功或失败的 Response
     */
    @RouteMapping(uri = "store/batchUpdateStock")
    public Response batchUpdateStock(Request request, org.hibernate.Session database) {
        // 安全检查：确保是已登录用户在操作
        if (request.getSession() == null || request.getSession().getCardNum() == 0) {
            return Response.Common.permissionDenied();
        }

        String updatesJson = request.getParams().get("updates");
        if (updatesJson == null) {
            return Response.Common.badRequest();
        }

        Transaction tx = null;
        try {
            // 使用 Gson 将客户端传来的JSON字符串反序列化成 Map<String, Long>
            // 注意：客户端使用的是 Map<UUID, Long>，Gson会把UUID转成String，所以这里用String作为Key
            Type type = new TypeToken<Map<String, Long>>(){}.getType();
            Map<String, Long> purchaseMap = new Gson().fromJson(updatesJson, type);

            // 开始数据库事务
            tx = database.beginTransaction();

            for (Map.Entry<String, Long> entry : purchaseMap.entrySet()) {
                UUID itemUuid = UUID.fromString(entry.getKey());
//                Integer purchasedAmount = entry.getValue(); // GSON 默认将数字解析为 Double，然后转为 Long，这里直接用 Long
                long purchasedAmount = entry.getValue();

                // 从数据库中获取最新的商品信息
                StoreItem item = database.get(StoreItem.class, itemUuid);
                if (item != null) {
                    // 计算新的库存和销量
                    int newStock = (int) (item.getStock() - purchasedAmount);
                    int newSalesVolume = (int) (item.getSalesVolume() + purchasedAmount);

                    // 更新对象属性 (后端再次校验，确保库存不为负)
                    item.setStock(Math.max(0, newStock));
                    item.setSalesVolume(newSalesVolume);

                    // 更新数据库中的记录
                    // 注意：因为 item 是从数据库加载的持久化对象，理论上 tx.commit() 时会自动更新。
                    // 但为保险起见，明确调用 update() 或 merge() 是更稳妥的做法。
                    database.merge(item);
                } else {
                    log.warn("在批量更新库存时，找不到UUID为 {} 的商品，跳过此条目。", itemUuid);
                }
            }

            // 提交整个事务
            tx.commit();
            log.info("成功为用户 {} 的交易批量更新了 {} 个商品的库存和销量。", request.getSession().getCardNum(), purchaseMap.size());
            return Response.Common.ok();

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            log.error("批量更新库存时发生严重错误", e);
            return Response.Common.internalError();
        }
    }
}