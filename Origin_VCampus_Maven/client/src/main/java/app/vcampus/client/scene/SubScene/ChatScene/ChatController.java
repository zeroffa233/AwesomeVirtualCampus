package app.vcampus.client.scene.SubScene.ChatScene;

import app.vcampus.client.viewmodel.ChatViewModel;
import app.vcampus.client.viewmodel.MessageViewModel;
import app.vcampus.server.enums.ChatTopic;
import com.jfoenix.controls.*;
import javafx.animation.PauseTransition;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * ChatController 作为 ChatView.fxml 的控制器。
 * (已对接 ViewModel 接口的最终版本)
 *
 * 【修改】: 实现了根据视图的可见性自动启停轮询的功能，解决了切出界面后后台依然轮询的问题。
 */
public class ChatController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private ScrollPane chatScrollPane;
    @FXML
    private VBox messageContainerVBox;
    @FXML
    private JFXTextArea userInputArea;
    @FXML
    private JFXButton sendButton;
    @FXML
    private JFXButton saveUsernameButton;
    @FXML
    private JFXTextField usernameTextField;
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

        // 4. 【对接】修改：不再在这里直接启动轮询。
        //    我们将其移至下面的监听器中，以根据视图的活动状态来管理轮询。
        // viewModel.startPolling(); // <-- 已移除此行

        // 新增监听器：监听 rootPane 的 parent 属性。
        // 当 parent 不为 null 时，意味着视图被添加到了某个容器中（即“显示”）。
        // 当 parent 变为 null 时，意味着视图从容器中被移除了（即“切出”）。
        rootPane.parentProperty().addListener((obs, oldParent, newParent) -> {
            if (newParent != null) {
                // 视图被显示，开始轮询
                System.out.println("Chat view is now active. Starting polling.");
                viewModel.startPolling();
            } else {
                // 视图被切走，停止轮询
                System.out.println("Chat view is now inactive. Stopping polling.");
                viewModel.stopPolling();
            }
        });

        System.out.println("ChatController initialized. Polling will be managed by view's active state.");
    }

    private void setupBindings() {
        // 将输入框的文本内容与 ViewModel 的 userInput 属性双向绑定
        userInputArea.textProperty().bindBidirectional(viewModel.userInputProperty());

        // 当输入框为空时，禁用发送按钮
        sendButton.disableProperty().bind(userInputArea.textProperty().isEmpty());

        topicComboBox.setItems(viewModel.getAvailableTopics());
        topicComboBox.valueProperty().bindBidirectional(viewModel.selectedTopicProperty());

        viewModel.currentUserNicknameProperty().addListener((obs, oldVal, newVal) -> {
            // 当 ViewModel 的数据更新时，同步到 TextField
            if (newVal != null && !newVal.equals(usernameTextField.getText())) {
                usernameTextField.setText(newVal);
            }
        });

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
    private void onSaveUsernameClicked() {
        String originalName = viewModel.currentUserNicknameProperty().get();
        String newName = usernameTextField.getText().trim();

        // 如果新昵称无效或与旧昵称相同，则不执行任何操作
        if (newName.isEmpty() || newName.equals(originalName)) {
            usernameTextField.setText(originalName); // 恢复为原始昵称
            return;
        }

        // 禁用按钮防止重复点击
        saveUsernameButton.setDisable(true);

        // 调用 ViewModel 的方法，并传入一个回调函数来处理结果
        viewModel.updateUsername(newName, success -> {
            // 无论成功与否，都重新启用按钮
            saveUsernameButton.setDisable(false);

            if (!success) {
                // 如果失败，显示错误信息
                usernameTextField.setText("用户名长度必须在2-20个字符之间!");

                // 创建一个2秒的延迟
                PauseTransition delay = new PauseTransition(Duration.seconds(2));

                // 延迟结束后，将 TextField 的内容恢复为原始昵称
                delay.setOnFinished(event -> usernameTextField.setText(originalName));

                // 播放延迟动画
                delay.play();
            }
            // 如果成功，我们什么都不用做。
            // ViewModel 的 forceRefresh() 会获取到最新的 ChatState，
            // 触发 currentUserNicknameProperty 的更新，
            // 我们的监听器会自动将 TextField 的内容更新为新的、已确认的昵称。
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
     * 这个方法仍然有用，可以作为应用关闭时的最终清理步骤。
     */
    public void shutdown() {
        // 【对接】调用 ViewModel 的清理方法
        viewModel.stopPolling();
        System.out.println("ChatController shutdown. ViewModel polling stopped.");
    }
}