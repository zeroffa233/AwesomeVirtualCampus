package app.vcampus.server.enums;

/**
 * 聊天主题枚举。
 * 定义了不同的聊天区域及其标识。
 */
public enum ChatTopic {
    /**
     * 综合闲聊区。
     */
    GENERAL("topic1", "综合闲聊区"),
    /**
     * 学术交流区。
     */
    ACADEMIC("topic2", "学术交流区"),
    /**
     * 生活资讯区。
     */
    LIFESTYLE("topic3", "生活资讯区"),
    /**
     * 校园活动区。
     */
    ACTIVITIES("topic4", "校园活动区");

    /**
     * 主题的唯一标识符。
     */
    private final String topicId;
    /**
     * 主题的显示名称。
     */
    private final String displayName;

    /**
     * 构造函数。
     *
     * @param topicId     主题ID。
     * @param displayName 显示名称。
     */
    ChatTopic(String topicId, String displayName) {
        this.topicId = topicId;
        this.displayName = displayName;
    }

    /**
     * 获取主题ID。
     *
     * @return 主题ID。
     */
    public String getTopicId() {
        return topicId;
    }

    /**
     * 获取显示名称。
     *
     * @return 显示名称。
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 重写 toString() 方法，以便在 JFXComboBox 中能直接显示 displayName。
     *
     * @return 显示名称。
     */
    @Override
    public String toString() {
        return this.displayName;
    }
}