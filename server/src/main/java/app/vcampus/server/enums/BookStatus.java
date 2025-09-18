package app.vcampus.server.enums;

import lombok.Getter;

/**
 * 图书状态枚举。
 * 用于表示图书的当前状态，并关联一个用于UI显示的颜色。
 */
@Getter
public enum BookStatus implements LabelledEnum {
    /**
     * 可借状态，显示为绿色。
     */
    available("可借", 0xff508e54),
    /**
     * 已借出状态，显示为黄色。
     */
    lend("借出", 0xffd9b44a),
    /**
     * 新书（未上架）状态，显示为黄色。
     */
    newly("新书（未上架）", 0xffd9b44a),
    /**
     * 已归还（未上架）状态，显示为黄色。
     */
    returned("归还（未上架）", 0xffd9b44a);

    /**
     * 状态的中文标签。
     */
    private final String label;
    /**
     * 用于UI显示的颜色值。
     */
    private final Integer color;

    /**
     * 构造函数。
     *
     * @param label 状态的中文标签。
     * @param color 相关的颜色值。
     */
    BookStatus(String label, Integer color) {
        this.label = label;
        this.color = color;
    }

}