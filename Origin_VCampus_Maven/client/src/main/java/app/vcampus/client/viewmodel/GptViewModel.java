package app.vcampus.client.viewmodel;

import app.vcampus.client.gateway.GptClient;
import app.vcampus.client.util.ChatSession;
import app.vcampus.client.util.ChatSession.ChatSessionSummary;
import app.vcampus.client.util.MessageEntry;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GptViewModel {

    // Properties for UI binding
    private final StringProperty userInput = new SimpleStringProperty("");
    private final ObservableList<Message> chatMessages = FXCollections.observableArrayList();
    private final BooleanProperty sendButtonDisabled = new SimpleBooleanProperty(false);
    private final ObservableList<ChatSessionSummary> chatHistory = FXCollections.observableArrayList();

    // API related constants
    private final String WELCOME_MESSAGE = "你好！我是AI助手，有什么可以帮你的吗？";
    private final String SYSTEM_PROMPT = "你是一个乐于助人的AI助手, 正在和一个学生宋兵甲对话。";
    private final String MODEL = "deepseek-v3.1";
    private final String API_KEY = "sk-588905851d1b4421ae51c9ad64fb120b";
    private final String API_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    // Data Gateway
    private final GptClient gptClient = GptClient.getInstance();
    private ChatSession currentSession;
    private UUID currentStreamingMessageId;


    public void initializeSession() {
        loadChatHistorySummaries();
        List<ChatSessionSummary> history = gptClient.getChatHistorySummaries();
        if (!history.isEmpty()) {
            loadSession(history.get(0).getId());
        } else {
            createNewSession();
        }
    }

    /**
     * Deletes a session from the gateway and updates the UI.
     * If the deleted session is the one currently being viewed, it creates a new session.
     *
     * @param sessionIdToDelete The UUID of the session to delete.
     */
    public void deleteSession(UUID sessionIdToDelete) {
        if (sessionIdToDelete == null) return;

        boolean wasActiveSession = currentSession != null && currentSession.getId().equals(sessionIdToDelete);

        // 1. Delete from the data source
        gptClient.deleteChatSession(sessionIdToDelete);

        // 2. Check if the deleted session was the active one
        if (wasActiveSession) {
            // [FIX] Set currentSession to null BEFORE creating a new one.
            // This prevents createNewSession() from saving the session we just deleted.
            currentSession = null;
            createNewSession();
        }

        // 3. Refresh the list of summaries in the UI
        loadChatHistorySummaries();
    }

    // #region Properties for UI Binding
    public StringProperty userInputProperty() { return userInput; }
    public ObservableList<Message> getChatMessages() { return chatMessages; }
    public BooleanProperty sendButtonDisabledProperty() { return sendButtonDisabled; }
    public ObservableList<ChatSessionSummary> getChatHistory() { return chatHistory; }
    // #endregion

    // #region Session Management Core Logic
    public void createNewSession() {
        if (currentSession != null && currentSession.getMessageHistory().size() > 1) {
            saveCurrentSession();
        }

        currentSession = new ChatSession(UUID.randomUUID());
        currentSession.addMessage(new MessageEntry(UUID.randomUUID(), new JSONObject().put("role", "system").put("content", SYSTEM_PROMPT)));

        rebuildChatDisplay();
        addMessage(new Message(UUID.randomUUID(), "system", WELCOME_MESSAGE, false));
        loadChatHistorySummaries(); // Refresh list to show the new chat if user types and saves
    }

    public void loadSession(UUID sessionId) {
        if (currentSession != null && currentSession.getId().equals(sessionId)) {
            return;
        }
        if (currentSession != null && currentSession.getMessageHistory().size() > 1) {
            saveCurrentSession();
        }

        try {
            ChatSession loadedSession = gptClient.loadChatSession(sessionId);
            if (loadedSession != null) {
                currentSession = loadedSession;
                rebuildChatDisplay();
            } else {
                throw new Exception("Session not found in client.");
            }
        } catch (Exception e) {
            System.err.println("Error loading session " + sessionId + ": " + e.getMessage());
            addMessage(new Message(UUID.randomUUID(), "system", "加载聊天记录失败，已创建新聊天。", false));
            createNewSession();
        }
    }

    public void saveCurrentSession() {
        if (currentSession == null || currentSession.getMessageHistory().size() <= 1) {
            return; // Don't save empty sessions
        }
        currentSession.updateTitle();
        currentSession.setLastModified(System.currentTimeMillis());
        gptClient.saveChatSession(currentSession);
        loadChatHistorySummaries(); // Refresh the list with updated title/order
    }

    private void loadChatHistorySummaries() {
        Platform.runLater(() -> {
            chatHistory.setAll(gptClient.getChatHistorySummaries());
        });
    }

    private void rebuildChatDisplay() {
        Platform.runLater(() -> {
            chatMessages.clear();
            List<MessageEntry> history = currentSession.getMessageHistory();
            for (MessageEntry entry : history) {
                JSONObject msgJson = entry.getMessage();
                String role = msgJson.getString("role");
                String content = msgJson.getString("content");

                if (!"system".equals(role)) {
                    chatMessages.add(new Message(entry.getId(), role, content, true));
                }
            }
        });
    }
    // #endregion

    // #region Message Handling
    public void sendMessage() {
        String userMessageContent = userInput.get().trim();
        if (userMessageContent.isEmpty()) return;

        UUID userMessageId = UUID.randomUUID();
        JSONObject userMessageJson = new JSONObject().put("role", "user").put("content", userMessageContent);
        currentSession.addMessage(new MessageEntry(userMessageId, userMessageJson));
        addMessage(new Message(userMessageId, "user", userMessageContent, true));
        userInput.set("");

        currentStreamingMessageId = UUID.randomUUID();
        addMessage(new Message(currentStreamingMessageId, "model", "思考中...", true));
        sendButtonDisabled.set(true);

        new Thread(() -> {
            try {
                List<JSONObject> apiMessages = currentSession.getMessageHistory().stream()
                        .map(MessageEntry::getMessage)
                        .collect(Collectors.toList());
                callApi(apiMessages);
            } catch (Exception e) {
                String errorMsg = "请求API出错: " + e.getMessage();
                addMessage(new Message(UUID.randomUUID(), "system", errorMsg, false));
                e.printStackTrace();
            } finally {
                Platform.runLater(() -> sendButtonDisabled.set(false));
            }
        }).start();
    }

    public void deleteMessage(UUID messageIdToDelete) {
        if (messageIdToDelete == null) return;
        currentSession.removeMessage(messageIdToDelete);
        chatMessages.removeIf(message -> message.getId().equals(messageIdToDelete));
    }

    public void addMessage(Message message) { Platform.runLater(() -> chatMessages.add(message)); }

    public void updateStreamingMessage(UUID messageId, String newContent) {
        Platform.runLater(() -> {
            chatMessages.stream()
                    .filter(m -> m.getId().equals(messageId))
                    .findFirst()
                    .ifPresent(message -> message.appendStreamingContent(newContent));
        });
    }

    private void callApi(List<JSONObject> currentMessages) throws Exception {
        URL url = new URL(API_BASE_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setDoOutput(true);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", MODEL);
        requestBody.put("messages", currentMessages);
        requestBody.put("stream", true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.toString().getBytes("utf-8"));
        }

        StringBuilder fullModelResponse = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    String jsonData = line.substring(6);
                    if ("[DONE]".equals(jsonData)) break;
                    JSONObject chunk = new JSONObject(jsonData);
                    if (chunk.has("choices")) {
                        JSONObject delta = chunk.getJSONArray("choices").getJSONObject(0).optJSONObject("delta");
                        if (delta != null && delta.has("content")) {
                            String content = delta.getString("content");
                            fullModelResponse.append(content);
                            updateStreamingMessage(currentStreamingMessageId, content);
                        }
                    }
                }
            }
        }

        if (fullModelResponse.length() > 0) {
            JSONObject modelMessageJson = new JSONObject().put("role", "assistant").put("content", fullModelResponse.toString());
            currentSession.addMessage(new MessageEntry(currentStreamingMessageId, modelMessageJson));
            saveCurrentSession();
        }
    }
    // #endregion

    // This class remains here as it's part of the ViewModel layer due to JavaFX properties.
    public static class Message {
        private final UUID id;
        private final String sender;
        private final String content;
        private final boolean deletable;
        private final StringProperty streamingContent;

        public Message(UUID id, String sender, String content, boolean deletable) {
            this.id = id; this.sender = sender; this.content = content; this.deletable = deletable;
            this.streamingContent = new SimpleStringProperty(content);
        }

        public UUID getId() { return id; }
        public String getSender() { return sender; }
        public String getContent() { return content; }
        public boolean isDeletable() { return deletable; }
        public StringProperty streamingContentProperty() { return streamingContent; }
        public void appendStreamingContent(String newContent) {
            if ("思考中...".equals(streamingContent.get())) {
                streamingContent.set(newContent);
            } else {
                streamingContent.set(streamingContent.get() + newContent);
            }
        }
    }
}