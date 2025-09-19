package app.vcampus.server.net;

import app.vcampus.server.utility.router.Router;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
// 导入 SelfSignedCertificate
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.NonNull;
import org.hibernate.SessionFactory;

// 不再需要这些导入
// import java.io.FileInputStream;
// import java.security.KeyStore;
// import javax.net.ssl.KeyManagerFactory;

/**
 * Netty 服务器类。
 * 负责启动和配置基于 Netty 的网络服务器。
 */
public class NettyServer {
    /**
     * 服务器监听的端口号。
     */
    private final int port;

    /**
     * 构造一个 NettyServer 实例。
     *
     * @param port 服务器要监听的端口。
     */
    public NettyServer(int port) {
        this.port = port;
    }

    /**
     * 启动服务器。
     *
     * @param router  用于处理请求的路由器。
     * @param session Hibernate 的 SessionFactory，用于数据库操作。
     * @throws Exception 如果服务器启动过程中发生错误。
     */
    public void run(Router router, SessionFactory session) throws Exception {
        // 1. 在内存中配置SSL
//        final SslContext sslCtx;
//        try {
//            // 直接在内存中生成自签名证书
//            SelfSignedCertificate ssc = new SelfSignedCertificate();
//            // 使用生成的证书和私钥构建SslContext
//            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to initialize the SSL context from memory", e);
//        }

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(@NonNull SocketChannel ch) {
                            // 2. 将SslHandler添加到pipeline的首位
                            //ch.pipeline().addLast(sslCtx.newHandler(ch.alloc()));
                            ch.pipeline()
                                    .addLast(new JsonObjectDecoder(100 * 1024 * 1024))
                                    .addLast(new NettyHandler(router, session));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(port).sync();
            System.out.println("Server started on port " + port);
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

}