package app.vcampus.server.enums;

public enum ChatTopic {
    GENERAL("topic1", "综合闲聊区"),
    ACADEMIC("topic2", "学术交流区"),
    LIFESTYLE("topic3", "生活资讯区"),
    ACTIVITIES("topic4", "校园活动区");

    private final String topicId;
    private final String displayName;

    ChatTopic(String topicId, String displayName) {
        this.topicId = topicId;
        this.displayName = displayName;
    }

    public String getTopicId() {
        return topicId;
    }

    public String getDisplayName() {
        return displayName;
    }

    // 重写 toString() 方法，以便在 JFXComboBox 中能直接显示 displayName
    @Override
    public String toString() {
        return this.displayName;
    }
}
