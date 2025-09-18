package app.vcampus.server.utility;

import lombok.Data;

/**
 * 会话类。
 * 用于存储用户的会话信息，如卡号和角色。
 */
@Data
public class Session {
    /**
     * 用户的卡号。
     */
    int cardNum;
    /**
     * 用户拥有的角色数组。
     */
    String[] roles;

    /**
     * 检查当前会话是否具有指定角色的权限。
     *
     * @param role 要检查的角色。
     * @return 如果会话具有权限，则返回 true。
     *         "anonymous" 角色总是返回 true。
     *         "admin" 角色拥有所有权限。
     */
    public boolean permission(String role) {
        if (role.equals("anonymous")) {
            return true;
        } else if (roles == null) {
            return false;
        }

        for (String r : roles) {
            if (r.equals(role) || r.equals("admin")) {
                return true;
            }
        }

        return false;
    }
}