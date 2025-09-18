package app.vcampus.server.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Date;
import java.util.UUID;

/**
 * 选课记录实体类。
 * 用于记录学生选课的基本信息。
 */
@Entity
@Data
@Slf4j
@Table(name = "select_record")
public class SelectRecord implements IEntity {
    /**
     * 选课记录的唯一标识符，作为主键。
     */
    @Id
    public UUID uuid;

    /**
     * 关联的教学班的 UUID。
     */
    @Column(nullable = false)
    public UUID classUuid;

    /**
     * 选课学生的卡号。
     */
    @Column(nullable = false)
    public Integer cardNumber;

    /**
     * 学生的成绩，以 JSON 格式存储。
     */
    @JdbcTypeCode(SqlTypes.JSON)
    public Grades grade;

    /**
     * 选课时间。
     */
    public Date selectTime;

    /**
     * 关联的学生对象，瞬态字段，不映射到数据库。
     */
    @Transient
    public Student student;
}