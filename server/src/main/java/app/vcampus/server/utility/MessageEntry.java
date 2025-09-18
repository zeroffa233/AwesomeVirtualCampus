package app.vcampus.server.utility;

import org.json.JSONObject;
import java.util.UUID;

/**
 * 消息条目类。
 * 用于封装一个带唯一标识符的 JSON 消息对象。
 */
public class MessageEntry {
    /**
     * 消息的唯一标识符。
     */
    private final UUID id;
    /**
     * 消息内容的 JSON 对象。
     */
    private final JSONObject message;

    /**
     * 构造一个新的消息条目。
     *
     * @param id      消息的唯一标识符。
     * @param message 消息内容的 JSON 对象。
     */
    public MessageEntry(UUID id, JSONObject message) {
        this.id = id;
        this.message = message;
    }

    /**
     * 获取消息的唯一标识符。
     *
     * @return 消息的 UUID。
     */
    public UUID getId() { return id; }

    /**
     * 获取消息内容的 JSON 对象。
     *
     * @return 消息的 JSONObject。
     */
    public JSONObject getMessage() { return message; }
}