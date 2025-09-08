package app.vcampus.client.scene;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTextArea;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
//TODO: 删除消息

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

    private final String WELCOME_MESSAGE = "你好！我是AI助手，有什么可以帮你的吗？";
    private final String SYSTEM_PROMPT = "你是一个乐于助人的AI助手, 正在和";
    private final String MODEL = "deepseek-v3.1";
    private final String API_KEY = "sk-588905851d1b4421ae51c9ad64fb120b"; // 替换为你的API Key
    private final String API_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions"; // 替换为你的API Base URL
    private List<JSONObject> messages = new ArrayList<>(); // 存储对话历史

    private static final double MESSAGE_MAX_WIDTH_PERCENT = 0.7; // 70% 的聊天区域宽度
    private static final double MESSAGE_MIN_WIDTH = 50.0; // 最小宽度，例如50像素
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化，可以加载一些默认消息或者设置UI样式
        addMessageToChat("system", WELCOME_MESSAGE);
        messages.add(new JSONObject().put("role", "system").put("content", SYSTEM_PROMPT));
        // 自动将ScrollPane滚动到底部 [8]
        chatScrollPane.vvalueProperty().bind(chatDisplayArea.heightProperty());
    }

    @FXML
    private void sendMessage() {
        String userMessage = userInputField.getText().trim();
        if (userMessage.isEmpty()) {
            return;
        }

        // 1. 用户输入内容追加到聊天记录最末端并更新显示
        addMessageToChat("user", userMessage);
        userInputField.clear();

        // 将用户消息添加到历史记录
        messages.add(new JSONObject().put("role", "user").put("content", userMessage));

        // 2. 为模型回应创建占位符并立即添加到UI
        // 这里我们先添加一个空的或加载中的消息，之后会流式更新
        Platform.runLater(() -> {
            HBox modelMessageContainer = new HBox();
            modelMessageContainer.setPadding(new javafx.geometry.Insets(5));
            modelMessageContainer.setAlignment(Pos.CENTER_LEFT); // 模型消息左对齐

            currentModelResponseText = new Text("思考中..."); // 占位符
            TextFlow textFlow = new TextFlow(currentModelResponseText);
            textFlow.setMaxWidth(chatDisplayArea.getWidth() * MESSAGE_MAX_WIDTH_PERCENT); // 设置最大宽度
            textFlow.setMinWidth(MESSAGE_MIN_WIDTH);
            textFlow.setPadding(new javafx.geometry.Insets(8)); // 统一padding
            textFlow.setStyle("-fx-border-radius: 10px; -fx-background-radius: 10px;"); // 统一圆角

            // 设置模型消息的特定样式
            currentModelResponseText.setStyle("-fx-fill: white;"); // 模型回应文本颜色
            textFlow.setStyle(textFlow.getStyle() + "-fx-background-color: #4EB052;"); // 使用你指定的背景色

            modelMessageContainer.getChildren().add(textFlow);
            chatDisplayArea.getChildren().add(modelMessageContainer);
        });


        // 调用API获取模型回应
        sendButton.setDisable(true); // 禁用发送按钮，防止重复发送
        new Thread(() -> {
            try {
                callApi(messages); // 修改后的 callApi 方法将直接更新UI
                Platform.runLater(() -> {
                    // API调用完成后，启用发送按钮
                    sendButton.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    // 如果发生错误，显示错误信息
                    addMessageToChat("system", "请求API出错: " + e.getMessage());
                    sendButton.setDisable(false); // 启用发送按钮
                });
                e.printStackTrace();
            }
        }).start();
    }

    private HBox createMessageContainer(String sender) {
        HBox messageContainer = new HBox();
        messageContainer.setPadding(new javafx.geometry.Insets(5));

        if ("user".equals(sender)) {
            messageContainer.setAlignment(Pos.CENTER_RIGHT);
        } else if ("model".equals(sender)) {
            messageContainer.setAlignment(Pos.CENTER_LEFT);
        } else { // system 消息
            messageContainer.setAlignment(Pos.CENTER);
        }
        return messageContainer;
    }

    private void addMessageToChat(String sender, String message) {
        Platform.runLater(() -> {
            HBox messageContainer = createMessageContainer(sender);

            // 使用TextFlow来更好地处理文本换行和样式 [18]
            Text messageText = new Text(message);
            TextFlow textFlow = new TextFlow(messageText);
            textFlow.setMaxWidth(chatDisplayArea.getWidth() * MESSAGE_MAX_WIDTH_PERCENT); // 设置最大宽度
            textFlow.setMinWidth(MESSAGE_MIN_WIDTH);
            System.out.println(sender);
            if ("user".equals(sender)) {
                messageText.setStyle("-fx-fill: white;");
                textFlow.setStyle("-fx-background-color: #007bff; -fx-padding: 8px; -fx-border-radius: 10px; -fx-background-radius: 10px;");
            } else { // system 消息
                messageText.setStyle("-fx-fill: gray; -fx-font-style: italic;");
            }

            messageContainer.getChildren().add(textFlow);
            chatDisplayArea.getChildren().add(messageContainer); // VBox的getChildren().add()方法用于添加节点 [1, 3, 4, 5]

        });
    }

    // 更新模型回应的文本片段
    private void updateModelResponseStream(String newContent) {
        Platform.runLater(() -> {
            if (currentModelResponseText.getText().equals("思考中...")) {
                currentModelResponseText.setText(""); // 清除占位符
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
        requestBody.put("model", MODEL); // 根据你的API选择合适的模型
        requestBody.put("messages", currentMessages); // 发送整个对话历史
        requestBody.put("stream", true); // 请求流式响应

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        StringBuilder fullModelResponse = new StringBuilder(); // 用于累积完整的模型回应
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
                                    updateModelResponseStream(content); // 流式更新UI [17, 22]
                                }
                            }
                        }
                    }
                }
            }
        }
        // API调用结束后，将完整的模型回应添加到对话历史中
        messages.add(new JSONObject().put("role", "assistant").put("content", fullModelResponse.toString()));
    }
}