// 文件位置: server/src/main/java/app/vcampus/server/controller/HistoryController.java
package app.vcampus.server.controller;

import app.vcampus.server.entity.UserTransactionHistory;
import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;
import app.vcampus.server.utility.Session;
import app.vcampus.server.utility.router.RouteMapping;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Transaction;

@Slf4j
public class HistoryController {

    /**
     * 获取当前用户的交易历史。
     */
    @RouteMapping(uri = "history/get")
    public Response getHistory(Request request, org.hibernate.Session database) {
        // 1. 【核心修正】更健壮的权限和身份验证
        Session session = request.getSession();
        if (session == null || session.getCardNum() == null || session.getCardNum().isEmpty()) {
            return Response.Common.permissionDenied();
        }

        try {
            // 2. 安全地将 String 类型的 cardNum 转换为 Integer
            Integer cardNum = Integer.parseInt(session.getCardNum());

            // 3. 从数据库获取历史记录
            UserTransactionHistory history = database.get(UserTransactionHistory.class, cardNum);

            if (history == null) {
                // 如果用户还没有历史记录，返回一个表示空列表的JSON字符串 "[]"
                return Response.Common.ok("[]");
            }

            // 4. 返回存储在数据库中的JSON字符串
            return Response.Common.ok(history.getHistoryJson());

        } catch (NumberFormatException e) {
            // 如果 cardNum 字符串无法被解析为整数，这是一个异常情况
            log.warn("Invalid card number format in session: {}", session.getCardNum());
            return Response.Common.badRequest();
        }
    }

    /**
     * 更新当前用户的交易历史。
     */
    @RouteMapping(uri = "history/update")
    public Response updateHistory(Request request, org.hibernate.Session database) {
        // 1. 【核心修正】更健壮的权限和身份验证
        Session session = request.getSession();
        if (session == null || session.getCardNum() == null || session.getCardNum().isEmpty()) {
            return Response.Common.permissionDenied();
        }

        // 2. 从请求参数中获取要更新的JSON数据
        String historyJson = request.getParams().get("historyJson");
        if (historyJson == null) {
            return Response.Common.badRequest();
        }

        Transaction tx = null;
        try {
            // 3. 安全地将 String 类型的 cardNum 转换为 Integer
            Integer cardNum = Integer.parseInt(session.getCardNum());
            UserTransactionHistory newHistory = new UserTransactionHistory(cardNum, historyJson);

            tx = database.beginTransaction();
            database.merge(newHistory); // 使用 merge 来处理插入或更新
            tx.commit();

            log.info("Successfully updated transaction history for user: {}", cardNum);
            return Response.Common.ok();

        } catch (NumberFormatException e) {
            // 如果 cardNum 字符串无法被解析为整数
            log.warn("Invalid card number format in session: {}", session.getCardNum());
            if (tx != null) tx.rollback();
            return Response.Common.badRequest();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            log.error("Failed to update transaction history for user: {}", session.getCardNum(), e);
            return Response.Common.internalError();
        }
    }
}