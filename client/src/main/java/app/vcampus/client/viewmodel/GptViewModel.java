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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture; // 【修改】导入 CompletableFuture
import java.util.stream.Collectors;

public class GptViewModel {

    // Properties for UI binding
    private final StringProperty userInput = new SimpleStringProperty("");
    private final ObservableList<Message> chatMessages = FXCollections.observableArrayList();
    private final BooleanProperty sendButtonDisabled = new SimpleBooleanProperty(false);
    private final ObservableList<ChatSessionSummary> chatHistory = FXCollections.observableArrayList();
    private final ObjectProperty<ChatSession> currentSessionProperty = new SimpleObjectProperty<>();

    // 【修改】将 HttpClient 声明为成员变量以便复用
    private final HttpClient httpClient = HttpClient.newHttpClient();

    // 【修改】将天气获取方法改为异步，并返回 CompletableFuture
    private CompletableFuture<String> fetchWeatherAsync() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.open-meteo.com/v1/forecast?latitude=31.89&longitude=118.82&current=weather_code"))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    // 当HTTP请求成功后，此代码块执行
                    JSONObject jsonResponse = new JSONObject(response.body());
                    int weatherCode = jsonResponse.getJSONObject("current").getInt("weather_code");
                    return WMO_CODE_MAP.getOrDefault(weatherCode, "未知天气");
                })
                .exceptionally(e -> {
                    // 当发生异常时（如网络错误），此代码块执行
                    e.printStackTrace();
                    return "获取失败";
                });
    }

    // 【修改】将课程表摘要的获取方法也改为异步
    private CompletableFuture<String> fetchScheduleSummaryAsync() {
        // 使用 supplyAsync 在后台线程池中执行耗时操作
        return CompletableFuture.supplyAsync(() -> {
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
        });
    }


    // API related constants
    private final String WELCOME_MESSAGE = "你好！我是AI助手，有什么可以帮你的吗？";

    // 【修改】SYSTEM_PROMPT 变为非 final 成员变量，将在异步加载数据后进行初始化
    private String SYSTEM_PROMPT;

    private final String MODEL = "deepseek-v3.1";
    private final String API_KEY = "sk-588905851d1b4421ae51c9ad64fb120b";
    private final String API_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    // Data Gateway
    public final GptClient gptClient = GptClient.getInstance();
    private UUID currentStreamingMessageId;


    // 【修改】重写初始化方法，以支持异步加载
    public void initializeSession() {
        // UI立即响应，显示加载信息
        Platform.runLater(() -> {
            chatMessages.clear();
            addMessage(new Message(UUID.randomUUID(), "system", "正在初始化AI助手，请稍候...", false));
        });

        // 异步获取天气和课程表信息
        CompletableFuture<String> weatherFuture = fetchWeatherAsync();
        CompletableFuture<String> scheduleFuture = fetchScheduleSummaryAsync();

        // 等待所有异步任务完成后再继续
        CompletableFuture.allOf(weatherFuture, scheduleFuture).thenRun(() -> {
            // .join() 在这里是安全的，因为我们已经处于 thenRun 的回调中，表示 Future 已完成
            String weather = weatherFuture.join();
            String schedule = scheduleFuture.join();

            // 构建 SYSTEM_PROMPT
            this.SYSTEM_PROMPT = "你是一个基于Deepseek的人工智能助手，运行在VCampus虚拟校园管理应用上，该应用负责管理东南大学本科生校园生活的大大小小的事务，功能涵盖课表查询与管理、学籍信息查询、校园通知推送、教务事务办理、校园服务预约、学习资源获取、社交互动、个人中心设置等。\n" +
                    " \n" +
                    "当前用户学籍信息包含：姓名"+ FakeRepository.user.name +"、性别"+FakeRepository.user.gender+"、一卡通号"+FakeRepository.user.cardNum+"、联系电话"+FakeRepository.user.phone+"、电子邮箱"+FakeRepository.user.email+"\n" +
                    " \n" +
                    "本学期于2025年8月25日星期一开学（第一周）当前用户课表： "+ schedule +"\n"+
                    " \n" +
                    "现在是"+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd E HH:mm:ss", Locale.CHINA))+"，天气"+ weather +"，目前校区内人流量基本正常，图书馆人流较高。"+
                    " \n" +
                    "你需基于上述信息，为用户提供精准、便捷的校园服务支持，例如解答课表疑问、协助查询学籍相关证明办理流程、推送用户所在学院的最新通知、指导预约图书馆自习座位或实验室使用权限等，同时严格保护用户个人信息安全，不泄露非公开的学籍与课表细节。\n"+
                    "";

            // 所有准备工作完成，回到 UI 线程进行后续的会话加载
            Platform.runLater(() -> {
                // 移除加载信息
                chatMessages.removeIf(m -> "正在初始化AI助手，请稍候...".equals(m.getContent()));

                loadChatHistorySummaries();
                List<ChatSessionSummary> history = gptClient.getChatHistorySummaries();
                if (!history.isEmpty()) {
                    loadSession(history.get(0).getId());
                } else {
                    createNewSession();
                }
            });

        });
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
        // 【注意】确保此方法在 SYSTEM_PROMPT 初始化之后被调用
        if (SYSTEM_PROMPT == null) {
            System.err.println("错误：SYSTEM_PROMPT 尚未初始化！");
            // 可以添加一个临时的或者错误的提示
            addMessage(new Message(UUID.randomUUID(), "system", "AI助手初始化失败，请稍后重试。", false));
            return;
        }
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

    private static final Map<Integer, String> WMO_CODE_MAP = Map.ofEntries(
            Map.entry(0, "晴天"),
            Map.entry(1, "大体晴朗"),
            Map.entry(2, "局部多云"),
            Map.entry(3, "多云"),
            Map.entry(4, "烟"),
            Map.entry(5, "霾"),
            Map.entry(6, "悬浮尘"),
            Map.entry(7, "风沙"),
            Map.entry(8, "尘卷风"),
            Map.entry(9, "沙尘暴"),
            Map.entry(10, "薄雾"),
            Map.entry(11, "浅雾"),
            Map.entry(12, "持续浅雾"),
            Map.entry(13, "闪电"),
            Map.entry(14, "降水，未及地面"),
            Map.entry(15, "远处有降水"),
            Map.entry(16, "附近有降水"),
            Map.entry(17, "雷暴，无降水"),
            Map.entry(18, "飑"),
            Map.entry(19, "漏斗云"),
            Map.entry(20, "毛毛雨"),
            Map.entry(21, "雨"),
            Map.entry(22, "雪"),
            Map.entry(23, "雨夹雪或冰粒"),
            Map.entry(24, "冻毛毛雨或冻雨"),
            Map.entry(25, "阵雨"),
            Map.entry(26, "阵雪或雨夹雪"),
            Map.entry(27, "阵性冰雹或雨夹雹"),
            Map.entry(28, "雾或冰雾"),
            Map.entry(29, "雷暴（伴有或无降水）"),
            Map.entry(30, "轻度或中度沙尘暴，过去一小时减弱"),
            Map.entry(31, "轻度或中度沙尘暴，过去一小时无明显变化"),
            Map.entry(32, "轻度或中度沙塵暴，过去一小时开始或增强"),
            Map.entry(33, "强度沙尘暴，过去一小时减弱"),
            Map.entry(34, "强度沙尘暴，过去一小时无明显变化"),
            Map.entry(35, "强度沙尘暴，过去一小时开始或增强"),
            Map.entry(36, "轻度或中度吹雪（低于视线）"),
            Map.entry(37, "强度吹雪（低于视线）"),
            Map.entry(38, "轻度或中度吹雪（高于视线）"),
            Map.entry(39, "强度吹雪（高于视线）"),
            Map.entry(40, "远方有雾或冰雾"),
            Map.entry(41, "片状雾或冰雾"),
            Map.entry(42, "雾或冰雾，天空可见，过去一小时变薄"),
            Map.entry(43, "雾或冰雾，天空不可见"),
            Map.entry(44, "雾或冰雾，天空可见，过去一小时无明显变化"),
            Map.entry(45, "雾或冰雾，天空不可见"),
            Map.entry(46, "雾或冰雾，天空可见，过去一小时开始或变浓"),
            Map.entry(47, "雾或冰雾，天空不可见"),
            Map.entry(48, "雾，沉积白霜，天空可见"),
            Map.entry(49, "雾，沉积白霜，天空不可见"),
            Map.entry(50, "间歇性小毛毛雨"),
            Map.entry(51, "持续性小毛毛雨"),
            Map.entry(52, "间歇性中等毛毛雨"),
            Map.entry(53, "持续性中等毛毛雨"),
            Map.entry(54, "间歇性大毛毛雨"),
            Map.entry(55, "持续性大毛毛雨"),
            Map.entry(56, "轻微冻毛毛雨"),
            Map.entry(57, "中等或大冻毛毛雨"),
            Map.entry(58, "小雨和毛毛雨"),
            Map.entry(59, "中等或大雨和毛毛雨"),
            Map.entry(60, "间歇性小雨"),
            Map.entry(61, "持续性小雨"),
            Map.entry(62, "间歇性中雨"),
            Map.entry(63, "持续性中雨"),
            Map.entry(64, "间歇性大雨"),
            Map.entry(65, "持续性大雨"),
            Map.entry(66, "轻微冻雨"),
            Map.entry(67, "中等或大冻雨"),
            Map.entry(68, "小雨或毛毛雨夹雪"),
            Map.entry(69, "中等或大雨或毛毛雨夹雪"),
            Map.entry(70, "间歇性小雪"),
            Map.entry(71, "持续性小雪"),
            Map.entry(72, "间歇性中雪"),
            Map.entry(73, "持续性中雪"),
            Map.entry(74, "间歇性大雪"),
            Map.entry(75, "持续性大雪"),
            Map.entry(76, "冰晶（有或无雾）"),
            Map.entry(77, "雪粒（有或无雾）"),
            Map.entry(78, "孤立的星状雪晶（有或无雾）"),
            Map.entry(79, "冰粒"),
            Map.entry(80, "小阵雨"),
            Map.entry(81, "中等或大阵雨"),
            Map.entry(82, "猛烈阵雨"),
            Map.entry(83, "轻微雨夹雪阵雨"),
            Map.entry(84, "中等或大雨夹雪阵雨"),
            Map.entry(85, "小阵雪"),
            Map.entry(86, "中等或大阵雪"),
            Map.entry(87, "小雪珠或小冰雹阵雨，有或无雨或雨夹雪"),
            Map.entry(88, "中等或大雪珠或小冰雹阵雨，有或无雨或雨夹雪"),
            Map.entry(89, "无雷阵性冰雹，有或无雨或雨夹雪 - 轻微"),
            Map.entry(90, "无雷阵性冰雹，有或无雨或雨夹雪 - 中等或大"),
            Map.entry(91, "观测时有小雨，前一小时有雷暴"),
            Map.entry(92, "观测时有中等或大雨，前一小时有雷暴"),
            Map.entry(93, "观测时有小雪、雨夹雪或冰雹，前一小时有雷暴"),
            Map.entry(94, "观测时有中等或大雪、雨夹雪或冰雹，前一小时有雷暴"),
            Map.entry(95, "轻度或中度雷暴，无冰雹，但有雨和/或雪"),
            Map.entry(96, "轻度或中度雷暴，有冰雹"),
            Map.entry(97, "强度雷暴，无冰雹，但有雨和/或雪"),
            Map.entry(98, "伴有沙尘暴的雷暴"),
            Map.entry(99, "伴有冰雹的强雷暴")
    );
}