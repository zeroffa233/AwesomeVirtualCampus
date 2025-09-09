package app.vcampus.client.scene.SubScene.CourseScene;

import app.vcampus.client.viewmodel.TeachingAffairsViewModel;
import app.vcampus.server.entity.Course;
import app.vcampus.server.entity.TeachingClass;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;

public class CourseListItemController {

    @FXML private Label courseNameLabel;
    @FXML private Label courseIdLabel;
    @FXML private HBox chipBox;
    @FXML private ToggleButton expandToggle;
    @FXML private ScrollPane tcScroll;
    @FXML private HBox teachingClassBox;
    @FXML private VBox expandContainer;

    private Course course;
    private TeachingAffairsViewModel vm;

    private double lastExpandedHeight = 0.0;
    private static final double MAX_ALLOWED_EXPANDED_HEIGHT = 600.0; // 展开最大高度
    private static final Duration ANIM_DURATION = Duration.millis(300);

    public void bind(Course course, TeachingAffairsViewModel vm) {
        this.course = course;
        this.vm = vm;

        courseNameLabel.setText(course.getCourseName());
        courseIdLabel.setText(course.getCourseId());

        chipBox.getChildren().clear();
        Label chipCredit = createChip(String.format("学分: %.2f", course.getCredit() / 1.0));
        chipBox.getChildren().add(chipCredit);

        populateTeachingClasses();

        expandToggle.selectedProperty().addListener((obs, oldV, newV) -> {
            expandToggle.setText(newV ? "收起" : "展开");
            if (newV) collapseOthers(); // 展开时折叠其它项
            animateExpand(newV);
        });

        // 初始为收起
        tcScroll.setVisible(false);
        tcScroll.setManaged(false);
        expandContainer.setMaxHeight(0);
        expandContainer.setPrefHeight(0);
        expandContainer.setMinHeight(0);
    }

    private Label createChip(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("chip");
        l.setStyle("-fx-background-color: #16a085; -fx-text-fill: white; -fx-padding: 4 8 4 8; -fx-background-radius: 12;");
        return l;
    }

    private void populateTeachingClasses() {
        teachingClassBox.getChildren().clear();
        if (course.getTeachingClasses() == null) return;

        for (TeachingClass tc : course.getTeachingClasses()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("teaching_class_card.fxml"));
                Node node = loader.load();
                TeachingClassCardController ctrl = loader.getController();
                ctrl.bind(tc, vm);
                teachingClassBox.getChildren().add(node);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 测量展开高度
        Platform.runLater(() -> {
            teachingClassBox.applyCss();
            teachingClassBox.layout();

            double measured = teachingClassBox.prefHeight(-1);
            if (measured <= 0) {
                measured = teachingClassBox.getChildren().size() * 240.0; // 兜底估算
            }
            lastExpandedHeight = Math.min(MAX_ALLOWED_EXPANDED_HEIGHT, measured + 20.0);

            if (expandToggle.isSelected()) {
                expandContainer.setPrefHeight(lastExpandedHeight);
                expandContainer.setMaxHeight(lastExpandedHeight);
                expandContainer.setMinHeight(50);
                tcScroll.setVisible(true);
                tcScroll.setManaged(true);
            }
        });
    }

    private void animateExpand(boolean expand) {
        if (expand && lastExpandedHeight <= 1.0) {
            // 高度未测量，稍后再试
            Platform.runLater(() -> animateExpand(true));
            return;
        }

        double to = expand ? lastExpandedHeight : 0.0;

        if (expand) {
            tcScroll.setManaged(true);
            tcScroll.setVisible(true);
        }

        KeyValue kvMax = new KeyValue(expandContainer.maxHeightProperty(), to, Interpolator.EASE_BOTH);
        KeyValue kvPref = new KeyValue(expandContainer.prefHeightProperty(), to, Interpolator.EASE_BOTH);
        KeyValue kvMin = new KeyValue(expandContainer.minHeightProperty(), expand ? 50.0 : 0.0, Interpolator.EASE_BOTH);

        KeyFrame kf = new KeyFrame(ANIM_DURATION, kvMax, kvPref, kvMin);
        Timeline t = new Timeline(kf);

        t.setOnFinished(evt -> {
            if (!expand) {
                tcScroll.setVisible(false);
                tcScroll.setManaged(false);
                expandContainer.setPrefHeight(0);
                expandContainer.setMaxHeight(0);
                expandContainer.setMinHeight(0);
            }
        });

        t.play();
    }

    /**
     * 展开当前课程时，折叠同一个父容器里的其它课程
     */
    private void collapseOthers() {
        if (expandToggle.getScene() == null) return;

        // 遍历父容器中所有 CourseListItemController
        VBox parent = (VBox) this.expandToggle.getScene().lookup("#coursesContainer");
        if (parent != null) {
            for (Node child : parent.getChildren()) {
                if (child != this.expandToggle.getParent().getParent().getParent()) {
                    // 查找子控制器
                    CourseListItemController ctrl = (CourseListItemController) child.getProperties().get("controller");
                    if (ctrl != null && ctrl != this && ctrl.expandToggle.isSelected()) {
                        ctrl.expandToggle.setSelected(false);
                        ctrl.animateExpand(false);
                    }
                }
            }
        }
    }

    /**
     * 在 ChooseClassController.populateCourses() 里绑定 controller
     */
    public void registerSelf(Node root) {
        root.getProperties().put("controller", this);
    }
}
