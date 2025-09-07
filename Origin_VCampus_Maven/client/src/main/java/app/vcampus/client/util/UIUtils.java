package app.vcampus.client.util;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.List;
import java.util.Random;

public class UIUtils {

    public static void createFlowingLightAnimation(Pane animationPane) {
        Random random = new Random();
        List<Color> colors = List.of(
                Color.web("#81C784", 0.8),
                Color.web("#A5D7A7", 0.8),
                Color.web("#C9E7CA", 0.8)
        );

        for (int i = 0; i < 12; i++) {
            Rectangle ribbon = new Rectangle(random.nextDouble() * 50 + 15, 2000);
            ribbon.setFill(colors.get(random.nextInt(colors.size())));
            ribbon.setEffect(new GaussianBlur(80));
            ribbon.setOpacity(0.0);

            ribbon.setTranslateX(random.nextDouble() * 500 - 100);
            ribbon.setTranslateY(random.nextDouble() * 200 - 400);
            ribbon.setRotate(random.nextDouble() * 30 - 15);

            animationPane.getChildren().add(ribbon);

            Timeline timeline = new Timeline();
            timeline.setCycleCount(Timeline.INDEFINITE);

            KeyValue kvX = new KeyValue(ribbon.translateXProperty(), ribbon.getTranslateX() + random.nextDouble() * 200 - 100);
            KeyValue kvY = new KeyValue(ribbon.translateYProperty(), 850);
            KeyValue kvOpacity1 = new KeyValue(ribbon.opacityProperty(), 0.8);
            KeyValue kvOpacity2 = new KeyValue(ribbon.opacityProperty(), 0.0);

            KeyFrame kf0 = new KeyFrame(Duration.ZERO, new KeyValue(ribbon.translateYProperty(), -400));
            KeyFrame kf1 = new KeyFrame(Duration.seconds(random.nextDouble() * 8 + 4), kvOpacity1);
            KeyFrame kf2 = new KeyFrame(Duration.seconds(random.nextDouble() * 15 + 10), kvX, kvY, kvOpacity2);

            timeline.getKeyFrames().addAll(kf0, kf1, kf2);
            timeline.play();
        }
    }
}
