package app.vcampus.server.controller;

import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;
import app.vcampus.server.utility.router.RouteMapping;

/**
 * 索引控制器。
 * 提供一些通用的、与特定模块无关的功能，例如心跳检测。
 */
public class IndexController {
    /**
     * 心跳检测端点。
     * 用于检查服务器是否正在运行。
     *
     * @param request  请求对象。
     * @param database 数据库会话。
     * @return 一个成功的响应。
     */
    @RouteMapping(uri = "heartbeat")
    public Response heartbeat(Request request, org.hibernate.Session database) {
        return Response.Common.ok();
    }
}