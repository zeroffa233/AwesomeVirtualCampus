package app.vcampus.server.entity;

import app.vcampus.server.enums.Gender;
import app.vcampus.server.enums.PoliticalStatus;
import app.vcampus.server.enums.StudentStatus;
import app.vcampus.server.utility.DateUtility;
import jakarta.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Map;

/**
 * 学生实体类。
 * 映射到数据库中的 `student` 表。
 */
@Entity
@Data
@Table(name = "student")
@Slf4j
public class Student implements IEntity {
    /**
     * 学生的卡号，作为主键。
     */
    @Id
    @Column(name = "cardNumber")
    public Integer cardNumber;

    /**
     * 学生的学号。
     */
    @Column(nullable = false)
    public String studentNumber;

    /**
     * 学生的姓。
     */
    @Column(nullable = false)
    public String familyName;

    /**
     * 学生的名。
     */
    @Column(nullable = false)
    public String givenName;

    /**
     * 学生的性别。
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Gender gender;

    /**
     * 学生的出生日期。
     */
    public Date birthDate;

    /**
     * 学生的专业。
     */
    public String major;

    /**
     * 学生的学院。
     */
    public String school;

    /**
     * 学生的学籍状态。
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public StudentStatus status;

    /**
     * 学生的籍贯。
     */
    public String birthPlace;

    /**
     * 学生的政治面貌。
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public PoliticalStatus politicalStatus;


    /**
     * 根据 User 对象创建一个新的 Student 对象。
     * 主要用于管理员添加新学生时初始化学生信息。
     *
     * @param user 用户对象。
     * @return 初始化后的学生对象。
     */
    public static Student getStudent(User user) {
        Student student = new Student();
        student.setCardNumber(user.getCardNum());
        student.setStudentNumber("");
        student.setFamilyName("");
        student.setGivenName("");
        student.setGender(user.getGender());
        student.setBirthDate(null);
        student.setMajor("");
        student.setSchool("");
        student.setStatus(StudentStatus.inSchool);
        student.setBirthPlace("");
        student.setPoliticalStatus(PoliticalStatus.Masses);
        return student;
    }

}