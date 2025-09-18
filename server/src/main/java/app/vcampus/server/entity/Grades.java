package app.vcampus.server.entity;

import lombok.Data;

/**
 * 成绩实体类。
 * 用于记录学生的各类成绩信息。
 */
@Data
public class Grades {
    /**
     * 平时成绩。
     */
    public Integer general;
    /**
     * 期中成绩。
     */
    public Integer midterm;
    /**
     * 期末考试成绩。
     */
    public Integer finalExam;
    /**
     * 总成绩。
     */
    public Integer total;

    /**
     * 班级最高分。
     */
    public Integer classMax;
    /**
     * 班级最低分。
     */
    public Integer classMin;
    /**
     * 班级平均分。
     */
    public Double classAvg;
}