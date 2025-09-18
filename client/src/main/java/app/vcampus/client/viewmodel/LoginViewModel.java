package app.vcampus.client.viewmodel;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;

/**
 * 登录视图模型。
 * 负责处理登录界面的逻辑，包括用户输入、登录状态和错误消息。
 */
@Slf4j
public class LoginViewModel {
    /**
     * 用户名（卡号）输入框的属性。
     */
    public final StringProperty username = new SimpleStringProperty("");
    /**
     * 密码输入框的属性。
     */
    public final StringProperty password = new SimpleStringProperty("");
    /**
     * 登录状态的布尔属性。
     */
    public final BooleanProperty loginState = new SimpleBooleanProperty(false);
    /**
     * 错误消息文本属性。
     */
    public final StringProperty errorMessage = new SimpleStringProperty("");
    /**
     * 服务器地址输入框的属性。
     */
    public final StringProperty serverAddress = new SimpleStringProperty("127.0.0.1:9091");

    /**
     * 执行登录操作。
     * 在后台线程中连接服务器并验证用户凭据。
     */
    public void login() {
        errorMessage.set("");

        Task<Boolean> loginTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                app.vcampus.client.repository.FakeRepository.disconnect();

                try {
                    String[] serverAddr = serverAddress.get().split(":");
                    app.vcampus.client.net.NettyHandler handler = app.vcampus.client.Application.connect(serverAddr[0], Integer.parseInt(serverAddr[1]));
                    app.vcampus.client.repository.FakeRepository.handler = handler;
                    app.vcampus.client.repository.FakeRepository.isConnected = true;
                } catch (Exception e) {
                    throw new Exception("无法连接到服务器, 请检查服务器地址");
                }

                boolean loginSuccess = app.vcampus.client.repository.FakeRepository.login(username.get(), password.get());
                if (!loginSuccess) {
                    throw new Exception("一卡通号或密码错误");
                }

                return true;
            }
        };

        loginTask.setOnSucceeded(event -> {
            loginState.set(true);
        });

        loginTask.setOnFailed(event -> {
            app.vcampus.client.repository.FakeRepository.isConnected = false;
            password.set("");
            errorMessage.set(loginTask.getException().getMessage());
        });

        new Thread(loginTask).start();
    }
}