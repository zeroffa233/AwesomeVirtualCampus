package app.vcampus.client.net;

import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;
import com.google.gson.Gson;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * Netty客户端处理器，负责处理入站和出站消息。
 * 继承自SimpleChannelInboundHandler，用于处理特定类型的入站消息（此处为String）。
 */
@Slf4j
public class NettyHandler extends SimpleChannelInboundHandler<String> {
    /**
     * Gson实例，用于JSON序列化和反序列化。
     */
    private final Gson gson = new Gson();
    /**
     * 存储请求ID与对应回调函数的映射，用于处理异步响应。
     */
    private final Map<UUID, Consumer<Response>> callbacks = new HashMap<>();
    /**
     * 用于同步连接建立的计数器。
     */
    private final CountDownLatch connectionLatch = new CountDownLatch(1);
    /**
     * ChannelHandlerContext实例，代表当前通道的上下文。
     */
    private ChannelHandlerContext ctx;

    /**
     * 当通道激活（连接建立）时调用。
     * 记录连接信息，保存上下文，并释放连接等待锁。
     *
     * @param ctx 通道处理器上下文。
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("[{}] Connected", ctx.channel().id());
        this.ctx = ctx;
        connectionLatch.countDown();
    }

    /**
     * 当通道不激活（连接关闭）时调用。
     * 记录断开连接信息，并清除上下文。
     *
     * @param ctx 通道处理器上下文。
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("[{}] Disconnected", ctx.channel().id());
        this.ctx = null;
    }

    /**
     * 读取服务器发送的入站消息。
     * 将接收到的JSON字符串反序列化为Response对象，并调用对应的回调函数。
     *
     * @param ctx 通道处理器上下文。
     * @param msg 从服务器接收到的消息字符串。
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        try {
            Response response = gson.fromJson(msg, Response.class);
            if (!callbacks.containsKey(response.getId())) {
                throw new IllegalStateException("未找到回调函数");
            }
            callbacks.get(response.getId()).accept(response);
            callbacks.remove(response.getId());
        } catch (Exception e) {
            log.error("[{}] 异常: {}", ctx.channel().id(), e.getMessage());
        }
    }

    /**
     * 向服务器发送请求。
     * 等待连接建立，将请求序列化为JSON字符串并发送，同时注册回调函数以处理响应。
     *
     * @param request 要发送的请求对象。
     * @param callback 处理服务器响应的回调函数。
     */
    public void sendRequest(Request request, Consumer<Response> callback) {
        try {
            connectionLatch.await();
        } catch (InterruptedException e) {
            log.error("等待连接被中断", e);
            Thread.currentThread().interrupt();
            return;
        }

        if (ctx == null) {
            throw new IllegalStateException("通道未连接");
        }

        callbacks.put(request.getId(), callback);
        ctx.writeAndFlush(Unpooled.copiedBuffer(gson.toJson(request), CharsetUtil.UTF_8));
    }

    /**
     * 断开与服务器的连接。
     * 如果通道已连接，则关闭通道。
     */
    public void disconnect() {
        if (ctx != null) {
            ctx.close();
        }
    }

}