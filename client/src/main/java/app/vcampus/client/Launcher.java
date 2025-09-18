package app.vcampus.client;

/**
 * 启动器类，用于启动JavaFX应用程序。
 * 这是为了解决在胖JAR中JavaFX运行时组件不在模块路径上的问题。
 */
public class Launcher {
    /**
     * 应用程序的主方法，调用Main类的main方法启动JavaFX应用程序。
     * @param args 命令行参数。
     */
    public static void main(String[] args) {
        Main.main(args);
    }
}