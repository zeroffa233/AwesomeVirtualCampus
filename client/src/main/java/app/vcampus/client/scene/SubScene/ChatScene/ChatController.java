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
 * 聊天场景控制器。
 * 负责管理聊天界面的UI和交互，通过ChatViewModel与后端逻辑交互。
 * 实现了根据视图的可见性自动启停轮询的功能。
 */
public class ChatController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    /**
     * 聊天消息滚动面板。
     */
    @FXML
    private ScrollPane chatScrollPane;
    /**
     * 消息容器VBox。
     */
    @FXML
    private VBox messageContainerVBox;
    /**
     * 用户输入文本区域。
     */
    @FXML
    private JFXTextArea userInputArea;
    /**
     * 发送按钮。
     */
    @FXML
    private JFXButton sendButton;
    /**
     * 保存用户名按钮。
     */
    @FXML
    private JFXButton saveUsernameButton;
    /**
     * 用户名文本字段。
     */
    @FXML
    private JFXTextField usernameTextField;
    /**
     * 话题选择组合框。
     */
    @FXML
    private JFXComboBox<ChatTopic> topicComboBox;
    /**
     * 标题标签。
     */
    @FXML
    private Label titleLabel;

    /**
     * 聊天视图模型。
     */
    private ChatViewModel viewModel;
    /**
     * 消息节点映射。
     */
    private final Map<MessageViewModel, Node> messageNodeMap = new HashMap<>();

    /**
     * 初始化方法，在FXML文件加载完成后自动调用。
     *
     * @param location  URL定位资源。
     * @param resources 资源包。
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.viewModel = new ChatViewModel();

        setupBindings();
        setupListeners();

        rootPane.parentProperty().addListener((obs, oldParent, newParent) -> {
            if (newParent != null) {
                System.out.println("Chat view is now active. Starting polling.");
                viewModel.startPolling();
            } else {
                System.out.println("Chat view is now inactive. Stopping polling.");
                viewModel.stopPolling();
            }
        });

        System.out.println("ChatController initialized. Polling will be managed by view's active state.");
    }

    private void setupBindings() {
        userInputArea.textProperty().bindBidirectional(viewModel.userInputProperty());
        sendButton.disableProperty().bind(userInputArea.textProperty().isEmpty());
        topicComboBox.setItems(viewModel.getAvailableTopics());
        topicComboBox.valueProperty().bindBidirectional(viewModel.selectedTopicProperty());

        viewModel.currentUserNicknameProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(usernameTextField.getText())) {
                usernameTextField.setText(newVal);
            }
        });
    }

    /**
     * 设置监听器。
     */
    private void setupListeners() {
        viewModel.getMessages().addListener((ListChangeListener<MessageViewModel>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (MessageViewModel addedMsg : change.getAddedSubList()) {
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

        viewModel.selectedTopicProperty().addListener((obs, oldTopic, newTopic) -> {
            if (newTopic != null && !newTopic.equals(oldTopic)) {
                messageContainerVBox.getChildren().clear();
                messageNodeMap.clear();
            }
        });

        userInputArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER && event.isControlDown()) {
                onSendButtonClicked();
                event.consume();
            }
        });
    }

    /**
     * 发送按钮点击事件处理。
     */
    @FXML
    private void onSendButtonClicked() {
        viewModel.sendMessage();
    }

    /**
     * 保存用户名点击事件处理。
     */
    @FXML
    private void onSaveUsernameClicked() {
        String originalName = viewModel.currentUserNicknameProperty().get();
        String newName = usernameTextField.getText().trim();

        if (newName.isEmpty() || newName.equals(originalName)) {
            usernameTextField.setText(originalName);
            return;
        }

        saveUsernameButton.setDisable(true);

        viewModel.updateUsername(newName, success -> {
            saveUsernameButton.setDisable(false);

            if (!success) {
                usernameTextField.setText("用户名长度必须在2-20个字符之间!");

                PauseTransition delay = new PauseTransition(Duration.seconds(2));
                delay.setOnFinished(event -> usernameTextField.setText(originalName));
                delay.play();
            }
        });
    }

    /**
     * 创建消息节点。
     *
     * @param messageViewModel 消息视图模型。
     * @return 消息节点。
     */
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
     * 在场景切换或窗口关闭时，调用此方法来停止轮询。
     */
    public void shutdown() {
        viewModel.stopPolling();
        System.out.println("ChatController shutdown. ViewModel polling stopped.");
    }
}