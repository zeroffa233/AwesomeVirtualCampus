package app.vcampus.server.controller;

import app.vcampus.server.entity.CachedImage;
import app.vcampus.server.utility.Database;
import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;
import app.vcampus.server.utility.router.RouteMapping;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Base64;
import java.util.List;

/**
 * 图片控制器。
 * 处理图片资源的获取、添加、更新和删除。
 */
@Slf4j
public class ImageController {

    /**
     * 获取所有缓存的图片资源。
     *
     * @param request  请求对象。
     * @param database 数据库会话。
     * @return 包含所有图片列表的响应。
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
     * 添加或更新一张图片到数据库。
     * 客户端将图片的 key (SHA256) 和 Base64 编码后的数据发送过来。
     *
     * @param request  需要包含 "key" (String) 和 "data" (Base64 String) 的请求。
     * @param database 数据库会话。
     * @return 操作成功或失败的响应。
     */
    @RouteMapping(uri = "resource/images/addOrUpdate")
    public Response addOrUpdateImage(Request request, Session database) {
        Transaction tx = null;
        try {
            String key = request.getParams().get("key");
            String base64Data = request.getParams().get("data");

            if (key == null || key.isEmpty() || base64Data == null || base64Data.isEmpty()) {
                return Response.Common.error("Key and image data cannot be empty.");
            }

            byte[] imageData = Base64.getDecoder().decode(base64Data);

            CachedImage imageToSave = new CachedImage(key, imageData);

            tx = database.beginTransaction();
            database.merge(imageToSave);

            tx.commit();

            log.info("Successfully added/updated image with key: {}", key);
            return Response.Common.ok();

        } catch (IllegalArgumentException e) {
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

    /**
     * 删除一张图片。
     *
     * @param request  包含图片 key 的请求。
     * @param database 数据库会话。
     * @return 操作结果的响应。
     */
    @RouteMapping(uri = "resource/images/delete")
    public Response deleteImage(Request request, Session database) {
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

}