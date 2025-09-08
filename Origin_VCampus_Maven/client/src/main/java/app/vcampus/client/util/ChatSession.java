package app.vcampus.client.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatSession {
    private final UUID sessionId;
    private String title;
    private long lastModified;
    private final List<MessageEntry> messageHistory;

    public ChatSession(UUID sessionId) {
        this.sessionId = sessionId;
        this.title = "新聊天";
        this.lastModified = System.currentTimeMillis();
        this.messageHistory = new ArrayList<>();
    }

    // Copy constructor for safe saving in the mock client
    public ChatSession(ChatSession other) {
        this.sessionId = other.sessionId;
        this.title = other.title;
        this.lastModified = other.lastModified;
        this.messageHistory = new ArrayList<>(other.messageHistory);
    }

    public UUID getId() { return sessionId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public long getLastModified() { return lastModified; }
    public void setLastModified(long lastModified) { this.lastModified = lastModified; }
    public List<MessageEntry> getMessageHistory() { return messageHistory; }
    public void addMessage(MessageEntry entry) { messageHistory.add(entry); }
    public void removeMessage(UUID id) { messageHistory.removeIf(entry -> entry.getId().equals(id)); }

    public void updateTitle() {
        for (MessageEntry entry : messageHistory) {
            if ("user".equals(entry.getMessage().getString("role"))) {
                String content = entry.getMessage().getString("content");
                this.title = content.substring(0, Math.min(content.length(), 30)).replace("\n", " ");
                return;
            }
        }
    }

    public ChatSessionSummary getSummary() {
        return new ChatSessionSummary(sessionId, title, lastModified);
    }

    public static class ChatSessionSummary {
        private final UUID id;
        private final String title;
        private final long lastModified;

        public ChatSessionSummary(UUID id, String title, long lastModified) {
            this.id = id;
            this.title = title;
            this.lastModified = lastModified;
        }

        public UUID getId() { return id; }
        public String getTitle() { return title; }
        public long getLastModified() { return lastModified; }

        @Override
        public String toString() {
            return title;
        }
    }
}