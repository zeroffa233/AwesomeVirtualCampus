package app.vcampus.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * 商店商品实体类。
 * <p>
 * 记录商店商品的基本信息。
 * 数据库中的每条记录都可以在商店模块中显示和选择。
 * </p>
 */
@Entity
@Data
@Table(name = "store_item")
@Slf4j
public class StoreItem implements IEntity {
    /**
     * 商品的唯一标识符，作为主键，自动生成。
     */
    @Id
    public UUID uuid = UUID.randomUUID();

    /**
     * 商品名称。
     */
    @Column(nullable = false)
    public String itemName;

    /**
     * 商品价格（以分为单位）。
     */
    @Column(nullable = false)
    public Integer price;

    /**
     * 商品图片的链接。
     */
    @Column(nullable = false)
    public String pictureLink;

    /**
     * 商品条形码。
     */
    @Column(nullable = false)
    public String barcode;

    /**
     * 商品库存数量。
     */
    @Column(nullable = false)
    public Integer stock;

    /**
     * 商品销量，默认为0。
     */
    @Column(nullable = false)
    public Integer salesVolume = 0;

    /**
     * 商品描述。
     */
    @Column(columnDefinition = "TEXT")
    public String description;
}