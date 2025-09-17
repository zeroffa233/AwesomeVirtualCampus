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

import java.util.ArrayList; // 确保导入
import jakarta.persistence.criteria.CriteriaBuilder; // 确保导入
import jakarta.persistence.criteria.CriteriaQuery; // 确保导入
import jakarta.persistence.criteria.Predicate; // 确保导入
import jakarta.persistence.criteria.Root; // 确保导入

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

    /**
     * 7. 按条件搜索消息或评论
     *
     * @param request  请求对象
     * @param database Hibernate 会话
     * @return 包含搜索结果的响应
     */
    @RouteMapping(uri = "chat/search", role = "admin") // 假设此功能仅管理员可用
    public Response search(Request request, org.hibernate.Session database) {
        try {
            String type = request.getParams().get("type"); // "message" or "comment"
            String nickname = request.getParams().get("nickname");
            String cardNumStr = request.getParams().get("cardNum");
            String content = request.getParams().get("content");

            if (type == null || (!type.equals("message") && !type.equals("comment"))) {
                return Response.Common.error("Invalid search type. Must be 'message' or 'comment'.");
            }

            CriteriaBuilder cb = database.getCriteriaBuilder();
            List<Predicate> predicates = new ArrayList<>();
            List<?> results;

            if ("message".equals(type)) {
                CriteriaQuery<Message> cq = cb.createQuery(Message.class);
                Root<Message> root = cq.from(Message.class);

                // 内容匹配
                if (content != null && !content.isEmpty()) {
                    predicates.add(cb.like(root.get("content"), "%" + content + "%"));
                }

                // 卡号或昵称匹配
                if ((nickname != null && !nickname.isEmpty()) || (cardNumStr != null && !cardNumStr.isEmpty())) {
                    List<Integer> userCardNums = findCardNums(database, nickname, cardNumStr);
                    if (!userCardNums.isEmpty()) {
                        predicates.add(root.get("uploaderCardNum").in(userCardNums));
                    } else {
                        // 如果指定了昵称或卡号但找不到匹配的用户，则返回空结果
                        return Response.Common.ok(Map.of("results", Collections.emptyList(), "identities", Collections.emptyList()));
                    }
                }

                cq.where(cb.and(predicates.toArray(new Predicate[0])));
                results = database.createQuery(cq).getResultList();

            } else { // "comment"
                CriteriaQuery<Comment> cq = cb.createQuery(Comment.class);
                Root<Comment> root = cq.from(Comment.class);

                if (content != null && !content.isEmpty()) {
                    predicates.add(cb.like(root.get("content"), "%" + content + "%"));
                }

                if ((nickname != null && !nickname.isEmpty()) || (cardNumStr != null && !cardNumStr.isEmpty())) {
                    List<Integer> userCardNums = findCardNums(database, nickname, cardNumStr);
                    if (!userCardNums.isEmpty()) {
                        predicates.add(root.get("uploaderCardNum").in(userCardNums));
                    } else {
                        return Response.Common.ok(Map.of("results", Collections.emptyList(), "identities", Collections.emptyList()));
                    }
                }

                cq.where(cb.and(predicates.toArray(new Predicate[0])));
                results = database.createQuery(cq).getResultList();
            }

            // 为了显示，同时返回所有用户信息
            List<Identity> identities = database.createQuery("FROM Identity", Identity.class).getResultList();

            return Response.Common.ok(Map.of(
                    "results", results,
                    "identities", identities
            ));

        } catch (Exception e) {
            return Response.Common.error("Search failed: " + e.getMessage());
        }
    }

    // 辅助方法：根据昵称或卡号查找匹配的 cardNum 列表
    private List<Integer> findCardNums(org.hibernate.Session database, String nickname, String cardNumStr) {
        CriteriaBuilder cb = database.getCriteriaBuilder();
        CriteriaQuery<Identity> cq = cb.createQuery(Identity.class);
        Root<Identity> root = cq.from(Identity.class);
        List<Predicate> userPredicates = new ArrayList<>();

        if (nickname != null && !nickname.isEmpty()) {
            userPredicates.add(cb.like(root.get("userName"), "%" + nickname + "%"));
        }
        if (cardNumStr != null && !cardNumStr.isEmpty()) {
            try {
                userPredicates.add(cb.equal(root.get("cardNum"), Integer.parseInt(cardNumStr)));
            } catch (NumberFormatException e) {
                // 忽略无效的卡号格式
            }
        }

        if (userPredicates.isEmpty()) return Collections.emptyList();

        cq.select(root).where(cb.or(userPredicates.toArray(new Predicate[0])));
        List<Identity> users = database.createQuery(cq).getResultList();

        return users.stream().map(Identity::getCardNum).collect(Collectors.toList());
    }


    /**
     * 8. 删除消息或评论 (修正后)
     *
     * @param request  请求对象
     * @param database Hibernate 会话
     * @return 成功或失败的响应
     */
    @RouteMapping(uri = "chat/delete", role = "admin")
    public Response delete(Request request, org.hibernate.Session database) {
        Transaction tx = null;
        try {
            String type = request.getParams().get("type"); // "message" or "comment"
            String idStr = request.getParams().get("id");
            UUID id = UUID.fromString(idStr);

            tx = database.beginTransaction();

            if ("message".equals(type)) {
                Message message = database.get(Message.class, id);
                if (message != null) {

                    // --- ADDED LOGIC START ---
                    // 1. 在删除消息之前，先获取其所有关联的评论ID
                    List<UUID> commentIds = message.getCommentIds();
                    if (commentIds != null && !commentIds.isEmpty()) {
                        // 2. 高效地批量删除所有这些评论
                        // 使用 HQL (Hibernate Query Language) 来执行批量删除操作
                        database.createMutationQuery("DELETE FROM Comment c WHERE c.id IN (:ids)")
                                .setParameter("ids", commentIds)
                                .executeUpdate();
                    }
                    // --- ADDED LOGIC END ---

                    // 3. 从关联的 ChatRoom 中移除 Message ID
                    // 注意：此查询可能因没有直接的反向链接而效率不高，但对于当前结构是必要的
                    ChatRoom room = database.createQuery("FROM ChatRoom cr WHERE :messageId MEMBER OF cr.messageIds", ChatRoom.class)
                            .setParameter("messageId", id)
                            .uniqueResult();
                    if (room != null) {
                        room.getMessageIds().remove(id);
                        database.merge(room);
                    }

                    // 4. 最后删除消息本身
                    database.remove(message);

                } else {
                    tx.rollback(); // 消息不存在，回滚事务
                    return Response.Common.error("Message not found");
                }
            } else if ("comment".equals(type)) {
                Comment comment = database.get(Comment.class, id);
                if (comment != null) {
                    // 从关联的 Message 中移除 Comment ID
                    Message message = database.createQuery("FROM Message m WHERE :commentId MEMBER OF m.commentIds", Message.class)
                            .setParameter("commentId", id)
                            .uniqueResult();
                    if(message != null) {
                        message.getCommentIds().remove(id);
                        database.merge(message);
                    }
                    // 删除评论本身
                    database.remove(comment);
                } else {
                    tx.rollback();
                    return Response.Common.error("Comment not found");
                }
            } else {
                tx.rollback();
                return Response.Common.error("Invalid delete type");
            }

            tx.commit();
            return Response.Common.ok("Successfully deleted");

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace(); // 在服务器日志中打印详细错误，便于调试
            return Response.Common.error("Delete failed: " + e.getMessage());
        }
    }
}
