package app.vcampus.client.scene;

import app.vcampus.client.Main;
import app.vcampus.client.viewmodel.LoginViewModel;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

/**
 * 登录场景控制器。
 * 负责处理登录界面的UI逻辑、数据绑定和用户交互。
 */
public class LoginScene implements Initializable {

    @FXML
    private JFXTextField usernameField;
    /**
     * 密码输入框。
     */
    @FXML
    private JFXPasswordField passwordField;
    /**
     * 服务器地址输入框。
     */
    @FXML
    private JFXTextField serverAddressField;
    /**
     * 错误信息标签。
     */
    @FXML
    private Label errorLabel;
    /**
     * 登录按钮。
     */
    @FXML
    private JFXButton loginButton;
    /**
     * 服务器地址设置按钮。
     */
    @FXML
    private JFXButton serverAddressButton;
    /**
     * 动画面板。
     */
    @FXML
    private Pane animationPane;

    /**
     * 登录视图模型。
     */
    private final LoginViewModel viewModel = new LoginViewModel();

    /**
     * 初始化方法，在FXML文件加载完成后自动调用。
     *
     * @param location  URL定位资源。
     * @param resources 资源包。
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        usernameField.textProperty().bindBidirectional(viewModel.username);
        passwordField.textProperty().bindBidirectional(viewModel.password);
        serverAddressField.textProperty().bindBidirectional(viewModel.serverAddress);
        errorLabel.textProperty().bind(viewModel.errorMessage);
        errorLabel.visibleProperty().bind(viewModel.errorMessage.isNotEmpty());
        errorLabel.managedProperty().bind(viewModel.errorMessage.isNotEmpty());

        viewModel.loginState.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Platform.runLater(() -> {
                    try {
                        Main.showMainPanel();
                    } catch (IOException e) {
                        e.printStackTrace();
                        viewModel.errorMessage.set("Could not load main application window.");
                    }
                });
            }
        });

        createFlowingLightAnimation();
    }

    private void createFlowingLightAnimation() {
        Random random = new Random();
        List<Color> colors = List.of(
                Color.web("#A5D6A7", 1.0),
                Color.web("#FFF59D", 1.0),
                Color.web("#C8E6C9", 1.0),
                Color.web("#FFF9C4", 1.0)
        );

        for (int i = 0; i < 12; i++) {
            Rectangle ribbon = new Rectangle(random.nextDouble() * 50 + 15, 2000);
            ribbon.setFill(colors.get(random.nextInt(colors.size())));
            ribbon.setEffect(new GaussianBlur(80));
            ribbon.setOpacity(0.0);

            ribbon.setTranslateX(random.nextDouble() * 500 - 100);
            ribbon.setTranslateY(random.nextDouble() * 200 - 400);
            ribbon.setRotate(random.nextDouble() * 30 - 15);

            animationPane.getChildren().add(ribbon);

            Timeline timeline = new Timeline();
            timeline.setCycleCount(Timeline.INDEFINITE);

            KeyValue kvX = new KeyValue(ribbon.translateXProperty(), ribbon.getTranslateX() + random.nextDouble() * 200 - 100);
            KeyValue kvY = new KeyValue(ribbon.translateYProperty(), 850);
            KeyValue kvOpacity1 = new KeyValue(ribbon.opacityProperty(), 0.8);
            KeyValue kvOpacity2 = new KeyValue(ribbon.opacityProperty(), 0.0);

            KeyFrame kf0 = new KeyFrame(Duration.ZERO, new KeyValue(ribbon.translateYProperty(), -400));
            KeyFrame kf1 = new KeyFrame(Duration.seconds(random.nextDouble() * 8 + 4), kvOpacity1);
            KeyFrame kf2 = new KeyFrame(Duration.seconds(random.nextDouble() * 15 + 10), kvX, kvY, kvOpacity2);

            timeline.getKeyFrames().addAll(kf0, kf1, kf2);
            timeline.play();
        }
    }

    /**
     * 处理登录操作。
     */
    @FXML
    private void handleLogin() {
        viewModel.login();
    }

    /**
     * 处理设置服务器地址操作。
     */
    @FXML
    private void handleSetServerAddress() {
        boolean isVisible = serverAddressField.isVisible();
        serverAddressField.setVisible(!isVisible);
        serverAddressField.setManaged(!isVisible);
    }
}