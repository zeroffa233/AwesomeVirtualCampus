package app.vcampus.client.scene.components;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * 展开/折叠动画工具类。
 * 提供对 VBox 控件进行平滑展开和折叠动画的静态方法。
 */
public class ExpandCollapseUtil {

    /**
     * 对 VBox 执行展开或折叠动画。
     *
     * @param detailsBox 要执行动画的 VBox。
     * @param expand     true 为展开，false 为折叠。
     * @param duration   动画持续时间。
     */
    public static void animate(VBox detailsBox, boolean expand, Duration duration) {
        animate(detailsBox, expand, duration, null);
    }

    /**
     * 对 VBox 执行展开或折叠动画，并在动画结束后执行回调。
     *
     * @param detailsBox 要执行动画的 VBox。
     * @param expand     true 为展开，false 为折叠。
     * @param duration   动画持续时间。
     * @param onFinished 动画结束时执行的回调。
     */
    public static void animate(VBox detailsBox, boolean expand, Duration duration, Runnable onFinished) {
        Platform.runLater(() -> {
            if (expand) {
                detailsBox.setManaged(true);
                detailsBox.setVisible(true);
                detailsBox.applyCss();
                detailsBox.layout();
            }

            double targetHeight = expand ? detailsBox.prefHeight(-1) : 0.0;
            if (Double.isNaN(targetHeight) || targetHeight < 0) targetHeight = 0;

            double startHeight = expand ? 0 : detailsBox.getHeight();
            detailsBox.setMaxHeight(startHeight);

            Timeline timeline = new Timeline();
            KeyValue kv = new KeyValue(detailsBox.maxHeightProperty(), targetHeight);
            KeyFrame kf = new KeyFrame(duration, kv);
            timeline.getKeyFrames().add(kf);

            timeline.setOnFinished(evt -> {
                if (!expand) {
                    detailsBox.setVisible(false);
                    detailsBox.setManaged(false);
                    detailsBox.setMaxHeight(Region.USE_COMPUTED_SIZE);
                } else {
                    detailsBox.setMaxHeight(Region.USE_COMPUTED_SIZE);
                }

                if (detailsBox.getParent() != null) {
                    detailsBox.getParent().requestLayout();
                }

                if (onFinished != null) onFinished.run();
            });

            timeline.currentTimeProperty().addListener((obs, oldT, newT) -> {
                if (detailsBox.getParent() != null) detailsBox.getParent().requestLayout();
            });

            timeline.play();
        });
    }
}