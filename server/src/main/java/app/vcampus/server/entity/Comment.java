package app.vcampus.server.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 评论实体类。
 * <p>
 * 代表对某条 Message 的一条评论。
 * 直接映射到数据库的 `chat_comments` 表。
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chat_comments")
public class Comment {

    /**
     * 评论的唯一标识符，作为主键，自动生成。
     */
    @Id
    private UUID id = UUID.randomUUID();

    /**
     * 评论发送的时间戳。
     */
    @Column(nullable = false)
    private long timestamp;

    /**
     * 发表评论用户的卡号。
     */
    @Column(nullable = false)
    private Integer uploaderCardNum;

    /**
     * 评论的文本内容。
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 点赞该评论的用户卡号列表。
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "comment_likes", joinColumns = @JoinColumn(name = "comment_id"))
    @Column(name = "user_card_num")
    private List<Integer> likeList = new ArrayList<>();
}