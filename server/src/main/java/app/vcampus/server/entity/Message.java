package app.vcampus.server.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 消息实体类。
 * <p>
 * 代表聊天室中的一条主消息（帖子）。
 * 直接映射到数据库的 `chat_messages` 表。
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chat_messages")
public class Message {

    /**
     * 消息的唯一标识符，作为主键，自动生成。
     */
    @Id
    private UUID id = UUID.randomUUID();

    /**
     * 消息发送的时间戳。
     */
    @Column(nullable = false)
    private long timestamp;

    /**
     * 发帖用户的卡号。
     */
    @Column(nullable = false)
    private Integer uploaderCardNum;

    /**
     * 消息的文本内容。
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 消息所属的主题ID。
     */
    @Column(nullable = false)
    private String topicId;

    /**
     * 点赞该消息的用户卡号列表。
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "message_likes", joinColumns = @JoinColumn(name = "message_id"))
    @Column(name = "card_num")
    private List<Integer> likeList = new ArrayList<>();

    /**
     * 该消息下的评论ID列表。
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "message_comment_ids", joinColumns = @JoinColumn(name = "message_id"))
    @Column(name = "comment_id")
    private List<UUID> commentIds = new ArrayList<>();
}