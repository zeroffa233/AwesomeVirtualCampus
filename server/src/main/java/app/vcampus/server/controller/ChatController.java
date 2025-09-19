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

/**
 * 聊天控制器。
 * 处理与聊天室相关的操作，如获取状态、发帖、评论、点赞、搜索和删除。
 */
public class ChatController {

    /**
     * 获取聊天室的完整状态。
     * 客户端通过轮询此接口来刷新聊天内容。
     *
     * @param request  请求对象，包含 topicId。
     * @param database 数据库会话。
     * @return 包含 messages, comments, identities 的响应。
     */
    @RouteMapping(uri = "chat/state")
    public Response getChatRoomState(Request request, org.hibernate.Session database) {
        Transaction tx = null;
        try {
            String topicId = request.getParams().get("topicId");
            if (topicId == null || topicId.isEmpty()) {
                return Response.Common.error("topicId parameter is required");
            }

            ChatRoom room = database.get(ChatRoom.class, topicId);
            if (room == null) {
                tx = database.beginTransaction();
                room = new ChatRoom();
                room.setTopicId(topicId);
                database.persist(room);
                tx.commit();
            }

            List<Message> messages = Collections.emptyList();
            if (room.getMessageIds() != null && !room.getMessageIds().isEmpty()) {
                messages = database.createQuery("FROM Message WHERE id IN (:ids)", Message.class)
                        .setParameter("ids", room.getMessageIds())
                        .getResultList();
            }

            List<UUID> allCommentIds = messages.stream()
                    .flatMap(message -> message.getCommentIds().stream())
                    .collect(Collectors.toList());

            List<Comment> comments = Collections.emptyList();
            if (!allCommentIds.isEmpty()) {
                comments = database.createQuery("FROM Comment WHERE id IN (:ids)", Comment.class)
                        .setParameter("ids", allCommentIds)
                        .getResultList();
            }

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
     * 发布新帖子。
     *
     * @param request  包含 topicId 和 content 的请求。
     * @param database 数据库会话。
     * @return 操作结果的响应。
     */
    @RouteMapping(uri = "chat/post")
    public Response postMessage(Request request, org.hibernate.Session database) {
        Transaction tx = null;
        try {
            String topicId = request.getParams().get("topicId");
            String content = request.getParams().get("content");
            Integer userCardNum = request.getSession().getCardNum();

            tx = database.beginTransaction();

            ChatRoom room = database.get(ChatRoom.class, topicId);
            if (room == null) {
                room = new ChatRoom();
                room.setTopicId(topicId);
                database.persist(room);
            }

            Message newMessage = new Message();
            newMessage.setId(UUID.randomUUID());
            newMessage.setTimestamp(System.currentTimeMillis());
            newMessage.setUploaderCardNum(userCardNum);
            newMessage.setContent(content);
            newMessage.setTopicId(topicId);

            database.persist(newMessage);

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
     * 对指定帖子发布新评论。
     *
     * @param request  包含 messageId 和 content 的请求。
     * @param database 数据库会话。
     * @return 操作结果的响应。
     */
    @RouteMapping(uri = "chat/message/comment")
    public Response postComment(Request request, org.hibernate.Session database) {
        Transaction tx = null;
        try {
            String messageIdStr = request.getParams().get("messageId");
            if (messageIdStr == null) return Response.Common.error("messageId is required");
            UUID messageId = UUID.fromString(messageIdStr);

            String content = request.getParams().get("content");
            Integer userCardNum = request.getSession().getCardNum();

            if (content == null || content.trim().isEmpty()) {
                return Response.Common.error("Content cannot be empty");
            }

            tx = database.beginTransaction();

            Comment newComment = new Comment();
            newComment.setId(UUID.randomUUID());
            newComment.setTimestamp(System.currentTimeMillis());
            newComment.setUploaderCardNum(userCardNum);
            newComment.setContent(content);
            database.persist(newComment);

            Message parentMessage = database.get(Message.class, messageId);
            if (parentMessage == null) {
                tx.rollback();
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
     * 切换帖子的点赞状态。
     *
     * @param request  包含 messageId 的请求。
     * @param database 数据库会话。
     * @return 操作结果的响应。
     */
    @RouteMapping(uri = "chat/message/like")
    public Response toggleMessageLike(Request request, org.hibernate.Session database) {
        Transaction tx = null;
        try {
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
                message.getLikeList().remove(userCardNum);
            } else {
                message.getLikeList().add(userCardNum);
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
     * 切换评论的点赞状态。
     *
     * @param request  包含 commentId 的请求。
     * @param database 数据库会话。
     * @return 操作结果的响应。
     */
    @RouteMapping(uri = "chat/comment/like")
    public Response toggleCommentLike(Request request, org.hibernate.Session database) {
        Transaction tx = null;
        try {
            String commentIdStr = request.getParams().get("commentId");
            if (commentIdStr == null) return Response.Common.error("commentId is required");
            UUID commentId = UUID.fromString(commentIdStr);

            Integer userCardNum = request.getSession().getCardNum();

            tx = database.beginTransaction();

            Comment comment = database.get(Comment.class, commentId);
            if (comment == null) {
                tx.rollback();
                return Response.Common.error("Comment not found");
            }

            if (comment.getLikeList().contains(userCardNum)) {
                comment.getLikeList().remove(userCardNum);
            } else {
                comment.getLikeList().add(userCardNum);
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
     * 更新用户名。
     *
     * @param request  包含 newUserName 的请求。
     * @param database 数据库会话。
     * @return 操作结果的响应。
     */
    @RouteMapping(uri = "identity/update")
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
     * 根据条件搜索消息或评论。
     *
     * @param request  包含搜索参数 (type, nickname, cardNum, content) 的请求。
     * @param database 数据库会话。
     * @return 包含搜索结果的响应。
     */
    @RouteMapping(uri = "chat/search", role = "admin")
    public Response search(Request request, org.hibernate.Session database) {
        try {
            String type = request.getParams().get("type");
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

            List<Identity> identities = database.createQuery("FROM Identity", Identity.class).getResultList();

            return Response.Common.ok(Map.of(
                    "results", results,
                    "identities", identities
            ));

        } catch (Exception e) {
            return Response.Common.error("Search failed: " + e.getMessage());
        }
    }

    /**
     * 根据昵称或卡号查找匹配的 cardNum 列表。
     *
     * @param database   数据库会话。
     * @param nickname   要搜索的昵称。
     * @param cardNumStr 要搜索的卡号字符串。
     * @return 匹配的卡号列表。
     */
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
     * 删除消息或评论。
     *
     * @param request  包含 type 和 id 的请求。
     * @param database 数据库会话。
     * @return 操作结果的响应。
     */
    @RouteMapping(uri = "chat/delete", role = "admin")
    public Response delete(Request request, org.hibernate.Session database) {
        Transaction tx = null;
        try {
            String type = request.getParams().get("type");
            String idStr = request.getParams().get("id");
            UUID id = UUID.fromString(idStr);

            tx = database.beginTransaction();

            if ("message".equals(type)) {
                Message message = database.get(Message.class, id);
                if (message != null) {

                    List<UUID> commentIds = message.getCommentIds();
                    if (commentIds != null && !commentIds.isEmpty()) {
                        database.createMutationQuery("DELETE FROM Comment c WHERE c.id IN (:ids)")
                                .setParameter("ids", commentIds)
                                .executeUpdate();
                    }

                    ChatRoom room = database.createQuery("FROM ChatRoom cr WHERE :messageId MEMBER OF cr.messageIds", ChatRoom.class)
                            .setParameter("messageId", id)
                            .uniqueResult();
                    if (room != null) {
                        room.getMessageIds().remove(id);
                        database.merge(room);
                    }

                    database.remove(message);

                } else {
                    tx.rollback();
                    return Response.Common.error("Message not found");
                }
            } else if ("comment".equals(type)) {
                Comment comment = database.get(Comment.class, id);
                if (comment != null) {
                    Message message = database.createQuery("FROM Message m WHERE :commentId MEMBER OF m.commentIds", Message.class)
                            .setParameter("commentId", id)
                            .uniqueResult();
                    if(message != null) {
                        message.getCommentIds().remove(id);
                        database.merge(message);
                    }
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
            e.printStackTrace();
            return Response.Common.error("Delete failed: " + e.getMessage());
        }
    }
}