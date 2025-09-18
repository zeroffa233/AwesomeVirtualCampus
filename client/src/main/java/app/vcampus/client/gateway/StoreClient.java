package app.vcampus.client.gateway;

import app.vcampus.client.net.NettyHandler;
import app.vcampus.server.entity.IEntity;
import app.vcampus.server.entity.StoreItem;
import app.vcampus.server.entity.StoreTransaction;
import app.vcampus.server.utility.Pair;
import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 商店客户端，提供与商店系统交互的功能，包括购买商品、管理商品（添加、更新、搜索）、查询交易记录和销售额。
 */
@Slf4j
public class StoreClient extends BaseClient {

    /**
     * 购买商品。
     *
     * @param handler Netty处理器。
     * @param items 购买的商品列表，包含商品UUID和数量。
     * @return 如果购买成功则返回true，否则返回false。
     */
    public static Boolean buyItems(NettyHandler handler, List<Pair<UUID, Integer>> items) {
        Request request = new Request();
        request.setUri("store/buy");
        request.setParams(Map.of("items", BaseClient.toJson(items)));
        try {
            Response response = BaseClient.sendRequest(handler, request);
            return response.getStatus().equals("success");
        } catch (InterruptedException e) {
            log.warn("购买商品失败", e);
            return false;
        }
    }

    /**
     * 获取所有商品列表。
     *
     * @param handler Netty处理器。
     * @return 所有商品的列表，如果获取失败则返回null。
     */
    public static List<StoreItem> getAll(NettyHandler handler) {
        Request request = new Request();
        request.setUri("store/filter");
        try {
            Response response = BaseClient.sendRequest(handler, request);

            if (response.getStatus().equals("success")) {
                List<String> raw_data = (List<String>) response.getData();
                List<StoreItem> data = new ArrayList<>();
                raw_data.forEach(json -> data.add(IEntity.fromJson(json, StoreItem.class)));
                return data;
            } else {
                return null;
            }
        } catch (Exception e) {
            log.warn(String.valueOf(e));
            return null;
        }
    }

    /**
     * 获取所有交易记录。
     *
     * @param handler Netty处理器。
     * @return 所有交易记录的列表，如果获取失败则返回null。
     */
    public static List<StoreTransaction> getAllTransaction(NettyHandler handler) {
        Request request = new Request();
        request.setUri("store/user/getAllTransactions");
        try {
            Response response = BaseClient.sendRequest(handler, request);
            if (response.getStatus().equals("success")) {
                List<String> raw_data = (List<String>) response.getData();
                List<StoreTransaction> data = new ArrayList<>();
                raw_data.forEach(json -> data.add(IEntity.fromJson(json, StoreTransaction.class)));
                return data;
            } else {
                throw new RuntimeException("获取交易信息失败");
            }
        } catch (Exception e) {
            log.warn(String.valueOf(e));
            return null;
        }
    }

    /**
     * 获取今日销售额。
     *
     * @param handler Netty处理器。
     * @return 今日销售额，如果获取失败则返回null。
     */
    public static Integer getTodaySalesVolume(NettyHandler handler) {
        Request request = new Request();
        request.setUri("store/staff/getTodaySales");
        try {
            Response response = BaseClient.sendRequest(handler, request);
            if (response.getStatus().equals("success")) {
                return Integer.valueOf(((Map<String, String>) response.getData()).get("salesVolume"));
            } else {
                throw new RuntimeException("获取交易信息失败");
            }
        } catch (Exception e) {
            log.warn(String.valueOf(e));
            return null;
        }
    }

    /**
     * 添加新商品。
     *
     * @param handler Netty处理器。
     * @param newStoreItem 要添加的StoreItem对象。
     * @return 如果添加成功则返回true，否则返回false。
     */
    public static boolean addItem(NettyHandler handler, StoreItem newStoreItem) {
        Request request = new Request();
        request.setUri("storeItem/addItem");
        request.setParams(Map.of("item", newStoreItem.toJson()));
        try {
            Response response = BaseClient.sendRequest(handler, request);
            return response.getStatus().equals("success");
        } catch (InterruptedException e) {
            log.warn("添加商品失败", e);
            return false;
        }
    }

    /**
     * 更新商品信息。
     *
     * @param handler Netty处理器。
     * @param storeItem 要更新的StoreItem对象。
     * @return 如果更新成功则返回true，否则返回false。
     */
    public static boolean updateItem(NettyHandler handler, StoreItem storeItem) {
        Request request = new Request();
        request.setUri("storeItem/updateItem");
        request.setParams(Map.of(
                "storeItem", storeItem.toJson()
        ));
        try {
            Response response = BaseClient.sendRequest(handler, request);
            return response.getStatus().equals("success");
        } catch (InterruptedException e) {
            log.warn("更新商品失败", e);
            return false;
        }
    }

    /**
     * 搜索商品。
     *
     * @param handler Netty处理器。
     * @param keyword 搜索关键词。
     * @return 匹配商品的列表，如果搜索失败则返回null。
     */
    public static List<StoreItem> searchItem(NettyHandler handler, String keyword) {
        Request request = new Request();
        request.setUri("storeItem/searchItem");
        request.setParams(Map.of(
                "keyword", keyword
        ));
        try {
            Response response = BaseClient.sendRequest(handler, request);
            if (response.getStatus().equals("success")) {
                List<String> raw_data = ((Map<String, List<String>>) response.getData()).get("items");
                return raw_data.stream().map(json -> IEntity.fromJson(json, StoreItem.class)).toList();
            } else {
                throw new RuntimeException("获取商品信息失败");
            }
        } catch (InterruptedException e) {
            log.warn("获取商品信息失败", e);
            return null;
        }
    }

    /**
     * 根据UUID搜索商品。
     *
     * @param handler Netty处理器。
     * @param uuid 商品的UUID。
     * @return 匹配的StoreItem对象，如果搜索失败则返回null。
     */
    public static StoreItem searchId(NettyHandler handler, String uuid) {
        Request request = new Request();
        request.setUri("storeItem/searchId");
        request.setParams(Map.of("uuid", uuid));
        try {
            Response response = BaseClient.sendRequest(handler, request);
            if (response.getStatus().equals("success")) {
                StoreItem data = IEntity.fromJson((String) response.getData(), StoreItem.class);
                return data;
            } else {
                throw new RuntimeException("获取商品失败");
            }
        } catch (InterruptedException e) {
            log.warn("获取商品失败", e);
            return null;
        }
    }

    /**
     * 获取交易记录。
     *
     * @param handler Netty处理器。
     * @return 交易记录的Map，如果获取失败则返回null。
     */
    public static Map<String, List<StoreTransaction>> getTransaction(NettyHandler handler) {
        Request request = new Request();
        request.setUri("storeTransaction/getRecords");
        try {
            Response response = BaseClient.sendRequest(handler, request);
            if (response.getStatus().equals("success")) {
                Map<String, List<String>> raw_data = (Map<String, List<String>>) response.getData();
                Map<String, List<StoreTransaction>> data = new HashMap<>();
                raw_data.forEach((key, value) -> data.put(key, value.stream().map(
                        json -> IEntity.fromJson(json, StoreTransaction.class)).toList()));
                return data;
            } else {
                throw new RuntimeException("获取商品信息失败");
            }
        } catch (Exception e) {
            log.warn(String.valueOf(e));
            return null;
        }
    }

    /**
     * 搜索交易记录。
     *
     * @param handler Netty处理器。
     * @param keyword 搜索关键词。
     * @return 匹配交易记录的Map，如果搜索失败则返回null。
     */
    public static Map<String, List<StoreTransaction>> searchTransaction(NettyHandler handler, String keyword) {
        Request request = new Request();
        request.setUri("storeTransaction/searchTransaction");
        request.setParams(Map.of(
                "keyword", keyword
        ));
        try {
            Response response = BaseClient.sendRequest(handler, request);
            if (response.getStatus().equals("success")) {
                Map<String, List<String>> raw_data = (Map<String, List<String>>) response.getData();
                Map<String, List<StoreTransaction>> data = new HashMap<>();
                raw_data.forEach((key, value) -> data.put(key, value.stream().map(json -> IEntity.fromJson(json, StoreTransaction.class)).toList()));
                return data;
            } else {
                throw new RuntimeException("获取交易信息失败");
            }
        } catch (InterruptedException e) {
            log.warn("获取书籍信息失败", e);
            return null;
        }
    }

    /**
     * 【新增】向服务器发送批量更新商品库存和销量的请求。
     *
     * @param handler Netty处理器
     * @param purchaseMap 一个Map，key是商品UUID，value是购买数量
     * @return 操作是否成功
     */
    public static boolean batchUpdateStock(NettyHandler handler, Map<UUID, Long> purchaseMap) {
        try {
            Request request = new Request();
            request.setUri("store/batchUpdateStock");
            // Gson可以很好地处理 Map<UUID, Long> 到 JSON String 的转换
            String updatesJson = new Gson().toJson(purchaseMap);
            request.setParams(Map.of("updates", updatesJson));

            Response response = BaseClient.sendRequest(handler, request);
            return response != null && response.getStatus().equals("success");
        } catch (Exception e) {
            log.error("批量更新库存在客户端发生异常", e);
            return false;
        }
    }
}


