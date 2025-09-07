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

    private boolean isNavRailVisible = false;

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

    @FXML
    private void handleOverlayClick() {
        if (isNavRailVisible) {
            toggleNavRail();
        }
    }

    private static final Duration ANIMATION_SPEED = Duration.millis(450);
    private static final Interpolator CUSTOM_EASING = Interpolator.SPLINE(0.2, 0.8, 0.2, 1.0);
    public void toggleNavRail() {
        TranslateTransition navRailTransition = new TranslateTransition(ANIMATION_SPEED, navRail);
        FadeTransition overlayFade = new FadeTransition(ANIMATION_SPEED, overlayPane);

        // --- 修改：使用我们自定义的插值器 ---
        navRailTransition.setInterpolator(CUSTOM_EASING);
        overlayFade.setInterpolator(CUSTOM_EASING);

        if (isNavRailVisible) {
            navRailTransition.setToX(-navRail.getPrefWidth());
            overlayFade.setToValue(0.0);
            overlayFade.setOnFinished(event -> overlayPane.setVisible(false));
        } else {
            overlayPane.setVisible(true);
            navRailTransition.setToX(0);
            overlayFade.setToValue(1.0);
            overlayFade.setOnFinished(null);
        }

        ParallelTransition parallelTransition = new ParallelTransition(navRailTransition, overlayFade);
        parallelTransition.play();
        isNavRailVisible = !isNavRailVisible;
    }
}