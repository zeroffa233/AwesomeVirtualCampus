package app.vcampus.server.enums;

import lombok.Getter;

/**
 * 交易类型枚举。
 */
@Getter
public enum TransactionType implements LabelledEnum {
    /**
     * 充值。
     */
    deposit("充值"),
    /**
     * 支出。
     */
    payment("支出");

    /**
     * 枚举的中文标签。
     */
    private final String label;

    /**
     * 构造函数。
     *
     * @param label 中文标签。
     */
    TransactionType(String label) {
        this.label = label;
    }
}