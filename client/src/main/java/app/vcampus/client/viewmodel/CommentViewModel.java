package app.vcampus.client.viewmodel;

import app.vcampus.server.entity.Comment;
import app.vcampus.server.entity.Identity; // 导入Identity
import javafx.beans.property.*;

import java.util.Map; // 导入Map
import java.util.UUID;

public class CommentViewModel {
    private final UUID id;
    private final StringProperty uploaderName = new SimpleStringProperty();
    private final StringProperty content = new SimpleStringProperty();
    private final IntegerProperty likeCount = new SimpleIntegerProperty();
    private final BooleanProperty isLikedByMe = new SimpleBooleanProperty();

    public CommentViewModel(UUID id) {
        this.id = id;
    }

    /**
     * 【修改点】: 修改update方法的签名和实现
     * @param model The new Comment data from the server.
     * @param identityMap A map of all user identities for name lookup.
     * @param currentUserCardNum The card number of the currently logged-in user.
     */
    public void update(Comment model, Map<Integer, Identity> identityMap, Integer currentUserCardNum) {
        // 从identityMap中查找用户名
        Identity uploader = identityMap.get(model.getUploaderCardNum());
        this.uploaderName.set(uploader != null ? uploader.getUserName() : "未知用户");

        this.content.set(model.getContent());
        this.likeCount.set(model.getLikeList().size());

        // 判断点赞状态，现在比较的是currentUserCardNum
        this.isLikedByMe.set(model.getLikeList().contains(currentUserCardNum));
    }

    // --- Getters for Properties ---
    public UUID getId() { return id; }
    public StringProperty uploaderNameProperty() { return uploaderName; }
    public StringProperty contentProperty() { return content; }
    public IntegerProperty likeCountProperty() { return likeCount; }
    public BooleanProperty isLikedByMeProperty() { return isLikedByMe; }
}