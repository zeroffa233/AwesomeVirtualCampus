// File: server/src/main/java/app/vcampus/server/controller/ImageController.java
package app.vcampus.server.controller;

import app.vcampus.server.entity.CachedImage;
import app.vcampus.server.entity.IEntity; // 确保 IEntity 导入
import app.vcampus.server.utility.Database;
import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;
import app.vcampus.server.utility.router.RouteMapping;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
public class ImageController {

    /**
     * 【查】获取所有图片资源。
     */
    @RouteMapping(uri = "resource/images/all")
    public Response getAllImages(Request request, Session database) {
        try {
            List<CachedImage> allImages = Database.loadAllData(CachedImage.class, database);
            return Response.Common.ok(allImages);
        } catch (Exception e) {
            log.error("Failed to fetch all images from cache table", e);
            return Response.Common.error("Failed to fetch images from database.");
        }
    }

    /**
     * 【增/改 - 统一入口】
     * 添加或更新一张图片到数据库。
     * 客户端将图片的 key (SHA256) 和 Base64 编码后的数据发送过来。
     *
     * @param request  需要包含 "key" (String) 和 "data" (Base64 String)
     * @param database Hibernate Session
     * @return 操作成功或失败的 Response
     */
    @RouteMapping(uri = "resource/images/addOrUpdate") // <-- 我们使用一个新的、更清晰的URI
    public Response addOrUpdateImage(Request request, Session database) {
        Transaction tx = null;
        try {
            // 1. 从请求中获取 key 和 Base64 编码的数据
            String key = request.getParams().get("key");
            String base64Data = request.getParams().get("data");

            // 2. 验证输入
            if (key == null || key.isEmpty() || base64Data == null || base64Data.isEmpty()) {
                return Response.Common.error("Key and image data cannot be empty.");
            }

            // 3. 【核心解码步骤】将 Base64 字符串解码为 byte[]
            byte[] imageData = Base64.getDecoder().decode(base64Data);

            // 4. 创建或准备要持久化的实体对象
            CachedImage imageToSave = new CachedImage(key, imageData);

            // 5. 执行数据库事务
            tx = database.beginTransaction();
            // merge() 会自动处理：如果数据库中已存在该 key，则更新；如果不存在，则插入。
            database.merge(imageToSave);

            tx.commit();

            log.info("Successfully added/updated image with key: {}", key);
            return Response.Common.ok();

        } catch (IllegalArgumentException e) {
            // 如果 base64Data 不是合法的 Base64 字符串, decode 会抛出此异常
            log.error("Failed to process image due to Base64 decoding error for key: {}", request.getParams().get("key"), e);
            return Response.Common.error("Invalid Base64 image data.");
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            log.error("Failed to add/update image in database for key: {}", request.getParams().get("key"), e);
            return Response.Common.error("An internal error occurred while saving the image.");
        }
    }

    // ... 你原有的 deleteImage 方法可以保持不变，它写得很好 ...
    /**
     * 【删】删除一张图片。
     */
    @RouteMapping(uri = "resource/images/delete")
    public Response deleteImage(Request request, Session database) {
        // ... 此处代码无需修改 ...
        Transaction tx = null;
        try {
            String key = request.getParams().get("key");
            if (key == null || key.isEmpty()) {
                return Response.Common.error("Key cannot be empty.");
            }

            CachedImage imageToDelete = database.get(CachedImage.class, key);
            if (imageToDelete == null) {
                return Response.Common.error("Image with key '" + key + "' not found.");
            }

            tx = database.beginTransaction();
            database.remove(imageToDelete);
            tx.commit();

            log.info("Successfully deleted image with key: {}", key);
            return Response.Common.ok();

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            log.error("Failed to delete image from database", e);
            return Response.Common.error("An internal error occurred while deleting the image.");
        }
    }

    // 为了保持清晰，你可以移除或注释掉旧的 addImage 和 updateImage 方法
}