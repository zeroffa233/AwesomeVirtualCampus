package app.vcampus.client.viewmodel;

import app.vcampus.server.entity.Comment;
import app.vcampus.server.entity.Identity;
import javafx.beans.property.*;

import java.util.Map;
import java.util.UUID;

/**
 * 评论视图模型。
 * <p>
 * 代表UI上显示的一条评论。
 * 它包装了原始的 Comment 实体，并提供了用于数据绑定的 JavaFX 属性。
 * </p>
 */
public class CommentViewModel {
    private final UUID id;
    private final StringProperty uploaderName = new SimpleStringProperty();
    private final StringProperty content = new SimpleStringProperty();
    private final IntegerProperty likeCount = new SimpleIntegerProperty();
    private final BooleanProperty isLikedByMe = new SimpleBooleanProperty();

    /**
     * 构造函数。
     *
     * @param id 评论的UUID。
     */
    public CommentViewModel(UUID id) {
        this.id = id;
    }

    /**
     * 根据新的数据模型和上下文信息更新视图模型。
     *
     * @param model              来自服务器的新 Comment 数据。
     * @param identityMap        所有用户身份的Map，用于查找用户名。
     * @param currentUserCardNum 当前登录用户的卡号。
     */
    public void update(Comment model, Map<Integer, Identity> identityMap, Integer currentUserCardNum) {
        Identity uploader = identityMap.get(model.getUploaderCardNum());
        this.uploaderName.set(uploader != null ? uploader.getUserName() : "未知用户");

        this.content.set(model.getContent());
        this.likeCount.set(model.getLikeList().size());
        this.isLikedByMe.set(model.getLikeList().contains(currentUserCardNum));
    }

    /**
     * 获取评论的UUID。
     *
     * @return 评论的UUID。
     */
    public UUID getId() { return id; }

    /**
     * 获取评论者姓名的属性。
     *
     * @return 评论者姓名的 StringProperty。
     */
    public StringProperty uploaderNameProperty() { return uploaderName; }

    /**
     * 获取评论内容的属性。
     *
     * @return 评论内容的 StringProperty。
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
}