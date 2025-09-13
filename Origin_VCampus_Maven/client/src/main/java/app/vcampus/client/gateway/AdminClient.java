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

@Slf4j
public class AdminClient extends BaseClient {
    public static List<User> getAllUsers(NettyHandler handler) {
        Request request = new Request();
        request.setUri("admin/user/search");
        request.setParams(Map.of("keyword", ""));

        try {
            Response response = BaseClient.sendRequest(handler, request);
            if (response.getStatus().equals("success")) {
                Type type = new TypeToken<List<String>>() {
                }.getType();
                List<String> data = new Gson().fromJson(response.getData().toString(), type);
                return data.stream().map(s -> IEntity.fromJson(s, User.class)).collect(Collectors.toList());
            } else {
                throw new RuntimeException("Failed to get all users");
            }
        } catch (Exception e) {
            log.warn("Failed to get all users", e);
            return null;
        }
    }

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
