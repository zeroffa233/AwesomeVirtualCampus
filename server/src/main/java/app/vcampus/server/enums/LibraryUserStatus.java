package app.vcampus.server.enums;

import lombok.Getter;

/**
 * 图书馆用户状态枚举。
 * 注意：此枚举当前未被使用。
 */
@Getter
public enum LibraryUserStatus {
    /**
     * 信誉合格。
     */
    nice("信誉合格"),
    /**
     * 信誉不合格。
     */
    bad("信誉不合格");

    /**
     * 枚举的中文标签。
     */
    private String label;

    /**
     * 构造函数。
     *
     * @param label 中文标签。
     */
    LibraryUserStatus(String label) {
        this.label = label;
    }
}