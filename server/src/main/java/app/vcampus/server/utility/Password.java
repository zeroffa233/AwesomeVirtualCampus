package app.vcampus.server.utility;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

/**
 * 密码工具类。
 * 提供密码哈希和验证功能。
 */
public class Password {
    /**
     * 获取一个 Argon2 密码编码器实例。
     *
     * @return Argon2PasswordEncoder 实例。
     */
    public static Argon2PasswordEncoder encoder() {
        return new Argon2PasswordEncoder(16, 32, 1, 1 << 12, 3);
    }

    /**
     * 对密码进行哈希处理。
     *
     * @param password 要哈希的原始密码。
     * @return 哈希后的密码字符串。
     */
    public static String hash(String password) {
        return encoder().encode(password);
    }

    /**
     * 验证密码是否正确。
     *
     * @param password 待验证的原始密码。
     * @param hashed   存储的哈希密码。
     * @return 如果密码匹配，则返回 true。
     */
    public static boolean verify(String password, String hashed) {
        return encoder().matches(password, hashed);
    }
}