package app.vcampus.server.net;

import app.vcampus.server.utility.Request;
import app.vcampus.server.utility.Response;
import app.vcampus.server.utility.Session;
import app.vcampus.server.utility.router.Router;
import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;

/**
 * Netty 处理器类。
 * 负责处理客户端连接、消息读取和响应发送。
 */
@Slf4j
public class NettyHandler extends ChannelInboundHandlerAdapter {
    private final Gson gson = new Gson();
    private final Router router;
    private final SessionFactory database;
    private Session session;

    /**
     * 构造一个新的 NettyHandler。
     *
     * @param router   请求路由器。
     * @param database 数据库会话工厂。
     */
    public NettyHandler(Router router, SessionFactory database) {
        this.router = router;
        this.database = database;
    }

    /**
     * 当一个新的连接建立时被调用。
     *
     * @param ctx Channel 处理器上下文。
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("[{}] Client connected from {}", ctx.channel().id(), ctx.channel().remoteAddress());
        session = new Session();
    }

    /**
     * 当连接关闭时被调用。
     *
     * @param ctx Channel 处理器上下文。
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("[{}] Client disconnected", ctx.channel().id());
    }

    /**
     * 当接收到消息时被调用。
     *
     * @param ctx Channel 处理器上下文。
     * @param msg 接收到的消息。
     */
    @Override
    public void channelRead(@NonNull ChannelHandlerContext ctx, @NonNull Object msg) {
        ByteBuf in = (ByteBuf) msg;
        try {
            log.info("[{}] Received: {}", ctx.channel().id(), in.toString(CharsetUtil.UTF_8));
            Request request = gson.fromJson(in.toString(CharsetUtil.UTF_8), Request.class);
            request.setSession(session);

            Response response;

            if (!router.hasRoute(request.getUri())) {
                log.info("[{}] Route not found: {}", ctx.channel().id(), request.getUri());
                response = Response.Common.notFound();
            } else if (!session.permission(router.getRole(request.getUri()))) {
                log.info("[{}] Permission denied: {}", ctx.channel().id(), request.getUri());
                response = Response.Common.permissionDenied();
            } else {
                response = router.invoke(request, database.openSession());
            }

            if (response.getSession() != null) {
                log.info("[{}] Session updated: {}", ctx.channel().id(), response.getSession());
                session = response.getSession();
            }

            response.setId(request.getId());
            sendResponse(ctx, response);
        } catch (Exception e) {
            log.error("[{}] Exception: {}", ctx.channel().id(), e.getMessage());
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 发送响应给客户端。
     *
     * @param ctx      Channel 处理器上下文。
     * @param response 要发送的响应。
     */
    private void sendResponse(ChannelHandlerContext ctx, Response response) {
        ctx.writeAndFlush(Unpooled.copiedBuffer(gson.toJson(response), CharsetUtil.UTF_8));
    }

    /**
     * 当捕获到异常时被调用。
     *
     * @param ctx   Channel 处理器上下文。
     * @param cause 异常。
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("[{}] Exception: {}", ctx.channel().id(), cause.getMessage());
        ctx.close();
    }
}