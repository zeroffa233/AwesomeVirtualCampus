package app.vcampus.server.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Excel 学生成绩实体类。
 * 用于 EasyExcel 导入导出学生成绩数据。
 */
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class ExcelStudentGrade {
    /**
     * 学号，对应 Excel 中的“学号”列。
     */
    @ExcelProperty("学号")
    @NonNull
    public String studentNumber;

    /**
     * 姓名，对应 Excel 中的“姓名”列。
     */
    @ExcelProperty("姓名")
    @NonNull
    public String name;

    /**
     * 平时分，对应 Excel 中的“平时分”列。
     */
    @ExcelProperty("平时分")
    public Integer general;

    /**
     * 期中分，对应 Excel 中的“期中分”列。
     */
    @ExcelProperty("期中分")
    public Integer midterm;

    /**
     * 期末分，对应 Excel 中的“期末分”列。
     */
    @ExcelProperty("期末分")
    public Integer finalExam;

    /**
     * 总评分，对应 Excel 中的“总评分”列。
     */
    @ExcelProperty("总评分")
    public Integer total;

}