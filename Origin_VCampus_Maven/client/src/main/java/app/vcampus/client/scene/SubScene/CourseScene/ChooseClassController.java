package app.vcampus.client.scene.SubScene.CourseScene;

import app.vcampus.client.viewmodel.TeachingAffairsViewModel;
import app.vcampus.server.entity.Course;
import javafx.application.Platform;
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

    private final TeachingAffairsViewModel vm = new TeachingAffairsViewModel();

    private final ScheduledExecutorService poller = Executors.newSingleThreadScheduledExecutor();

    @FXML
    public void initialize() {
        headingLabel.setText("选课");
        captionLabel.setText("选课系统");

        // 初始化 viewModel（会触发后台加载）
        vm.myClasses.init();

        // 立即尝试填充（如果已有数据）
        populateCourses(vm.myClasses.allCourses);

        // 短期轮询，检测数据变化并刷新 UI（最多 6 次，间隔 400ms）
        final int[] times = {0};
        ScheduledFuture<?> future = poller.scheduleAtFixedRate(() -> {
            times[0]++;
            List<Course> list = vm.myClasses.allCourses;
            // 当数据填充后，刷新 UI
            if (!list.isEmpty()) {
                Platform.runLater(() -> populateCourses(list));
                // cancel polling after first fill
                poller.shutdown();
            } else if (times[0] >= 6) {
                // 停止轮询（避免长期占用）
                poller.shutdown();
            }
        }, 200, 400, TimeUnit.MILLISECONDS);
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
