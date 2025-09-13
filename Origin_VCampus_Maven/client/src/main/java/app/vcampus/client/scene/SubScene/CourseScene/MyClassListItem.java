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
 * 列表项控件：展示一个 TeachingClass 的信息，并支持导出学生名单（通过回调）。
 */
public class MyClassListItem extends HBox {

    @FXML
    private javafx.scene.control.Label courseNameLabel;
    @FXML
    private javafx.scene.control.Label courseIdLabel;
    @FXML
    private javafx.scene.control.Label scheduleLabel;
    @FXML
    private javafx.scene.control.Label placeLabel;
    @FXML
    private JFXButton exportButton;

    private final TeachingClass tc;
    private final BiConsumer<TeachingClass, File> saveCallback;

    public MyClassListItem(TeachingClass tc, BiConsumer<TeachingClass, File> saveCallback) {
        this.tc = Objects.requireNonNull(tc, "TeachingClass must not be null");
        this.saveCallback = saveCallback;

        // 加载 FXML（使用 fx:root，资源必须在 resources 下）
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

        // 注入完成后安全地初始化 UI
        safeInit();
    }

    /** 初始化显示与事件 */
    private void safeInit() {
        // 显示基础信息（若需要更动态的更新，请把 TeachingClass 包装为属性 view-model）
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

    /** 打开文件选择器并触发回调进行导出 */
    private void openFileChooserAndExport() {
        // 弹出保存对话框
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出学生名单");
        // 初始文件名：课程名-教学班UUID.xlsx（若课程名包含文件系统不支持字符，可进一步 sanitize）
        String baseName = (tc.getCourse() != null && tc.getCourse().getCourseName() != null)
                ? tc.getCourse().getCourseName()
                : "students";
        String uuid = tc.getUuid() != null ? tc.getUuid().toString() : "";
        fileChooser.setInitialFileName(baseName + (uuid.isEmpty() ? "" : "-" + uuid) + ".xlsx");

        // 限制文件类型为 xlsx（便于用户）
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel 工作簿 (*.xlsx)", "*.xlsx"));

        Window owner = (getScene() != null) ? getScene().getWindow() : null;
        File file = fileChooser.showSaveDialog(owner);

        if (file == null) {
            // 用户取消保存
            return;
        }

        if (saveCallback == null) {
            // 没有回调：提示用户并打印日志
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.ERROR, "导出功能未绑定，无法保存到文件。", ButtonType.OK);
                alert.setHeaderText("导出失败");
                alert.show();
            });
            System.err.println("[ERROR] saveCallback is null for MyClassListItem (teaching class: " + tc + ")");
            return;
        }

        try {
            // 触发回调（一般由 ViewModel 负责异步写入文件）
            saveCallback.accept(tc, file);

            // 给用户一个非阻塞提示：已开始导出（具体成功/失败由 ViewModel/Repository 日志或后续回调处理）
            Platform.runLater(() -> {
                Alert info = new Alert(AlertType.INFORMATION, "已开始导出。导出过程在后台执行，完成后请检查所选文件。", ButtonType.OK);
                info.setHeaderText("导出已启动");
                info.show();
            });
        } catch (Exception ex) {
            // 若回调抛出异常，告知用户
            ex.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.ERROR, "导出时出现错误：" + ex.getMessage(), ButtonType.OK);
                alert.setHeaderText("导出失败");
                alert.show();
            });
        }
    }

    /** 如果需要释放资源或解绑绑定，请调用此方法（当前实现为占位） */
    public void dispose() {
        // 如果将来对 Label 做了绑定，需要在这里解除绑定以避免内存泄露
        try {
            if (courseNameLabel != null) courseNameLabel.textProperty().unbind();
            if (courseIdLabel != null) courseIdLabel.textProperty().unbind();
            if (scheduleLabel != null) scheduleLabel.textProperty().unbind();
            if (placeLabel != null) placeLabel.textProperty().unbind();
        } catch (Exception ignored) { }
    }
}
