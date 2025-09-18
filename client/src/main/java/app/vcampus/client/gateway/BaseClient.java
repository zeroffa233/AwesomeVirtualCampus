package app.vcampus.client.gateway;

import app.vcampus.client.net.NettyHandler;
import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 所有客户端的基类，提供与服务器通信的基础方法。
 */
@Slf4j
public class BaseClient {
    /**
     * 向服务器发送请求并等待响应。
     *
     * @param handler Netty处理器。
     * @param request 要发送的请求对象。
     * @return 服务器返回的响应对象。
     * @throws InterruptedException 如果等待响应时线程被中断。
     */
    public static Response sendRequest(NettyHandler handler, Request request) throws InterruptedException {
        request.setSession(app.vcampus.client.repository.FakeRepository.session);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Response> response = new AtomicReference<>();

        // 【核心修正 1】在发送请求前，我们就已经知道了它的URI。
        // 我们把它保存在一个 final 变量中，以便在回调函数内部安全地使用。
        final String requestUri = request.getUri();
        log.debug("发送请求: {}", request);

        handler.sendRequest(request, res -> {
            // 在回调函数中，我们使用之前保存的 requestUri 变量来进行判断

            // 【核心修正 2】使用“智能日志”逻辑
            if ("resource/images/all".equals(requestUri)) {
                // 如果是获取所有图片的请求，我们打印一个不包含data的摘要日志
                // 注意：我们只使用 res 对象中确实存在的方法，如 getId(), getStatus(), getMessage()
                log.debug("收到响应: Response(id={}, status={}, message={}, data=[...图片列表已省略...])",
                        res.getId(),
                        res.getStatus(),
                        res.getMessage());
            } else {
                // 对于所有其他请求，照常打印完整的响应对象
                log.debug("收到响应: {}", res);
            }

            response.set(res);
            latch.countDown();
        });

        latch.await();

        return response.get();
    }

    /**
     * 将Java对象转换为JSON字符串。
     *
     * @param obj 要转换的Java对象。
     * @return 对应的JSON字符串。
     */
    public static String toJson(Object obj) {
        return new Gson().toJson(obj);
    }
}