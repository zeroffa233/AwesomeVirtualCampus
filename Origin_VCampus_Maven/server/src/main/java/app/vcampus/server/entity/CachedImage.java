package app.vcampus.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "image_cache") // 数据库中的表名
public class CachedImage implements IEntity{

    @Id
    @Column(name = "image_key", nullable = false, unique = true)
    private String key; // 主键，例如 "/images/DARKSOULS.png"

    @Lob // 表示这是一个大对象 (Large Object)
    @Column(name = "image_data", nullable = false, columnDefinition="LONGBLOB") // 显式指定为 LONGBLOB
    private byte[] imageData; // 存储图片的二进制数据

    // Hibernate 要求必须有一个无参构造函数
    public CachedImage() {
    }

    public CachedImage(String key, byte[] imageData) {
        this.key = key;
        this.imageData = imageData;
    }

    // --- Getters and Setters ---
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }
}