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

/**
 * Netty客户端，用于连接服务器并处理网络通信。
 * 实现Callable接口，以便在单独的线程中执行网络连接操作。
 */
public class NettyClient implements Callable<NettyHandler> {
    /**
     * 用于处理客户端I/O操作的事件循环组。
     */
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup();
    /**
     * 服务器主机名或IP地址。
     */
    private final String host;
    /**
     * 服务器端口号。
     */
    private final int port;

    /**
     * 构造一个新的NettyClient实例。
     *
     * @param host 服务器主机名或IP地址。
     * @param port 服务器端口号。
     */
    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 建立与服务器的连接并返回一个NettyHandler实例。
     *
     * @return 处理网络通信的NettyHandler实例。
     * @throws Exception 如果连接过程中发生错误。
     */
    @Override
    public NettyHandler call() throws Exception { // 声明抛出Exception
        // 1. 配置客户端SSL
//        final SslContext sslCtx = SslContextBuilder.forClient()
//                // 使用 InsecureTrustManagerFactory 来信任任何证书，仅用于开发测试！
//                .trustManager(InsecureTrustManagerFactory.INSTANCE).build();

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
                    //ch.pipeline().addLast(sslCtx.newHandler(ch.alloc(), host, port));

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