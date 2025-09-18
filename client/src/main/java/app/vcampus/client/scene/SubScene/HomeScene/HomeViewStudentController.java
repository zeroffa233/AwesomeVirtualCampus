package app.vcampus.client.scene.SubScene.HomeScene;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

import java.net.URL;
import java.time.LocalTime;
import java.util.ResourceBundle;

/**
 * 学生主页视图控制器。
 * 负责学生登录后主页的问候语和欢迎信息的展示。
 */
public class HomeViewStudentController implements Initializable {

    @FXML
    private Label greetingLabel;
    @FXML
    private Label welcomeLabel;

    /**
     * 初始化方法，在FXML文件加载完成后自动调用。
     *
     * @param location  URL定位资源。
     * @param resources 资源包。
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LocalTime now = LocalTime.now();
        String greeting;
        if (now.isBefore(LocalTime.of(12, 0))) {
            greeting = "早上好";
        } else if (now.isBefore(LocalTime.of(18, 0))) {
            greeting = "下午好";
        } else {
            greeting = "晚上好";
        }
        greetingLabel.setText(greeting);

        welcomeLabel.setText("！欢迎来到虚拟校园。");

        greetingLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold;");
        greetingLabel.setTextFill(Color.web("#212121"));

        welcomeLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: normal;");
        welcomeLabel.setTextFill(Color.web("#757575"));
    }
}