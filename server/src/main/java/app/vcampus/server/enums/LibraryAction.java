package app.vcampus.server.enums;

import lombok.Getter;

/**
 * 图书馆操作枚举。
 * 注意：此枚举当前未被使用。
 */
@Getter
public enum LibraryAction implements LabelledEnum {
    /**
     * 借出。
     */
    borrow("借出"),
    /**
     * 还书。
     */
    returnBook("还书");

    /**
     * 枚举的中文标签。
     */
    private final String label;

    /**
     * 构造函数。
     *
     * @param label 中文标签。
     */
    LibraryAction(String label) {
        this.label = label;
    }

}