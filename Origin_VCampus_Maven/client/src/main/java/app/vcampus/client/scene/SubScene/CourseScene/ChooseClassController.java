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

public class ChooseClassController {

    @FXML private Label headingLabel;
    @FXML private Label captionLabel;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox coursesContainer;

    private TeachingAffairsViewModel vm;

    public void setViewModel(TeachingAffairsViewModel vm) {
        System.out.println("[ChooseClass] setViewModel vm=" + (vm == null ? "null" : vm.hashCode()));
        this.vm = vm;
        if (this.vm == null) return;

        // 启动/初始化数据（幂等）
        this.vm.myClasses.init();

        // 初次填充（可能已有数据）
        Platform.runLater(() -> populateCourses(this.vm.myClasses.allCourses));

        // 订阅数据变化（当 allCourses 被填充时刷新 UI）
        try {
            this.vm.myClasses.allCourses.addListener((ListChangeListener<Course>) change -> {
                Platform.runLater(() -> populateCourses(this.vm.myClasses.allCourses));
            });
        } catch (Exception ignored) {}

        // 短期轮询备援：如果数据短时间内没来则再次检查（兼容性）
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


    private final ScheduledExecutorService poller = Executors.newSingleThreadScheduledExecutor();

    @FXML
    public void initialize() {
        headingLabel.setText("选课");
        captionLabel.setText("选课系统");

    }

    private void populateCourses(List<Course> courses) {
        coursesContainer.getChildren().clear();
        for (Course c : courses) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("course_list_item.fxml"));
                Node node = loader.load();
                CourseListItemController ctrl = loader.getController();
                ctrl.bind(c, vm);
                ctrl.registerSelf(node); // 注册到 node 的 properties
                coursesContainer.getChildren().add(node);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
