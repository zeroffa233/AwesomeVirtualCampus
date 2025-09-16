package app.vcampus.server.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Identity 实体，用于维护校园卡号和用户名的映射关系。
 * 直接映射到数据库的 'identities' 表。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "identities")
public class Identity {

    @Id
    private Integer cardNum;

    private String userName;
}