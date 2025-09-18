package app.vcampus.server.utility;

import app.vcampus.server.entity.Comment;
import app.vcampus.server.entity.Identity;
import app.vcampus.server.entity.Message;
import lombok.Data;

import java.util.List;

/**
 * 聊天状态数据传输对象 (DTO)。
 * <p>
 * 这是一个简单的 POJO (Plain Old Java Object)，用于封装从服务端 /state API 返回的完整聊天室状态。
 * 它不包含任何业务逻辑，仅用于数据传输和反序列化。
 * </p>
 */
@Data
public class ChatState {
    /**
     * 聊天室中的消息列表。
     */
    private List<Message> messages;
    /**
     * 聊天室中的评论列表。
     */
    private List<Comment> comments;
    /**
     * 聊天室中的身份列表。
     */
    private List<Identity> identities;
}