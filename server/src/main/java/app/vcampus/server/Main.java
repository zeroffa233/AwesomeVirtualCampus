package app.vcampus.server;

import app.vcampus.server.controller.*;
import app.vcampus.server.net.NettyServer;
import app.vcampus.server.utility.Database;
import app.vcampus.server.utility.router.Router;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.io.Console;

/**
 * 服务器主类。
 */
public class Main {

    /**
     * 数据库用户名。
     */
    private static String DB_USERNAME;
    /**
     * 数据库密码。
     */
    private static String DB_PASSWORD;

    /**
     * 服务器程序入口点。
     *
     * @param args 命令行参数。
     * @throws Exception 启动过程中可能发生的任何异常。
     */
    public static void main(String[] args) throws Exception {
        Router router = new Router();
        router.addController(AuthController.class);
        router.addController(IndexController.class);
        router.addController(StudentStatusController.class);
        router.addController(LibraryBookController.class);
        router.addController(StoreController.class);
        router.addController(TeachingAffairsController.class);
        router.addController(FinanceController.class);
        router.addController(AdminController.class);
        router.addController(GptController.class);
        router.addController(ImageController.class);
        router.addController(ChatController.class);
        router.addController(HistoryController.class);

        // 从控制台读取数据库用户名和密码
        Console console = System.console();
        if (console == null) {
            System.err.println("❌ 控制台不可用。请在终端中运行此程序！ ");
            System.exit(-1);
        }
        DB_USERNAME = console.readLine("Enter database username: ");
        char[] passwordChars = console.readPassword("database password:");
        DB_PASSWORD = new String(passwordChars);

        // 初始化数据库和服务器
        SessionFactory databaseFactory = Database.init(DB_USERNAME, DB_PASSWORD);
        org.hibernate.Session database = databaseFactory.openSession();
        Transaction tx = database.beginTransaction();
        tx.commit();
        NettyServer server = new NettyServer(9091);
        server.run(router, databaseFactory);
    }
}