package app.vcampus.client.gateway;

import app.vcampus.client.util.ChatSession;
import app.vcampus.client.util.ChatSession.ChatSessionSummary;
import app.vcampus.client.util.MessageEntry;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * GptClient provides a gateway to access GPT chat history.
 * In the current development phase, it acts as a mock, holding chat records in memory.
 * In the future, this class will be updated to make network calls to a remote server.
 */
public class GptClient {

    private static final GptClient instance = new GptClient();

    // In-memory store to simulate a remote database
    private final Map<UUID, ChatSession> inMemoryStore = new ConcurrentHashMap<>();

    private GptClient() {
        // Pre-populate with some mock data for demonstration purposes
        initializeMockData();
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

    private void initializeMockData() {

        UUID session2Id = UUID.fromString("fedcba98-7654-3210-fedc-ba9876543210");
        ChatSession session2 = new ChatSession(session2Id);
        session2.setTitle("规划一次旅行");
        session2.setLastModified(System.currentTimeMillis()); // Make it the newest
        session2.addMessage(new MessageEntry(UUID.randomUUID(), new JSONObject().put("role", "system").put("content", "你是一个旅行规划助手。")));
        session2.addMessage(new MessageEntry(UUID.randomUUID(), new JSONObject().put("role", "user").put("content", "我想去云南玩，有什么推荐吗？")));
        session2.addMessage(new MessageEntry(UUID.randomUUID(), new JSONObject().put("role", "assistant").put("content", "云南的旅游方案...")));
        inMemoryStore.put(session2Id, session2);

        System.out.println("[GptClient] Mock data initialized with " + inMemoryStore.size() + " sessions.");
    }
}