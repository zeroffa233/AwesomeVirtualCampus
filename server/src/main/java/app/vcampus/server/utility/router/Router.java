package app.vcampus.server.utility.router;

import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 路由器类。
 * 用于将请求路由到控制器。
 * 基于注解和反射实现。
 */
@Slf4j
public class Router {
    private final Map<String, Object> controllerBeans = new HashMap<>();
    private final Map<String, Action> uri2Action = new HashMap<>();
    private final Map<String, String> uri2Role = new HashMap<>();

    /**
     * 向路由器添加一个控制器。
     *
     * @param cls 控制器的类。
     */
    public void addController(Class<?> cls) {
        try {
            log.info("Router: addController: cls: {}", cls.getName());

            Method[] methods = cls.getDeclaredMethods();
            for (Method method : methods) {
                Annotation[] annotations = method.getAnnotations();
                for (Annotation annotation : annotations) {
                    if (annotation.annotationType() == RouteMapping.class) {
                        RouteMapping anno = (RouteMapping) annotation;
                        String uri = anno.uri();
                        String role = anno.role();
                        if (!controllerBeans.containsKey(cls.getName())) {
                            controllerBeans.put(cls.getName(), cls.getDeclaredConstructor().newInstance());
                        }
                        uri2Action.put(uri, new Action(controllerBeans.get(cls.getName()), method));
                        uri2Role.put(uri, role);
                        log.info("Router: addController: uri: {}, method: {}, role: {}", uri, method.getName(), role);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Router: addRouter: Exception: {}", e.getMessage());
        }
    }

    /**
     * 检查路由器是否包含某个路由。
     *
     * @param uri 要检查的 URI。
     * @return 如果路由器包含该路由，则返回 true。
     */
    public boolean hasRoute(String uri) {
        return uri2Action.containsKey(uri);
    }

    /**
     * 获取路由所需的角色。
     *
     * @param uri 要检查的 URI。
     * @return 路由所需的角色。
     */
    public String getRole(String uri) {
        return uri2Role.get(uri);
    }

    /**
     * 调用一个路由。
     *
     * @param request  要调用的请求。
     * @param database 数据库会话。
     * @return 路由的响应。
     */
    public Response invoke(Request request, Session database) {
        Action action = uri2Action.get(request.getUri());
        log.info("Router: invoke: action: {}", action);
        if (action != null) {
            return (Response) action.call(request, database);
        } else {
            return Response.Common.notFound();
        }
    }

    /**
     * Action 记录类。
     * 用于存储路由的操作。
     *
     * @param object 要调用的控制器。
     * @param method 要调用的方法。
     */
    private record Action(Object object, Method method) {

        public Object call(Request request, Session database) {
            try {
                return method.invoke(object, request, database);
            } catch (IllegalAccessException e) {
                log.error("Router: Action: call: IllegalAccessException: {}", e.getMessage());
                return Response.Common.error("访问错误: " + e.getMessage());
            } catch (InvocationTargetException e) {
                log.error("Router: Action: call: InvocationTargetException: {}", e.getMessage());
                // 获取原始异常信息，提供更具体的错误
                Throwable targetException = e.getTargetException();
                return Response.Common.error("执行错误: " + (targetException != null ? targetException.getMessage() : e.getMessage()));
            }
        }
    }

}