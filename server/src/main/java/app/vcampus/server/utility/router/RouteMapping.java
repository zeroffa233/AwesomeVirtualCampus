package app.vcampus.server.utility.router;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 路由映射注解。
 * 用于将 URI 和角色标记为路由。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RouteMapping {
    /**
     * 路由的 URI。
     *
     * @return URI 字符串。
     */
    String uri();

    /**
     * 访问路由所需的用户角色。
     * 默认为 "anonymous"，表示任何用户都可以访问。
     *
     * @return 角色字符串。
     */
    String role() default "anonymous";
}