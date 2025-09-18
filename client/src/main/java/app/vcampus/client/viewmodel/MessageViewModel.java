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
 * 消息视图模型。
 * <p>
 * 代表UI上显示的一条帖子及其所有评论。
 * 它包装了原始的 Message 实体，并提供了用于数据绑定的 JavaFX 属性。
 * 它还持有一个其下所有评论的视图模型列表。
 * </p>
 */
public class MessageViewModel {
    private final UUID id;
    private final StringProperty uploaderName = new SimpleStringProperty();
    private final StringProperty timestamp = new SimpleStringProperty();
    private final StringProperty content = new SimpleStringProperty();
    private final IntegerProperty likeCount = new SimpleIntegerProperty();
    private final BooleanProperty isLikedByMe = new SimpleBooleanProperty();

    private final ObservableList<CommentViewModel> comments = FXCollections.observableArrayList();
    private long timestampValue;

    /**
     * 构造函数。
     *
     * @param id 消息的UUID。
     */
    public MessageViewModel(UUID id) { this.id = id; }

    /**
     * 根据新的数据模型及其相关的评论和身份信息更新视图模型。
     * 此方法还会对其内部的评论列表执行差异更新。
     *
     * @param model              来自服务器的新 Message 数据。
     * @param allCommentsMap     所有评论的Map，用于快速查找。
     * @param identityMap        所有用户身份的Map，用于快速查找。
     * @param currentUserCardNum 当前登录用户的卡号。
     */
    public void update(Message model, Map<UUID, Comment> allCommentsMap, Map<Integer, Identity> identityMap, Integer currentUserCardNum) {
        Identity uploader = identityMap.get(model.getUploaderCardNum());
        this.uploaderName.set(uploader != null ? uploader.getUserName() : "未知用户");

        this.timestampValue = model.getTimestamp();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.timestamp.set(sdf.format(new Date(this.timestampValue)));

        this.content.set(model.getContent());
        this.likeCount.set(model.getLikeList().size());
        this.isLikedByMe.set(model.getLikeList().contains(currentUserCardNum));

        Map<UUID, CommentViewModel> existingCommentVMs = this.comments.stream()
                .collect(Collectors.toMap(CommentViewModel::getId, Function.identity()));

        List<CommentViewModel> updatedComments = model.getCommentIds().stream()
                .map(allCommentsMap::get)
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparing(Comment::getTimestamp))
                .map(commentModel -> {
                    CommentViewModel cvm = existingCommentVMs.getOrDefault(commentModel.getId(), new CommentViewModel(commentModel.getId()));
                    cvm.update(commentModel, identityMap, currentUserCardNum);
                    return cvm;
                })
                .collect(Collectors.toList());

        this.comments.setAll(updatedComments);
    }

    /**
     * 获取消息的UUID。
     *
     * @return 消息的UUID。
     */
    public UUID getId() { return id; }

    /**
     * 获取发帖人姓名的属性。
     *
     * @return 发帖人姓名的 StringProperty。
     */
    public StringProperty uploaderNameProperty() { return uploaderName; }

    /**
     * 获取格式化时间戳的属性。
     *
     * @return 时间戳的 StringProperty。
     */
    public StringProperty timestampProperty() { return timestamp; }

    /**
     * 获取消息内容的属性。
     *
     * @return 消息内容的 StringProperty。
     */
    public StringProperty contentProperty() { return content; }

    /**
     * 获取点赞数的属性。
     *
     * @return 点赞数的 IntegerProperty。
     */
    public IntegerProperty likeCountProperty() { return likeCount; }

    /**
     * 获取当前用户是否点赞的属性。
     *
     * @return 是否点赞的 BooleanProperty。
     */
    public BooleanProperty isLikedByMeProperty() { return isLikedByMe; }

    /**
     * 获取评论列表。
     *
     * @return 评论视图模型的 ObservableList。
     */
    public ObservableList<CommentViewModel> getComments() { return comments; }

    /**
     * 获取原始时间戳的值。
     *
     * @return long 类型的时间戳。
     */
    public long getTimestampValue() {return timestampValue;}
}