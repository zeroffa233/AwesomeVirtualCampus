package app.vcampus.server.controller;

import app.vcampus.server.entity.*;
import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;
import app.vcampus.server.utility.router.RouteMapping;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Transaction;

import java.util.Map;

/**
 * GPT 控制器。
 * 处理与 GPT 对话上下文相关的拉取和推送操作。
 */
@Slf4j
public class GptController {
    /**
     * 拉取用户的 GPT 对话上下文。
     * 如果用户是第一次使用，会为其创建一个空的上下文。
     *
     * @param request  请求对象。
     * @param database 数据库会话。
     * @return 包含 GPT 上下文信息的响应。
     */
    @RouteMapping(uri = "gpt/pull", role = "gpt_user")
    public Response postContext(Request request, org.hibernate.Session database) {
        Integer cardNumber = request.getSession().getCardNum();
        GptContext ctx = database.get(GptContext.class, cardNumber);
        if (ctx == null) {
            Transaction tx = database.beginTransaction();
            ctx = new GptContext();
            ctx.setCardNumber(cardNumber);
            ctx.setContext("{}");
            database.persist(ctx);
            tx.commit();
        }

        return Response.Common.ok(Map.of("ctx", ctx.toJson()));
    }

    /**
     * 推送（更新）用户的 GPT 对话上下文。
     *
     * @param request  包含更新后上下文的请求。
     * @param database 数据库会话。
     * @return 操作结果的响应。
     */
    @RouteMapping(uri = "gpt/push", role = "gpt_user")
    public Response updateContext(Request request, org.hibernate.Session database) {
        Integer cardNumber = request.getSession().getCardNum();
        String serStorage = request.getParams().get("context");
        GptContext ctx = database.get(GptContext.class, cardNumber);
        Transaction tx = database.beginTransaction();
        ctx.setContext(serStorage);
        database.merge(ctx);
        tx.commit();

        return Response.Common.ok(Map.of());
    }


}