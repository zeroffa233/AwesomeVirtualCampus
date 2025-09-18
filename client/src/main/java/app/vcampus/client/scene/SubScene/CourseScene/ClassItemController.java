package app.vcampus.client.scene.SubScene.CourseScene;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

/**
 * 课程表中的课程项控制器。
 * 负责展示单个课程格子的信息，并根据课程名称动态生成背景颜色。
 */
public class ClassItemController {
        @FXML private VBox root;
    /**
     * 课程标签。
     */
    @FXML private Label courseLabel;
    /**
     * 教师标签。
     */
    @FXML private Label teacherLabel;
    /**
     * 地点标签。
     */
    @FXML private Label placeLabel;

    /**
     * 设置课程项显示的数据。
     *
     * @param courseName  课程名称。
     * @param teacherName 教师姓名。
     * @param place       上课地点。
     */
    public void setData(String courseName, String teacherName, String place) {
        String c = courseName == null || courseName.isBlank() ? "未命名课程" : courseName;
        String t = teacherName == null ? "" : teacherName;
        String p = place == null ? "" : place;

        courseLabel.setText(c);
        teacherLabel.setText(t);
        placeLabel.setText(p);

        Tooltip tt = new Tooltip(c);
        Tooltip.install(root, tt);

        String key = c;
        int hash = Math.abs(key.hashCode() == Integer.MIN_VALUE ? 0 : key.hashCode());
        double hue = (hash % 360);
        double sat = 0.62;
        double light1 = 0.90;
        double light2 = 0.78;

        String c1 = hslToHex(hue, sat, light1);
        String c2 = hslToHex(hue, sat * 0.9, light2);

        double[] rgb = hexToRgbNormalized(c2);
        double luminance = 0.2126 * rgb[0] + 0.7152 * rgb[1] + 0.0722 * rgb[2];
        String textColor = luminance < 0.6 ? "#FFFFFF" : "#123754";

        String style = String.join(" ",
                "-fx-background-color: " + c1 + ";",
                "-fx-background-radius: 10;",
                "-fx-padding: 8;"
        );
        root.setStyle(style);

        courseLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: 700;");
        teacherLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-opacity: 0.95; -fx-font-size: 12;");
        placeLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-opacity: 0.85; -fx-font-size: 12;");
    }

    private static String hslToHex(double h, double s, double l) {
        double[] rgb = hslToRgb(h, s, l);
        int r = (int) Math.round(rgb[0] * 255);
        int g = (int) Math.round(rgb[1] * 255);
        int b = (int) Math.round(rgb[2] * 255);
        return String.format("#%02x%02x%02x", clampByte(r), clampByte(g), clampByte(b));
    }

    /**
     * 将HSL颜色转换为RGB颜色。
     *
     * @param h 色相。
     * @param s 饱和度。
     * @param l 亮度。
     * @return RGB颜色数组。
     */
    private static double[] hslToRgb(double h, double s, double l) {
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

    /**
     * 限制字节值在0-255之间。
     *
     * @param v 字节值。
     * @return 限制后的字节值。
     */
    private static int clampByte(int v) {
        if (v < 0) return 0;
        if (v > 255) return 255;
        return v;
    }

    /**
     * 将十六进制颜色转换为归一化的RGB颜色。
     *
     * @param hex 十六进制颜色字符串。
     * @return 归一化的RGB颜色数组。
     */
    private static double[] hexToRgbNormalized(String hex) {
        hex = hex.replace("#", "");
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return new double[]{r / 255.0, g / 255.0, b / 255.0};
    }
}