package app.vcampus.server.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.UUID;

/**
 * 图书馆交易实体类。
 * 用于记录图书的借阅和归还信息。
 */
@Entity
@Data
@Slf4j
@Table(name = "library_transaction")
public class LibraryTransaction implements IEntity {
    /**
     * 交易的唯一标识符，作为主键，自动生成。
     */
    @Id
    public UUID uuid = UUID.randomUUID();

    /**
     * 关联的图书的 UUID。
     */
    @Column(nullable = false)
    public UUID bookUuid;

    /**
     * 借阅用户的ID。
     */
    @Column(nullable = false)
    public Integer userId;

    /**
     * 借书时间。
     */
    @Column(nullable = false)
    public Date borrowTime;

    /**
     * 应还时间。
     */
    @Column(nullable = false)
    public Date dueTime;

    /**
     * 实际还书时间。
     */
    public Date returnTime;

    /**
     * 关联的图书对象，瞬态字段，不映射到数据库。
     */
    @Transient
    public LibraryBook book;
}