package app.vcampus.client.scene;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * 教务场景控制器。
 * 负责教务主界面的初始化和基本UI展示。
 */
public class TeachingAffairsController {

    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private Label cardLabel;

    /**
     * 初始化方法，在FXML文件加载完成后自动调用。
     */
    @FXML
    public void initialize() {
        titleLabel.setText("欢迎使用教务系统");
    }
}