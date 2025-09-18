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
import javafx.scene.paint.Paint;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * 主场景控制器。
 * 负责管理应用的主界面，包括侧边栏、内容窗格以及子场景之间的切换和动画效果。
 */
public class MainSceneController implements Initializable {

    @FXML
    private StackPane contentPane;
    /**
     * 侧边栏。
     */
    @FXML
    private VBox sideBar;
    /**
     * 侧边栏控制器。
     */
    @FXML
    private SideBarController sideBarController;
    /**
     * 覆盖面板。
     */
    @FXML
    private Pane overlayPane;
    /**
     * 视图标题。
     */
    @FXML
    private Label viewTitle;
    /**
     * 背景动画面板。
     */
    @FXML
    private Pane backgroundAnimationPane;
    /**
     * 菜单按钮。
     */
    @FXML
    private JFXButton menuButton;
    /**
     * 二级菜单容器。
     */
    @FXML
    private HBox secondaryMenuContainer;

    /**
     * 共享的教学事务视图模型。
     */
    private final app.vcampus.client.viewmodel.TeachingAffairsViewModel sharedVm = new app.vcampus.client.viewmodel.TeachingAffairsViewModel();

    /**
     * 初始化方法，在FXML文件加载完成后自动调用。
     *
     * @param location  URL定位资源。
     * @param resources 资源包。
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sideBar.setMaxWidth(Region.USE_PREF_SIZE);
        StackPane.setAlignment(sideBar, javafx.geometry.Pos.CENTER_LEFT);
        overlayPane.managedProperty().bind(overlayPane.visibleProperty());

        sideBar.setOpacity(0);
        Platform.runLater(() -> {
            sideBar.setTranslateX(-sideBar.getWidth());
            sideBar.setOpacity(1);

            String fxmlPath = "/app/vcampus/client/scene/SubScene/HomeScene/HomeViewStudent.fxml"; // Default
            if (app.vcampus.client.repository.FakeRepository.user != null && app.vcampus.client.repository.FakeRepository.user.getRoles() != null) {
                java.util.List<String> roles = java.util.Arrays.asList(app.vcampus.client.repository.FakeRepository.user.getRoles());
                if (roles.contains("admin")) {
                    fxmlPath = "/app/vcampus/client/scene/SubScene/HomeScene/HomeViewAdmin.fxml";
                } else if (roles.contains("teacher")) {
                    fxmlPath = "/app/vcampus/client/scene/SubScene/HomeScene/HomeViewTeacher.fxml";
                }
            }

            loadSubScene(fxmlPath);
            updateTitle("主页");
        });
        if (sideBarController != null) {
            sideBarController.setMainSceneController(this);
        }

        menuButton.setRipplerFill(Paint.valueOf("#607830DE"));
    }

    /**
     * 加载子场景到主内容窗格。
     *
     * @param fxmlPath 要加载的 FXML 文件路径。
     */
    public void loadSubScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            Node view = loader.load();
            Object controller = loader.getController();
            if (controller != null) {
                if (controller instanceof app.vcampus.client.scene.SubScene.CourseScene.ChooseClassController) {
                    ((app.vcampus.client.scene.SubScene.CourseScene.ChooseClassController) controller).setViewModel(sharedVm);
                } else if (controller instanceof app.vcampus.client.scene.SubScene.CourseScene.MyScheduleController) {
                    ((app.vcampus.client.scene.SubScene.CourseScene.MyScheduleController) controller).setViewModel(sharedVm);
                } else if (controller instanceof app.vcampus.client.scene.SubScene.CourseScene.MyClassSubsceneController) {
                    ((app.vcampus.client.scene.SubScene.CourseScene.MyClassSubsceneController) controller).setViewModel(sharedVm);
                } else if (controller instanceof app.vcampus.client.scene.SubScene.CourseScene.AddCourseController) {
                    ((app.vcampus.client.scene.SubScene.CourseScene.AddCourseController) controller).setViewModel(sharedVm);
                } else if (controller instanceof app.vcampus.client.scene.SubScene.CourseScene.AddTeachingClassController) {
                    ((app.vcampus.client.scene.SubScene.CourseScene.AddTeachingClassController) controller).setViewModel(sharedVm);
                }
            }

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

    /**
     * 切换主视图内容。
     *
     * @param fxmlPath           要加载的 FXML 文件路径。
     * @param title              视图标题。
     * @param secondaryMenuItems 二级菜单项列表。
     */
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

    /**
     * 切换导航栏（侧边栏）的显示和隐藏。
     */
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

    /**
     * 更新视图标题。
     *
     * @param newTitle 新的标题文本。
     */
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

    /**
     * 导航栏是否可见。
     */
    private boolean isNavRailVisible = false;
    /**
     * 动画速度。
     */
    private static final Duration ANIMATION_SPEED = Duration.millis(350);
    /**
     * 自定义缓动插值器。
     */
    private static final Interpolator CUSTOM_EASING = Interpolator.SPLINE(0.4, 0.1, 0.2, 1.0);
    /**
     * 并行过渡动画。
     */
    private ParallelTransition parallelTransition;

    /**
     * 处理菜单按钮点击事件。
     */
    @FXML
    private void handleMenuButtonClick() {
        PauseTransition pause = new PauseTransition(Duration.millis(50));
        pause.setOnFinished(event -> toggleNavRail());
        pause.play();
    }

    /**
     * 处理覆盖面板点击事件。
     */
    @FXML
    private void handleOverlayClick() {
        if (isNavRailVisible) {
            toggleNavRail();
        }
    }

    /**
     * 隐藏二级菜单。
     */
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

    /**
     * 显示二级菜单。
     */
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
}