package app.vcampus.client.viewmodel;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoginViewModel {
    public final StringProperty username = new SimpleStringProperty("");
    public final StringProperty password = new SimpleStringProperty("");
    public final BooleanProperty loginState = new SimpleBooleanProperty(false);
    public final StringProperty errorMessage = new SimpleStringProperty("");
    public final StringProperty serverAddress = new SimpleStringProperty("127.0.0.1:9091");

    public void login() {
        errorMessage.set("");

        Task<Boolean> loginTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                // Always disconnect before trying to connect
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
