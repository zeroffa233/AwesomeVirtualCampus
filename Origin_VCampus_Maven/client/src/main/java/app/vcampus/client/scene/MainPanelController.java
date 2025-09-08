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
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class MainPanelController implements Initializable {

    // FXML Injections
    @FXML
    private StackPane contentPane;
    @FXML
    private VBox navRail;
    @FXML
    private NavRailController navRailController;
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
        navRailController.setMainPanelController(this);
        navRail.setMaxWidth(Region.USE_PREF_SIZE);
        StackPane.setAlignment(navRail, javafx.geometry.Pos.CENTER_LEFT);
        overlayPane.managedProperty().bind(overlayPane.visibleProperty());

        navRail.setOpacity(0);

        Platform.runLater(() -> {
            navRail.setTranslateX(-navRail.getWidth());
            navRail.setOpacity(1);
            switchView("/app/vcampus/client/scene/sub/HomeView.fxml", "主页", List.of());
            contentPane.requestFocus();
        });
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
        toggleNavRail();
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

        TranslateTransition navRailTransition = new TranslateTransition(ANIMATION_SPEED, navRail);
        FadeTransition overlayFade = new FadeTransition(ANIMATION_SPEED, overlayPane);
        navRailTransition.setInterpolator(CUSTOM_EASING);
        overlayFade.setInterpolator(CUSTOM_EASING);

        if (show) {
            overlayPane.setVisible(true);
            navRailTransition.setToX(0);
            overlayFade.setToValue(1.0);
        } else {
            navRailTransition.setToX(-navRail.getWidth());
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