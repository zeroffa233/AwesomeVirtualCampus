package app.vcampus.client.viewmodel;

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
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class GptViewModel {

    // Properties for UI binding
    private final StringProperty userInput = new SimpleStringProperty("");
    private final ObservableList<Message> chatMessages = FXCollections.observableArrayList();
    private final BooleanProperty sendButtonDisabled = new SimpleBooleanProperty(false);

    // API related constants and fields
    private final String WELCOME_MESSAGE = "你好！我是AI助手，有什么可以帮你的吗？";
    private final String SYSTEM_PROMPT = "你是一个乐于助人的AI助手, 正在和一个学生宋兵甲对话。";
    private final String MODEL = "deepseek-v3.1";
    private final String API_KEY = "sk-588905851d1b4421ae51c9ad64fb120b"; // Replace with your API Key
    private final String API_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions"; // Replace with your API Base URL

    // Internal data structures
    private List<MessageEntry> apiMessageHistory = new ArrayList<>(); // Store dialogue history for API calls
    private UUID currentStreamingMessageId; // To link the streaming model response to its UUID

    public GptViewModel() {
        // Initialize with a welcome message
        addMessage(new Message(UUID.randomUUID(), "system", WELCOME_MESSAGE, false));
        // Add the initial system prompt to the API message list
        apiMessageHistory.add(new MessageEntry(UUID.randomUUID(), new JSONObject().put("role", "system").put("content", SYSTEM_PROMPT)));
    }

    public StringProperty userInputProperty() {
        return userInput;
    }

    public ObservableList<Message> getChatMessages() {
        return chatMessages;
    }

    public BooleanProperty sendButtonDisabledProperty() {
        return sendButtonDisabled;
    }

    /**
     * Represents a single message in the chat.
     */
    public static class Message {
        private final UUID id;
        private final String sender;
        private final String content;
        private final boolean deletable;
        private StringProperty streamingContent; // For model streaming responses

        public Message(UUID id, String sender, String content, boolean deletable) {
            this.id = id;
            this.sender = sender;
            this.content = content;
            this.deletable = deletable;
            this.streamingContent = new SimpleStringProperty(content);
        }

        public UUID getId() {
            return id;
        }

        public String getSender() {
            return sender;
        }

        public String getContent() {
            return content;
        }

        public boolean isDeletable() {
            return deletable;
        }

        public StringProperty streamingContentProperty() {
            return streamingContent;
        }

        public void appendStreamingContent(String newContent) {
            if (streamingContent.get().equals("思考中...")) {
                streamingContent.set(newContent);
                return;
            }
            streamingContent.set(streamingContent.get() + newContent);
        }
    }

    // Custom class to hold UUID and message JSONObject for API history
    private static class MessageEntry {
        UUID id;
        JSONObject message;

        public MessageEntry(UUID id, JSONObject message) {
            this.id = id;
            this.message = message;
        }

        public UUID getId() {
            return id;
        }

        public JSONObject getMessage() {
            return message;
        }
    }

    /**
     * Adds a message to the chat messages list (for UI display).
     *
     * @param message The message to add.
     */
    public void addMessage(Message message) {
        chatMessages.add(message);
    }

    /**
     * Updates the content of a streaming model response.
     *
     * @param messageId The ID of the message to update.
     * @param newContent The new content to append.
     */
    public void updateStreamingMessage(UUID messageId, String newContent) {
        Platform.runLater(() -> { // 将更新操作放到 JavaFX 线程
            for (int i = 0; i < chatMessages.size(); i++) {
                if (chatMessages.get(i).getId().equals(messageId)) {
                    chatMessages.get(i).appendStreamingContent(newContent);
                    break;
                }
            }
        });
    }

    /**
     * Handles sending a user message and initiating the API call.
     */
    public void sendMessage() {
        String userMessageContent = userInput.get().trim();
        if (userMessageContent.isEmpty()) {
            return;
        }

        // 1. User input content added to chat history and displayed
        UUID userMessageId = UUID.randomUUID();
        apiMessageHistory.add(new MessageEntry(userMessageId, new JSONObject().put("role", "user").put("content", userMessageContent)));
        addMessage(new Message(userMessageId, "user", userMessageContent, true));
        userInput.set(""); // Clear input field

        // 2. Create placeholder for model response and add to UI immediately
        currentStreamingMessageId = UUID.randomUUID();
        // Add a placeholder message to the UI that will be updated during streaming
        addMessage(new Message(currentStreamingMessageId, "model", "思考中...", true));

        sendButtonDisabled.set(true); // Disable send button

        new Thread(() -> {
            try {
                callApi(apiMessageHistory.stream().map(MessageEntry::getMessage).collect(Collectors.toList()));
                // After API call completes, re-enable send button
                sendButtonDisabled.set(false);
            } catch (Exception e) {
                // If an error occurs, display an error message
                addMessage(new Message(UUID.randomUUID(), "system", "请求API出错: " + e.getMessage(), false));
                sendButtonDisabled.set(false); // Re-enable send button
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Handles the deletion of a message from both the UI and the message history.
     *
     * @param messageIdToDelete The UUID of the message to delete.
     */
    public void deleteMessage(UUID messageIdToDelete) {
        if (messageIdToDelete == null) {
            System.err.println("Attempted to delete a message without a valid UUID.");
            return;
        }

        // 1. Remove from the API messages list
        apiMessageHistory.removeIf(entry -> entry.getId().equals(messageIdToDelete));

        // 2. Remove from the displayed chat messages
        chatMessages.removeIf(message -> message.getId().equals(messageIdToDelete));
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
            byte[] input = requestBody.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        StringBuilder fullModelResponse = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                if (responseLine.startsWith("data: ")) {
                    String jsonData = responseLine.substring(6);
                    if (jsonData.equals("[DONE]")) {
                        break;
                    }
                    JSONObject chunk = new JSONObject(jsonData);
                    if (chunk.has("choices")) {
                        JSONObject choice = chunk.getJSONArray("choices").getJSONObject(0);
                        if (choice.has("delta")) {
                            JSONObject delta = choice.getJSONObject("delta");
                            if (delta.has("content")) {
                                String content = delta.getString("content");
                                if (content != null && !content.isEmpty()) {
                                    fullModelResponse.append(content);
                                    // Update the streaming message in the ViewModel, which will notify the UI
                                    updateStreamingMessage(currentStreamingMessageId, content);
                                }
                            }
                        }
                    }
                }
            }
        }
        // After API call completes, add the full model response to the dialogue history
        apiMessageHistory.add(new MessageEntry(currentStreamingMessageId, new JSONObject().put("role", "assistant").put("content", fullModelResponse.toString())));
    }
}