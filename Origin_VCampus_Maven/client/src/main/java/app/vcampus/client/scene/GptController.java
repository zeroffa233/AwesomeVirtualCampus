package app.vcampus.client.scene;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;

import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import javafx.event.ActionEvent;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javafx.scene.input.KeyCode; // 导入 KeyCode

//TODO: 网络持久化?

public class GptController implements Initializable {
    @FXML
    private ScrollPane chatScrollPane;
    @FXML
    private VBox chatDisplayArea;
    @FXML
    private JFXTextArea userInputField;
    @FXML
    private JFXButton sendButton;

    private Text currentModelResponseText;
    private UUID currentModelMessageUuid; // To link the streaming model response to its UUID

    private final String WELCOME_MESSAGE = "你好！我是AI助手，有什么可以帮你的吗？";
    private final String SYSTEM_PROMPT = "你是一个乐于助人的AI助手, 正在和一个学生宋兵甲对话。";
    private final String MODEL = "deepseek-v3.1";
    private final String API_KEY = "sk-588905851d1b4421ae51c9ad64fb120b"; // 替换为你的API Key
    private final String API_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions"; // 替换为你的API Base URL

    // Custom class to hold UUID and message JSONObject
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

    private List<MessageEntry> messages = new ArrayList<>(); // Store dialogue history for API calls
    private Map<UUID, HBox> displayedMessageNodes = new HashMap<>(); // Link UUID to displayed HBox for removal

    private static final double MESSAGE_MAX_WIDTH_PERCENT = 0.7; // 70% 的聊天区域宽度
    private static final double MESSAGE_MIN_WIDTH = 50.0; // 最小宽度，例如50像素

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize with a welcome message (not deletable)
        addMessageToDisplay("system", WELCOME_MESSAGE, null, false);
        // Add the initial system prompt to the API message list (not displayed as a separate deletable bubble)
        messages.add(new MessageEntry(UUID.randomUUID(), new JSONObject().put("role", "system").put("content", SYSTEM_PROMPT)));
        // Auto-scroll to the bottom
        chatScrollPane.vvalueProperty().bind(chatDisplayArea.heightProperty());

        // 添加键盘事件监听器以处理 Ctrl + Enter
        userInputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && event.isControlDown()) {
                sendMessage();
                event.consume(); // 消费事件，防止在JFXTextArea中插入换行符
            }
        });
    }

    @FXML
    private void sendMessage() {
        String userMessage = userInputField.getText().trim();
        if (userMessage.isEmpty()) {
            return;
        }

        // 1. User input content added to chat history and displayed
        UUID userMessageUuid = UUID.randomUUID();
        messages.add(new MessageEntry(userMessageUuid, new JSONObject().put("role", "user").put("content", userMessage)));
        addMessageToDisplay("user", userMessage, userMessageUuid, true); // User message is deletable
        userInputField.clear();

        // 2. Create placeholder for model response and add to UI immediately
        currentModelMessageUuid = UUID.randomUUID(); // Generate UUID for the model response

        Platform.runLater(() -> {
            HBox modelMessageContainer = new HBox();
            modelMessageContainer.setPadding(new javafx.geometry.Insets(5));
            modelMessageContainer.setAlignment(Pos.CENTER_LEFT); // Model messages are left-aligned
            modelMessageContainer.setUserData(currentModelMessageUuid); // Link HBox to UUID
            displayedMessageNodes.put(currentModelMessageUuid, modelMessageContainer); // Add to map for quick lookup

            currentModelResponseText = new Text("思考中..."); // Placeholder text
            TextFlow textFlow = new TextFlow(currentModelResponseText);
            textFlow.setMaxWidth(chatDisplayArea.getWidth() * MESSAGE_MAX_WIDTH_PERCENT);
            textFlow.setMinWidth(MESSAGE_MIN_WIDTH);
            textFlow.setPadding(new javafx.geometry.Insets(8));
            textFlow.setStyle("-fx-border-radius: 10px; -fx-background-radius: 10px;");
            currentModelResponseText.setStyle("-fx-fill: white;");
            textFlow.setStyle(textFlow.getStyle() + "-fx-background-color: #4EB052;");

            JFXButton deleteButton = new JFXButton("X");
            deleteButton.setStyle("-fx-background-color: #ff6666; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 0 5 0 5; -fx-min-width: 25px; -fx-max-width: 25px; -fx-min-height: 25px; -fx-max-height: 25px; -fx-background-radius: 12.5; -fx-border-radius: 12.5;");
            deleteButton.setUserData(currentModelMessageUuid); // Link button to UUID
            deleteButton.setOnAction(this::deleteMessage);

            HBox buttonWrapper = new HBox(deleteButton);
            buttonWrapper.setAlignment(Pos.CENTER);
            buttonWrapper.setPadding(new javafx.geometry.Insets(0, 0, 0, 5)); // Padding to the left of the button

            modelMessageContainer.getChildren().addAll(textFlow, buttonWrapper);
            chatDisplayArea.getChildren().add(modelMessageContainer);
        });

        // Call API to get model response
        sendButton.setDisable(true); // Disable send button to prevent multiple sends
        new Thread(() -> {
            try {
                // Pass only the JSONObject messages to the API
                callApi(messages.stream().map(MessageEntry::getMessage).collect(Collectors.toList()));
                Platform.runLater(() -> {
                    // Re-enable send button after API call completes
                    sendButton.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    // If an error occurs, display an error message
                    addMessageToDisplay("system", "请求API出错: " + e.getMessage(), null, false);
                    sendButton.setDisable(false); // Re-enable send button
                });
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Adds a message to the chat display area.
     *
     * @param sender    The sender of the message ("user", "model", or "system").
     * @param message   The content of the message.
     * @param messageId The UUID of the message. Can be null for non-deletable messages.
     * @param deletable True if a delete button should be added, false otherwise.
     */
    private void addMessageToDisplay(String sender, String message, UUID messageId, boolean deletable) {
        Platform.runLater(() -> {
            HBox messageContainer = new HBox();
            messageContainer.setPadding(new javafx.geometry.Insets(5));
            messageContainer.setUserData(messageId); // Store UUID in HBox for potential future use or consistency

            Text messageText = new Text(message);
            TextFlow textFlow = new TextFlow(messageText);
            textFlow.setMaxWidth(chatDisplayArea.getWidth() * MESSAGE_MAX_WIDTH_PERCENT);
            textFlow.setMinWidth(MESSAGE_MIN_WIDTH);
            textFlow.setPadding(new javafx.geometry.Insets(8));
            textFlow.setStyle("-fx-border-radius: 10px; -fx-background-radius: 10px;");

            if ("user".equals(sender)) {
                messageContainer.setAlignment(Pos.CENTER_RIGHT);
                messageText.setStyle("-fx-fill: white;");
                textFlow.setStyle(textFlow.getStyle() + "-fx-background-color: #007bff;");
            } else if ("model".equals(sender)) {
                // Model messages are primarily handled by the placeholder creation in sendMessage()
                // This branch would be for non-streaming model responses added directly here.
                messageContainer.setAlignment(Pos.CENTER_LEFT);
                messageText.setStyle("-fx-fill: white;");
                textFlow.setStyle(textFlow.getStyle() + "-fx-background-color: #4EB052;");
            } else { // "system" messages (like welcome or error messages)
                messageContainer.setAlignment(Pos.CENTER);
                messageText.setStyle("-fx-fill: gray; -fx-font-style: italic;");
                textFlow.setStyle(textFlow.getStyle() + "-fx-background-color: transparent;"); // System messages usually no background
                textFlow.setPadding(new javafx.geometry.Insets(0)); // Remove padding for system messages
            }

            if (deletable && messageId != null) {
                JFXButton deleteButton = new JFXButton("X");
                deleteButton.setStyle("-fx-background-color: #ff6666; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 0 5 0 5; -fx-min-width: 25px; -fx-max-width: 25px; -fx-min-height: 25px; -fx-max-height: 25px; -fx-background-radius: 12.5; -fx-border-radius: 12.5;");
                deleteButton.setUserData(messageId);
                deleteButton.setOnAction(this::deleteMessage);

                HBox buttonWrapper = new HBox(deleteButton);
                buttonWrapper.setAlignment(Pos.CENTER);

                if ("user".equals(sender)) { // For user (right-aligned) messages, put delete button before textFlow
                    buttonWrapper.setPadding(new javafx.geometry.Insets(0, 5, 0, 0)); // Padding to the right of the button
                    messageContainer.getChildren().addAll(buttonWrapper, textFlow);
                } else { // For model (left-aligned) messages, put delete button after textFlow
                    buttonWrapper.setPadding(new javafx.geometry.Insets(0, 0, 0, 5)); // Padding to the left of the button
                    messageContainer.getChildren().addAll(textFlow, buttonWrapper);
                }
            } else { // Not deletable or no UUID
                messageContainer.getChildren().add(textFlow);
            }

            chatDisplayArea.getChildren().add(messageContainer);
            if (messageId != null) {
                displayedMessageNodes.put(messageId, messageContainer);
            }
        });
    }

    /**
     * Handles the deletion of a message from both the UI and the message history.
     *
     * @param event The ActionEvent triggered by the delete button.
     */
    private void deleteMessage(ActionEvent event) {
        JFXButton sourceButton = (JFXButton) event.getSource();
        UUID messageIdToDelete = (UUID) sourceButton.getUserData();

        if (messageIdToDelete == null) {
            System.err.println("Attempted to delete a message without a valid UUID.");
            return;
        }

        Platform.runLater(() -> {
            // 1. Remove from the API messages list
            messages.removeIf(entry -> entry.getId().equals(messageIdToDelete));

            // 2. Remove the message's HBox from the displayed UI
            HBox messageHBox = displayedMessageNodes.get(messageIdToDelete);
            if (messageHBox != null) {
                chatDisplayArea.getChildren().remove(messageHBox);
                displayedMessageNodes.remove(messageIdToDelete);
            } else {
                System.err.println("Could not find HBox for message ID: " + messageIdToDelete);
            }
        });
    }

    // Updates the model response text dynamically as it streams
    private void updateModelResponseStream(String newContent) {
        Platform.runLater(() -> {
            if (currentModelResponseText.getText().equals("思考中...")) {
                currentModelResponseText.setText(""); // Clear placeholder
            }
            if (currentModelResponseText != null) {
                currentModelResponseText.setText(currentModelResponseText.getText() + newContent);
            }
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
        requestBody.put("model", MODEL); // Select the appropriate model for your API
        requestBody.put("messages", currentMessages); // Send the entire dialogue history
        requestBody.put("stream", true); // Request streaming response

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        StringBuilder fullModelResponse = new StringBuilder(); // Accumulate the complete model response
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
                                    updateModelResponseStream(content); // Stream update UI
                                }
                            }
                        }
                    }
                }
            }
        }
        // After API call completes, add the full model response to the dialogue history
        // Use the stored currentModelMessageUuid
        messages.add(new MessageEntry(currentModelMessageUuid, new JSONObject().put("role", "assistant").put("content", fullModelResponse.toString())));
    }
}