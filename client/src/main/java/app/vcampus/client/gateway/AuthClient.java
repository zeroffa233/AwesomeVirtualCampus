package app.vcampus.client.gateway;

import app.vcampus.client.net.NettyHandler;
import app.vcampus.server.entity.IEntity;
import app.vcampus.server.entity.User;
import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 认证客户端，提供用户认证相关的功能。
 * 继承自BaseClient，用于与服务器进行通信。
 */
@Slf4j
public class AuthClient extends BaseClient {
    /**
     * 用户登录。
     *
     * @param handler Netty处理器。
     * @param username 用户名（卡号）。
     * @param password 密码。
     * @return 登录成功的User对象，如果登录失败则返回null。
     */
    public static User login(NettyHandler handler, String username, String password) {
        Request request = new Request();
        request.setUri("auth/login");
        request.setParams(Map.of(
                "cardNum", username,
                "password", password
        ));

        try {
            Response response = BaseClient.sendRequest(handler, request);
            if (response.getStatus().equals("success")) {
                String data = ((Map<String, String>) response.getData()).get("user");
                return IEntity.fromJson(data, User.class);
            } else {
                throw new RuntimeException("Failed to login");
            }
        } catch (Exception e) {
            log.warn("Fail to login", e);
            return null;
        }
    }
}
