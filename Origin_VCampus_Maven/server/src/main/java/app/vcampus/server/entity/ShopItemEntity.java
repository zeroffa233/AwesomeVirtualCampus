package app.vcampus.server.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "shop_items")
public class ShopItemEntity {

    @Id
    @Column(columnDefinition = "binary(16)")
    private UUID uuid;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private Double price;

    @Column(nullable = false)
    private Integer stock;

    @Lob
    @Column(nullable = false, columnDefinition = "mediumblob")
    private byte[] image;

    @Column
    private String description;

    @Column(name = "owner_card_number", nullable = false) // 【新增】
    private String ownerCardNumber;
}