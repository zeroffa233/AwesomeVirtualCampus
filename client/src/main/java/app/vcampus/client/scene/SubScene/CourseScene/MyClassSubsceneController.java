package app.vcampus.client.scene.SubScene.CourseScene;

import app.vcampus.client.viewmodel.TeachingAffairsViewModel;
import app.vcampus.server.entity.TeachingClass;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

/**
 * “我的课程”子场景控制器。
 * 负责展示教师的授课列表。
 */
public class MyClassSubsceneController {

    @FXML
    private VBox classListVBox;

    /**
     * 教务视图模型。
     */
    private TeachingAffairsViewModel viewModel;

    /**
     * 设置视图模型，并初始化数据和监听器。
     *
     * @param viewModel 教务视图模型。
     */
    public void setViewModel(TeachingAffairsViewModel viewModel) {
        this.viewModel = viewModel;

        viewModel.myTeachingClasses.init();

        viewModel.myTeachingClasses.myClasses.addListener((javafx.collections.ListChangeListener<TeachingClass>) change -> {
            Platform.runLater(this::rebuildList);
        });

        rebuildList();
    }

    /**
     * 刷新课程列表数据。
     * 调用viewModel的refresh方法重新加载数据。
     */
    public void refresh() {
        if (viewModel != null) {
            viewModel.myTeachingClasses.refresh();
        }
    }

    /**
     * 重建课程列表。
     */
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