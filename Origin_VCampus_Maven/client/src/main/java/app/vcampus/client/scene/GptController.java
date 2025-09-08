package app.vcampus.client.scene;

import app.vcampus.client.viewmodel.GptViewModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label; // Using Label for text to easily wrap and style
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.event.ActionEvent;
import javafx.scene.input.KeyCode;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GptController implements Initializable {
    @FXML
    private ScrollPane chatScrollPane;
    @FXML
    private VBox chatDisplayArea;
    @FXML
    private JFXTextArea userInputField;
    @FXML
    private JFXButton sendButton;

    private GptViewModel viewModel;
    private Map<UUID, HBox> displayedMessageNodes = new HashMap<>(); // Link UUID to displayed HBox for removal

    private static final double MESSAGE_MAX_WIDTH_PERCENT = 0.7; // 70% of chat area width
    private static final double MESSAGE_MIN_WIDTH = 50.0; // Minimum width, e.g., 50 pixels

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        viewModel = new GptViewModel();

        // Bind UI elements to ViewModel properties
        userInputField.textProperty().bindBidirectional(viewModel.userInputProperty());
        sendButton.disableProperty().bind(viewModel.sendButtonDisabledProperty());

        // Listen for changes in chatMessages and update UI
        viewModel.getChatMessages().addListener((javafx.collections.ListChangeListener.Change<? extends GptViewModel.Message> change) -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (GptViewModel.Message message : change.getAddedSubList()) {
                        addMessageToDisplay(message);
                    }
                }
                if (change.wasRemoved()) {
                    for (GptViewModel.Message message : change.getRemoved()) {
                        removeMessageFromDisplay(message.getId());
                    }
                }
            }
        });

        // Auto-scroll to the bottom
        chatScrollPane.vvalueProperty().bind(chatDisplayArea.heightProperty());

        // Add keyboard event listener for Ctrl + Enter
        userInputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && event.isControlDown()) {
                viewModel.sendMessage();
                event.consume(); // Consume event to prevent newline in JFXTextArea
            }
        });

        // Manually add initial welcome messages from ViewModel to ensure they appear
        // This is done once at initialization. Subsequent messages are handled by the listener.
        for (GptViewModel.Message message : viewModel.getChatMessages()) {
            addMessageToDisplay(message);
        }
    }

    @FXML
    private void sendMessage(ActionEvent event) {
        viewModel.sendMessage();
    }

    /**
     * Adds a message to the chat display area based on the ViewModel's Message object.
     *
     * @param message The Message object from the ViewModel.
     */
    private void addMessageToDisplay(GptViewModel.Message message) {
        Platform.runLater(() -> {
            HBox messageContainer = new HBox();
            messageContainer.setPadding(new javafx.geometry.Insets(5));
            messageContainer.setUserData(message.getId()); // Store UUID in HBox
            displayedMessageNodes.put(message.getId(), messageContainer);

            Label messageLabel = new Label();
            messageLabel.textProperty().bind(message.streamingContentProperty()); // Bind to streaming content
            messageLabel.setWrapText(true);

            TextFlow textFlow = new TextFlow(messageLabel);
            textFlow.setMaxWidth(chatDisplayArea.getWidth() * MESSAGE_MAX_WIDTH_PERCENT);
            textFlow.setMinWidth(MESSAGE_MIN_WIDTH);
            textFlow.setPadding(new javafx.geometry.Insets(8));
            textFlow.setStyle("-fx-border-radius: 10px; -fx-background-radius: 10px;");

            if ("user".equals(message.getSender())) {
                messageContainer.setAlignment(Pos.CENTER_RIGHT);
                messageLabel.setStyle("-fx-text-fill: white;");
                textFlow.setStyle(textFlow.getStyle() + "-fx-background-color: #007bff;");
            } else if ("model".equals(message.getSender())) {
                messageContainer.setAlignment(Pos.CENTER_LEFT);
                messageLabel.setStyle("-fx-text-fill: white;");
                textFlow.setStyle(textFlow.getStyle() + "-fx-background-color: #4EB052;");
            } else { // "system" messages
                messageContainer.setAlignment(Pos.CENTER);
                messageLabel.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
                textFlow.setStyle(textFlow.getStyle() + "-fx-background-color: transparent;");
                textFlow.setPadding(new javafx.geometry.Insets(0));
            }

            if (message.isDeletable()) {
                JFXButton deleteButton = new JFXButton("X");
                deleteButton.setStyle("-fx-background-color: #ff6666; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 0 5 0 5; -fx-min-width: 25px; -fx-max-width: 25px; -fx-min-height: 25px; -fx-max-height: 25px; -fx-background-radius: 12.5; -fx-border-radius: 12.5;");
                deleteButton.setUserData(message.getId());
                deleteButton.setOnAction(this::deleteMessageAction);

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

    /**
     * Removes a message's HBox from the displayed UI.
     *
     * @param messageIdToDelete The UUID of the message to remove.
     */
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

    /**
     * Handles the deletion of a message from the ViewModel.
     *
     * @param event The ActionEvent triggered by the delete button.
     */
    private void deleteMessageAction(ActionEvent event) {
        JFXButton sourceButton = (JFXButton) event.getSource();
        UUID messageIdToDelete = (UUID) sourceButton.getUserData();
        viewModel.deleteMessage(messageIdToDelete);
    }
}