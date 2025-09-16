package app.vcampus.client.gateway;

import app.vcampus.client.net.NettyHandler;
import app.vcampus.client.repository.FakeRepository; // 假设NettyHandler从这里获取
import app.vcampus.server.utility.ChatState;
import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement; // ADDED: 导入 JsonElement

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * ChatClient 提供了一个网关，用于访问后端的聊天室服务。
 * 它封装了所有与服务器的HTTP API交互的细节。
 */
public class ChatClient extends BaseClient {
    // 使用 Gson 进行 Map 和对象之间的转换
    private final Gson gson = new GsonBuilder().create();
    private final NettyHandler handler;

    // 单例模式
    private static final ChatClient instance = new ChatClient(FakeRepository.handler);

    private ChatClient(NettyHandler handler) {
        this.handler = handler;
    }

    public static ChatClient getInstance() {
        return instance;
    }

    /**
     * 1. 获取聊天室状态
     * @param topicId 聊天室的主题ID
     * @return 一个 ChatState 对象，包含了聊天室的完整数据
     * @throws IOException 如果网络请求失败或服务器返回错误
     */
    public ChatState getChatRoomState(String topicId) throws IOException {
        Request request = new Request();
        request.setUri("chat/state");
        request.setParams(Map.of("topicId", topicId));
        try {
            Response response = BaseClient.sendRequest(handler, request);
            if (!response.getStatus().equals("success")) {
                throw new IOException("Failed to get chat room state: " + response.getMessage());
            }

            // MODIFIED: 采用更稳健的转换方式来避免数字类型问题
            // 1. 将通用的 Map 结构转换为 GSON 的 JsonElement 树结构
            JsonElement jsonElement = gson.toJsonTree(response.getData());

            // 2. 从 JsonElement 树直接反序列化为目标对象。
            //    这种方法能正确处理从 Double 到 Integer/Long 的类型转换。
            return gson.fromJson(jsonElement, ChatState.class);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request was interrupted", e);
        }
    }

    /**
     * 2. 发布新帖子
     * @param topicId 帖子所属的聊天室ID
     * @param content 帖子的内容
     * @throws IOException 如果网络请求失败或服务器返回错误
     */
    public void postMessage(String topicId, String content) throws IOException {
        Request request = new Request();
        request.setUri("chat/post");
        request.setParams(Map.of("topicId", topicId, "content", content));

        sendActionRequest(request, "Failed to post message");
    }

    /**
     * 3. 发布新评论
     * @param messageId 评论所属的帖子ID
     * @param content 评论的内容
     * @throws IOException 如果网络请求失败或服务器返回错误
     */
    public void postComment(UUID messageId, String content) throws IOException {
        Request request = new Request();
        request.setUri("chat/message/comment");
        request.setParams(Map.of("messageId", messageId.toString(), "content", content));

        sendActionRequest(request, "Failed to post comment");
    }

    /**
     * 4. 切换帖子点赞状态
     * @param messageId 要点赞/取消点赞的帖子ID
     * @throws IOException 如果网络请求失败或服务器返回错误
     */
    public void toggleMessageLike(UUID messageId) throws IOException {
        Request request = new Request();
        request.setUri("chat/message/like");
        request.setParams(Map.of("messageId", messageId.toString()));

        sendActionRequest(request, "Failed to toggle message like");
    }

    /**
     * 5. 切换评论点赞状态
     * @param commentId 要点赞/取消点赞的评论ID
     * @throws IOException 如果网络请求失败或服务器返回错误
     */
    public void toggleCommentLike(UUID commentId) throws IOException {
        Request request = new Request();
        request.setUri("chat/comment/like");
        request.setParams(Map.of("commentId", commentId.toString()));

        sendActionRequest(request, "Failed to toggle comment like");
    }

    /**
     * 6. 修改用户名
     * @param newName 新的用户名
     * @throws IOException 如果网络请求失败或服务器返回错误
     */
    public void updateUsername(String newName) throws IOException {
        Request request = new Request();
        request.setUri("identity/update");
        request.setParams(Map.of("newUserName", newName));

        sendActionRequest(request, "Failed to update username");
    }

    /**
     * 辅助方法，用于发送无返回数据的操作性请求，并处理通用错误。
     * @param request 要发送的请求对象
     * @param errorMessagePrefix 错误信息的前缀
     * @throws IOException 如果请求失败
     */
    private void sendActionRequest(Request request, String errorMessagePrefix) throws IOException {
        try {
            Response response = BaseClient.sendRequest(handler, request);
            if (!response.getStatus().equals("success")) {
                throw new IOException(errorMessagePrefix + ": " + response.getMessage());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request was interrupted", e);
        }
    }
}