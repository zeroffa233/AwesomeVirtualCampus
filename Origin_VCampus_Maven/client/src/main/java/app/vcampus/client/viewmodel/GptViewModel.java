package app.vcampus.client.viewmodel;

import app.vcampus.client.gateway.GptClient;
import app.vcampus.client.util.ChatSession;
import app.vcampus.client.util.ChatSession.ChatSessionSummary;
import app.vcampus.client.util.MessageEntry;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.json.JSONObject;
import java.util.Comparator;

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
    private final ObjectProperty<ChatSession> currentSessionProperty = new SimpleObjectProperty<>();

    // API related constants
    private final String WELCOME_MESSAGE = "你好！我是AI助手，有什么可以帮你的吗？";
    private final String SYSTEM_PROMPT = "你是一个乐于助人的AI助手Assistant-DeepSeek, 正在和一个学生对话，今天是9月10日。";
    private final String MODEL = "deepseek-v3.1";
    private final String API_KEY = "sk-588905851d1b4421ae51c9ad64fb120b";
    private final String API_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    // Data Gateway
    private final GptClient gptClient = GptClient.getInstance();
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

    public void deleteSession(UUID sessionIdToDelete) {
        if (sessionIdToDelete == null) return;

        boolean wasActiveSession = currentSessionProperty.get() != null && currentSessionProperty.get().getId().equals(sessionIdToDelete);

        gptClient.deleteChatSession(sessionIdToDelete);

        if (wasActiveSession) {
            currentSessionProperty.set(null);
            createNewSession();
        }

        loadChatHistorySummaries();
    }

    // #region Properties for UI Binding
    public StringProperty userInputProperty() { return userInput; }
    public ObservableList<Message> getChatMessages() { return chatMessages; }
    public BooleanProperty sendButtonDisabledProperty() { return sendButtonDisabled; }
    public ObservableList<ChatSessionSummary> getChatHistory() { return chatHistory; }
    public ReadOnlyObjectProperty<ChatSession> currentSessionProperty() { return currentSessionProperty; }
    // #endregion

    // #region Session Management Core Logic
    public void createNewSession() {
        if (currentSessionProperty.get() != null && currentSessionProperty.get().getMessageHistory().size() > 1) {
            saveCurrentSession();
        }

        ChatSession newSession = new ChatSession(UUID.randomUUID());
        newSession.addMessage(new MessageEntry(UUID.randomUUID(), new JSONObject().put("role", "system").put("content", SYSTEM_PROMPT)));
        currentSessionProperty.set(newSession);

        rebuildChatDisplay();
        addMessage(new Message(UUID.randomUUID(), "system", WELCOME_MESSAGE, false));
        loadChatHistorySummaries();
    }

    public void loadSession(UUID sessionId) {
        if (currentSessionProperty.get() != null && currentSessionProperty.get().getId().equals(sessionId)) {
            return;
        }
        if (currentSessionProperty.get() != null && currentSessionProperty.get().getMessageHistory().size() > 1) {
            saveCurrentSession();
        }

        try {
            // 加载新会话
            ChatSession loadedSession = gptClient.loadChatSession(sessionId);
            if (loadedSession != null) {
                currentSessionProperty.set(loadedSession);
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
        ChatSession session = currentSessionProperty.get();
        if (session == null || session.getMessageHistory().size() <= 1) {
            return; // Don't save empty sessions
        }
        session.updateTitle();
        session.setLastModified(System.currentTimeMillis());
        gptClient.saveChatSession(session);
        loadChatHistorySummaries(); // Refresh the list with updated title/order
    }

    private void loadChatHistorySummaries() {
        Platform.runLater(() -> {
            List<ChatSessionSummary> summaries = gptClient.getChatHistorySummaries();
            // 【修改】按 lastModified 时间戳进行降序（最新优先）排序
            summaries.sort(Comparator.comparing(ChatSession.ChatSessionSummary::getLastModified).reversed());
            chatHistory.setAll(summaries);
        });
    }

    private void rebuildChatDisplay() {
        Platform.runLater(() -> {
            chatMessages.clear();
            List<MessageEntry> history = currentSessionProperty.get().getMessageHistory();
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
        currentSessionProperty.get().addMessage(new MessageEntry(userMessageId, userMessageJson));
        addMessage(new Message(userMessageId, "user", userMessageContent, true));
        userInput.set("");

        currentStreamingMessageId = UUID.randomUUID();
        addMessage(new Message(currentStreamingMessageId, "model", "思考中...", true));
        sendButtonDisabled.set(true);

        new Thread(() -> {
            try {
                List<JSONObject> apiMessages = currentSessionProperty.get().getMessageHistory().stream()
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
        currentSessionProperty.get().removeMessage(messageIdToDelete);
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
            currentSessionProperty.get().addMessage(new MessageEntry(currentStreamingMessageId, modelMessageJson));
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
