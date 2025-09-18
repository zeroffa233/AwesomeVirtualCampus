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

public class LoginScene implements Initializable {

    @FXML
    private JFXTextField usernameField;
    @FXML
    private JFXPasswordField passwordField;
    @FXML
    private JFXTextField serverAddressField;
    @FXML
    private Label errorLabel;
    @FXML
    private JFXButton loginButton;
    @FXML
    private JFXButton serverAddressButton;
    @FXML
    private Pane animationPane;

    private final LoginViewModel viewModel = new LoginViewModel();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Bindings
        usernameField.textProperty().bindBidirectional(viewModel.username);
        passwordField.textProperty().bindBidirectional(viewModel.password);
        serverAddressField.textProperty().bindBidirectional(viewModel.serverAddress);
        errorLabel.textProperty().bind(viewModel.errorMessage);
        errorLabel.visibleProperty().bind(viewModel.errorMessage.isNotEmpty());
        errorLabel.managedProperty().bind(viewModel.errorMessage.isNotEmpty());

        // Login state listener
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

        // Start background animation
        createFlowingLightAnimation();
    }

    private void createFlowingLightAnimation() {
        Random random = new Random();
        List<Color> colors = List.of(
                Color.web("#A5D6A7", 1.0), // Gentle Green
                Color.web("#FFF59D", 1.0), // Gentle Yellow
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

    @FXML
    private void handleLogin() {
        viewModel.login();
    }

    @FXML
    private void handleSetServerAddress() {
        boolean isVisible = serverAddressField.isVisible();
        serverAddressField.setVisible(!isVisible);
        serverAddressField.setManaged(!isVisible);
    }
}