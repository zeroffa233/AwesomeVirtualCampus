package app.vcampus.server.enums;

import lombok.Getter;

/**
 * 性别枚举。
 */
@Getter
public enum Gender implements LabelledEnum {
    /**
     * 男性。
     */
    male("男"),
    /**
     * 女性。
     */
    female("女"),
    /**
     * 未指定。
     */
    unspecified("未指定");

    /**
     * 枚举的中文标签。
     */
    private final String label;

    /**
     * 构造函数。
     *
     * @param label 中文标签。
     */
    Gender(String label) {
        this.label = label;
    }
}