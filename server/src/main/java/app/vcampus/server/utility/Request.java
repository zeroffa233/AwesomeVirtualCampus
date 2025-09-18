package app.vcampus.server.utility;

import lombok.Data;

import java.util.Map;
import java.util.UUID;

/**
 * 请求类。
 * 用于封装客户端发送到服务器的请求。
 */
@Data
public class Request {
    /**
     * 请求的唯一标识符，自动生成。
     */
    UUID id = UUID.randomUUID();

    /**
     * 请求的目标统一资源标识符 (URI)。
     */
    String uri;
    /**
     * 请求参数的映射表。
     */
    Map<String, String> params;

    /**
     * 与请求关联的会话对象。
     */
    Session session;
}