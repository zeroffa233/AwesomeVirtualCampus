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

/**
 * 课程列表项控制器。
 * 负责展示单个课程的信息，并提供展开/折叠功能以显示其下的教学班列表。
 */
public class CourseListItemController {

    @FXML private Label courseNameLabel;
    /**
     * 课程ID标签。
     */
    @FXML private Label courseIdLabel;
    /**
     * 芯片盒。
     */
    @FXML private HBox chipBox;
    /**
     * 展开/折叠切换按钮。
     */
    @FXML private ToggleButton expandToggle;
    /**
     * 教学班滚动面板。
     */
    @FXML private ScrollPane tcScroll;
    /**
     * 教学班容器。
     */
    @FXML private HBox teachingClassBox;
    /**
     * 展开容器。
     */
    @FXML private VBox expandContainer;

    /**
     * 课程对象。
     */
    private Course course;
    /**
     * 教务视图模型。
     */
    private TeachingAffairsViewModel vm;

    /**
     * 上次展开的高度。
     */
    private double lastExpandedHeight = 0.0;
    /**
     * 允许的最大展开高度。
     */
    private static final double MAX_ALLOWED_EXPANDED_HEIGHT = 600.0;
    /**
     * 动画持续时间。
     */
    private static final Duration ANIM_DURATION = Duration.millis(300);

    /**
     * 绑定课程数据和视图模型到列表项。
     *
     * @param course 课程对象。
     * @param vm     教务视图模型。
     */
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
            if (newV) collapseOthers();
            animateExpand(newV);
        });

        tcScroll.setVisible(false);
        tcScroll.setManaged(false);
        expandContainer.setMaxHeight(0);
        expandContainer.setPrefHeight(0);
        expandContainer.setMinHeight(0);
    }

    /**
     * 将此控制器实例注册到根节点，以便父控制器可以访问。
     *
     * @param root 列表项的根节点。
     */
    public void registerSelf(Node root) {
        root.getProperties().put("controller", this);
    }

    private Label createChip(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("chip");
        l.setStyle("-fx-background-color: #16a085; -fx-text-fill: white; -fx-padding: 4 8 4 8; -fx-background-radius: 12;");
        return l;
    }

    /**
     * 填充教学班信息。
     */
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

        Platform.runLater(() -> {
            teachingClassBox.applyCss();
            teachingClassBox.layout();

            double measured = teachingClassBox.prefHeight(-1);
            if (measured <= 0) {
                measured = teachingClassBox.getChildren().size() * 240.0;
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

    /**
     * 动画展开/折叠。
     *
     * @param expand 是否展开。
     */
    private void animateExpand(boolean expand) {
        if (expand && lastExpandedHeight <= 1.0) {
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
     * 折叠其他课程列表项。
     */
    private void collapseOthers() {
        if (expandToggle.getScene() == null) return;

        VBox parent = (VBox) this.expandToggle.getScene().lookup("#coursesContainer");
        if (parent != null) {
            for (Node child : parent.getChildren()) {
                if (child != this.expandToggle.getParent().getParent().getParent()) {
                    CourseListItemController ctrl = (CourseListItemController) child.getProperties().get("controller");
                    if (ctrl != null && ctrl != this && ctrl.expandToggle.isSelected()) {
                        ctrl.expandToggle.setSelected(false);
                        ctrl.animateExpand(false);
                    }
                }
            }
        }
    }
}