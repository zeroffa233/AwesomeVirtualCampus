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

public class ChatViewModel {

    // --- Properties for UI Binding ---
    private final ObservableList<MessageViewModel> messages = FXCollections.observableArrayList();
    private final StringProperty userInput = new SimpleStringProperty("");
    private final ObservableList<ChatTopic> availableTopics = FXCollections.observableArrayList();
    private final ObjectProperty<ChatTopic> selectedTopic = new SimpleObjectProperty<>();
    private final StringProperty currentUserNickname = new SimpleStringProperty("加载中...");

    // --- Dependencies & State ---
    private final ChatClient chatClient = ChatClient.getInstance();
    private Integer currentUserCardNum = FakeRepository.user.getCardNum(); // Placeholder for the logged-in user's card number
    private Timeline pollingTimeline;

    public ChatViewModel() {
        // 初始化可选的聊天室列表
        availableTopics.setAll(ChatTopic.values());
        // 设置默认选中的聊天室
        selectedTopic.set(ChatTopic.GENERAL);

        // 添加监听器，当用户切换聊天室时自动刷新
        selectedTopic.addListener((obs, oldTopic, newTopic) -> {
            if (newTopic != null && !newTopic.equals(oldTopic)) {
                // UI清理工作已交由 Controller 处理。
                // 这里只负责获取新聊天室的数据。
                fetchAndUpdateState();
            }
        });
    }

    // --- Property Getters for Controller ---
    public ObservableList<MessageViewModel> getMessages() { return messages; }
    public StringProperty userInputProperty() { return userInput; }
    public ObservableList<ChatTopic> getAvailableTopics() { return availableTopics; }
    public ObjectProperty<ChatTopic> selectedTopicProperty() { return selectedTopic; }
    public StringProperty currentUserNicknameProperty() { return currentUserNickname;}

        // --- Lifecycle Management ---
    public void startPolling() {
        if (pollingTimeline != null && pollingTimeline.getStatus() == Timeline.Status.RUNNING) {
            return; // Already running
        }

        // ADDED: 启动时立即执行一次加载，解决初始延迟问题
        fetchAndUpdateState();

        // 轮询间隔设置为5秒
        pollingTimeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> fetchAndUpdateState()));
        pollingTimeline.setCycleCount(Timeline.INDEFINITE);
        pollingTimeline.play();
    }

    public void stopPolling() {
        if (pollingTimeline != null) {
            pollingTimeline.stop();
        }
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

    // --- Core Logic: Data Fetching and Processing ---
    private void fetchAndUpdateState() {
        if (selectedTopic.get() == null) return;
        String topicId = selectedTopic.get().getTopicId();
        // Perform network operations on a background thread
        new Thread(() -> {
            try {
                ChatState newState = chatClient.getChatRoomState(topicId);
                // Switch back to the JavaFX Application Thread to update UI-bound properties
                Platform.runLater(() -> processStateUpdate(newState));
            } catch (Exception e) {
                System.err.println("Error fetching chat state: " + e.getMessage());
            }
        }).start();
    }

    private void processStateUpdate(ChatState state) {
        // 防御性检查，防止因收到空数据而清空UI
        if (state == null || state.getMessages() == null) {
            System.err.println("Received null or invalid chat state from server. Aborting UI update to prevent data loss.");
            return;
        }

        // 1. 准备高效处理所需的数据结构
        Map<Integer, Identity> identityMap = state.getIdentities().stream()
                .collect(Collectors.toMap(Identity::getCardNum, Function.identity()));
        Identity currentUserIdentity = identityMap.get(currentUserCardNum);
        if (currentUserIdentity != null) {
            // 仅当昵称实际发生变化时才更新属性，避免不必要的UI刷新
            if (!currentUserNickname.get().equals(currentUserIdentity.getUserName())) {
                currentUserNickname.set(currentUserIdentity.getUserName());
            }
        }

        Map<UUID, Comment> commentMap = state.getComments().stream()
                .collect(Collectors.toMap(Comment::getId, Function.identity()));

        // 将服务器返回的新消息按时间戳排序
        state.getMessages().sort(Comparator.comparing(Message::getTimestamp));

        // ******************** START OF THE FIX ********************
        // 这是修改的核心逻辑，采用差异化更新而非 setAll()

        // 2. 获取当前UI已有的MessageViewModel Map和新数据的ID Set，用于比较
        Map<UUID, MessageViewModel> existingViewModelsMap = messages.stream()
                .collect(Collectors.toMap(MessageViewModel::getId, vm -> vm));
        Map<UUID, Message> newMessageModelMap = state.getMessages().stream()
                .collect(Collectors.toMap(Message::getId, Function.identity()));

        // 3. 移除本地存在但在新数据中已不存在的消息 (例如被管理员删除的消息)
        List<MessageViewModel> toRemove = new ArrayList<>();
        for (MessageViewModel existingVM : messages) {
            if (!newMessageModelMap.containsKey(existingVM.getId())) {
                toRemove.add(existingVM);
            }
        }
        if (!toRemove.isEmpty()) {
            messages.removeAll(toRemove);
        }

        // 4. 更新已存在的消息，并识别出需要新增的消息
        List<MessageViewModel> toAdd = new ArrayList<>();
        for (Message messageModel : state.getMessages()) {
            MessageViewModel mvm = existingViewModelsMap.get(messageModel.getId());
            if (mvm != null) {
                // 如果消息已存在，则只更新其内部数据
                // UI会自动响应ViewModel内部属性的变化
                mvm.update(messageModel, commentMap, identityMap, currentUserCardNum);
            } else {
                // 如果是新消息，则创建新的ViewModel并准备添加
                mvm = new MessageViewModel(messageModel.getId());
                mvm.update(messageModel, commentMap, identityMap, currentUserCardNum);
                toAdd.add(mvm);
            }
        }

        // 5. 一次性添加所有新消息
        if (!toAdd.isEmpty()) {
            messages.addAll(toAdd);
        }

        // 6. 确保最终列表的顺序与服务器一致
        // 注意：这一步很重要，因为新消息可能不是按顺序插入的
        FXCollections.sort(messages, Comparator.comparing(vm -> vm.getTimestampValue()));

        // ********************* END OF THE FIX *********************
    }

    // --- User Action Handlers (to be called by Controller) ---
    public void sendMessage() {
        String content = userInput.get().trim();
        if (content.isEmpty()) return;
        if (selectedTopic.get() == null) return;
        String topicId = selectedTopic.get().getTopicId();

        userInput.set(""); // Clear input immediately for better user experience

        new Thread(() -> {
            try {
                chatClient.postMessage(topicId, content);
                Platform.runLater(this::forceRefresh);
            } catch (Exception e) {
                System.err.println("Failed to send message: " + e.getMessage());
            }
        }).start();
    }

    public void updateUsername(String newName, Consumer<Boolean> callback) {
        new Thread(() -> {
            try {
                // 假设 chatClient.updateUsername 会在失败时抛出异常
                chatClient.updateUsername(newName);
                // 成功后，在主线程执行回调和刷新
                Platform.runLater(() -> {
                    forceRefresh(); // 强制刷新以获取包含新昵称的 ChatState
                    callback.accept(true); // 通知 Controller 成功
                });
            } catch (Exception e) {
                System.err.println("Failed to update username: " + e.getMessage());
                // 失败后，在主线程执行回调
                Platform.runLater(() -> callback.accept(false)); // 通知 Controller 失败
            }
        }).start();
    }

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
}