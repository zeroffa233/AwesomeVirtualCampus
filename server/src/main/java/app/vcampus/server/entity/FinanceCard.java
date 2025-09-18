package app.vcampus.server.entity;

import app.vcampus.server.enums.CardStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 一卡通金融信息实体类。
 * 映射到数据库中的 `finance_card` 表。
 */
@Entity
@Data
@Table(name = "finance_card")
@Slf4j
public class FinanceCard implements IEntity {
    /**
     * 用户的卡号，作为主键。
     */
    @Id
    public Integer cardNumber = 0;

    /**
     * 卡内余额（以分为单位）。
     */
    public Integer balance = 0;

    /**
     * 一卡通的状态，默认为正常。
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public CardStatus status = CardStatus.normal;
}