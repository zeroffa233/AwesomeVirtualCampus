package app.vcampus.client.gateway;

import app.vcampus.client.net.NettyHandler;
import app.vcampus.client.repository.FakeRepository;
import app.vcampus.server.utility.ChatSession;
import app.vcampus.server.utility.ChatSession.ChatSessionSummary;
import app.vcampus.server.utility.MessageEntry;
import com.google.gson.reflect.TypeToken;
import org.json.JSONObject;
import com.google.gson.Gson;
import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * GptClient 提供了一个网关，用于访问GPT聊天历史记录。
 * 在内存中作为缓存保存聊天记录。
 */
@Slf4j
public class GptClient extends BaseClient {
    private static final GptClient instance = new GptClient(FakeRepository.handler);

    /**
     * 内存存储，模拟远程数据库。
     */
    private Map<UUID, ChatSession> inMemoryStore = new ConcurrentHashMap<>();
    /**
     * Netty处理器，用于发送请求。
     */
    private NettyHandler handler;

    /**
     * 构造函数。
     * @param handler Netty处理器。
     */
    private GptClient(NettyHandler handler) {
        this.handler = handler;
        initializeData();
    }

    /**
     * 获取GptClient的单例实例。
     * @return GptClient的单例实例。
     */
    public static GptClient getInstance() {
        return instance;
    }

    /**
     * 获取所有可用聊天会话的摘要列表。
     *
     * @return 聊天会话摘要的排序列表。
     */
    public List<ChatSessionSummary> getChatHistorySummaries() {
        System.out.println("[GptClient] 正在获取聊天历史摘要...");
        return inMemoryStore.values().stream()
                .map(ChatSession::getSummary)
                .sorted(Comparator.comparingLong(ChatSessionSummary::getLastModified).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 根据其唯一ID加载完整的聊天会话。
     *
     * @param sessionId 要加载的会话的UUID。
     * @return ChatSession对象，如果未找到则返回null。
     */
    public ChatSession loadChatSession(UUID sessionId) {
        System.out.println("[GptClient] 正在加载会话: " + sessionId);
        // 在实际场景中，这将返回一个防御性副本。
        return inMemoryStore.get(sessionId);
    }

    /**
     * 将聊天会话保存到数据存储中。
     *
     * @param session 要保存的ChatSession对象。
     */
    public void saveChatSession(ChatSession session) {
        System.out.println("[GptClient] 正在保存会话: " + session.getId());
        // 创建一个副本以避免引用问题，模拟实际保存。
        inMemoryStore.put(session.getId(), new ChatSession(session));
    }

    /**
     * 从数据存储中删除聊天会话。
     *
     * @param sessionId 要删除的会话的UUID。
     */
    public void deleteChatSession(UUID sessionId) {
        System.out.println("[GptClient] 正在删除会话: " + sessionId);
        inMemoryStore.remove(sessionId);
    }

    /**
     * 初始化数据，从服务器拉取GPT上下文。
     */
    private void initializeData() {
        Gson gs = new Gson();
        Request request = new Request();
        request.setUri("gpt/pull");
        request.setParams(Map.of());
        Response response;
        String serStorage=null;
        try {
            response = BaseClient.sendRequest(handler, request);

            // 1. 获取最外层的 Map
            Map<String, Object> responseData = (Map<String, Object>) response.getData();
            if (responseData != null && responseData.containsKey("ctx")) {
                // 2. 将 "ctx" 键对应的值作为字符串获取
                String ctxJsonString = (String) responseData.get("ctx");

                // 3. 使用 Gson 将这个字符串解析成一个新的 Map
                Type ctxMapType = new TypeToken<Map<String, Object>>() {
                }.getType();
                Map<String, Object> ctxData = gs.fromJson(ctxJsonString, ctxMapType);

                // 4. 现在可以从解析后的 ctxData Map 中安全地获取 "context"
                if (ctxData != null && ctxData.containsKey("context")) {
                    serStorage = (String) ctxData.get("context");
                }
            }
        } catch (InterruptedException e) {
            log.warn("拉取上下文失败", e);
            return;
        }
        Type mapType = new TypeToken<Map<UUID, ChatSession>>() {
        }.getType();
        inMemoryStore = gs.fromJson(serStorage, mapType);
        if (inMemoryStore==null){inMemoryStore=new HashMap<>();        }
        System.out.println("[GptClient] 已初始化 " + inMemoryStore.size() + " 个会话。");
    }

    /**
     * 保存数据，将GPT上下文推送到服务器。
     */
    public void saveData() {
        Gson gs = new Gson();
        Request request = new Request();
        request.setUri("gpt/push");
        request.setParams(Map.of("context", gs.toJson(inMemoryStore)));
        try {
            Response response = BaseClient.sendRequest(handler, request);
            if (!response.getStatus().equals("success")){throw new InterruptedException();}
        } catch (InterruptedException e) {
            log.warn("推送上下文失败");
        }
    }
}



