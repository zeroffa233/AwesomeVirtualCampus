package app.vcampus.client.scene;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class TeachingAffairsController {

    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private Label cardLabel;

    /**
     * 初始化方法，会在FXML加载完成后调用
     */
    @FXML
    public void initialize() {
        // 可以在这里做一些初始化操作，比如动态设置文本、颜色等
        titleLabel.setText("欢迎使用教务系统");
        //subtitleLabel.setText("Please Select Your Courses");
    }
}
