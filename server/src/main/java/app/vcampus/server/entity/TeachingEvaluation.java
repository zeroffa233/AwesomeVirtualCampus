package app.vcampus.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

/**
 * 教学评估实体类。
 * 映射到数据库中的 `teaching_evaluation` 表。
 */
@Entity
@Data
@Table(name = "teaching_evaluation")
@Slf4j
public class TeachingEvaluation implements IEntity {
    /**
     * 评估记录的唯一标识符，作为主键。
     */
    @Id
    public UUID uuid;

    /**
     * 关联的教学班的 UUID。
     */
    @Column(nullable = false)
    public UUID classUuid;

    /**
     * 进行评估的学生的ID。
     */
    @Column(nullable = false)
    public Integer studentId;

    /**
     * 评估结果，以整数列表形式存储为 JSON。
     */
    @JdbcTypeCode(SqlTypes.JSON)
    public List<Integer> result;

    /**
     * 学生的文字评论。
     */
    @Column(columnDefinition = "TEXT")
    public String comment;
}