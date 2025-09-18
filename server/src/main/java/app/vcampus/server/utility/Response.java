package app.vcampus.server.utility;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * 响应类。
 * 用于封装服务器对客户端请求的响应。
 */
@Data
@RequiredArgsConstructor
public class Response {
    /**
     * 响应的唯一标识符。
     */
    UUID id;
    /**
     * 响应状态，例如 "success" 或 "error"。
     */
    @NonNull String status;
    /**
     * 响应消息，提供有关响应状态的更多信息。
     */
    @NonNull String message;

    /**
     * 响应携带的数据对象。
     */
    Object data;
    /**
     * 瞬态会话对象，不参与序列化。
     */
    transient Session session = null;

    /**
     * 包含常用静态响应的内部类。
     */
    public static class Common {
        /**
         * 创建一个表示成功的标准响应。
         *
         * @return 一个状态为 "success" 的响应对象。
         */
        public static Response ok() {
            return new Response("success", "OK");
        }

        /**
         * 创建一个表示成功并携带数据的响应。
         *
         * @param data 要包含在响应中的数据。
         * @return 一个包含数据的成功响应对象。
         */
        public static Response ok(Object data) {
            Response response = new Response("success", "OK");
            response.setData(data);
            return response;
        }

        /**
         * 创建一个表示错误的响应。
         *
         * @param message 错误信息。
         * @return 一个包含指定错误信息的状态为 "error" 的响应对象。
         */
        public static Response error(String message) {
            return new Response("error", message);
        }

        /**
         * 创建一个表示权限不足的响应。
         *
         * @return 一个 "Permission denied" 的错误响应对象。
         */
        public static Response permissionDenied() {
            return new Response("error", "Permission denied");
        }

        /**
         * 创建一个表示错误请求的响应。
         *
         * @return 一个 "Bad request" 的错误响应对象。
         */
        public static Response badRequest() {
            return new Response("error", "Bad request");
        }

        /**
         * 创建一个表示未找到资源的响应。
         *
         * @return 一个 "Not found" 的错误响应对象。
         */
        public static Response notFound() {
            return new Response("error", "Not found");
        }

        /**
         * 创建一个表示服务器内部错误的响应。
         *
         * @return 一个 "Internal error" 的错误响应对象。
         */
        public static Response internalError() {
            return new Response("error", "Internal error");
        }
    }
}