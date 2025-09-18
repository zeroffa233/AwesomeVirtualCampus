package app.vcampus.server.enums;

import lombok.Getter;

@Getter
public enum TransactionType implements LabelledEnum {
    deposit("充值"),
    credit("支出");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }
}
