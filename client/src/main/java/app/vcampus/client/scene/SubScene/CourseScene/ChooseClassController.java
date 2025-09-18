package app.vcampus.client.scene.SubScene.CourseScene;

import app.vcampus.client.viewmodel.TeachingAffairsViewModel;
import app.vcampus.server.entity.Course;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.Node;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

/**
 * 选课场景控制器。
 * 负责展示所有可选课程及其教学班的列表。
 */
public class ChooseClassController {

    @FXML private Label headingLabel;
    /**
     * 标题标签。
     */
    @FXML private Label captionLabel;
    /**
     * 滚动面板。
     */
    @FXML private ScrollPane scrollPane;
    /**
     * 课程容器。
     */
    @FXML private VBox coursesContainer;

    /**
     * 教务视图模型。
     */
    private TeachingAffairsViewModel vm;

    /**
     * 设置视图模型，并初始化数据和监听器。
     *
     * @param vm 教务视图模型。
     */
    public void setViewModel(TeachingAffairsViewModel vm) {
        this.vm = vm;
        if (this.vm == null) return;

        this.vm.myClasses.init();

        Platform.runLater(() -> populateCourses(this.vm.myClasses.allCourses));

        try {
            this.vm.myClasses.allCourses.addListener((ListChangeListener<Course>) change -> {
                Platform.runLater(() -> populateCourses(this.vm.myClasses.allCourses));
            });
        } catch (Exception ignored) {}

        final ScheduledExecutorService poller = Executors.newSingleThreadScheduledExecutor();
        final int[] tries = {0};
        ScheduledFuture<?> future = poller.scheduleAtFixedRate(() -> {
            tries[0]++;
            List<Course> list = this.vm.myClasses.allCourses;
            if (list != null && !list.isEmpty()) {
                Platform.runLater(() -> populateCourses(list));
                poller.shutdown();
            } else if (tries[0] >= 6) {
                poller.shutdown();
            }
        }, 150, 400, TimeUnit.MILLISECONDS);
    }

    /**
     * 初始化方法，在FXML文件加载完成后自动调用。
     */
    @FXML
    public void initialize() {

    }

    private void populateCourses(List<Course> courses) {
        coursesContainer.getChildren().clear();
        for (Course c : courses) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("course_list_item.fxml"));
                Node node = loader.load();
                CourseListItemController ctrl = loader.getController();
                ctrl.bind(c, vm);
                ctrl.registerSelf(node);
                coursesContainer.getChildren().add(node);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}