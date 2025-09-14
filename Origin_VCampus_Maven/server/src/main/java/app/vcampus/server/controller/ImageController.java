// File: server/src/main/java/app/vcampus/server/controller/ImageController.java
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
import java.util.Map;

@Slf4j
public class ImageController {

    /**
     * 【查 - C(R)UD】
     * 获取所有用于客户端缓存的图片资源。
     * 通常由客户端在启动时调用。
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
     * 【增 - (C)RUD】
     * 添加一张新的图片到数据库。
     * 通常由后台管理界面调用。
     *
     * @param request  需要包含 "key" 和 "imageData" (Base64编码的字符串)
     * @param database Hibernate Session
     * @return 操作成功或失败的 Response
     */
    @RouteMapping(uri = "resource/images/add")
    public Response addImage(Request request, Session database) {
        Transaction tx = null;
        try {
            String key = request.getParams().get("key");
            String base64Data = request.getParams().get("imageData");

            if (key == null || key.isEmpty() || base64Data == null || base64Data.isEmpty()) {
                return Response.Common.error("Key and imageData cannot be empty.");
            }

            // 检查 Key 是否已存在
            if (database.get(CachedImage.class, key) != null) {
                return Response.Common.error("Image with key '" + key + "' already exists.");
            }

            // 将 Base64 字符串解码为 byte[]
            byte[] imageData = Base64.getDecoder().decode(base64Data);

            CachedImage newImage = new CachedImage(key, imageData);

            tx = database.beginTransaction();
            database.persist(newImage);
            tx.commit();

            log.info("Successfully added image with key: {}", key);
            return Response.Common.ok();

        } catch (IllegalArgumentException e) {
            log.error("Failed to add image due to Base64 decoding error", e);
            return Response.Common.error("Invalid Base64 image data.");
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            log.error("Failed to add image to database", e);
            return Response.Common.error("An internal error occurred while adding the image.");
        }
    }

    /**
     * 【改 - CR(U)D】
     * 更新一张已存在的图片。
     *
     * @param request 需要包含 "key" 和 "imageData" (Base64编码的字符串)
     * @param database Hibernate Session
     * @return 操作成功或失败的 Response
     */
    @RouteMapping(uri = "resource/images/update") // 假设只有管理员能更新
    public Response updateImage(Request request, Session database) {
        Transaction tx = null;
        try {
            String key = request.getParams().get("key");
            String base64Data = request.getParams().get("imageData");

            if (key == null || key.isEmpty() || base64Data == null || base64Data.isEmpty()) {
                return Response.Common.error("Key and imageData cannot be empty.");
            }

            // 将 Base64 字符串解码为 byte[]
            byte[] imageData = Base64.getDecoder().decode(base64Data);

            CachedImage imageToUpdate = new CachedImage(key, imageData);

            tx = database.beginTransaction();
            // merge() 会自动处理插入或更新
            database.merge(imageToUpdate);
            tx.commit();

            log.info("Successfully updated image with key: {}", key);
            return Response.Common.ok();

        } catch (IllegalArgumentException e) {
            log.error("Failed to update image due to Base64 decoding error", e);
            return Response.Common.error("Invalid Base64 image data.");
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            log.error("Failed to update image in database", e);
            return Response.Common.error("An internal error occurred while updating the image.");
        }
    }

    /**
     * 【删 - CRU(D)】
     * 删除一张图片。
     *
     * @param request 需要包含 "key"
     * @param database Hibernate Session
     * @return 操作成功或失败的 Response
     */
    @RouteMapping(uri = "resource/images/delete") // 假设只有管理员能删除
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