package app.vcampus.client;

import app.vcampus.client.net.NettyClient;
import app.vcampus.client.net.NettyHandler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * 客户端应用程序的入口点，负责管理网络连接。
 */
public class Application {
    /**
     * 用于执行异步任务的线程池。
     */
    private static final ExecutorService executorService = java.util.concurrent.Executors.newCachedThreadPool();

    /**
     * 连接到指定的服务器地址和端口。
     *
     * @param address 服务器地址。
     * @param port 服务器端口。
     * @return 连接成功的NettyHandler实例。
     * @throws ExecutionException 如果计算抛出异常。
     * @throws InterruptedException 如果当前线程在等待时被中断。
     */
    public static NettyHandler connect(String address, int port) throws ExecutionException, InterruptedException {
        NettyClient client = new NettyClient(address, port);
        Future<NettyHandler> future = executorService.submit(client);
        return future.get();
    }
}