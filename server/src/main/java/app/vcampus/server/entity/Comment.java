package app.vcampus.server.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Comment 实体，代表对某条 Message 的一条评论。
 * 直接映射到数据库的 'chat_comments' 表。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chat_comments")
public class Comment {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false)
    private long timestamp;

    // 【修改点 1】: 将 uploaderName 替换为 uploaderCardNum
    @Column(nullable = false)
    private Integer uploaderCardNum;

    @Column(columnDefinition = "TEXT")
    private String content;

    // 【修改点 2】: 点赞列表存储 Integer (cardNum) 而不是 String (userName)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "comment_likes", joinColumns = @JoinColumn(name = "comment_id"))
    @Column(name = "user_card_num") // 列名也建议修改以保持清晰
    private List<Integer> likeList = new ArrayList<>();
}