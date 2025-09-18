package app.vcampus.server.entity;


import app.vcampus.server.utility.Pair;
import app.vcampus.server.utility.TextUtility;
import jakarta.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

/**
 * 教学班实体类。
 * 映射到数据库中的 `class` 表。
 */
@Entity
@Data
@Slf4j
@Table(name = "class")
public class TeachingClass implements IEntity {
    /**
     * 教学班的唯一标识符，作为主键，自动生成。
     */
    @Id
    public UUID uuid = UUID.randomUUID();

    /**
     * 关联的课程的 UUID。
     */
    @Column(nullable = false)
    public UUID courseUuid;

    /**
     * 关联的课程对象，瞬态字段，不映射到数据库。
     */
    @Transient
    public Course course;

    /**
     * 学生的选课记录，瞬态字段，不映射到数据库。
     */
    @Transient
    public SelectRecord selectRecord;

    /**
     * 授课教师的ID。
     */
    @Column(nullable = false)
    public Integer teacherId;

    /**
     * 授课教师的姓名，瞬态字段，不映射到数据库。
     */
    @Transient
    public String teacherName;

    /**
     * 课程安排，以 JSON 格式存储。
     * 结构为：[((起始周, 结束周), (星期几, (开始节次, 结束节次))))]
     */
    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    public List<Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>>> schedule;

    /**
     * 上课地点。
     */
    @Column(nullable = false)
    public String place;

    /**
     * 课程容量。
     */
    @Column(nullable = false)
    public Integer capacity;

    /**
     * 已选人数，瞬态字段，不映射到数据库。
     */
    @Transient
    public Integer selectedCount;

    /**
     * 当前学生是否已评价该课程，瞬态字段，不映射到数据库。
     */
    @Transient
    public Boolean isEvaluated;

    /**
     * 评估结果，瞬态字段，不映射到数据库。
     * 结构为：(评估分数列表, 评论列表)
     */
    @Transient
    public Pair<List<List<Integer>>, List<String>> evaluationResult;

    /**
     * 生成人类可读的课程安排字符串。
     *
     * @return 格式化后的课程安排字符串。
     */
    public String humanReadableSchedule() {
        StringBuilder content = new StringBuilder();
        String currentSeparator = "";
        for (Pair<Pair<Integer, Integer>, Pair<Integer, Pair<Integer, Integer>>> pair : schedule) {
            content.append(currentSeparator);
            content.append(pair.getFirst().getFirst()).append("-").append(pair.getFirst().getSecond()).append(" 周，");
            content.append("周").append(TextUtility.intToChineseWeek(pair.getSecond().getFirst())).append(" ");
            content.append(pair.getSecond().getSecond().getFirst()).append("-").append(pair.getSecond().getSecond().getSecond()).append(" 节");
            currentSeparator = "\n";
        }

        return content.toString();
    }
}