// 文件路径: client/src/main/java/app/vcampus/client/gateway/ImageClient.java
package app.vcampus.client.gateway;

import app.vcampus.client.net.NettyHandler; // 确保导入 NettyHandler
import app.vcampus.client.repository.FakeRepository;
import app.vcampus.server.entity.CachedImage;
import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import app.vcampus.server.entity.IEntity;

/**
 * 客户端网关，专门用于与服务端的 ImageController 进行通信。
 * 该类的实现完全遵循项目中既有的、通过 FakeRepository 访问全局 handler 的模式。
 */
@Slf4j
public class ImageClient extends BaseClient {

    private static final Gson gson = new Gson();

    /**
     * 从服务器获取所有用于缓存的图片。
     *
     * @return 如果成功，返回包含所有图片数据的列表；如果失败，返回 null。
     */
    public static List<CachedImage> getAllImages() {
        Request request = new Request();
        request.setUri("resource/images/all");

        try {
            Response response = BaseClient.sendRequest(FakeRepository.handler, request);

            if (response.getStatus().equals("success")) {
                Type type = new TypeToken<List<CachedImage>>(){}.getType();
                String json = gson.toJson(response.getData());
                return gson.fromJson(json, type);
            } else {
                throw new Exception(response.getMessage());
            }
        } catch (Exception e) {
            log.warn("从服务器获取全部图片失败", e);
            return null;
        }
    }

    /**
     * 【已补全】
     * 向服务器添加或更新一张图片。
     * 服务端的 `updateImage` 方法可以同时处理新增和更新，
     * 因此我们在客户端可以将其合并为一个便捷的方法。
     *
     * @param key       图片的哈希值。
     * @param imageData 图片的原始二进制数据。
     * @return 如果操作成功，返回 true；否则记录日志并返回 false。
     */
    public static boolean addOrUpdateImage(String key, byte[] imageData) {
        Request request = new Request();
        request.setUri("resource/images/update"); // 我们使用 update 路由，因为它能同时处理新增和更新
        // 【核心修正】使用 URL and Filename safe Base64 编码器
        String safeBase64Data = Base64.getUrlEncoder().encodeToString(imageData);

        request.setParams(Map.of(
                "key", key,
                "imageData", safeBase64Data // 将二进制数据编码为 Base64 字符串
        ));

        try {
            Response response = BaseClient.sendRequest(FakeRepository.handler, request);
            return response.getStatus().equals("success");
        } catch (Exception e) {
            log.warn("添加或更新图片失败, Key: " + key, e);
            return false;
        }
    }

    /**
     * 【已补全】
     * 向服务器删除一张图片。
     *
     * @param key     要删除的图片的哈希值。
     * @return 如果操作成功，返回 true；否则返回 false。
     */
    public static boolean deleteImage(String key) {
        Request request = new Request();
        request.setUri("resource/images/delete");
        request.setParams(Map.of(
                "key", key
        ));

        try {
            Response response = BaseClient.sendRequest(FakeRepository.handler, request);
            return response.getStatus().equals("success");
        } catch (Exception e) {
            log.warn("删除图片失败, Key: " + key, e);
            return false;
        }
    }

    public static String calculateSHA256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("无法获取 SHA-256 哈希算法实例", e);
        }
    }



}