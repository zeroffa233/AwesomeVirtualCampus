package app.vcampus.client.viewmodel;

import app.vcampus.client.gateway.ChatClient;
import app.vcampus.client.repository.FakeRepository;
import app.vcampus.server.entity.Comment;
import app.vcampus.server.entity.Identity;
import app.vcampus.server.entity.Message;
import app.vcampus.server.enums.ChatTopic;
import app.vcampus.server.utility.ChatState;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 聊天视图模型。
 * 负责处理聊天主界面的逻辑，包括轮询更新、发送消息、点赞、评论和UI数据绑定。
 */
public class ChatViewModel {

    private final ObservableList<MessageViewModel> messages = FXCollections.observableArrayList();
    private final StringProperty userInput = new SimpleStringProperty("");
    private final ObservableList<ChatTopic> availableTopics = FXCollections.observableArrayList();
    private final ObjectProperty<ChatTopic> selectedTopic = new SimpleObjectProperty<>();
    private final StringProperty currentUserNickname = new SimpleStringProperty("加载中...");

    private final ChatClient chatClient = ChatClient.getInstance();
    private Integer currentUserCardNum = FakeRepository.user.getCardNum();
    private Timeline pollingTimeline;

    /**
     * 构造函数。
     * 初始化可用的聊天主题列表和默认选择，并添加主题切换的监听器。
     */
    public ChatViewModel() {
        availableTopics.setAll(ChatTopic.values());
        selectedTopic.set(ChatTopic.GENERAL);

        selectedTopic.addListener((obs, oldTopic, newTopic) -> {
            if (newTopic != null && !newTopic.equals(oldTopic)) {
                fetchAndUpdateState();
            }
        });
    }

    /**
     * 获取消息列表。
     *
     * @return 消息视图模型的 ObservableList。
     */
    public ObservableList<MessageViewModel> getMessages() { return messages; }

    /**
     * 获取用户输入属性。
     *
     * @return 用户输入的 StringProperty。
     */
    public StringProperty userInputProperty() { return userInput; }

    /**
     * 获取可用的聊天主题列表。
     *
     * @return 聊天主题的 ObservableList。
     */
    public ObservableList<ChatTopic> getAvailableTopics() { return availableTopics; }

    /**
     * 获取当前选定主题的属性。
     *
     * @return 当前选定主题的 ObjectProperty。
     */
    public ObjectProperty<ChatTopic> selectedTopicProperty() { return selectedTopic; }

    /**
     * 获取当前用户昵称的属性。
     *
     * @return 当前用户昵称的 StringProperty。
     */
    public StringProperty currentUserNicknameProperty() { return currentUserNickname;}

    /**
     * 开始轮询以获取聊天室状态更新。
     */
    public void startPolling() {
        if (pollingTimeline != null && pollingTimeline.getStatus() == Timeline.Status.RUNNING) {
            return;
        }

        fetchAndUpdateState();

        pollingTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> fetchAndUpdateState()));
        pollingTimeline.setCycleCount(Timeline.INDEFINITE);
        pollingTimeline.play();
    }

    /**
     * 停止轮询。
     */
    public void stopPolling() {
        if (pollingTimeline != null) {
            pollingTimeline.stop();
        }
    }

    /**
     * 发送消息。
     */
    public void sendMessage() {
        String content = userInput.get().trim();
        if (content.isEmpty()) return;
        if (selectedTopic.get() == null) return;
        String topicId = selectedTopic.get().getTopicId();

        userInput.set("");

        new Thread(() -> {
            try {
                chatClient.postMessage(topicId, content);
                Platform.runLater(this::forceRefresh);
            } catch (Exception e) {
                System.err.println("Failed to send message: " + e.getMessage());
            }
        }).start();
    }

    /**
     * 更新用户名。
     *
     * @param newName  新用户名。
     * @param callback 操作完成后的回调函数。
     */
    public void updateUsername(String newName, Consumer<Boolean> callback) {
        new Thread(() -> {
            try {
                chatClient.updateUsername(newName);
                Platform.runLater(() -> {
                    forceRefresh();
                    callback.accept(true);
                });
            } catch (Exception e) {
                System.err.println("Failed to update username: " + e.getMessage());
                Platform.runLater(() -> callback.accept(false));
            }
        }).start();
    }

    /**
     * 切换帖子的点赞状态。
     *
     * @param mvm 要操作的消息视图模型。
     */
    public void toggleMessageLike(MessageViewModel mvm) {
        new Thread(() -> {
            try {
                chatClient.toggleMessageLike(mvm.getId());
                Platform.runLater(this::forceRefresh);
            } catch (Exception e) {
                System.err.println("Failed to toggle message like: " + e.getMessage());
            }
        }).start();
    }

    /**
     * 发表评论。
     *
     * @param mvm     要评论的消息视图模型。
     * @param content 评论内容。
     */
    public void postComment(MessageViewModel mvm, String content) {
        if (content == null || content.trim().isEmpty()) return;
        new Thread(() -> {
            try {
                chatClient.postComment(mvm.getId(), content);
                Platform.runLater(this::forceRefresh);
            } catch (Exception e) {
                System.err.println("Failed to post comment: " + e.getMessage());
            }
        }).start();
    }

    /**
     * 切换评论的点赞状态。
     *
     * @param cvm 要操作的评论视图模型。
     */
    public void toggleCommentLike(CommentViewModel cvm) {
        new Thread(() -> {
            try {
                chatClient.toggleCommentLike(cvm.getId());
                Platform.runLater(this::forceRefresh);
            } catch (Exception e) {
                System.err.println("Failed to toggle comment like: " + e.getMessage());
            }
        }).start();
    }

    private void forceRefresh() {
        if (pollingTimeline != null) {
            pollingTimeline.stop();
        }

        fetchAndUpdateState();

        if (pollingTimeline != null) {
            pollingTimeline.play();
        }
    }

    private void fetchAndUpdateState() {
        if (selectedTopic.get() == null) return;
        String topicId = selectedTopic.get().getTopicId();
        new Thread(() -> {
            try {
                ChatState newState = chatClient.getChatRoomState(topicId);
                Platform.runLater(() -> processStateUpdate(newState));
            } catch (Exception e) {
                System.err.println("Error fetching chat state: " + e.getMessage());
            }
        }).start();
    }

    private void processStateUpdate(ChatState state) {
        if (state == null || state.getMessages() == null) {
            System.err.println("Received null or invalid chat state from server. Aborting UI update to prevent data loss.");
            return;
        }

        Map<Integer, Identity> identityMap = state.getIdentities().stream()
                .collect(Collectors.toMap(Identity::getCardNum, Function.identity()));
        Identity currentUserIdentity = identityMap.get(currentUserCardNum);
        if (currentUserIdentity != null) {
            if (!currentUserNickname.get().equals(currentUserIdentity.getUserName())) {
                currentUserNickname.set(currentUserIdentity.getUserName());
            }
        }

        Map<UUID, Comment> commentMap = state.getComments().stream()
                .collect(Collectors.toMap(Comment::getId, Function.identity()));

        state.getMessages().sort(Comparator.comparing(Message::getTimestamp));

        Map<UUID, MessageViewModel> existingViewModelsMap = messages.stream()
                .collect(Collectors.toMap(MessageViewModel::getId, vm -> vm));
        Map<UUID, Message> newMessageModelMap = state.getMessages().stream()
                .collect(Collectors.toMap(Message::getId, Function.identity()));

        List<MessageViewModel> toRemove = new ArrayList<>();
        for (MessageViewModel existingVM : messages) {
            if (!newMessageModelMap.containsKey(existingVM.getId())) {
                toRemove.add(existingVM);
            }
        }
        if (!toRemove.isEmpty()) {
            messages.removeAll(toRemove);
        }

        List<MessageViewModel> toAdd = new ArrayList<>();
        for (Message messageModel : state.getMessages()) {
            MessageViewModel mvm = existingViewModelsMap.get(messageModel.getId());
            if (mvm != null) {
                mvm.update(messageModel, commentMap, identityMap, currentUserCardNum);
            } else {
                mvm = new MessageViewModel(messageModel.getId());
                mvm.update(messageModel, commentMap, identityMap, currentUserCardNum);
                toAdd.add(mvm);
            }
        }

        if (!toAdd.isEmpty()) {
            messages.addAll(toAdd);
        }

        FXCollections.sort(messages, Comparator.comparing(vm -> vm.getTimestampValue()));
    }
}