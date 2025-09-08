package app.vcampus.server;

import app.vcampus.server.controller.*;
import app.vcampus.server.entity.Course;
import app.vcampus.server.entity.TeachingClass;
import app.vcampus.server.net.NettyServer;
import app.vcampus.server.utility.Database;
import app.vcampus.server.utility.Pair;
import app.vcampus.server.utility.router.Router;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.io.Console;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import java.util.List;
import java.util.UUID;

/**
 * Main class of the server.
 */
public class
Main {

    /**
     * Entry function of the server.
     *
     * @param args Command line arguments.
     * @throws Exception Any exception that may occur.
     */
    private static String DB_USERNAME;
    private static String DB_PASSWORD;

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

        // 读取数据库用户名 & 密码
        Console console = System.console();
        if (console == null) {
            System.err.println("❌ 控制台不可用。请在终端中运行此程序！");
            System.exit(-1);
        }
        DB_USERNAME = console.readLine("Enter database username: ");
        // 读取密码（输入不会显示）
        char[] passwordChars = console.readPassword("database password:");
        DB_PASSWORD = new String(passwordChars);

        SessionFactory databaseFactory = Database.init(DB_USERNAME, DB_PASSWORD);
        org.hibernate.Session database = databaseFactory.openSession();
        Transaction tx = database.beginTransaction();
        tx.commit();
        NettyServer server = new NettyServer(9091);
        server.run(router, databaseFactory);
    }
}