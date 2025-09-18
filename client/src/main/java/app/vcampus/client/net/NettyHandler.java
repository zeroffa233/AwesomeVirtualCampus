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
 * NettyHandler class.
 */
@Slf4j
public class NettyHandler extends SimpleChannelInboundHandler<String> {
    private final Gson gson = new Gson();
    private final Map<UUID, Consumer<Response>> callbacks = new HashMap<>();
    private final CountDownLatch connectionLatch = new CountDownLatch(1);
    private ChannelHandlerContext ctx;

    /**
     * Called when a new connection is made.
     *
     * @param ctx The channel handler context.
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("[{}] Connected", ctx.channel().id());
        this.ctx = ctx;
        connectionLatch.countDown();
    }

    /**
     * Called when a connection is closed.
     *
     * @param ctx The channel handler context.
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("[{}] Disconnected", ctx.channel().id());
        this.ctx = null;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        try {
            Response response = gson.fromJson(msg, Response.class);
            if (!callbacks.containsKey(response.getId())) {
                throw new IllegalStateException("Callback not found");
            }
            callbacks.get(response.getId()).accept(response);
            callbacks.remove(response.getId());
        } catch (Exception e) {
            log.error("[{}] Exception: {}", ctx.channel().id(), e.getMessage());
        }
    }

    /**
     * Send a request to the server.
     *
     * @param request The request.
     * @param callback The callback.
     */
    public void sendRequest(Request request, Consumer<Response> callback) {
        try {
            connectionLatch.await();
        } catch (InterruptedException e) {
            log.error("Waiting for connection interrupted", e);
            Thread.currentThread().interrupt();
            return;
        }

        if (ctx == null) {
            throw new IllegalStateException("Channel not connected");
        }

        callbacks.put(request.getId(), callback);
        ctx.writeAndFlush(Unpooled.copiedBuffer(gson.toJson(request), CharsetUtil.UTF_8));
    }

    public void disconnect() {
        if (ctx != null) {
            ctx.close();
        }
    }

}