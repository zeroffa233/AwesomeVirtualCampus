package app.vcampus.server.enums;

import lombok.Getter;

/**
 * 学生学籍状态枚举。
 */
@Getter
public enum StudentStatus implements LabelledEnum {
    /**
     * 在籍。
     */
    inSchool("在籍"),
    /**
     * 毕业。
     */
    graduated("毕业"),
    /**
     * 退学。
     */
    dropout("退学"),
    /**
     * 休学。
     */
    suspended("休学"),
    /**
     * 开除。
     */
    expelled("开除");

    /**
     * 枚举的中文标签。
     */
    private String label;

    /**
     * 构造函数。
     *
     * @param label 中文标签。
     */
    StudentStatus(String label) {
        this.label = label;
    }
}