package app.vcampus.server.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Message 实体，代表聊天室中的一条主消息（帖子）。
 * 直接映射到数据库的 'chat_messages' 表。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chat_messages")
public class Message {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private long timestamp;

    @Column(nullable = false)
    private Integer uploaderCardNum;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String topicId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "message_likes", joinColumns = @JoinColumn(name = "message_id"))
    @Column(name = "card_num")
    private List<Integer> likeList = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "message_comment_ids", joinColumns = @JoinColumn(name = "message_id"))
    @Column(name = "comment_id")
    private List<UUID> commentIds = new ArrayList<>();
}