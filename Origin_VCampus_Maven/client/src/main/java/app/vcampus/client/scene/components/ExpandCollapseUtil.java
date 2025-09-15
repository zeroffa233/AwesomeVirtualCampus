package app.vcampus.client.scene.components;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;


public class ExpandCollapseUtil {

    public static void animate(VBox detailsBox, boolean expand, Duration duration) {
        animate(detailsBox, expand, duration, null);
    }

    public static void animate(VBox detailsBox, boolean expand, Duration duration, Runnable onFinished) {
        // 必须在 JavaFX 线程运行
        Platform.runLater(() -> {
            // 折叠时先把高度值设置为当前计算高度，展开时先将节点设为可管理和可见以便计算目标高度
            if (expand) {
                detailsBox.setManaged(true);
                detailsBox.setVisible(true);
                detailsBox.applyCss();
                detailsBox.layout();
            }

            // 计算目标高度（使用 prefHeight(-1) 更稳定）
            double targetHeight = expand ? detailsBox.prefHeight(-1) : 0.0;
            // 如果目标高度为 NaN 或 0 则使用当前高度或最小值（防止动画异常）
            if (Double.isNaN(targetHeight) || targetHeight < 0) targetHeight = expand ? 0 : 0;

            // 先设置当前 maxHeight 为实际高度（展开时从 0 到 targetHeight，折叠时从当前到 0）
            double startHeight = expand ? 0 : detailsBox.getHeight();
            // 保证属性可动画：先设置为 startHeight
            detailsBox.setMaxHeight(startHeight);

            Timeline timeline = new Timeline();
            KeyValue kv = new KeyValue(detailsBox.maxHeightProperty(), targetHeight);
            KeyFrame kf = new KeyFrame(duration, kv);
            timeline.getKeyFrames().add(kf);

            timeline.setOnFinished(evt -> {
                // 折叠完成后释放布局空间
                if (!expand) {
                    detailsBox.setVisible(false);
                    detailsBox.setManaged(false);
                    // 将 maxHeight 恢复为 USE_COMPUTED_SIZE，避免后续布局问题
                    detailsBox.setMaxHeight(Region.USE_COMPUTED_SIZE);
                } else {
                    // 展开完成后让布局使用计算高度
                    detailsBox.setMaxHeight(Region.USE_COMPUTED_SIZE);
                }

                // 确保父容器重新布局
                if (detailsBox.getParent() != null) {
                    detailsBox.getParent().requestLayout();
                }

                if (onFinished != null) onFinished.run();
            });

            // 在动画播放期间，也请求父容器布局以保证过渡顺滑
            timeline.currentTimeProperty().addListener((obs, oldT, newT) -> {
                if (detailsBox.getParent() != null) detailsBox.getParent().requestLayout();
            });

            timeline.play();
        });
    }
}
