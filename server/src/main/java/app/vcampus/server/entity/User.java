package app.vcampus.server.entity;

import app.vcampus.server.enums.Gender;
import jakarta.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 用户实体类。
 * 映射到数据库中的 `user` 表。
 */
@Entity
@Data
@Slf4j
@Table(name = "user")
public class User implements IEntity {
    /**
     * 用户的卡号，作为主键。
     */
    @Id
    @Column(name = "card_number")
    public Integer cardNum;

    /**
     * 用户的密码，经过哈希处理。
     */
    @Column(nullable = false)
    public String password;

    /**
     * 用户的姓名。
     */
    @Column(nullable = false)
    public String name;

    /**
     * 用户的性别。
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Gender gender;

    /**
     * 用户的电话号码。
     */
    @Column(nullable = false)
    public String phone;
    /**
     * 用户的电子邮箱。
     */
    @Column(nullable = false)
    public String email;

    /**
     * 用户的角色字符串，多个角色用逗号分隔。
     */
    @Column(name = "role")
    public String roleStr;

    /**
     * 获取用户的角色数组。
     *
     * @return 角色字符串数组。
     */
    public String[] getRoles() {
        return roleStr.split(",");
    }

    /**
     * 设置用户的角色。
     *
     * @param roles 角色字符串数组。
     */
    public void setRoles(String[] roles) {
        this.roleStr = String.join(",", roles);
    }

}