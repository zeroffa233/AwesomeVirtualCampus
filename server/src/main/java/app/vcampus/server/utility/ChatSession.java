package app.vcampus.server.utility;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 聊天会话类。
 * 管理一个独立的聊天会话，包括会话ID、标题、消息历史等。
 */
public class ChatSession {
    /**
     * 会话的唯一标识符。
     */
    private final UUID sessionId;
    /**
     * 会话的标题。
     */
    private String title;
    /**
     * 会话最后修改的时间戳。
     */
    private long lastModified;
    /**
     * 会话的消息历史记录。
     */
    private final List<MessageEntry> messageHistory;

    /**
     * 根据会话ID构造一个新的聊天会话。
     *
     * @param sessionId 会话的唯一标识符。
     */
    public ChatSession(UUID sessionId) {
        this.sessionId = sessionId;
        this.title = "新聊天";
        this.lastModified = System.currentTimeMillis();
        this.messageHistory = new ArrayList<>();
    }

    /**
     * 复制构造函数。
     * 用于安全地复制一个 ChatSession 对象，例如在模拟客户端中保存时。
     *
     * @param other 要复制的另一个 ChatSession 对象。
     */
    public ChatSession(ChatSession other) {
        this.sessionId = other.sessionId;
        this.title = other.title;
        this.lastModified = other.lastModified;
        this.messageHistory = new ArrayList<>(other.messageHistory);
    }

    /**
     * 获取会话ID。
     *
     * @return 会话的 UUID。
     */
    public UUID getId() { return sessionId; }

    /**
     * 获取会话标题。
     *
     * @return 会话标题。
     */
    public String getTitle() { return title; }

    /**
     * 设置会话标题。
     *
     * @param title 新的会话标题。
     */
    public void setTitle(String title) { this.title = title; }

    /**
     * 获取最后修改时间。
     *
     * @return 最后修改时间的时间戳。
     */
    public long getLastModified() { return lastModified; }

    /**
     * 设置最后修改时间。
     *
     * @param lastModified 新的最后修改时间戳。
     */
    public void setLastModified(long lastModified) { this.lastModified = lastModified; }

    /**
     * 获取消息历史记录。
     *
     * @return 消息条目列表。
     */
    public List<MessageEntry> getMessageHistory() { return messageHistory; }

    /**
     *向消息历史记录中添加一条消息。
     *
     * @param entry 要添加的消息条目。
     */
    public void addMessage(MessageEntry entry) { messageHistory.add(entry); }

    /**
     * 根据ID从消息历史记录中移除一条消息。
     *
     * @param id 要移除的消息的ID。
     */
    public void removeMessage(UUID id) { messageHistory.removeIf(entry -> entry.getId().equals(id)); }

    /**
     * 根据第一条用户消息自动更新会话标题。
     * 标题被截断为最多30个字符。
     */
    public void updateTitle() {
        for (MessageEntry entry : messageHistory) {
            if ("user".equals(entry.getMessage().getString("role"))) {
                String content = entry.getMessage().getString("content");
                this.title = content.substring(0, Math.min(content.length(), 30)).replace("\n", " ");
                return;
            }
        }
    }

    /**
     * 获取此会话的摘要信息。
     *
     * @return ChatSessionSummary 对象。
     */
    public ChatSessionSummary getSummary() {
        return new ChatSessionSummary(sessionId, title, lastModified);
    }

    /**
     * 聊天会话摘要类。
     * 用于在列表中显示聊天会话的简要信息。
     */
    public static class ChatSessionSummary {
        private final UUID id;
        private final String title;
        private final long lastModified;

        /**
         * 构造一个聊天会话摘要。
         *
         * @param id           会话ID。
         * @param title        会话标题。
         * @param lastModified 最后修改时间。
         */
        public ChatSessionSummary(UUID id, String title, long lastModified) {
            this.id = id;
            this.title = title;
            this.lastModified = lastModified;
        }

        /**
         * 获取会.话ID。
         *
         * @return 会话 UUID。
         */
        public UUID getId() { return id; }

        /**
         * 获取会话标题。
         *
         * @return 会话标题。
         */
        public String getTitle() { return title; }

        /**
         * 获取最后修改时间。
         *
         * @return 最后修改时间的时间戳。
         */
        public long getLastModified() { return lastModified; }

        /**
         * 返回会话的字符串表示形式，即其标题。
         *
         * @return 会话标题。
         */
        @Override
        public String toString() {
            return title;
        }
    }
}
