package app.vcampus.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setResizable(false);
        // For UI debugging, call startForDebug(). For normal operation, call showLogin().
        //showLogin();
        startForDebug();
    }

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
                    // Set other properties of the user as needed for UI testing
                    app.vcampus.client.repository.FakeRepository.user = debugUser;

                } catch (Exception e) {
                    System.err.println("Debug connection failed: " + e.getMessage());
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


    public static void showLogin() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("/app/vcampus/client/scene/LoginScene.fxml")));
        primaryStage.setTitle("VCampus Login");
        primaryStage.setScene(new Scene(root, 400, 600));
        primaryStage.show();
        primaryStage.requestFocus();
    }

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

    public static void main(String[] args) {
        launch(args);
    }
}
