package app.vcampus.client.scene;

import app.vcampus.client.viewmodel.ChatViewModel;
import app.vcampus.client.viewmodel.MessageViewModel;
import app.vcampus.server.enums.ChatTopic;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXScrollPane;
import com.jfoenix.controls.JFXTextArea;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * ChatController 作为 ChatView.fxml 的控制器。
 * (已对接 ViewModel 接口的最终版本)
 */
public class ChatController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private JFXScrollPane chatScrollPane;
    @FXML
    private VBox messageContainerVBox;
    @FXML
    private JFXTextArea userInputArea;
    @FXML
    private JFXButton sendButton;
    @FXML
    private JFXButton changeUsernameButton;
    @FXML
    private JFXComboBox<ChatTopic> topicComboBox;
    @FXML
    private Label titleLabel;

    // 持有 ViewModel
    private ChatViewModel viewModel;

    // 用于追踪 MessageViewModel 对应的 UI 节点，以便于高效移除
    private final Map<MessageViewModel, Node> messageNodeMap = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. 初始化 ViewModel
        this.viewModel = new ChatViewModel();

        // 2. 设置数据绑定
        setupBindings();

        // 3. 设置事件监听器
        setupListeners();

        // 4. 【对接】启动 ViewModel 的数据轮询
        viewModel.startPolling();
        System.out.println("ChatController initialized. ViewModel polling started.");
    }

    private void setupBindings() {
        // 将输入框的文本内容与 ViewModel 的 userInput 属性双向绑定
        userInputArea.textProperty().bindBidirectional(viewModel.userInputProperty());

        // 当输入框为空时，禁用发送按钮
        sendButton.disableProperty().bind(userInputArea.textProperty().isEmpty());

        topicComboBox.setItems(viewModel.getAvailableTopics());
        topicComboBox.valueProperty().bindBidirectional(viewModel.selectedTopicProperty());

        // 动态绑定标题
//        titleLabel.textProperty().bind(Bindings.createStringBinding(
//                () -> {
//                    ChatTopic topic = viewModel.selectedTopicProperty().get();
//                    return topic != null ? "校园聊天室 - " + topic.getDisplayName() : "校园聊天室";
//                },
//                viewModel.selectedTopicProperty()
//        ));
    }

    private void setupListeners() {
        // 监听 ViewModel 中 messages 列表的变化
        viewModel.getMessages().addListener((ListChangeListener<MessageViewModel>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (MessageViewModel addedMsg : change.getAddedSubList()) {
                        // 防止因UI清理不及时而重复添加
                        if (!messageNodeMap.containsKey(addedMsg)) {
                            Node messageNode = createMessageNode(addedMsg);
                            messageNodeMap.put(addedMsg, messageNode);
                            messageContainerVBox.getChildren().add(messageNode);
                        }
                    }
                }
                if (change.wasRemoved()) {
                    for (MessageViewModel removedMsg : change.getRemoved()) {
                        Node nodeToRemove = messageNodeMap.remove(removedMsg);
                        if (nodeToRemove != null) {
                            messageContainerVBox.getChildren().remove(nodeToRemove);
                        }
                    }
                }
            }
        });

        // ADDED: 添加对 topic 切换的直接监听，以强制清空UI
        viewModel.selectedTopicProperty().addListener((obs, oldTopic, newTopic) -> {
            if (newTopic != null && !newTopic.equals(oldTopic)) {
                // 当 topic 发生变化时，直接、可靠地清空UI显示
                messageContainerVBox.getChildren().clear();
                messageNodeMap.clear();
            }
        });


        // 为输入框添加 Ctrl+Enter 快捷键发送消息
        userInputArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER && event.isControlDown()) {
                onSendButtonClicked();
                event.consume();
            }
        });
    }

    /**
     * 当用户点击发送按钮时被调用。
     */
    @FXML
    private void onSendButtonClicked() {
        // 【对接】调用 ViewModel 的 sendMessage 方法
        viewModel.sendMessage();
        // userInputArea 会通过双向绑定自动清空
    }

    /**
     * 当用户点击修改昵称按钮时被调用。
     */
    @FXML
    private void onChangeUsernameClicked() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("修改昵称");
        dialog.setHeaderText("请输入您的新昵称");
        dialog.setContentText("昵称:");

        dialog.showAndWait().ifPresent(newName -> {
            if (!newName.trim().isEmpty()) {
                // 【对接】调用 ViewModel 的 updateUsername 方法
                viewModel.updateUsername(newName);
            }
        });
    }


    private Node createMessageNode(MessageViewModel messageViewModel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/vcampus/client/scene/SubScene/ChatScene/MessageView.fxml"));
            Node messageNode = loader.load();
            MessageController messageController = loader.getController();
            messageController.setData(messageViewModel, this.viewModel);
            return messageNode;
        } catch (IOException e) {
            e.printStackTrace();
            return new Label("Error loading message view for ID: " + messageViewModel.getId());
        }
    }

    /**
     * 在场景切换或窗口关闭时，需要调用此方法来停止轮询。
     */
    public void shutdown() {
        // 【对接】调用 ViewModel 的清理方法
        viewModel.stopPolling();
        System.out.println("ChatController shutdown. ViewModel polling stopped.");
    }
}