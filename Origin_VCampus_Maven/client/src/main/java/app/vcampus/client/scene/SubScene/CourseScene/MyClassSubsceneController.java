package app.vcampus.client.scene.SubScene.CourseScene;

import app.vcampus.client.scene.SubScene.CourseScene.MyClassListItem;
import app.vcampus.client.viewmodel.TeachingAffairsViewModel;
import app.vcampus.server.entity.TeachingClass;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

import java.io.File;

public class MyClassSubsceneController {

    @FXML
    private VBox classListVBox;

    private TeachingAffairsViewModel viewModel;

    public void setViewModel(TeachingAffairsViewModel viewModel) {
        this.viewModel = viewModel;

        // 初始化数据（会异步加载）
        viewModel.myTeachingClasses.init();

        // 监听列表变化，变化时在 FX 线程重建 UI
        viewModel.myTeachingClasses.myClasses.addListener((javafx.collections.ListChangeListener<TeachingClass>) change -> {
            Platform.runLater(this::rebuildList);
        });

        // 如果已经有数据，先构建一次
        rebuildList();
    }

    private void rebuildList() {
        classListVBox.getChildren().clear();
        for (TeachingClass tc : viewModel.myTeachingClasses.myClasses) {
            MyClassListItem item = new MyClassListItem(tc, (teachingClass, file) -> {
                viewModel.myTeachingClasses.saveStudentList(teachingClass, file);
            });
            classListVBox.getChildren().add(item);
        }
    }
}

