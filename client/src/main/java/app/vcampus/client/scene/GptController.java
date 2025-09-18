package app.vcampus.client.scene;

import app.vcampus.server.utility.ChatSession;
import app.vcampus.server.utility.ChatSession.ChatSessionSummary;
import app.vcampus.client.viewmodel.GptViewModel;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextArea;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.util.*;

/**
 * GPT 聊天界面控制器。
 * 管理聊天消息、聊天历史和用户交互的显示，并将视图（FXML）与GptViewModel连接起来。
 */
public class GptController implements Initializable {

    @FXML private AnchorPane rootPane;
    /**
     * 聊天消息滚动面板。
     */
    @FXML private ScrollPane chatScrollPane;
    /**
     * 聊天消息显示区域。
     */
    @FXML private VBox chatDisplayArea;
    /**
     * 用户输入文本区域。
     */
    @FXML private JFXTextArea userInputField;
    /**
     * 发送消息按钮。
     */
    @FXML private JFXButton sendButton;
    /**
     * 聊天历史列表视图。
     */
    @FXML private JFXListView<ChatSessionSummary> chatHistoryListView;
    /**
     * 新建聊天按钮。
     */
    @FXML private JFXButton newChatButton;

    /**
     * GPT视图模型。
     */
    private GptViewModel viewModel;
    /**
     * 已显示消息的节点映射。
     */
    private final Map<UUID, HBox> displayedMessageNodes = new HashMap<>();
    /**
     * 助手头像图片。
     */
    private Image assistantAvatar;

    /**
     * 消息气泡最大宽度百分比。
     */
    private static final double MESSAGE_MAX_WIDTH_PERCENT = 0.7;
    /**
     * 消息气泡最小宽度。
     */
    private static final double MESSAGE_MIN_WIDTH = 50.0;
    /**
     * 消息内边距。
     */
    private static final Insets MESSAGE_PADDING = new Insets(5);
    /**
     * 气泡内边距。
     */
    private static final Insets BUBBLE_PADDING = new Insets(8, 12, 8, 12);
    /**
     * 头像大小。
     */
    private static final double AVATAR_SIZE = 30.0;
    /**
     * 助手名称。
     */
    private static final String ASSISTANT_NAME = "Assistant-DeepSeek";

    /**
     * 用户消息颜色（绿色）。
     */
    private static final String GREEN = "#607830DE";
    /**
     * 助手消息颜色（灰色）。
     */
    private static final String ASSISTANT_COLOR_GRAY = "#F5F5F5DE";

    /**
     * 消息气泡样式。
     */
    private static final String STYLE_MESSAGE_BUBBLE = "-fx-border-radius: 6px; -fx-background-radius: 6px;";
    /**
     * 用户消息气泡样式。
     */
    private static final String STYLE_USER_BUBBLE = STYLE_MESSAGE_BUBBLE + "-fx-background-color: " + GREEN + ";";
    /**
     * 助手消息气泡样式。
     */
    private static final String STYLE_ASSISTANT_BUBBLE = STYLE_MESSAGE_BUBBLE + "-fx-background-color: " + ASSISTANT_COLOR_GRAY + ";";
    /**
     * 系统消息气泡样式。
     */
    private static final String STYLE_SYSTEM_BUBBLE = "-fx-background-color: transparent;";

    /**
     * 用户消息文本样式。
     */
    private static final String STYLE_USER_TEXT = "-fx-fill: #FFFFFFDE;";
    /**
     * 助手消息文本样式。
     */
    private static final String STYLE_ASSISTANT_TEXT = "-fx-fill: black;";
    /**
     * 系统消息文本样式。
     */
    private static final String STYLE_SYSTEM_TEXT = "-fx-fill: gray; -fx-font-style: italic;";
    /**
     * 助手名称文本样式。
     */
    private static final String STYLE_ASSISTANT_NAME = "-fx-font-size: 15px; -fx-text-fill: #888888;";

    /**
     * 初始化方法，在FXML文件加载完成后自动调用。
     *
     * @param location  URL定位资源。
     * @param resources 资源包。
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        viewModel = new GptViewModel();

        loadAvatarImage();
        setupBindings();
        setupListeners();
        setupChatHistoryList();

        viewModel.initializeSession();
    }

    /**
     * 发送消息。
     */
    @FXML
    private void sendMessage() {
        viewModel.sendMessage();
    }

    /**
     * 创建新聊天。
     */
    @FXML
    private void createNewChat() {
        viewModel.createNewSession();
    }

    /**
     * 设置数据绑定。
     */
    private void setupBindings() {
        userInputField.textProperty().bindBidirectional(viewModel.userInputProperty());
        sendButton.disableProperty().bind(viewModel.sendButtonDisabledProperty());
        chatScrollPane.vvalueProperty().bind(chatDisplayArea.heightProperty());
    }

    /**
     * 设置事件监听器。
     */
    private void setupListeners() {
        viewModel.currentSessionProperty().addListener((obs, oldSession, newSession) -> {
            Platform.runLater(() -> {
                if (newSession == null) {
                    chatHistoryListView.getSelectionModel().clearSelection();
                } else {
                    chatHistoryListView.getItems().stream()
                            .filter(summary -> summary.getId().equals(newSession.getId()))
                            .findFirst()
                            .ifPresent(summaryToSelect -> {
                                if (!summaryToSelect.equals(chatHistoryListView.getSelectionModel().getSelectedItem())) {
                                    chatHistoryListView.getSelectionModel().select(summaryToSelect);
                                    chatHistoryListView.scrollTo(summaryToSelect);
                                }
                            });
                }
            });
        });

        viewModel.getChatMessages().addListener((javafx.collections.ListChangeListener.Change<? extends GptViewModel.Message> change) -> {
            while (change.next()) {
                if (change.wasRemoved()) {
                    change.getRemoved().forEach(msg -> removeMessageFromDisplay(msg.getId()));
                }
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(this::addMessageToDisplay);
                }
            }
        });

        userInputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && event.isControlDown()) {
                viewModel.sendMessage();
                event.consume();
            }
        });
        rootPane.sceneProperty().addListener((sceneObs, oldScene, newScene) -> {
            if (newScene==null){
                System.out.println("Quiting Gpt, Saving Context");
                viewModel.gptClient.saveData();
            }
        });
    }

    /**
     * 设置聊天历史列表。
     */
    private void setupChatHistoryList() {
        chatHistoryListView.setItems(viewModel.getChatHistory());
        chatHistoryListView.setCellFactory(listView -> new ChatHistoryCell(viewModel));

        chatHistoryListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && (oldSelection == null || !oldSelection.getId().equals(newSelection.getId()))) {
                viewModel.loadSession(newSelection.getId());
            }
        });
    }

    /**
     * 添加消息到显示区域。
     *
     * @param message 要添加的消息。
     */
    private void addMessageToDisplay(GptViewModel.Message message) {
        Platform.runLater(() -> {
            String sender = message.getSender();
            TextFlow messageBubble = createMessageBubble(message);
            HBox messageContainer = new HBox();
            messageContainer.setPadding(MESSAGE_PADDING);

            switch (sender) {
                case "user":
                    buildUserMessage(messageContainer, messageBubble, message);
                    break;
                case "model":
                case "assistant":
                    buildAssistantMessage(messageContainer, messageBubble, message);
                    break;
                default:
                    buildSystemMessage(messageContainer, messageBubble);
                    break;
            }

            displayedMessageNodes.put(message.getId(), messageContainer);
            chatDisplayArea.getChildren().add(messageContainer);
        });
    }

    /**
     * 构建用户消息气泡。
     *
     * @param container 消息容器。
     * @param bubble 消息气泡。
     * @param message 消息内容。
     */
    private void buildUserMessage(HBox container, TextFlow bubble, GptViewModel.Message message) {
        container.setAlignment(Pos.CENTER_RIGHT);
        if (message.isDeletable()) {
            JFXButton deleteButton = createDeleteButton(message.getId());
            HBox buttonWrapper = new HBox(deleteButton);
            buttonWrapper.setAlignment(Pos.BOTTOM_LEFT);
            buttonWrapper.setPadding(new Insets(0, 5, 0, 0));
            container.getChildren().addAll(buttonWrapper, bubble);
        } else {
            container.getChildren().add(bubble);
        }
    }

    /**
     * 构建助手消息气泡。
     *
     * @param container 消息容器。
     * @param bubble 消息气泡。
     * @param message 消息内容。
     */
    private void buildAssistantMessage(HBox container, TextFlow bubble, GptViewModel.Message message) {
        container.setAlignment(Pos.TOP_LEFT);
        container.setSpacing(10);

        ImageView avatarView = createAvatarView();

        Label nameLabel = new Label(ASSISTANT_NAME);
        nameLabel.setStyle(STYLE_ASSISTANT_NAME);
        VBox.setMargin(nameLabel, new Insets(0, 0, 4, 0));

        VBox nameAndBubbleVbox = new VBox(nameLabel, bubble);
        nameAndBubbleVbox.setAlignment(Pos.TOP_LEFT);

        container.getChildren().addAll(avatarView, nameAndBubbleVbox);

        if (message.isDeletable()) {
            JFXButton deleteButton = createDeleteButton(message.getId());
            HBox buttonWrapper = new HBox(deleteButton);
            buttonWrapper.setAlignment(Pos.BOTTOM_LEFT);
            buttonWrapper.setPadding(new Insets(0, 0, 0, 5));
            container.getChildren().add(buttonWrapper);
        }
    }

    /**
     * 构建系统消息气泡。
     *
     * @param container 消息容器。
     * @param bubble 消息气泡。
     */
    private void buildSystemMessage(HBox container, TextFlow bubble) {
        container.setAlignment(Pos.CENTER);
        container.getChildren().add(bubble);
    }

    /**
     * 创建消息气泡。
     *
     * @param message 消息内容。
     * @return 消息气泡的TextFlow。
     */
    private TextFlow createMessageBubble(GptViewModel.Message message) {
        Text textNode = new Text();
        textNode.textProperty().bind(message.streamingContentProperty());

        TextFlow textFlow = new TextFlow(textNode);
        textFlow.setPadding(BUBBLE_PADDING);
        textFlow.setMaxWidth(chatDisplayArea.getWidth() * MESSAGE_MAX_WIDTH_PERCENT);
        textFlow.setMinWidth(MESSAGE_MIN_WIDTH);

        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.color(0, 0, 0, 0.2));

        switch (message.getSender()) {
            case "user":
                textNode.setStyle(STYLE_USER_TEXT);
                textFlow.setStyle(STYLE_USER_BUBBLE);
                textFlow.setEffect(dropShadow);
                break;
            case "model":
            case "assistant":
                textNode.setStyle(STYLE_ASSISTANT_TEXT);
                textFlow.setStyle(STYLE_ASSISTANT_BUBBLE);
                textFlow.setEffect(dropShadow);
                break;
            default:
                textNode.setStyle(STYLE_SYSTEM_TEXT);
                textFlow.setStyle(STYLE_SYSTEM_BUBBLE);
                textFlow.setPadding(new Insets(0));
                break;
        }
        return textFlow;
    }

    /**
     * 创建删除按钮。
     *
     * @param messageId 消息ID。
     * @return 删除按钮。
     */
    private JFXButton createDeleteButton(UUID messageId) {
        JFXButton deleteButton = new JFXButton("×");
        deleteButton.getStyleClass().add("delete-button");

        Circle clip = new Circle(11, 11, 11);
        deleteButton.setClip(clip);

        deleteButton.setOnAction(event -> viewModel.deleteMessage(messageId));
        return deleteButton;
    }

    /**
     * 从显示区域移除消息。
     *
     * @param messageIdToDelete 要删除的消息ID。
     */
    private void removeMessageFromDisplay(UUID messageIdToDelete) {
        Platform.runLater(() -> {
            HBox messageHBox = displayedMessageNodes.remove(messageIdToDelete);
            if (messageHBox != null) {
                chatDisplayArea.getChildren().remove(messageHBox);
            } else {
                System.err.println("Could not find HBox for message ID: " + messageIdToDelete);
            }
        });
    }

    /**
     * 加载助手头像图片。
     */
    private void loadAvatarImage() {
        try {
            assistantAvatar = new Image(getClass().getResourceAsStream("/images/AssistantAvatar.png"));
        } catch (Exception e) {
            System.err.println("Assistant avatar image not found. Please check the path /images/AssistantAvatar.png");
            assistantAvatar = null;
        }
    }

    /**
     * 创建助手头像视图。
     *
     * @return 助手头像的ImageView。
     */
    private ImageView createAvatarView() {
        ImageView imageView = new ImageView(assistantAvatar);
        imageView.setFitWidth(AVATAR_SIZE);
        imageView.setFitHeight(AVATAR_SIZE);
        imageView.setPreserveRatio(true);

        Circle clip = new Circle(AVATAR_SIZE / 2);
        clip.setCenterX(AVATAR_SIZE / 2);
        clip.setCenterY(AVATAR_SIZE / 2);
        imageView.setClip(clip);

        return imageView;
    }

    /**
     * 聊天历史列表单元格。
     */
    private static class ChatHistoryCell extends JFXListCell<ChatSession.ChatSessionSummary> {
        private final HBox hbox = new HBox();
        private final Label label = new Label();
        private final JFXButton deleteButton = new JFXButton("×");
        private final Region spacer = new Region();
        private final GptViewModel viewModel;

        /**
         * 构造函数。
         *
         * @param viewModel GPT视图模型。
         */
        public ChatHistoryCell(GptViewModel viewModel) {
            super();
            this.viewModel = viewModel;

            hbox.setAlignment(Pos.CENTER_LEFT);
            hbox.setSpacing(10);
            HBox.setHgrow(spacer, Priority.ALWAYS);
            hbox.getChildren().addAll(label, spacer, deleteButton);

            deleteButton.getStyleClass().add("delete-button");

            Circle clip = new Circle(11, 11, 11);
            deleteButton.setClip(clip);
        }

        /**
         * 更新列表项。
         *
         * @param item 聊天会话摘要。
         * @param empty 是否为空。
         */
        @Override
        protected void updateItem(ChatSession.ChatSessionSummary item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
            setGraphic(null);

            if (!empty && item != null) {
                label.setText(item.getTitle());
                deleteButton.setOnAction(event -> viewModel.deleteSession(item.getId()));
                setGraphic(hbox);
            }
        }
    }
}