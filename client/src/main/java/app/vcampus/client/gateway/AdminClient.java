package app.vcampus.client.gateway;

import app.vcampus.client.net.NettyHandler;
import app.vcampus.server.entity.IEntity;
import app.vcampus.server.entity.User;
import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员客户端，提供管理用户相关的功能。
 * 继承自BaseClient，用于与服务器进行通信。
 */
@Slf4j
public class AdminClient extends BaseClient {
    /**
     * 获取所有用户列表。
     *
     * @param handler Netty处理器。
     * @return 所有用户的列表，如果失败则返回null。
     */
    public static List<User> getAllUsers(NettyHandler handler) {
        Request request = new Request();
        request.setUri("admin/user/search");
        request.setParams(Map.of("keyword", ""));

        try {
            Response response = BaseClient.sendRequest(handler, request);
            if (response.getStatus().equals("success")) {
                Type type = new TypeToken<List<User>>() {
                }.getType();
                return new Gson().fromJson(response.getData().toString(), type);
            } else {
                throw new RuntimeException("Failed to get all users");
            }
        } catch (Exception e) {
            log.warn("Failed to get all users", e);
            return null;
        }
    }

    /**
     * 添加新用户。
     *
     * @param handler Netty处理器。
     * @param cardNum 用户卡号。
     * @param name 用户名。
     * @param password 密码。
     * @param gender 性别。
     * @param email 邮箱。
     * @param phone 电话。
     * @param roles 角色字符串。
     * @return 如果添加成功则返回true，否则返回false。
     */
    public static boolean addUser(NettyHandler handler, int cardNum, String name, String password, String gender, String email, String phone, String roles) {
        Request request = new Request();
        request.setUri("admin/user/add");

        Map<String, Object> userPayload = new HashMap<>();
        userPayload.put("cardNum", cardNum);
        userPayload.put("name", name);
        userPayload.put("password", password);
        userPayload.put("gender", gender);
        userPayload.put("email", email);
        userPayload.put("phone", phone);
        userPayload.put("roleStr", roles);

        request.setParams(Map.of("user", toJson(userPayload)));

        try {
            Response response = BaseClient.sendRequest(handler, request);
            return response.getStatus().equals("success");
        } catch (Exception e) {
            log.warn("Failed to add user", e);
            return false;
        }
    }

    /**
     * 更新用户信息。
     *
     * @param handler Netty处理器。
     * @param cardNum 用户卡号。
     * @param roles 角色字符串。
     * @param password 密码。
     * @return 如果更新成功则返回true，否则返回false。
     */
    public static boolean updateUser(NettyHandler handler, int cardNum, String roles, String password) {
        Request request = new Request();
        request.setUri("admin/user/modify");
        request.setParams(Map.of(
                "cardNum", String.valueOf(cardNum),
                "roles", roles,
                "password", password
        ));

        try {
            Response response = BaseClient.sendRequest(handler, request);
            return response.getStatus().equals("success");
        } catch (Exception e) {
            log.warn("Failed to update user", e);
            return false;
        }
    }

    /**
     * 删除用户。
     *
     * @param handler Netty处理器。
     * @param cardNum 用户卡号。
     * @return 如果删除成功则返回true，否则返回false。
     */
    public static boolean deleteUser(NettyHandler handler, int cardNum) {
        Request request = new Request();
        request.setUri("admin/user/delete");
        request.setParams(Map.of("cardNum", String.valueOf(cardNum)));

        try {
            Response response = BaseClient.sendRequest(handler, request);
            return response.getStatus().equals("success");
        } catch (Exception e) {
            log.warn("Failed to delete user", e);
            return false;
        }
    }
}
