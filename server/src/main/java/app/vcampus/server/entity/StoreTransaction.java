package app.vcampus.server.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.UUID;

/**
 * 商店交易实体类。
 * <p>
 * 记录商店交易中的商品基本信息。
 * 当用户选择购买某样商品时，会生成一条商店交易记录。
 * 用于计算总支出和跟踪销售统计。
 * </p>
 */
@Entity
@Data
@Table(name = "store_transaction")
@Slf4j
public class StoreTransaction implements IEntity {
    /**
     * 交易的唯一标识符，作为主键。
     */
    @Id
    public UUID uuid;

    /**
     * 交易涉及的商品UUID。
     */
    @Column(nullable = false)
    public UUID itemUUID;

    /**
     * 交易时商品的单价（以分为单位）。
     */
    @Column(nullable = false)
    public Integer itemPrice;

    /**
     * 交易的商品数量。
     */
    @Column(nullable = false)
    public Integer amount;

    /**
     * 购买用户的卡号。
     */
    @Column(nullable = false)
    public Integer cardNumber;

    /**
     * 交易发生的时间。
     */
    @Column(nullable = false)
    public Date time;

    /**
     * 交易备注。
     */
    public String remark;

    /**
     * 关联的商品对象，瞬态字段，不映射到数据库。
     */
    @Transient
    public StoreItem item;

}