package app.vcampus.client.scene.SubScene.CourseScene;

import app.vcampus.server.entity.TeachingClass;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * “我的课程”列表项控件。
 * <p>
 * 负责展示一个教学班的详细信息，并提供导出学生名单的功能（通过回调实现）。
 * </p>
 */
public class MyClassListItem extends HBox {

    @FXML
    private javafx.scene.control.Label courseNameLabel;
    /**
     * 课程ID标签。
     */
    @FXML
    private javafx.scene.control.Label courseIdLabel;
    /**
     * 课程表标签。
     */
    @FXML
    private javafx.scene.control.Label scheduleLabel;
    /**
     * 地点标签。
     */
    @FXML
    private javafx.scene.control.Label placeLabel;
    /**
     * 导出按钮。
     */
    @FXML
    private JFXButton exportButton;

    /**
     * 教学班对象。
     */
    private final TeachingClass tc;
    /**
     * 保存回调函数。
     */
    private final BiConsumer<TeachingClass, File> saveCallback;

    /**
     * 构造函数。
     *
     * @param tc           教学班对象。
     * @param saveCallback 保存学生名单的回调函数。
     */
    public MyClassListItem(TeachingClass tc, BiConsumer<TeachingClass, File> saveCallback) {
        this.tc = Objects.requireNonNull(tc, "TeachingClass must not be null");
        this.saveCallback = saveCallback;

        String resourcePath = "/app/vcampus/client/scene/SubScene/CourseScene/MyClassListItem.fxml";
        URL fxmlUrl = getClass().getResource(resourcePath);
        if (fxmlUrl == null) {
            System.err.println("[ERROR] MyClassListItem FXML not found at: " + resourcePath);
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load MyClassListItem.fxml from: " + fxmlUrl);
            e.printStackTrace();
            return;
        }

        safeInit();
    }

    private void safeInit() {
        if (courseNameLabel != null) courseNameLabel.setText(tc.getCourse() != null ? tc.getCourse().getCourseName() : "");
        if (courseIdLabel != null) courseIdLabel.setText(tc.getCourse() != null ? tc.getCourse().getCourseId() : "");
        if (scheduleLabel != null) scheduleLabel.setText(tc.humanReadableSchedule() != null ? tc.humanReadableSchedule() : "");
        if (placeLabel != null) placeLabel.setText(tc.getPlace() != null ? tc.getPlace() : "");

        if (exportButton != null) {
            exportButton.setOnAction(evt -> openFileChooserAndExport());
        } else {
            System.err.println("[WARN] exportButton is null in MyClassListItem (FXML injection failed?)");
        }
    }

    /**
     * 打开文件选择器并导出学生名单。
     */
    private void openFileChooserAndExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出学生名单");
        String baseName = (tc.getCourse() != null && tc.getCourse().getCourseName() != null)
                ? tc.getCourse().getCourseName()
                : "students";
        String uuid = tc.getUuid() != null ? tc.getUuid().toString() : "";
        fileChooser.setInitialFileName(baseName + (uuid.isEmpty() ? "" : "-" + uuid) + ".xlsx");

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel 工作簿 (*.xlsx)", "*.xlsx"));

        Window owner = (getScene() != null) ? getScene().getWindow() : null;
        File file = fileChooser.showSaveDialog(owner);

        if (file == null) {
            return;
        }

        if (saveCallback == null) {
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.ERROR, "导出功能未绑定，无法保存到文件。", ButtonType.OK);
                alert.setHeaderText("导出失败");
                alert.show();
            });
            System.err.println("[ERROR] saveCallback is null for MyClassListItem (teaching class: " + tc + ")");
            return;
        }

        try {
            saveCallback.accept(tc, file);

            Platform.runLater(() -> {
                Alert info = new Alert(AlertType.INFORMATION, "已开始导出。导出过程在后台执行，完成后请检查所选文件。", ButtonType.OK);
                info.setHeaderText("导出已启动");
                info.show();
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.ERROR, "导出时出现错误：" + ex.getMessage(), ButtonType.OK);
                alert.setHeaderText("导出失败");
                alert.show();
            });
        }
    }

    /**
     * 释放资源或解除绑定。
     */
    public void dispose() {
        try {
            if (courseNameLabel != null) courseNameLabel.textProperty().unbind();
            if (courseIdLabel != null) courseIdLabel.textProperty().unbind();
            if (scheduleLabel != null) scheduleLabel.textProperty().unbind();
            if (placeLabel != null) placeLabel.textProperty().unbind();
        } catch (Exception ignored) { }
    }
}