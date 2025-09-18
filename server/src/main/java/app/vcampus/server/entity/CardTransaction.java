package app.vcampus.server.entity;

import app.vcampus.server.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.UUID;

/**
 * 一卡通交易实体类。
 * 映射到数据库中的 `card_transaction` 表。
 */
@Entity
@Data
@Table(name = "card_transaction")
@Slf4j
public class CardTransaction implements IEntity {
    /**
     * 交易的唯一标识符，作为主键，自动生成。
     */
    @Id
    public UUID uuid = UUID.randomUUID();

    /**
     * 交易关联的卡号。
     */
    @Column(nullable = false)
    public Integer cardNumber;

    /**
     * 交易金额（以分为单位）。
     */
    public Integer amount;

    /**
     * 交易类型（例如，充值、消费）。
     */
    @Enumerated(EnumType.STRING)
    public TransactionType type;

    /**
     * 交易描述。
     */
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    public String description;

    /**
     * 交易发生的时间。
     */
    public Date time;
}