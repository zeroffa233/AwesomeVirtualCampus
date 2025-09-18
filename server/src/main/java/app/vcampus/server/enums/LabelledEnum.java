package app.vcampus.server.enums;

/**
 * 带标签的枚举接口。
 * 实现此接口的枚举需要提供一个获取其显示标签的方法。
 */
public interface LabelledEnum {
    /**
     * 获取枚举的显示标签。
     *
     * @return 标签字符串。
     */
    String getLabel();
}