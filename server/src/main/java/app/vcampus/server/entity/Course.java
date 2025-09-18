package app.vcampus.server.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 课程实体类。
 * 映射到数据库中的 `course` 表。
 */
@Entity
@Data
@Table(name = "course")
@Slf4j
public class Course implements IEntity {
    /**
     * 课程的唯一标识符，作为主键。
     */
    @Id
    public UUID uuid;

    /**
     * 课程编号。
     */
    @Column(nullable = false)
    public String courseId;

    /**
     * 课程名称。
     */
    @Column(nullable = false)
    public String courseName;

    /**
     * 开课学院。
     */
    @Column(nullable = false)
    public String school;

    /**
     * 课程学分。
     */
    public float credit;

    /**
     * 该课程下的所有教学班列表，瞬态字段，不映射到数据库。
     */
    @Transient
    public List<TeachingClass> teachingClasses;

}