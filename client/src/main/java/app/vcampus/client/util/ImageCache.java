//client/src/main/java/app/vcampus/client/util/ImageCache.java
package app.vcampus.client.util;

import app.vcampus.client.gateway.ImageClient;
import app.vcampus.client.repository.FakeRepository;
import app.vcampus.server.entity.CachedImage;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 一个自初始化的、线程安全的图片缓存类。
 * <p>
 * 此类使用单例模式，通过 FakeRepository 访问全局 handler，实现自我初始化。
 * </p>
 */
public final class ImageCache {

    private static final ImageCache INSTANCE = new ImageCache();
    private final Map<String, Image> cache = new ConcurrentHashMap<>();
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);

    private ImageCache() {}

    /**
     * 获取 ImageCache 的单例实例。
     * <p>
     * 当任何代码第一次调用此方法时，将触发一次性的异步初始化。
     * </p>
     *
     * @return ImageCache 的唯一实例。
     */
    public static ImageCache getInstance() {
        INSTANCE.initOnce();
        return INSTANCE;
    }

    /**
     * 私有的、只执行一次的初始化方法。
     * <p>
     * 只有当 handler 存在（即已连接）且尚未初始化时，才执行。
     * </p>
     */
    private void initOnce() {
        if (FakeRepository.handler != null && isInitialized.compareAndSet(false, true)) {
            System.out.println("ImageCache: 检测到首次调用，开始从服务器进行异步初始化...");

            new Thread(() -> {
                List<CachedImage> imagesFromServer = ImageClient.getAllImages();

                if (imagesFromServer != null) {
                    for (CachedImage dto : imagesFromServer) {
                        if (dto.getKey() != null && dto.getImageData() != null) {
                            try (ByteArrayInputStream stream = new ByteArrayInputStream(dto.getImageData())) {
                                Image image = new Image(stream);
                                if (!image.isError()) {
                                    addImage(dto.getKey(), image);
                                }
                            } catch (Exception e) {
                                System.err.println("ImageCache (initOnce): 从服务器数据创建图片时出错, Key: " + dto.getKey());
                            }
                        }
                    }
                    System.out.println("ImageCache: 初始化成功，共加载 " + cache.size() + " 张图片到缓存。");
                } else {
                    System.err.println("ImageCache: 从服务器初始化失败，获取数据为 null。");
                }
            }).start();
        }
    }

    /**
     * 从缓存中获取图片。
     *
     * @param path 图片的路径或键。
     * @return 缓存的 Image 对象。
     * @throws RuntimeException 如果图片未在缓存中找到。
     */
    public Image getImage(final String path) {
        Image cachedImage = cache.get(path);
        if(cachedImage == null) throw new RuntimeException("Image not found: " + path);
        return cachedImage;
    }

    /**
     * 向缓存中添加图片。
     *
     * @param path  图片的路径或键。
     * @param image 要缓存的 Image 对象。
     */
    public void addImage(final String path, final Image image) {
        if (path != null && image != null) cache.put(path, image);
    }

    /**
     * 强制刷新缓存。
     * <p>
     * 此方法会异步地从服务器重新获取所有图片数据，并更新缓存。
     * </p>
     */
    public void refresh() {
        System.out.println("ImageCache: 正在强制刷新...");

        new Thread(() -> {
            List<CachedImage> imagesFromServer = ImageClient.getAllImages();

            if (imagesFromServer != null) {
                cache.clear();

                for (CachedImage dto : imagesFromServer) {
                    if (dto.getKey() != null && dto.getImageData() != null) {
                        try (ByteArrayInputStream stream = new ByteArrayInputStream(dto.getImageData())) {
                            Image image = new Image(stream);
                            if (!image.isError()) {
                                addImage(dto.getKey(), image);
                            }
                        } catch (Exception e) {
                            System.err.println("ImageCache (refresh): 从服务器数据创建图片时出错, Key: " + dto.getKey());
                        }
                    }
                }
                System.out.println("ImageCache: 缓存已成功刷新，共加载 " + cache.size() + " 张图片。");
            } else {
                System.err.println("ImageCache: 缓存刷新失败，从服务器获取数据为 null。");
            }
        }).start();
    }
}