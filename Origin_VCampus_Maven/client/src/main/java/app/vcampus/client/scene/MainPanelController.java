package app.vcampus.client.scene;

import app.vcampus.client.util.UIUtils;
import com.jfoenix.controls.JFXButton;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

import javafx.animation.Interpolator; //for toggleNavRail()
import javafx.animation.ParallelTransition;

public class MainPanelController implements Initializable {
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("DEBUG: Initializing MainPanelController...");
        System.out.println("DEBUG: FXML injection check: menuButton is " + (menuButton == null ? "null" : "not null"));

        navRailController.setMainPanelController(this);
        navRail.setMaxWidth(Region.USE_PREF_SIZE);
        navRail.setTranslateX(-navRail.getPrefWidth());
        StackPane.setAlignment(navRail, javafx.geometry.Pos.CENTER_LEFT);
        overlayPane.managedProperty().bind(overlayPane.visibleProperty());
        // UIUtils.createFlowingLightAnimation(backgroundAnimationPane);
        loadView("/app/vcampus/client/scene/sub/HomeView.fxml", "主页");
    }

    public void loadView(String fxmlPath, String title) {
        try {
            Node view = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            contentPane.getChildren().setAll(view);
            viewTitle.setText(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMenuButtonClick() {
        System.out.println("--- handleMenuButtonClick called ---");
        toggleNavRail();
    }


    // NOTE: The code below(till the end of this file) is AI-generated content
    private boolean isNavRailVisible = false;
    private static final Duration ANIMATION_SPEED = Duration.millis(450);
    private static final Interpolator CUSTOM_EASING = Interpolator.SPLINE(0.2, 0.8, 0.2, 1.0);

    @FXML
    private void handleOverlayClick() {
        if (isNavRailVisible) {
            toggleNavRail();
        }
    }

    private ParallelTransition parallelTransition;
    public void toggleNavRail() {
        // --- Stop any previous animation that might be running. ---
        // This is the key to making the animation interruptible.
        if (parallelTransition != null) {
            parallelTransition.stop();
        }

        // Determine the target state based on the current state.
        boolean show = !isNavRailVisible;

        TranslateTransition navRailTransition = new TranslateTransition(ANIMATION_SPEED, navRail);
        FadeTransition overlayFade = new FadeTransition(ANIMATION_SPEED, overlayPane);

        navRailTransition.setInterpolator(CUSTOM_EASING);
        overlayFade.setInterpolator(CUSTOM_EASING);

        if (show) {
            // Prepare the "show" animation.
            // The overlay must be set to visible before its fade-in animation can play.
            overlayPane.setVisible(true);
            navRailTransition.setToX(0);
            overlayFade.setToValue(1.0);
        } else {
            // Prepare the "hide" animation.
            navRailTransition.setToX(-navRail.getPrefWidth());
            overlayFade.setToValue(0.0);
        }
        // --- Create a new animation instance to take over. ---
        // This new animation will start from the current visual state of the elements.
        parallelTransition = new ParallelTransition(navRailTransition, overlayFade);

        // --- Core Change 3: Conditionally set the onFinished event for cleanup. ---
        // We only need to perform a cleanup action (set overlay to invisible)
        // when the animation's goal is to hide everything.
        if (!show) {
            parallelTransition.setOnFinished(event -> {
                overlayPane.setVisible(false);
            });
        }

        parallelTransition.play();

        // Immediately update the state to reflect the user's latest intent.
        // This ensures the next click will correctly determine the new target state.
        isNavRailVisible = show;
    }
}