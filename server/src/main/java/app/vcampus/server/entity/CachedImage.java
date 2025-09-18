package app.vcampus.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.ToString;

/**
 * 缓存图片实体类。
 * 映射到数据库中的 `image_cache` 表，用于存储图片的二进制数据。
 */
@Entity
@Table(name = "image_cache")
@ToString
public class CachedImage implements IEntity{

    /**
     * 图片的键，作为主键，通常是图片的路径。
     */
    @Id
    @Column(name = "image_key", nullable = false, unique = true)
    private String key;

    /**
     * 存储图片的二进制数据。
     */
    @Lob
    @Column(name = "image_data", nullable = false, columnDefinition="LONGBLOB")
    @ToString.Exclude
    private byte[] imageData;

    /**
     * 默认构造函数。
     * Hibernate 要求实体类必须有一个无参构造函数。
     */
    public CachedImage() {
    }

    /**
     * 构造一个新的缓存图片对象。
     *
     * @param key       图片的键。
     * @param imageData 图片的二进制数据。
     */
    public CachedImage(String key, byte[] imageData) {
        this.key = key;
        this.imageData = imageData;
    }

    /**
     * 获取图片的键。
     *
     * @return 图片的键。
     */
    public String getKey() {
        return key;
    }

    /**
     * 设置图片的键。
     *
     * @param key 新的图片键。
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * 获取图片的二进制数据。
     *
     * @return 图片的二进制数据。
     */
    public byte[] getImageData() {
        return imageData;
    }

    /**
     * 设置图片的二进制数据。
     *
     * @param imageData 新的图片二进制数据。
     */
    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }
}