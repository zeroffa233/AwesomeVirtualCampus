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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
     * @param key       图片的SHA256哈希值。
     * @param base64ImageData 经过 Base64 编码后的图片数据字符串。
     * @return 如果操作成功，返回 true；否则记录日志并返回 false。
     */
    /**
     * 【异步版】向服务器添加或更新一张图片。
     * 这个方法会立即返回一个 "Future"，你可以用它来获取最终的操作结果。
     *
     * @param key           图片的 SHA256 哈希值。
     * @param base64ImageData 经过 Base64 编码后的图片数据字符串。
     * @return 一个 CompletableFuture<Boolean>，当操作完成时，它会包含 true (成功) 或 false (失败)。
     */
    public static CompletableFuture<Boolean> addOrUpdateImage(String key, String base64ImageData) {
        // 1. 创建一个 CompletableFuture。我们可以把它想象成一个“结果的承诺”或“未来的收据”。
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        try {
            // 2. 构建请求参数 (这部分不变)
            Map<String, String> params = new HashMap<>();
            params.put("key", key);
            params.put("data", base64ImageData);

            Request request = new Request();
            request.setUri("resource/images/addOrUpdate");
            request.setParams(params);

            // 3. 【核心修正】调用异步的 sendRequest 方法
            // 我们提供一个回调函数 (lambda表达式)，定义了“收到响应后做什么”
            FakeRepository.handler.sendRequest(request, response -> {
                // 这个 lambda 里的代码会在未来的某个时刻 (收到响应时) 被执行
                if (response != null && "success".equals(response.getStatus())) {
                    // 如果响应成功，我们就兑现我们的“承诺”，把结果设置为 true
                    future.complete(true);
                } else {
                    // 如果响应失败或为 null，我们就兑现“承诺”，把结果设置为 false
                    future.complete(false);
                }
            });

        } catch (Exception e) {
            System.err.println("Error initiating image upload request: " + e.getMessage());
            // 如果在发送请求前就出错了，我们也需要完成 Future，并标记为异常失败
            future.completeExceptionally(e);
        }

        // 4. 立即返回这个“承诺”。此时请求可能还在网络中飞行，但调用者已经拿到了可以查询结果的“收据”。
        return future;
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