package app.vcampus.server.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ChatRoom 实体，代表一个聊天室主题，作为 Message 的容器。
 * 直接映射到数据库的 'chat_rooms' 表。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chat_rooms")
public class ChatRoom {

    @Id
    private String topicId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "chatroom_message_ids", joinColumns = @JoinColumn(name = "topic_id"))
    @Column(name = "message_id")
    private List<UUID> messageIds = new ArrayList<>();
}