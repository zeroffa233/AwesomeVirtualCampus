package app.vcampus.client.scene;

import com.jfoenix.controls.JFXButton;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
// import javafx.stage.Stage; // Stage 相关的导入已移除
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class MainSceneController implements Initializable {

    // FXML Injections
    @FXML
    private StackPane contentPane;
    @FXML
    private VBox sideBar;
    @FXML
    private SideBarController sideBarController;
    @FXML
    private Pane overlayPane;
    @FXML
    private Label viewTitle;
    @FXML
    private Pane backgroundAnimationPane;
    @FXML
    private JFXButton menuButton;
    @FXML
    private HBox secondaryMenuContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sideBar.setMaxWidth(Region.USE_PREF_SIZE);
        StackPane.setAlignment(sideBar, javafx.geometry.Pos.CENTER_LEFT);
        overlayPane.managedProperty().bind(overlayPane.visibleProperty());

        sideBar.setOpacity(0);
        Platform.runLater(() -> {
            sideBar.setTranslateX(-sideBar.getWidth());
            sideBar.setOpacity(1);
            switchView("/app/vcampus/client/scene/SubScene/HomeScene/HomeView.fxml", "主页", List.of());
        });
        if (sideBarController != null) {
            sideBarController.setMainSceneController(this);
        }

    }

    // public void setStage(Stage stage) { ... } // 此方法已根据您的要求被完全移除

    public void loadSubScene(String fxmlPath) {
        try {
            Node view = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            FadeTransition fadeOut = new FadeTransition(Duration.millis(150), contentPane);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(event -> {
                contentPane.getChildren().setAll(view);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(150), contentPane);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void switchView(String fxmlPath, String title, List<Node> secondaryMenuItems) {
        updateTitle(title);

        try {
            Node view = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }

        secondaryMenuContainer.getChildren().setAll(secondaryMenuItems);

        if (!isNavRailVisible) {
            showSecondaryMenu();
        } else {
            secondaryMenuContainer.setOpacity(0);
            secondaryMenuContainer.setVisible(false);
            secondaryMenuContainer.setManaged(false);
        }
    }


    @FXML
    private void handleMenuButtonClick() {
        // Delay the animation slightly to allow the UI to render the :pressed state
        PauseTransition pause = new PauseTransition(Duration.millis(50));
        pause.setOnFinished(event -> toggleNavRail());
        pause.play();
    }

    @FXML
    private void handleOverlayClick() {
        if (isNavRailVisible) {
            toggleNavRail();
        }
    }

    // region --- NavRail (Sidebar) Animation ---
    private boolean isNavRailVisible = false;
    private static final Duration ANIMATION_SPEED = Duration.millis(350);
    private static final Interpolator CUSTOM_EASING = Interpolator.SPLINE(0.4, 0.1, 0.2, 1.0);
    private ParallelTransition parallelTransition;

    public void toggleNavRail() {
        if (parallelTransition != null) {
            parallelTransition.stop();
        }

        boolean show = !isNavRailVisible;
        if (show) {
            hideSecondaryMenu();
        } else {
            showSecondaryMenu();
        }

        TranslateTransition navRailTransition = new TranslateTransition(ANIMATION_SPEED, sideBar);
        FadeTransition overlayFade = new FadeTransition(ANIMATION_SPEED, overlayPane);
        navRailTransition.setInterpolator(CUSTOM_EASING);
        overlayFade.setInterpolator(CUSTOM_EASING);

        if (show) {
            overlayPane.setVisible(true);
            navRailTransition.setToX(0);
            overlayFade.setToValue(1.0);
        } else {
            navRailTransition.setToX(-sideBar.getWidth());
            overlayFade.setToValue(0.0);
        }

        parallelTransition = new ParallelTransition(navRailTransition, overlayFade);
        if (!show) {
            parallelTransition.setOnFinished(event -> overlayPane.setVisible(false));
        }
        parallelTransition.play();
        isNavRailVisible = show;
    }
    // endregion

    // region --- TopBar Animation Logic ---
    public void updateTitle(String newTitle) {
        if (viewTitle.getText() != null && viewTitle.getText().equals(newTitle)) return;

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), viewTitle);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setInterpolator(Interpolator.EASE_IN);

        fadeOut.setOnFinished(event -> {
            viewTitle.setText(newTitle);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), viewTitle);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.setInterpolator(Interpolator.EASE_OUT);
            fadeIn.play();
        });
        fadeOut.play();
    }

    private void hideSecondaryMenu() {
        FadeTransition fade = new FadeTransition(ANIMATION_SPEED, secondaryMenuContainer);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setInterpolator(CUSTOM_EASING);
        fade.setOnFinished(e -> {
            secondaryMenuContainer.setVisible(false);
            secondaryMenuContainer.setManaged(false);
        });
        fade.play();
    }

    private void showSecondaryMenu() {
        if (secondaryMenuContainer.getChildren().isEmpty()) return;

        secondaryMenuContainer.setVisible(true);
        secondaryMenuContainer.setManaged(true);

        FadeTransition fade = new FadeTransition(ANIMATION_SPEED, secondaryMenuContainer);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.setInterpolator(CUSTOM_EASING);
        fade.play();
    }
    // endregion
}