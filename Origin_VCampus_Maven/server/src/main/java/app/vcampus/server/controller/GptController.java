package app.vcampus.server.controller;

import app.vcampus.server.entity.*;
import app.vcampus.server.enums.TransactionType;
import app.vcampus.server.utility.Database;
import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;
import app.vcampus.server.utility.router.RouteMapping;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Transaction;

import java.util.Map;


@Slf4j
public class GptController {
    /**
     *  solve client to get the user's record
     * @param request  from client with role and uri
     * @param database datebase
     * @return  it returns an “OK” response with a map containing a JSON string representing the finance card information
     */
    @RouteMapping(uri = "gpt/pull", role = "gpt_user")
    public Response postContext(Request request, org.hibernate.Session database) {
        Integer cardNumber = request.getSession().getCardNum();

        GptContext ctx = database.get(GptContext.class, cardNumber);

        if (ctx == null) {
            Transaction tx = database.beginTransaction();
            ctx = new GptContext();
            ctx.setCardNumber(cardNumber);
            ctx.setContext("");
            database.persist(ctx);
            tx.commit();
        }

        return Response.Common.ok(Map.of("ctx", ctx.toJson()));
    }

    @RouteMapping(uri = "gpt/push", role = "gpt_user")
    public Response updateContext(Request request, org.hibernate.Session database) {
        Integer cardNumber = request.getSession().getCardNum();
        String serStorage=request.getParams().get("context");
        GptContext ctx = database.get(GptContext.class, cardNumber);

        Transaction tx = database.beginTransaction();
        ctx.setContext(serStorage);
        database.merge(ctx);
        tx.commit();

        return Response.Common.ok(Map.of());
    }


}

