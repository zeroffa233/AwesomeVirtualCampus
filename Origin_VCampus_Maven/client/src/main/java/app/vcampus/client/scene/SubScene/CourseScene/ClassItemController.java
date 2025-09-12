package app.vcampus.client.scene.SubScene.CourseScene;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

public class ClassItemController {
    @FXML private VBox root;
    @FXML private Label courseLabel;
    @FXML private Label teacherLabel;
    @FXML private Label placeLabel;

    public void setData(String courseName, String teacherName, String place) {
        // 设置文本（保底）
        String c = courseName == null || courseName.isBlank() ? "未命名课程" : courseName;
        String t = teacherName == null ? "" : teacherName;
        String p = place == null ? "" : place;

        courseLabel.setText(c);
        teacherLabel.setText(t);
        placeLabel.setText(p);

        // tooltip 显示完整课程名
        Tooltip tt = new Tooltip(c);
        Tooltip.install(root, tt);

        // 生成一对颜色（基于课程名的稳定哈希）
        String key = c;
        int hash = Math.abs(key.hashCode() == Integer.MIN_VALUE ? 0 : key.hashCode());
        double hue = (hash % 360);
        double sat = 0.62;    // 饱和度
        double light1 = 0.90; // 上方较亮
        double light2 = 0.78; // 下方稍暗一点

        // 转成 RGB hex
        String c1 = hslToHex(hue, sat, light1);
        String c2 = hslToHex(hue, sat * 0.9, light2);

        // 计算适合的文字颜色（根据较暗 color2 的亮度）
        double[] rgb = hexToRgbNormalized(c2);
        double luminance = 0.2126 * rgb[0] + 0.7152 * rgb[1] + 0.0722 * rgb[2];
        String textColor = luminance < 0.6 ? "#FFFFFF" : "#123754";

        // 应用内联样式（渐变 + 圆角 + 边框阴影）
        String style = String.join(" ",
                "-fx-background-color: linear-gradient(to bottom right, " + c1 + " 0%, " + c2 + " 100%);",
                "-fx-background-radius: 10;",
                "-fx-padding: 8;"
        );
        root.setStyle(style);

        // 设置文字颜色
        courseLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: 700;");
        teacherLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-opacity: 0.95; -fx-font-size: 12;");
        placeLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-opacity: 0.85; -fx-font-size: 12;");
    }

    // ----------------- 辅助函数 -----------------

    // HSL -> hex (#RRGGBB)
    private static String hslToHex(double h, double s, double l) {
        double[] rgb = hslToRgb(h, s, l);
        int r = (int) Math.round(rgb[0] * 255);
        int g = (int) Math.round(rgb[1] * 255);
        int b = (int) Math.round(rgb[2] * 255);
        return String.format("#%02x%02x%02x", clampByte(r), clampByte(g), clampByte(b));
    }

    private static double[] hslToRgb(double h, double s, double l) {
        // h: 0-360, s,l: 0-1
        double c = (1 - Math.abs(2 * l - 1)) * s;
        double hh = h / 60.0;
        double x = c * (1 - Math.abs(hh % 2 - 1));
        double r1 = 0, g1 = 0, b1 = 0;
        if (0 <= hh && hh < 1) { r1 = c; g1 = x; b1 = 0; }
        else if (1 <= hh && hh < 2) { r1 = x; g1 = c; b1 = 0; }
        else if (2 <= hh && hh < 3) { r1 = 0; g1 = c; b1 = x; }
        else if (3 <= hh && hh < 4) { r1 = 0; g1 = x; b1 = c; }
        else if (4 <= hh && hh < 5) { r1 = x; g1 = 0; b1 = c; }
        else { r1 = c; g1 = 0; b1 = x; }
        double m = l - c / 2.0;
        return new double[]{r1 + m, g1 + m, b1 + m};
    }

    private static int clampByte(int v) {
        if (v < 0) return 0;
        if (v > 255) return 255;
        return v;
    }

    private static double[] hexToRgbNormalized(String hex) {
        // hex like #rrggbb
        hex = hex.replace("#", "");
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return new double[]{r / 255.0, g / 255.0, b / 255.0};
    }
}
