package app.vcampus.client.scene;

import app.vcampus.client.util.ChatSession;
import app.vcampus.client.util.ChatSession.ChatSessionSummary;
import app.vcampus.client.viewmodel.GptViewModel;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextArea;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


import java.net.URL;
import java.util.*;

/**
 * Controller for the GPT Chat interface.
 * Manages the display of chat messages, chat history, and user interactions.
 * Connects the view (FXML) with the GptViewModel.
 */
public class GptController implements Initializable {

    // --- FXML UI Elements ---
    @FXML private ScrollPane chatScrollPane;
    @FXML private VBox chatDisplayArea;
    @FXML private JFXTextArea userInputField;
    @FXML private JFXButton sendButton;
    @FXML private JFXListView<ChatSessionSummary> chatHistoryListView;
    @FXML private JFXButton newChatButton;

    // --- ViewModel and State ---
    private GptViewModel viewModel;
    private final Map<UUID, HBox> displayedMessageNodes = new HashMap<>();
    // 【新增】用于缓存头像图片，避免重复加载
    private Image assistantAvatar;

    // --- Constants for Styling and Layout ---
    private static final double MESSAGE_MAX_WIDTH_PERCENT = 0.7;
    private static final double MESSAGE_MIN_WIDTH = 50.0;
    private static final Insets MESSAGE_PADDING = new Insets(5);
    private static final Insets BUBBLE_PADDING = new Insets(8, 12, 8, 12);
    // 【新增】定义头像和助手名称相关的常量
    private static final double AVATAR_SIZE = 30.0;
    private static final String ASSISTANT_NAME = "Assistant-DeepSeek";


    // --- CSS Style Constants ---
    private static final String GREEN = "#4EB052"; // 定义一个品牌绿色
    private static final String ASSISTANT_COLOR_GRAY = "#F5F5F5"; // 定义助手的灰色

    private static final String STYLE_MESSAGE_BUBBLE = "-fx-border-radius: 15px; -fx-background-radius: 15px;";
    // 【修改 1】用户气泡背景色改为我们定义的品牌绿色
    private static final String STYLE_USER_BUBBLE = STYLE_MESSAGE_BUBBLE + "-fx-background-color: " + GREEN + ";";
    // 【修改 2】助手气泡背景色改为我们定义的灰色
    private static final String STYLE_ASSISTANT_BUBBLE = STYLE_MESSAGE_BUBBLE + "-fx-background-color: " + ASSISTANT_COLOR_GRAY + ";";
    private static final String STYLE_SYSTEM_BUBBLE = "-fx-background-color: transparent;";

    // 【修改 3】用户文字颜色保持白色 (与绿色背景搭配)
    private static final String STYLE_USER_TEXT = "-fx-fill: white;";
    // 【修改 4】助手文字颜色改为黑色 (与灰色背景搭配)
    private static final String STYLE_ASSISTANT_TEXT = "-fx-fill: black;";
    private static final String STYLE_SYSTEM_TEXT = "-fx-fill: gray; -fx-font-style: italic;";

    // 【新增】助手名称标签的样式
    private static final String STYLE_ASSISTANT_NAME = "-fx-font-size: 10px; -fx-text-fill: #888888;";

    private static final String STYLE_MESSAGE_DELETE_BUTTON =
            "-fx-background-color: #ff3b30;" + // iOS red
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: bold;" +
                    "-fx-padding: 0;" +
                    "-fx-min-width: 20px; -fx-max-width: 20px;" +
                    "-fx-min-height: 20px; -fx-max-height: 20px;" +
                    "-fx-background-radius: 10;" +
                    "-fx-border-radius: 10;";
    private static final String STYLE_HISTORY_DELETE_BUTTON = STYLE_MESSAGE_DELETE_BUTTON; // Re-use for consistency

    // --- Initialization ---

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        viewModel = new GptViewModel();

        // 【新增】在初始化时加载头像图片
        loadAvatarImage();

        setupBindings();
        setupListeners();
        setupChatHistoryList();

        viewModel.initializeSession();
    }

    // --- FXML Event Handlers ---

    /**
     * Handles the action of clicking the send button.
     * Delegates the action to the ViewModel.
     * @param event The action event.
     */
    @FXML
    private void sendMessage(ActionEvent event) {
        viewModel.sendMessage();
    }

    /**
     * Handles the action of clicking the "New Chat" button.
     * Delegates the action to the ViewModel.
     * @param event The action event.
     */
    @FXML
    private void createNewChat(ActionEvent event) {
        viewModel.createNewSession();
    }

    /**
     * Called when the view is about to be closed.
     * Ensures the current chat session is saved.
     */
    public void shutdown() {
        if (viewModel != null) {
            viewModel.saveCurrentSession();
            System.out.println("GptController shutdown hook executed, session saved.");
        }
    }

    // --- Private Setup Methods ---

    /**
     * Sets up data bindings between the View and the ViewModel.
     */
    private void setupBindings() {
        userInputField.textProperty().bindBidirectional(viewModel.userInputProperty());
        sendButton.disableProperty().bind(viewModel.sendButtonDisabledProperty());

        // Auto-scroll to the bottom of the chat display when new messages are added
        chatScrollPane.vvalueProperty().bind(chatDisplayArea.heightProperty());
    }

    /**
     * Sets up listeners for UI events and ViewModel changes.
     */
    private void setupListeners() {
        // Listen for message changes in the ViewModel to update the UI
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

        // Add Ctrl+Enter keyboard shortcut for sending a message
        userInputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && event.isControlDown()) {
                viewModel.sendMessage();
                event.consume(); // Prevent the Enter key from adding a new line
            }
        });
    }

    /**
     * Configures the chat history ListView, including its cell factory and selection model.
     */
    private void setupChatHistoryList() {
        chatHistoryListView.setItems(viewModel.getChatHistory());
        chatHistoryListView.setCellFactory(listView -> new ChatHistoryCell(viewModel));

        // Listen for selection changes to load the corresponding chat session
        chatHistoryListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && (oldSelection == null || !oldSelection.getId().equals(newSelection.getId()))) {
                viewModel.loadSession(newSelection.getId());
            }
        });
    }


    // --- Core UI Update Logic ---

    /**
     * 【修改】此方法被重构以处理不同发送者（用户、助手、系统）的布局。
     * Adds a graphical representation of a message to the chat display area.
     * For assistant messages, it now includes an avatar and a name label.
     *
     * @param message The message data object.
     */
    private void addMessageToDisplay(GptViewModel.Message message) {
        Platform.runLater(() -> {
            String sender = message.getSender();
            TextFlow messageBubble = createMessageBubble(message);
            HBox messageContainer = new HBox();
            messageContainer.setPadding(MESSAGE_PADDING);

            // 根据发送者类型构建不同的消息布局
            switch (sender) {
                case "user":
                    buildUserMessage(messageContainer, messageBubble, message);
                    break;
                case "model":
                case "assistant":
                    buildAssistantMessage(messageContainer, messageBubble, message);
                    break;
                default: // system
                    buildSystemMessage(messageContainer, messageBubble);
                    break;
            }

            // 存储并显示消息节点
            displayedMessageNodes.put(message.getId(), messageContainer);
            chatDisplayArea.getChildren().add(messageContainer);
        });
    }

    /**
     * 【新增】构建用户消息的布局。
     */
    private void buildUserMessage(HBox container, TextFlow bubble, GptViewModel.Message message) {
        container.setAlignment(Pos.CENTER_RIGHT);
        if (message.isDeletable()) {
            JFXButton deleteButton = createDeleteButton(message.getId());
            HBox buttonWrapper = new HBox(deleteButton);
            buttonWrapper.setAlignment(Pos.CENTER);
            buttonWrapper.setPadding(new Insets(0, 5, 0, 0));
            container.getChildren().addAll(buttonWrapper, bubble);
        } else {
            container.getChildren().add(bubble);
        }
    }

    /**
     * 【新增】构建助手消息的布局，包含头像、名称和气泡。
     */
    private void buildAssistantMessage(HBox container, TextFlow bubble, GptViewModel.Message message) {
        container.setAlignment(Pos.TOP_LEFT); // 整体顶端对齐
        container.setSpacing(10); // 头像和内容之间的间距

        // 1. 创建头像
        ImageView avatarView = createAvatarView();

        // 2. 创建名称标签和消息气泡的垂直容器
        Label nameLabel = new Label(ASSISTANT_NAME);
        nameLabel.setStyle(STYLE_ASSISTANT_NAME);
        VBox.setMargin(nameLabel, new Insets(0, 0, 4, 0)); // 名称和气泡间的垂直间距

        VBox nameAndBubbleVbox = new VBox(nameLabel, bubble);
        nameAndBubbleVbox.setAlignment(Pos.TOP_LEFT);

        // 3. 组合最终布局
        container.getChildren().addAll(avatarView, nameAndBubbleVbox);

        // 4. 添加删除按钮（如果需要）
        if (message.isDeletable()) {
            JFXButton deleteButton = createDeleteButton(message.getId());
            HBox buttonWrapper = new HBox(deleteButton);
            buttonWrapper.setAlignment(Pos.CENTER);
            buttonWrapper.setPadding(new Insets(0, 0, 0, 5));
            container.getChildren().add(buttonWrapper);
        }
    }

    /**
     * 【新增】构建系统消息的布局。
     */
    private void buildSystemMessage(HBox container, TextFlow bubble) {
        container.setAlignment(Pos.CENTER);
        container.getChildren().add(bubble);
    }


    /**
     * Creates a styled TextFlow bubble for a given message.
     *
     * @param message The message to display.
     * @return A configured TextFlow node.
     */
    private TextFlow createMessageBubble(GptViewModel.Message message) {
        Text textNode = new Text();
        textNode.textProperty().bind(message.streamingContentProperty());

        TextFlow textFlow = new TextFlow(textNode);
        textFlow.setPadding(BUBBLE_PADDING);
        textFlow.setMaxWidth(chatDisplayArea.getWidth() * MESSAGE_MAX_WIDTH_PERCENT);
        textFlow.setMinWidth(MESSAGE_MIN_WIDTH);

        switch (message.getSender()) {
            case "user":
                textNode.setStyle(STYLE_USER_TEXT);
                textFlow.setStyle(STYLE_USER_BUBBLE);
                break;
            case "model":
            case "assistant":
                textNode.setStyle(STYLE_ASSISTANT_TEXT);
                textFlow.setStyle(STYLE_ASSISTANT_BUBBLE);
                break;
            default: // "system" messages
                textNode.setStyle(STYLE_SYSTEM_TEXT);
                textFlow.setStyle(STYLE_SYSTEM_BUBBLE);
                textFlow.setPadding(new Insets(0)); // No padding for system messages
                break;
        }
        return textFlow;
    }

    /**
     * Creates a styled delete button for a message.
     *
     * @param messageId The ID of the message to be deleted on click.
     * @return A configured JFXButton node.
     */
    private JFXButton createDeleteButton(UUID messageId) {
        JFXButton deleteButton = new JFXButton("X");
        deleteButton.setStyle(STYLE_MESSAGE_DELETE_BUTTON);
        deleteButton.setOnAction(event -> viewModel.deleteMessage(messageId));
        return deleteButton;
    }


    /**
     * Removes a message's graphical node from the chat display area.
     *
     * @param messageIdToDelete The unique ID of the message to remove.
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

    // --- 【新增】辅助方法 ---

    /**
     * 【新增】加载头像图片资源。
     * 请确保在 `src/main/resources/images/` 目录下有一张名为 `assistant_avatar.png` 的图片。
     */
    private void loadAvatarImage() {
        try {
            // 从资源路径加载图片
            assistantAvatar = new Image(getClass().getResourceAsStream("/images/AssistantAvatar.png"));
        } catch (Exception e) {
            System.err.println("Assistant avatar image not found. Please check the path /images/AssistantAvatar.png");
            // 你可以在这里加载一个备用图像或什么都不做
            assistantAvatar = null;
        }
    }

    /**
     * 【新增】创建并配置助手的圆形头像 ImageView。
     * @return 配置好的 ImageView 节点。
     */
    private ImageView createAvatarView() {
        ImageView imageView = new ImageView(assistantAvatar);
        imageView.setFitWidth(AVATAR_SIZE);
        imageView.setFitHeight(AVATAR_SIZE);
        imageView.setPreserveRatio(true);

        // 创建一个圆形用于裁剪
        Circle clip = new Circle(AVATAR_SIZE / 2);
        clip.setCenterX(AVATAR_SIZE / 2);
        clip.setCenterY(AVATAR_SIZE / 2);
        imageView.setClip(clip);

        return imageView;
    }


    // --- Inner Class for Chat History Cell ---

    /**
     * A custom ListCell for displaying chat history items.
     * Includes the chat title and a delete button.
     */
    private static class ChatHistoryCell extends JFXListCell<ChatSession.ChatSessionSummary> {
        private final HBox hbox = new HBox();
        private final Label label = new Label();
        private final JFXButton deleteButton = new JFXButton("X");
        private final Region spacer = new Region();
        private final GptViewModel viewModel;

        public ChatHistoryCell(GptViewModel viewModel) {
            super();
            this.viewModel = viewModel;

            // Configure cell layout
            hbox.setAlignment(Pos.CENTER_LEFT);
            hbox.setSpacing(10);
            HBox.setHgrow(spacer, Priority.ALWAYS);
            hbox.getChildren().addAll(label, spacer, deleteButton);

            deleteButton.setStyle(STYLE_HISTORY_DELETE_BUTTON);
        }

        @Override
        protected void updateItem(ChatSession.ChatSessionSummary item, boolean empty) {
            super.updateItem(item, empty);
            // Always clear previous content
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