package app.vcampus.client.viewmodel;

import app.vcampus.server.entity.Comment;
import app.vcampus.server.entity.Identity;
import app.vcampus.server.entity.Message;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * MessageViewModel 代表UI上显示的一条帖子及其所有评论。
 * 它包装了原始的 Message 实体，并提供了用于数据绑定的 JavaFX 属性。
 * 它还持有一个其下所有评论的 ViewModel 列表。
 */
public class MessageViewModel {
    private final UUID id;
    private final StringProperty uploaderName = new SimpleStringProperty();
    private final StringProperty timestamp = new SimpleStringProperty(); // 格式化后的时间
    private final StringProperty content = new SimpleStringProperty();
    private final IntegerProperty likeCount = new SimpleIntegerProperty();
    private final BooleanProperty isLikedByMe = new SimpleBooleanProperty();

    // 列表属性，用于绑定到评论区的ListView或VBox
    private final ObservableList<CommentViewModel> comments = FXCollections.observableArrayList();
    private long timestampValue;

    public MessageViewModel(UUID id) { this.id = id; }

    /**
     * Updates the ViewModel based on new data and its related comments/identities.
     * This method also performs a diff-update on its own list of comments.
     * @param model The new Message data from the server.
     * @param allCommentsMap A map of all comments for quick lookup.
     * @param identityMap A map of user identities for quick lookup.
     * @param currentUserCardNum The card number of the currently logged-in user.
     */
    public void update(Message model, Map<UUID, Comment> allCommentsMap, Map<Integer, Identity> identityMap, Integer currentUserCardNum) {
        Identity uploader = identityMap.get(model.getUploaderCardNum());
        this.uploaderName.set(uploader != null ? uploader.getUserName() : "未知用户");

        // --- UPDATE THIS PART ---
        this.timestampValue = model.getTimestamp(); // 保存原始long值
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.timestamp.set(sdf.format(new Date(this.timestampValue))); // 使用保存的值进行格式化


        this.content.set(model.getContent());
        this.likeCount.set(model.getLikeList().size());
        this.isLikedByMe.set(model.getLikeList().contains(currentUserCardNum));

        // --- Diff-update for comments ---
        Map<UUID, CommentViewModel> existingCommentVMs = this.comments.stream()
                .collect(Collectors.toMap(CommentViewModel::getId, Function.identity()));

        List<CommentViewModel> updatedComments = model.getCommentIds().stream()
                .map(allCommentsMap::get)
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparing(Comment::getTimestamp))
                .map(commentModel -> {
                    CommentViewModel cvm = existingCommentVMs.getOrDefault(commentModel.getId(), new CommentViewModel(commentModel.getId()));

                    // 【修改点】: 调用新的update方法，传递完整的上下文
                    cvm.update(commentModel, identityMap, currentUserCardNum);

                    return cvm;
                })
                .collect(Collectors.toList());

        this.comments.setAll(updatedComments);
    }

    // --- Getters for Properties ---
    public UUID getId() { return id; }
    public StringProperty uploaderNameProperty() { return uploaderName; }
    public StringProperty timestampProperty() { return timestamp; }
    public StringProperty contentProperty() { return content; }
    public IntegerProperty likeCountProperty() { return likeCount; }
    public BooleanProperty isLikedByMeProperty() { return isLikedByMe; }
    public ObservableList<CommentViewModel> getComments() { return comments; }
    public long getTimestampValue() {return timestampValue;}
}