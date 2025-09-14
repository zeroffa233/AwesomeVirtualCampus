//client/src/main/java/app/vcampus/client/util/ImageCache.java
package app.vcampus.client.util;

import app.vcampus.client.gateway.ImageClient;
import app.vcampus.client.repository.FakeRepository; // 【重要】导入 FakeRepository
import app.vcampus.server.entity.CachedImage;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 【最终版】一个自初始化的、线程安全的图片缓存类。
 * 它通过 FakeRepository 访问全局 handler，实现自我初始化。
 */
public final class ImageCache {

    private static final ImageCache INSTANCE = new ImageCache();
    private final Map<String, Image> cache = new ConcurrentHashMap<>();
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);

    private ImageCache() {}

    public static ImageCache getInstance() {
        // 当任何代码第一次调用 getInstance() 时，就触发一次性的初始化。
        INSTANCE.initOnce();
        return INSTANCE;
    }

    /**
     * 私有的、只执行一次的初始化方法。
     */
    private void initOnce() {
        // 只有当 handler 存在（即已连接）且尚未初始化时，才执行
        if (FakeRepository.handler != null && isInitialized.compareAndSet(false, true)) {
            System.out.println("ImageCache: 检测到首次调用，开始从服务器进行异步初始化...");

            new Thread(() -> {
                // 直接调用我们新的、静态的 ImageClient.getAllImages()
                List<CachedImage> imagesFromServer = ImageClient.getAllImages();

                if (imagesFromServer != null) {
                    for (CachedImage dto : imagesFromServer) {
                        // ... (填充缓存的逻辑保持不变)
                    }
                    System.out.println("ImageCache: 初始化成功，共加载 " + imagesFromServer.size() + " 张图片。");
                } else {
                    System.err.println("ImageCache: 从服务器初始化失败。");
                }
            }).start();
        }
    }

    // ... getImage 和 addImage 方法保持您原有的、简洁的版本 ...
    public Image getImage(final String path) {
        Image cachedImage = cache.get(path);
        if(cachedImage == null) throw new RuntimeException("Image not found: " + path);
        return cachedImage;
    }

    public void addImage(final String path, final Image image) {
        if (path != null && image != null) cache.put(path, image);
    }
}