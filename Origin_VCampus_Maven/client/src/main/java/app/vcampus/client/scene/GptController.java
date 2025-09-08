package app.vcampus.client.scene;

import app.vcampus.client.util.ChatSession;
import app.vcampus.client.viewmodel.GptViewModel;
import app.vcampus.client.util.ChatSession.ChatSessionSummary;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.event.ActionEvent;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXListCell; // Import JFXListCell
import com.jfoenix.controls.JFXTextArea;

import java.net.URL;
import java.util.*;

public class GptController implements Initializable {
    // FXML UI elements for chat display
    @FXML private ScrollPane chatScrollPane;
    @FXML private VBox chatDisplayArea;
    @FXML private JFXTextArea userInputField;
    @FXML private JFXButton sendButton;

    // FXML UI elements for chat history
    @FXML private JFXListView<ChatSessionSummary> chatHistoryListView;
    @FXML private JFXButton newChatButton;

    private GptViewModel viewModel;
    private final Map<UUID, HBox> displayedMessageNodes = new HashMap<>();

    private static final double MESSAGE_MAX_WIDTH_PERCENT = 0.7;
    private static final double MESSAGE_MIN_WIDTH = 50.0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        viewModel = new GptViewModel();

        // --- Bindings for Chat Area ---
        userInputField.textProperty().bindBidirectional(viewModel.userInputProperty());
        sendButton.disableProperty().bind(viewModel.sendButtonDisabledProperty());

        // Listen for changes in chatMessages to update UI
        viewModel.getChatMessages().addListener((javafx.collections.ListChangeListener.Change<? extends GptViewModel.Message> change) -> {
            while (change.next()) {
                if (change.wasRemoved()) {
                    for (GptViewModel.Message message : change.getRemoved()) {
                        removeMessageFromDisplay(message.getId());
                    }
                }
                if (change.wasAdded()) {
                    for (GptViewModel.Message message : change.getAddedSubList()) {
                        addMessageToDisplay(message);
                    }
                }
            }
        });

        // Auto-scroll to the bottom
        chatScrollPane.vvalueProperty().bind(chatDisplayArea.heightProperty());

        // Keyboard shortcut for sending message
        userInputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && event.isControlDown()) {
                viewModel.sendMessage();
                event.consume();
            }
        });

        // --- Bindings for Chat History ---

        chatHistoryListView.setCellFactory(listView -> new ChatHistoryCell(viewModel));
        chatHistoryListView.setItems(viewModel.getChatHistory());
        chatHistoryListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && (oldSelection == null || !oldSelection.getId().equals(newSelection.getId()))) {
                viewModel.loadSession(newSelection.getId());
            }
        });

        // Manually trigger initial UI population
        viewModel.initializeSession();
    }

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

            // Style the delete button
            deleteButton.setStyle(
                    "-fx-background-color: #ff6666;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 10px;" +
                            "-fx-padding: 0;" +
                            "-fx-min-width: 20px; -fx-max-width: 20px;" +
                            "-fx-min-height: 20px; -fx-max-height: 20px;" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-radius: 10;"
            );
        }

        @Override
        protected void updateItem(ChatSession.ChatSessionSummary item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                label.setText(item.getTitle());

                // Set the action for the button. It calls the ViewModel's delete method.
                deleteButton.setOnAction(event -> {
                    if (item != null) {
                        viewModel.deleteSession(item.getId());
                    }
                });

                // [FIX] Explicitly set text to null to prevent the default text from showing.
                setText(null);

                setGraphic(hbox);
            }
        }
    }


    /**
     * Called when the view is about to be closed. Ensures the current session is saved.
     */
    public void shutdown() {
        if (viewModel != null) {
            viewModel.saveCurrentSession();
            System.out.println("GptController shutdown hook executed, session saved.");
        }
    }


    @FXML
    private void sendMessage(ActionEvent event) {
        viewModel.sendMessage();
    }

    @FXML
    private void createNewChat(ActionEvent event) {
        viewModel.createNewSession();
    }

    private void addMessageToDisplay(GptViewModel.Message message) {
        Platform.runLater(() -> {
            HBox messageContainer = new HBox();
            messageContainer.setPadding(new javafx.geometry.Insets(5));
            displayedMessageNodes.put(message.getId(), messageContainer);

            // [FIX] 1. Replace the Label with a Text node.
            // The Text node is the fundamental component for text layout and handles wrapping more gracefully.
            Text textNode = new Text();
            textNode.textProperty().bind(message.streamingContentProperty());

            // The TextFlow will now manage the wrapping of the Text node within its bounds.
            TextFlow textFlow = new TextFlow(textNode);

            // Keep the original, static max width calculation. This is correct.
            textFlow.setMaxWidth(chatDisplayArea.getWidth() * MESSAGE_MAX_WIDTH_PERCENT);

            textFlow.setMinWidth(MESSAGE_MIN_WIDTH);
            textFlow.setPadding(new javafx.geometry.Insets(8));
            textFlow.setStyle("-fx-border-radius: 10px; -fx-background-radius: 10px;");

            switch (message.getSender()) {
                case "user":
                    messageContainer.setAlignment(Pos.CENTER_RIGHT);
                    // [FIX] 2. For a Text node, the color property is '-fx-fill', not '-fx-text-fill'.
                    textNode.setStyle("-fx-fill: white;");
                    textFlow.setStyle(textFlow.getStyle() + "-fx-background-color: #007bff;");
                    break;
                case "model":
                case "assistant":
                    messageContainer.setAlignment(Pos.CENTER_LEFT);
                    textNode.setStyle("-fx-fill: white;");
                    textFlow.setStyle(textFlow.getStyle() + "-fx-background-color: #4EB052;");
                    break;
                default: // "system" messages
                    messageContainer.setAlignment(Pos.CENTER);
                    textNode.setStyle("-fx-fill: gray; -fx-font-style: italic;");
                    textFlow.setStyle(textFlow.getStyle() + "-fx-background-color: transparent;");
                    textFlow.setPadding(new javafx.geometry.Insets(0));
                    break;
            }

            if (message.isDeletable()) {
                // This part remains completely unchanged
                JFXButton deleteButton = new JFXButton("X");
                deleteButton.setStyle("-fx-background-color: #ff6666; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 0 5 0 5; -fx-min-width: 25px; -fx-max-width: 25px; -fx-min-height: 25px; -fx-max-height: 25px; -fx-background-radius: 12.5; -fx-border-radius: 12.5;");
                deleteButton.setOnAction(event -> viewModel.deleteMessage(message.getId()));

                HBox buttonWrapper = new HBox(deleteButton);
                buttonWrapper.setAlignment(Pos.CENTER);

                if ("user".equals(message.getSender())) {
                    buttonWrapper.setPadding(new javafx.geometry.Insets(0, 5, 0, 0));
                    messageContainer.getChildren().addAll(buttonWrapper, textFlow);
                } else {
                    buttonWrapper.setPadding(new javafx.geometry.Insets(0, 0, 0, 5));
                    messageContainer.getChildren().addAll(textFlow, buttonWrapper);
                }
            } else {
                messageContainer.getChildren().add(textFlow);
            }
            chatDisplayArea.getChildren().add(messageContainer);
        });
    }

    private void removeMessageFromDisplay(UUID messageIdToDelete) {
        Platform.runLater(() -> {
            HBox messageHBox = displayedMessageNodes.get(messageIdToDelete);
            if (messageHBox != null) {
                chatDisplayArea.getChildren().remove(messageHBox);
                displayedMessageNodes.remove(messageIdToDelete);
            } else {
                System.err.println("Could not find HBox for message ID: " + messageIdToDelete);
            }
        });
    }
}