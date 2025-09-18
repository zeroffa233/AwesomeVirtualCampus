package app.vcampus.server.utility;

import lombok.Data;

/**
 * 通用配对类。
 * 用于存储一对异构或同构的对象。
 *
 * @param <F> 第一个元素的类型。
 * @param <S> 第二个元素的类型。
 */
@Data
public class Pair<F, S> {
    /**
     * 对中的第一个元素。
     */
    F first;
    /**
     * 对中的第二个元素。
     */
    S second;

    /**
     * 构造一个包含指定元素的配对。
     *
     * @param first  第一个元素。
     * @param second 第二个元素。
     */
    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    /**
     * 默认构造函数。
     * 创建一个两个元素都为 null 的配对。
     */
    public Pair() {

    }
}