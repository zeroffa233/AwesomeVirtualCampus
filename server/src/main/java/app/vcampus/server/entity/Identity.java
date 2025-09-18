package app.vcampus.server.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 身份实体类。
 * <p>
 * 用于维护校园卡号和用户名的映射关系。
 * 直接映射到数据库的 `identities` 表。
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "identities")
public class Identity {

    /**
     * 用户的卡号，作为主键。
     */
    @Id
    private Integer cardNum;

    /**
     * 用户的姓名。
     */
    private String userName;
}