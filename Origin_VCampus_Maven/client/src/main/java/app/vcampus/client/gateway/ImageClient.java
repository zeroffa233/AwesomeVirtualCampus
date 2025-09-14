// 文件路径: client/src/main/java/app/vcampus/client/gateway/ImageClient.java
package app.vcampus.client.gateway;

import app.vcampus.client.repository.FakeRepository; // 【重要】导入 FakeRepository
import app.vcampus.server.entity.CachedImage;
import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.Base64;
import java.util.List;
import java.util.Map;

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
            // 【核心修正】直接通过 FakeRepository.handler 调用 sendRequest
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

    // ... (addOrUpdateImage 和 deleteImage 方法也同样使用 FakeRepository.handler) ...
}