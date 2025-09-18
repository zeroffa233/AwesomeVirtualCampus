package app.vcampus.server.enums;

import lombok.Getter;

/**
 * 一卡通状态枚举。
 */
@Getter
public enum CardStatus implements LabelledEnum {
    /**
     * 正常。
     */
    normal("正常"),
    /**
     * 冻结。
     */
    frozen("冻结");

    /**
     * 枚举的中文标签。
     */
    private final String label;

    /**
     * 构造函数。
     *
     * @param label 中文标签。
     */
    CardStatus(String label) {
        this.label = label;
    }
}