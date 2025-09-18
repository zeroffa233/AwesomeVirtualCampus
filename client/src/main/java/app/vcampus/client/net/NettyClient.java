package app.vcampus.client.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext; // 导入
import io.netty.handler.ssl.SslContextBuilder; // 导入
import io.netty.handler.ssl.util.InsecureTrustManagerFactory; // 导入
import io.netty.util.CharsetUtil;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;

public class NettyClient implements Callable<NettyHandler> {
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup();
    private final String host;
    private final int port;

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public NettyHandler call() throws Exception { // 声明抛出Exception
        // 1. 配置客户端SSL
        final SslContext sslCtx = SslContextBuilder.forClient()
                // 使用 InsecureTrustManagerFactory 来信任任何证书，仅用于开发测试！
                .trustManager(InsecureTrustManagerFactory.INSTANCE).build();

        try {
            NettyHandler handler = new NettyHandler();

            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(@NotNull SocketChannel ch) throws Exception {
                    // 2. 将SslHandler添加到pipeline的首位
                    ch.pipeline().addLast(sslCtx.newHandler(ch.alloc(), host, port));

                    ch.pipeline().addLast(new JsonObjectDecoder(100 * 1024 * 1024))
                            .addLast(new StringEncoder(CharsetUtil.UTF_8))
                            .addLast(new StringDecoder(CharsetUtil.UTF_8))
                            .addLast(handler);
                }
            });

            ChannelFuture f = b.connect(host, port).syncUninterruptibly();

            return handler;
        } finally {
            // 注意：这里的 workerGroup 不应该在一次调用后就关闭，
            // 因为它是静态共享的。它的关闭应该在应用程序退出时统一管理。
            // 在您原始代码中这里是空的，保持即可。
        }
    }
}