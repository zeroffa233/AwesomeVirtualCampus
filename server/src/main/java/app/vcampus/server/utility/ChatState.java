package app.vcampus.server.utility;

import app.vcampus.server.entity.Comment;
import app.vcampus.server.entity.Identity;
import app.vcampus.server.entity.Message;
import lombok.Data;

import java.util.List;

/**
 * ChatState DTO (Data Transfer Object)。
 * 这是一个简单的POJO，用于封装从服务端 /state API 返回的完整聊天室状态。
 * 它不包含任何业务逻辑，仅用于数据传输和反序列化。
 */
@Data
public class ChatState {
    private List<Message> messages;
    private List<Comment> comments;
    private List<Identity> identities;
}