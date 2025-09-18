package app.vcampus.server.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Excel 学生名单实体类。
 * 用于 EasyExcel 导入导出学生名单数据。
 */
@Data
@AllArgsConstructor
public class ExcelStudentList {
    /**
     * 学号，对应 Excel 中的“学号”列。
     */
    @ExcelProperty("学号")
    public String studentNumber;

    /**
     * 姓名，对应 Excel 中的“姓名”列。
     */
    @ExcelProperty("姓名")
    public String name;
}