package app.vcampus.client.scene;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class MainPanelController implements Initializable {
    @FXML
    private StackPane contentPane;

    @FXML
    private VBox navRailPane; // The root pane of NavRail.fxml, injected via fx:id

    @FXML
    private NavRailController navRailController; // The controller of NavRail.fxml

    private boolean isNavRailVisible = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Pass this controller to the navRail controller
        navRailController.setMainPanelController(this);

        // Initially hide the nav rail off-screen
        navRailPane.setTranslateX(-navRailPane.getPrefWidth());
        StackPane.setAlignment(navRailPane, javafx.geometry.Pos.CENTER_LEFT);

        // Load home view by default
        loadView("/app/vcampus/client/scene/subscene/HomeView.fxml");
    }

    public void loadView(String fxmlPath) {
        try {
            Node view = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMenuButtonClick() {
        toggleNavRail();
    }

    public void toggleNavRail() {
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), navRailPane);

        if (isNavRailVisible) {
            transition.setToX(-navRailPane.getWidth());
        } else {
            transition.setToX(0);
        }

        transition.play();
        isNavRailVisible = !isNavRailVisible;
    }
}