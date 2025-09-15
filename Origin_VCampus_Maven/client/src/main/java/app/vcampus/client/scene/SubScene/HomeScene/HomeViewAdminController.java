package app.vcampus.client.scene.SubScene.HomeScene;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

import java.net.URL;
import java.time.LocalTime;
import java.util.ResourceBundle;

public class HomeViewAdminController implements Initializable {

    @FXML
    private Label greetingLabel;
    @FXML
    private Label welcomeLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Set Greeting based on time
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

        // 2. Set generic Welcome message
        welcomeLabel.setText("！欢迎来到虚拟校园。");

        // 3. Apply Material-inspired styles
        greetingLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold;");
        greetingLabel.setTextFill(Color.web("#212121")); // Material Design Grey 900

        welcomeLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: normal;");
        welcomeLabel.setTextFill(Color.web("#757575")); // Material Design Grey 600
    }
}
