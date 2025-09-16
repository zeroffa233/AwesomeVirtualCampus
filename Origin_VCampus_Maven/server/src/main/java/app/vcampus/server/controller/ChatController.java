package app.vcampus.server.controller;

import app.vcampus.server.entity.ChatRoom;
import app.vcampus.server.entity.Comment;
import app.vcampus.server.entity.Identity;
import app.vcampus.server.entity.Message;
import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;
import app.vcampus.server.utility.router.RouteMapping;
import jakarta.persistence.PersistenceException;
import org.hibernate.Transaction;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ChatController {

    /**
     * 1. 获取聊天室的完整状态
     * 客户端通过轮询此接口来刷新聊天内容。
     *
     * @param request  请求对象，包含 URI 和会话信息
     * @param database Hibernate 会话对象
     * @return 包含 messages, comments, identities 的响应
     */
    @RouteMapping(uri = "chat/state", role = "chat_user")
    public Response getChatRoomState(Request request, org.hibernate.Session database) {
        Transaction tx = null;
        try {
            String topicId = request.getParams().get("topicId");
            if (topicId == null || topicId.isEmpty()) {
                return Response.Common.error("topicId parameter is required");
            }

            // 检查并自动创建 ChatRoom
            ChatRoom room = database.get(ChatRoom.class, topicId);
            if (room == null) {
                tx = database.beginTransaction();
                room = new ChatRoom();
                room.setTopicId(topicId);
                database.persist(room);
                tx.commit();
            }

            // 获取该聊天室下的所有帖子
            List<Message> messages = Collections.emptyList();
            if (room.getMessageIds() != null && !room.getMessageIds().isEmpty()) {
                messages = database.createQuery("FROM Message WHERE id IN (:ids)", Message.class)
                        .setParameter("ids", room.getMessageIds())
                        .getResultList();
            }

            // 收集所有帖子下的所有评论ID
            List<UUID> allCommentIds = messages.stream()
                    .flatMap(message -> message.getCommentIds().stream())
                    .collect(Collectors.toList());

            // 获取所有相关的评论
            List<Comment> comments = Collections.emptyList();
            if (!allCommentIds.isEmpty()) {
                comments = database.createQuery("FROM Comment WHERE id IN (:ids)", Comment.class)
                        .setParameter("ids", allCommentIds)
                        .getResultList();
            }

            // 获取所有用户的身份信息，以便客户端可以将 cardNum 映射为 userName
            List<Identity> identities = database.createQuery("FROM Identity", Identity.class).getResultList();

            return Response.Common.ok(Map.of(
                    "messages", messages,
                    "comments", comments,
                    "identities", identities
            ));
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            return Response.Common.error("Failed to fetch chat room state: " + e.getMessage());
        }
    }

    /**
     * 2. 发布新帖子
     *
     * @param request  请求对象，包含参数和会话
     * @param database Hibernate 会话
     * @return 成功或失败的响应
     */
    @RouteMapping(uri = "chat/post", role = "chat_user")
    public Response postMessage(Request request, org.hibernate.Session database) {
        Transaction tx = null;
        try {
            String topicId = request.getParams().get("topicId");
            String content = request.getParams().get("content");
            Integer userCardNum = request.getSession().getCardNum();

            // ... (参数校验)

            tx = database.beginTransaction();

            // 检查并自动创建 ChatRoom
            ChatRoom room = database.get(ChatRoom.class, topicId);
            if (room == null) {
                room = new ChatRoom();
                room.setTopicId(topicId);
                database.persist(room);
            }

            // 创建 Message 实体
            Message newMessage = new Message();
            newMessage.setId(UUID.randomUUID()); // 尽管实体有默认值，但显式设置更清晰
            newMessage.setTimestamp(System.currentTimeMillis());
            newMessage.setUploaderCardNum(userCardNum);
            newMessage.setContent(content);
            newMessage.setTopicId(topicId); // <--- 这是最关键的修复！

            database.persist(newMessage);

            // 关联 Message 到 ChatRoom
            room.getMessageIds().add(newMessage.getId());
            database.merge(room);

            tx.commit();
            return Response.Common.ok("Message posted successfully");
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            return Response.Common.error("An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * 3. 发布新评论
     *
     * @param request  请求对象
     * @param database Hibernate 会话
     * @return 成功或失败的响应
     */
    // 修改点：URI 改为静态路由
    @RouteMapping(uri = "chat/message/comment", role = "chat_user")
    public Response postComment(Request request, org.hibernate.Session database) {
        Transaction tx = null;
        try {
            String messageIdStr = request.getParams().get("messageId");
            if (messageIdStr == null) return Response.Common.error("messageId is required");
            UUID messageId = UUID.fromString(messageIdStr);

            String content = request.getParams().get("content");
            Integer userCardNum = request.getSession().getCardNum(); // 直接获取CardNum

            if (content == null || content.trim().isEmpty()) {
                return Response.Common.error("Content cannot be empty");
            }

            // 【修改点】: 不再需要查询Identity来获取名字
            // Identity user = database.get(Identity.class, userCardNum);
            // if (user == null || user.getUserName() == null) {
            //     return Response.Common.error("User identity not found or username not set");
            // }

            tx = database.beginTransaction();

            // 创建新评论
            Comment newComment = new Comment();
            newComment.setId(UUID.randomUUID());
            newComment.setTimestamp(System.currentTimeMillis());
            newComment.setUploaderCardNum(userCardNum); // 【修改点】: 设置CardNum
            newComment.setContent(content);
            database.persist(newComment);

            // 关联到帖子
            Message parentMessage = database.get(Message.class, messageId);
            if (parentMessage == null) {
                tx.rollback(); // 回滚事务
                return Response.Common.error("Parent message not found");
            }
            parentMessage.getCommentIds().add(newComment.getId());
            database.merge(parentMessage);

            tx.commit();
            return Response.Common.ok("Comment posted successfully");
        } catch (IllegalArgumentException e) {
            return Response.Common.error("Invalid Message ID format");
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            return Response.Common.error("An error occurred while posting comment: " + e.getMessage());
        }
    }

    /**
     * 4. 切换帖子点赞状态
     *
     * @param request  请求对象
     * @param database Hibernate 会话
     * @return 成功或失败的响应
     */
    // 修改点：URI 改为静态路由
    @RouteMapping(uri = "chat/message/like", role = "chat_user")
    public Response toggleMessageLike(Request request, org.hibernate.Session database) {
        Transaction tx = null;
        try {
            // 修改点：从 params 获取 messageId
            String messageIdStr = request.getParams().get("messageId");
            if (messageIdStr == null) return Response.Common.error("messageId is required");
            UUID messageId = UUID.fromString(messageIdStr);

            Integer userCardNum = request.getSession().getCardNum();

            tx = database.beginTransaction();

            Message message = database.get(Message.class, messageId);
            if (message == null) {
                return Response.Common.error("Message not found");
            }

            if (message.getLikeList().contains(userCardNum)) {
                message.getLikeList().remove(userCardNum); // 取消点赞
            } else {
                message.getLikeList().add(userCardNum); // 点赞
            }
            database.merge(message);

            tx.commit();
            return Response.Common.ok("Message like status updated");
        } catch (IllegalArgumentException e) {
            return Response.Common.error("Invalid Message ID format");
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            return Response.Common.error("Failed to update message like status: " + e.getMessage());
        }
    }

    /**
     * 5. 切换评论点赞状态
     *
     * @param request  请求对象
     * @param database Hibernate 会话
     * @return 成功或失败的响应
     */
    // 修改点：URI 改为静态路由
    @RouteMapping(uri = "chat/comment/like", role = "chat_user")
    public Response toggleCommentLike(Request request, org.hibernate.Session database) {
        Transaction tx = null;
        try {
            String commentIdStr = request.getParams().get("commentId");
            if (commentIdStr == null) return Response.Common.error("commentId is required");
            UUID commentId = UUID.fromString(commentIdStr);

            Integer userCardNum = request.getSession().getCardNum(); // 获取CardNum

            // 【修改点】: 不再需要查询Identity
            // Identity user = database.get(Identity.class, userCardNum);
            // if (user == null || user.getUserName() == null) {
            //     return Response.Common.error("User identity not found or username not set");
            // }

            tx = database.beginTransaction();

            Comment comment = database.get(Comment.class, commentId);
            if (comment == null) {
                tx.rollback();
                return Response.Common.error("Comment not found");
            }

            // 【修改点】: 在Integer列表中添加/移除cardNum
            if (comment.getLikeList().contains(userCardNum)) {
                comment.getLikeList().remove(userCardNum); // 取消点赞
            } else {
                comment.getLikeList().add(userCardNum); // 点赞
            }
            database.merge(comment);

            tx.commit();
            return Response.Common.ok("Comment like status updated");
        } catch (IllegalArgumentException e) {
            return Response.Common.error("Invalid Comment ID format");
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            return Response.Common.error("Failed to update comment like status: "+ e.getMessage());
        }
    }

    /**
     * 6. 修改用户名
     *
     * @param request 请求对象
     * @param database Hibernate 会话
     * @return 成功或失败的响应
     */
    @RouteMapping(uri = "identity/update", role = "chat_user")
    public Response updateUsername(Request request, org.hibernate.Session database) {
        Transaction tx = null;
        try {
            String newUserName = request.getParams().get("newUserName");
            Integer userCardNum = request.getSession().getCardNum();

            if (newUserName == null || newUserName.trim().length() < 2 || newUserName.trim().length() > 20) {
                return Response.Common.error("Username must be between 2 and 20 characters");
            }

            tx = database.beginTransaction();
            Identity identity = database.get(Identity.class, userCardNum);
            if (identity == null) {
                // 如果用户第一次设置名字，则创建一个新的 Identity 记录
                identity = new Identity(userCardNum, newUserName);
                database.persist(identity);
            } else {
                identity.setUserName(newUserName);
                database.merge(identity);
            }
            tx.commit();

            return Response.Common.ok("Username updated successfully");
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            return Response.Common.error("Failed to update username: " + e.getMessage());
        }
    }
}
