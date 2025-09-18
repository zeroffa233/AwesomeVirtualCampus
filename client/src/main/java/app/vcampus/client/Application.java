package app.vcampus.client;

import app.vcampus.client.net.NettyClient;
import app.vcampus.client.net.NettyHandler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Application {
    private static final ExecutorService executorService = java.util.concurrent.Executors.newCachedThreadPool();

    public static NettyHandler connect(String address, int port) throws ExecutionException, InterruptedException {
        NettyClient client = new NettyClient(address, port);
        Future<NettyHandler> future = executorService.submit(client);
        return future.get();
    }
}