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
import javafx.scene.effect.DropShadow;
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

//TODO autosave on quit

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
    private Image assistantAvatar;

    // --- Constants for Styling and Layout ---
    private static final double MESSAGE_MAX_WIDTH_PERCENT = 0.7;
    private static final double MESSAGE_MIN_WIDTH = 50.0;
    private static final Insets MESSAGE_PADDING = new Insets(5);
    private static final Insets BUBBLE_PADDING = new Insets(8, 12, 8, 12);
    private static final double AVATAR_SIZE = 30.0;
    private static final String ASSISTANT_NAME = "Assistant-DeepSeek";


    // --- CSS Style Constants ---
    private static final String GREEN = "#607830DE";
    private static final String ASSISTANT_COLOR_GRAY = "#F5F5F5DE";

    private static final String STYLE_MESSAGE_BUBBLE = "-fx-border-radius: 6px; -fx-background-radius: 6px;";
    private static final String STYLE_USER_BUBBLE = STYLE_MESSAGE_BUBBLE + "-fx-background-color: " + GREEN + ";";
    private static final String STYLE_ASSISTANT_BUBBLE = STYLE_MESSAGE_BUBBLE + "-fx-background-color: " + ASSISTANT_COLOR_GRAY + ";";
    private static final String STYLE_SYSTEM_BUBBLE = "-fx-background-color: transparent;";

    private static final String STYLE_USER_TEXT = "-fx-fill: #FFFFFFDE;";
    private static final String STYLE_ASSISTANT_TEXT = "-fx-fill: black;";
    private static final String STYLE_SYSTEM_TEXT = "-fx-fill: gray; -fx-font-style: italic;";
    private static final String STYLE_ASSISTANT_NAME = "-fx-font-size: 15px; -fx-text-fill: #888888;";

    

    // --- Initialization ---

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        viewModel = new GptViewModel();
        final String inlineCss = """
            .jfx-list-cell .jfx-rippler {
                -jfx-rippler-fill: #B2C926B2;
            }
        """;

        // 2. 创建Data URL并添加到ListView的样式表中
        chatHistoryListView.getStylesheets().add("data:text/css," + inlineCss);
        loadAvatarImage();
        setupBindings();
        setupListeners();
        setupChatHistoryList();

        viewModel.initializeSession();
    }

    // --- FXML Event Handlers ---

    @FXML
    private void sendMessage(ActionEvent event) {
        viewModel.sendMessage();
    }

    @FXML
    private void createNewChat(ActionEvent event) {
        viewModel.createNewSession();
    }

    public void shutdown() {
        if (viewModel != null) {
            viewModel.saveCurrentSession();
            System.out.println("GptController shutdown hook executed, session saved.");
        }
    }

    // --- Private Setup Methods ---

    private void setupBindings() {
        userInputField.textProperty().bindBidirectional(viewModel.userInputProperty());
        sendButton.disableProperty().bind(viewModel.sendButtonDisabledProperty());
        chatScrollPane.vvalueProperty().bind(chatDisplayArea.heightProperty());
    }

    private void setupListeners() {
        // Listener to sync the UI selection with the ViewModel's current session
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
                                    chatHistoryListView.scrollTo(summaryToSelect); // Scroll to the selected item
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

        viewModel.getChatHistory().addListener((javafx.collections.ListChangeListener.Change<? extends ChatSessionSummary> c) -> {
            // 当列表被 setAll() 刷新后，这个监听器会触发
            // 我们需要重新应用选中效果
            Platform.runLater(() -> {
                ChatSession activeSession = viewModel.getCurrentSession();
                if (activeSession != null) {
                    // 在更新后的列表中找到与当前活动会话匹配的项
                    viewModel.getChatHistory().stream()
                            .filter(summary -> summary.getId().equals(activeSession.getId()))
                            .findFirst()
                            .ifPresent(summaryToSelect -> {
                                // 检查它是否已经是选中的项，以避免不必要的重绘或事件触发
                                if (chatHistoryListView.getSelectionModel().getSelectedItem() != summaryToSelect) {
                                    chatHistoryListView.getSelectionModel().select(summaryToSelect);
                                }
                            });
                }
            });
        });
    }

    private void setupChatHistoryList() {
        chatHistoryListView.setItems(viewModel.getChatHistory());
        chatHistoryListView.setCellFactory(listView -> new ChatHistoryCell(viewModel));

        chatHistoryListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && (oldSelection == null || !oldSelection.getId().equals(newSelection.getId()))) {
                viewModel.loadSession(newSelection.getId());
            }
        });
    }


    // --- Core UI Update Logic ---

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
                default: // system
                    buildSystemMessage(messageContainer, messageBubble);
                    break;
            }

            displayedMessageNodes.put(message.getId(), messageContainer);
            chatDisplayArea.getChildren().add(messageContainer);
        });
    }

    private void buildUserMessage(HBox container, TextFlow bubble, GptViewModel.Message message) {
        container.setAlignment(Pos.CENTER_RIGHT);
        if (message.isDeletable()) {
            JFXButton deleteButton = createDeleteButton(message.getId());
            HBox buttonWrapper = new HBox(deleteButton);
            // **【修改】将按钮垂直对齐方式改为底部**
            buttonWrapper.setAlignment(Pos.BOTTOM_LEFT);
            buttonWrapper.setPadding(new Insets(0, 5, 0, 0));
            container.getChildren().addAll(buttonWrapper, bubble);
        } else {
            container.getChildren().add(bubble);
        }
    }

    private void buildAssistantMessage(HBox container, TextFlow bubble, GptViewModel.Message message) {
        container.setAlignment(Pos.TOP_LEFT);
        container.setSpacing(10);

        ImageView avatarView = createAvatarView();

        Label nameLabel = new Label(ASSISTANT_NAME);
        nameLabel.setStyle(STYLE_ASSISTANT_NAME);
        VBox.setMargin(nameLabel, new Insets(0, 0, 4, 0));

        VBox nameAndBubbleVbox = new VBox(nameLabel, bubble);
        nameAndBubbleVbox.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(nameAndBubbleVbox, Priority.ALWAYS);
        nameAndBubbleVbox.setFillWidth(true);

        container.getChildren().addAll(avatarView, nameAndBubbleVbox);

        if (message.isDeletable()) {
            JFXButton deleteButton = createDeleteButton(message.getId());
            HBox buttonWrapper = new HBox(deleteButton);
            // **【修改】将按钮垂直对齐方式改为底部**
            buttonWrapper.setAlignment(Pos.BOTTOM_LEFT);
            buttonWrapper.setPadding(new Insets(0, 0, 0, 5));
            container.getChildren().add(buttonWrapper);
        }
    }

    private void buildSystemMessage(HBox container, TextFlow bubble) {
        container.setAlignment(Pos.CENTER);
        container.getChildren().add(bubble);
    }


    private TextFlow createMessageBubble(GptViewModel.Message message) {
        Text textNode = new Text();
        textNode.textProperty().bind(message.streamingContentProperty());

        TextFlow textFlow = new TextFlow(textNode);
<<<<<<< HEAD

        textNode.wrappingWidthProperty().bind(textFlow.widthProperty());

        textFlow.getStyleClass().add("chat-bubble");
=======
        textFlow.setPadding(BUBBLE_PADDING);
        textFlow.setMaxWidth(chatDisplayArea.getWidth() * MESSAGE_MAX_WIDTH_PERCENT);
        textFlow.setMinWidth(MESSAGE_MIN_WIDTH);

        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.color(0, 0, 0, 0.2));

>>>>>>> c2a3004 (Completed GPT style modification.)

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
            default: // "system" messages
                textNode.setStyle(STYLE_SYSTEM_TEXT);
                textFlow.setStyle(STYLE_SYSTEM_BUBBLE);
                textFlow.setPadding(new Insets(0));
                break;
        }
        return textFlow;
    }

    private JFXButton createDeleteButton(UUID messageId) {
        JFXButton deleteButton = new JFXButton("×");
        deleteButton.getStyleClass().add("delete-button");

        // Create a circular clip for the button
        Circle clip = new Circle(11, 11, 11);
        deleteButton.setClip(clip);

        deleteButton.setOnAction(event -> viewModel.deleteMessage(messageId));
        return deleteButton;
    }


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

    // --- 辅助方法 ---

    private void loadAvatarImage() {
        try {
            assistantAvatar = new Image(getClass().getResourceAsStream("/images/AssistantAvatar.png"));
        } catch (Exception e) {
            System.err.println("Assistant avatar image not found. Please check the path /images/AssistantAvatar.png");
            assistantAvatar = null;
        }
    }

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


    // --- Inner Class for Chat History Cell ---

    private static class ChatHistoryCell extends JFXListCell<ChatSession.ChatSessionSummary> {
        private final HBox hbox = new HBox();
        private final Label label = new Label();
        private final JFXButton deleteButton = new JFXButton("×");
        private final Region spacer = new Region();
        private final GptViewModel viewModel;

        public ChatHistoryCell(GptViewModel viewModel) {
            super();
            this.viewModel = viewModel;

            hbox.setAlignment(Pos.CENTER_LEFT);
            hbox.setSpacing(10);
            HBox.setHgrow(spacer, Priority.ALWAYS);
            hbox.getChildren().addAll(label, spacer, deleteButton);

            deleteButton.getStyleClass().add("delete-button");

            // Create a circular clip for the button
            Circle clip = new Circle(11, 11, 11);
            deleteButton.setClip(clip);
        }

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