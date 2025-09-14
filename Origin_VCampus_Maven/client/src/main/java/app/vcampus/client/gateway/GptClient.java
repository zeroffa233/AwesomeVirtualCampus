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
 * GptClient provides a gateway to access GPT chat history.
 * Holding chat records in memory as a cache.
 */
@Slf4j
public class GptClient extends BaseClient {
    //TODO find the real handler
    private static final GptClient instance = new GptClient(FakeRepository.handler);

    // In-memory store to simulate a remote database
    private Map<UUID, ChatSession> inMemoryStore = new ConcurrentHashMap<>();
    private NettyHandler handler;

    private GptClient(NettyHandler handler) {
        this.handler = handler;
        initializeData();
    }

    public static GptClient getInstance() {
        return instance;
    }

    /**
     * Retrieves a list of summaries for all available chat sessions.
     *
     * @return A sorted list of ChatSessionSummary objects.
     */
    public List<ChatSessionSummary> getChatHistorySummaries() {
        System.out.println("[GptClient] Fetching chat history summaries...");
        return inMemoryStore.values().stream()
                .map(ChatSession::getSummary)
                .sorted(Comparator.comparingLong(ChatSessionSummary::getLastModified).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Loads a full chat session by its unique ID.
     *
     * @param sessionId The UUID of the session to load.
     * @return The ChatSession object, or null if not found.
     */
    public ChatSession loadChatSession(UUID sessionId) {
        System.out.println("[GptClient] Loading session: " + sessionId);
        // In a real scenario, this would return a defensive copy.
        return inMemoryStore.get(sessionId);
    }

    /**
     * Saves a chat session to the data store.
     *
     * @param session The ChatSession object to save.
     */
    public void saveChatSession(ChatSession session) {
        System.out.println("[GptClient] Saving session: " + session.getId());
        // Create a copy to avoid reference issues, simulating a real save.
        inMemoryStore.put(session.getId(), new ChatSession(session));
    }

    /**
     * Deletes a chat session from the data store.
     *
     * @param sessionId The UUID of the session to delete.
     */
    public void deleteChatSession(UUID sessionId) {
        System.out.println("[GptClient] Deleting session: " + sessionId);
        inMemoryStore.remove(sessionId);
    }

    private void initializeData() {
        // TODO realIO
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
            log.warn("Fail to pull context", e);
            return;
        }
        Type mapType = new TypeToken<Map<UUID, ChatSession>>() {
        }.getType();
        inMemoryStore = gs.fromJson(serStorage, mapType);
        if (inMemoryStore==null){inMemoryStore=new HashMap<>();        }
        System.out.println("[GptClient] initialized with " + inMemoryStore.size() + " sessions.");
    }

    public void saveData() {
        Gson gs = new Gson();
        Request request = new Request();
        request.setUri("gpt/push");
        request.setParams(Map.of("context", gs.toJson(inMemoryStore)));
        try {
            Response response = BaseClient.sendRequest(handler, request);
            if (!response.getStatus().equals("success")){throw new InterruptedException();}
        } catch (InterruptedException e) {
            log.warn("Fail to push context");
        }
    }
}



