package app.vcampus.server.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * GPT 上下文实体类。
 * 映射到数据库中的 `gpt` 表，用于存储用户的 GPT 对话历史。
 */
@Entity
@Data
@Table(name = "gpt")
@Slf4j
public class GptContext implements IEntity {
    /**
     * 用户的卡号，作为主键。
     */
    @Id
    public Integer cardNumber = 0;

    /**
     * 存储的对话上下文，通常为 JSON 格式的长文本。
     */
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    public String context;
}