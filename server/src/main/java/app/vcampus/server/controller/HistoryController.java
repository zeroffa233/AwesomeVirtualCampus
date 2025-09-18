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
        // 1. 获取 Session
        Session session = request.getSession();

        // 2. 【核心修正】最关键的权限检查
        //    如果 session 不存在，或者 cardNum 是默认值 0（通常表示未初始化的 session），则拒绝访问。
        if (session == null || session.getCardNum() == 0) {
            return Response.Common.permissionDenied();
        }

        // 3. 从 session 中获取 int 类型的 cardNum (Java 会自动装箱为 Integer)
        Integer cardNum = session.getCardNum();

        // 4. 从数据库获取历史记录
        UserTransactionHistory history = database.get(UserTransactionHistory.class, cardNum);

        if (history == null) {
            // 如果用户还没有历史记录，返回一个表示空列表的JSON字符串 "[]"
            return Response.Common.ok("[]");
        }

        // 5. 返回存储在数据库中的JSON字符串
        return Response.Common.ok(history.getHistoryJson());
    }

    /**
     * 更新当前用户的交易历史。
     */
    @RouteMapping(uri = "history/update")
    public Response updateHistory(Request request, org.hibernate.Session database) {
        // 1. 获取 Session
        Session session = request.getSession();

        // 2. 【核心修正】最关键的权限检查
        if (session == null || session.getCardNum() == 0) {
            return Response.Common.permissionDenied();
        }

        // 3. 从请求参数中获取要更新的JSON数据
        String historyJson = request.getParams().get("historyJson");
        if (historyJson == null) {
            return Response.Common.badRequest();
        }

        Transaction tx = null;
        try {
            // 4. 从 session 中获取 int 类型的 cardNum
            Integer cardNum = session.getCardNum();
            UserTransactionHistory newHistory = new UserTransactionHistory(cardNum, historyJson);

            tx = database.beginTransaction();
            database.merge(newHistory);
            tx.commit();

            log.info("Successfully updated transaction history for user: {}", cardNum);
            return Response.Common.ok();

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            log.error("Failed to update transaction history for user: {}", session.getCardNum(), e);
            return Response.Common.internalError();
        }
    }
}