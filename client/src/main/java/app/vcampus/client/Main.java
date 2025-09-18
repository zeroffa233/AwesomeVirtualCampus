package app.vcampus.client;

import com.jfoenix.controls.JFXButton;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

/**
 * JavaFX应用程序的主入口点。
 * 负责初始化舞台、加载场景以及处理应用程序的启动和调试模式。
 */
public class Main extends Application {

    /**
     * 应用程序的主舞台。
     */
    private static Stage primaryStage;

    /**
     * JavaFX应用程序的入口方法。
     * @param stage 主舞台。
     * @throws IOException 如果加载FXML文件失败。
     */
    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setResizable(false);

        // 【核心修改】添加窗口关闭事件处理器
        // 当用户点击关闭按钮时，此代码块将被执行
        primaryStage.setOnCloseRequest(event -> {
            // Platform.exit() 会触发JavaFX应用的正常关闭流程
            javafx.application.Platform.exit();
            // System.exit(0) 确保Java虚拟机完全终止
            System.exit(0);
        });

        // For UI debugging, call startForDebug(). For normal operation, call showLogin().
        showLogin();
        //startForDebug();
    }

    /**
     * 启动调试模式，连接到服务器并显示主面板。
     */
    public static void startForDebug() {
        // This task will handle the network connection in the background
        javafx.concurrent.Task<Void> initTask = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Attempt to connect to the server
                    app.vcampus.client.net.NettyHandler handler = app.vcampus.client.Application.connect("127.0.0.1", 9091);
                    app.vcampus.client.repository.FakeRepository.handler = handler;
                    app.vcampus.client.repository.FakeRepository.isConnected = true;

                    // Create a dummy user for debugging purposes
                    app.vcampus.server.entity.User debugUser = new app.vcampus.server.entity.User();
                    debugUser.setName("Debug User");
                    debugUser.setRoleStr("library_staff");
                    debugUser.setCardNum(123456);


                } catch (Exception e) {
                    System.err.println("调试连接失败: " + e.getMessage());
                    // We can still proceed to show the UI for layout debugging
                }
                return null;
            }
        };

        initTask.setOnSucceeded(event -> {
            try {
                // Once the task is done (even if connection failed), show the main panel
                showMainPanel();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        initTask.setOnFailed(event -> {
            // Log any exception that occurred during the task
            initTask.getException().printStackTrace();
        });

        new Thread(initTask).start();
    }


    /**
     * 显示登录场景。
     * @throws IOException 如果加载FXML文件失败。
     */
    public static void showLogin() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("/app/vcampus/client/scene/LoginScene.fxml")));
        primaryStage.setTitle("VCampus 登录");
        primaryStage.setScene(new Scene(root, 400, 600));
        primaryStage.show();
        primaryStage.requestFocus();
    }

    /**
     * 显示主面板场景。
     * @throws IOException 如果加载FXML文件失败。
     */
    public static void showMainPanel() throws IOException {
        // 1. 加载 FXML 布局
        Parent root = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("/app/vcampus/client/scene/MainScene.fxml")));

        // 2. 创建场景 (Scene)
        Scene scene = new Scene(root, 1400, 800);

        // 3. 【核心Hack】将我们的全局样式表应用到这个场景上
        scene.getStylesheets().add(Objects.requireNonNull(Main.class.getResource("/css/disable_focus_indicator.css")).toExternalForm());

        // 4. 配置并显示舞台 (Stage)
        primaryStage.setTitle("VCampus");
        primaryStage.setScene(scene); // 将配置好样式的 scene 设置给 stage
        primaryStage.centerOnScreen();
        primaryStage.show();
        primaryStage.requestFocus();
    }

    /**
     * 应用程序的主方法。
     * @param args 命令行参数。
     */
    public static void main(String[] args) {
        launch(args);
    }
}