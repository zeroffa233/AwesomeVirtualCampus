package app.vcampus.client.util;
import javafx.scene.image.Image;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;

public final class ImageCache {

    private static final ImageCache INSTANCE = new ImageCache();
    private final Map<String, Image> cache = new ConcurrentHashMap<>();

    private ImageCache() {}

    public static ImageCache getInstance() {
        return INSTANCE;
    }

    public Image getImage(final String path) {
        Image cachedImage = cache.get(path);
        if(cachedImage == null) throw new RuntimeException("Image not found: " + path);
        return cachedImage;
    }

    public void addImage(final String path, final Image image) {
        if (path != null && image != null) cache.put(path, image);
    }
}