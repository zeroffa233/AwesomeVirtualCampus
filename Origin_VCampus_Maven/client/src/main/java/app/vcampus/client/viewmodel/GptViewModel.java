package app.vcampus.client.viewmodel;

import app.vcampus.client.gateway.GptClient;
import app.vcampus.client.gateway.TeachingAffairsClient;
import app.vcampus.client.repository.FakeRepository;
import app.vcampus.server.entity.TeachingClass;
import app.vcampus.server.utility.ChatSession;
import app.vcampus.server.utility.ChatSession.ChatSessionSummary;
import app.vcampus.server.utility.MessageEntry;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import static app.vcampus.client.gateway.TeachingAffairsClient.getSelectedClasses;

public class GptViewModel {

    // Properties for UI binding
    private final StringProperty userInput = new SimpleStringProperty("");
    private final ObservableList<Message> chatMessages = FXCollections.observableArrayList();
    private final BooleanProperty sendButtonDisabled = new SimpleBooleanProperty(false);
    private final ObservableList<ChatSessionSummary> chatHistory = FXCollections.observableArrayList();
    private final ObjectProperty<ChatSession> currentSessionProperty = new SimpleObjectProperty<>();
    /**
     * 初始化课程表摘要的私有辅助方法。
     * @return 代表课程安排的字符串，或者在没有课程时返回 "无"。
     */
    private String initializeScheduleSummary() {
        TeachingAffairsClient client = new TeachingAffairsClient();

        // 1. 首先尝试获取 getSelectedClasses
        List<TeachingClass> teachingClasses = TeachingAffairsClient.getSelectedClasses(FakeRepository.handler);

        // 2. 如果结果为 null 或者列表为空，则尝试 getMyTeachingClass
        if (teachingClasses == null || teachingClasses.isEmpty()) {
            teachingClasses = TeachingAffairsClient.getMyTeachingClasses(FakeRepository.handler);
        }

        // 3. 如果第二次尝试后依然为 null 或空，则返回 "无"
        if (teachingClasses == null || teachingClasses.isEmpty()) {
            return "无";
        }

        // 4. 如果获取到了课程列表，则进行处理并返回结果
        return teachingClasses.stream()
                .map(TeachingClass::humanReadableSchedule)
                .collect(Collectors.joining(","));
    }


    // API related constants
    private final String WELCOME_MESSAGE = "你好！我是AI助手，有什么可以帮你的吗？";
    private final String SYSTEM_PROMPT = "你是一个基于Deepseek的人工智能助手，运行在VCampus虚拟校园管理应用上，该应用负责管理东南大学本科生校园生活的大大小小的事务，功能涵盖课表查询与管理、学籍信息查询、校园通知推送、教务事务办理、校园服务预约、学习资源获取、社交互动、个人中心设置等。\n" +
            " \n" +
            "当前用户学籍信息包含：姓名"+ FakeRepository.user.name +"、性别"+FakeRepository.user.gender+"、一卡通号"+FakeRepository.user.cardNum+"、联系电话"+FakeRepository.user.phone+"、电子邮箱"+FakeRepository.user.email+"\n" +
            " \n" +
            "本学期于9月22日星期一开学（第一周）当前用户课表： "+ initializeScheduleSummary() +"\n"+
            " \n" +
            "现在是"+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd E HH:mm:ss", Locale.CHINA))+"，天气晴，目前校区内人流量基本正常，图书馆人流较高。"+
            " \n" +
            "你需基于上述信息，为用户提供精准、便捷的校园服务支持，例如解答课表疑问、协助查询学籍相关证明办理流程、推送用户所在学院的最新通知、指导预约图书馆自习座位或实验室使用权限等，同时严格保护用户个人信息安全，不泄露非公开的学籍与课表细节。";
    private final String MODEL = "deepseek-v3.1";
    private final String API_KEY = "sk-588905851d1b4421ae51c9ad64fb120b";
    private final String API_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    // Data Gateway
    public final GptClient gptClient = GptClient.getInstance();
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
    public ObservableList<app.vcampus.server.utility.ChatSession.ChatSessionSummary> getChatHistory() { return chatHistory; }
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
