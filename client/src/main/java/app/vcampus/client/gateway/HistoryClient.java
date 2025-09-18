// 文件位置: client/src/main/java/app/vcampus/client/gateway/HistoryClient.java
package app.vcampus.client.gateway;

import app.vcampus.client.net.NettyHandler;
import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;
import app.vcampus.server.utility.ShopTransactionRecord;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class HistoryClient {

    /**
     * 从服务器获取当前用户的交易历史。
     *
     * @param handler Netty处理器
     * @return 包含交易记录的列表，如果失败则返回空列表
     */
    public static List<ShopTransactionRecord> getHistory(NettyHandler handler) {
        try {
            Request request = new Request();
            request.setUri("history/get");

            Response response = BaseClient.sendRequest(handler, request);

            if (response != null && response.getStatus().equals("success")) {
                // 后端返回的是一个JSON字符串，我们需要将它反序列化成一个List<ShopTransactionRecord>
                String historyJson = (String) response.getData();
                return new Gson().fromJson(historyJson, new TypeToken<List<ShopTransactionRecord>>(){}.getType());
            } else {
                log.warn("Failed to get transaction history from server: {}", response != null ? response.getMessage() : "null response");
            }
        } catch (Exception e) {
            log.error("Exception occurred while getting transaction history", e);
        }
        return Collections.emptyList(); // 发生任何错误时，返回一个安全的空列表
    }

    /**
     * 更新服务器上当前用户的交易历史。
     *
     * @param handler Netty处理器
     * @param history 最新的完整交易历史列表
     * @return 操作是否成功
     */
    public static boolean updateHistory(NettyHandler handler, List<ShopTransactionRecord> history) {
        try {
            Request request = new Request();
            request.setUri("history/update");

            // 将整个列表序列化成一个JSON字符串
            String historyJson = new Gson().toJson(history);
            request.setParams(Map.of("historyJson", historyJson));

            Response response = BaseClient.sendRequest(handler, request);

            return response != null && response.getStatus().equals("success");
        } catch (Exception e) {
            log.error("Exception occurred while updating transaction history", e);
            return false;
        }
    }
}