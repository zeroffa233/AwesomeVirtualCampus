package app.vcampus.server.controller;

import app.vcampus.server.entity.User;
import app.vcampus.server.utility.Password;
import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;
import app.vcampus.server.utility.Session;
import app.vcampus.server.utility.router.RouteMapping;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 认证控制器。
 * 处理用户的登录和登出请求。
 */
@Slf4j
public class AuthController {
    /**
     * 用户登录。
     * 通过卡号和密码进行验证。
     *
     * @param request  包含卡号和密码的请求。
     * @param database 数据库会话。
     * @return 包含会话信息和用户数据的响应，或错误信息。
     */
    @RouteMapping(uri = "auth/login")
    public Response login(Request request, org.hibernate.Session database) {
        try {
            String cardNum = request.getParams().get("cardNum");
            String password = request.getParams().get("password");

            if (cardNum == null || password == null) {
                return Response.Common.badRequest();
            }
            User user = database.get(User.class, Integer.parseInt(cardNum));
            boolean passwordCorrect = user != null && Password.verify(password, user.getPassword());
            log.info("Password verification for user {} result: {}", cardNum, passwordCorrect);
            if (!passwordCorrect) {
                return Response.Common.error("Incorrect card number or password");
            }
            user.setPassword(null);

            Response response = Response.Common.ok();
            Session session = new Session();
            session.setCardNum(user.getCardNum());
            session.setRoles(user.getRoles());
            response.setSession(session);

            response.setData(Map.of("user", user.toJson()));

            return response;
        } catch (Exception e) {
            return Response.Common.error(e.getMessage());
        }
    }

    /**
     * 用户登出。
     * 清除当前会话。
     *
     * @param request  请求对象。
     * @param database 数据库会话。
     * @return 包含空会话的成功响应。
     */
    @RouteMapping(uri = "auth/logout")
    public Response logout(Request request, org.hibernate.Session database) {
        Response response = Response.Common.ok();
        Session session = new Session();
        response.setSession(session);
        return response;
    }
}