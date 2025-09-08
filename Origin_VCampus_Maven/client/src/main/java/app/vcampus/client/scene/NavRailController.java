package app.vcampus.client.scene;

import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class NavRailController implements Initializable {

    // --- FXML 注入 ---
    @FXML
    private JFXButton homeButton;
    @FXML
    private JFXButton studentStatusButton;
    @FXML
    private JFXButton teachingAffairsButton;
    @FXML
    private JFXButton libraryButton;
    @FXML
    private JFXButton shopButton;
    @FXML
    private JFXButton financeButton;
    @FXML
    private JFXButton adminButton;
    @FXML
    private JFXButton gptButton;

    private MainPanelController mainPanelController;
    private JFXButton activeButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (homeButton != null) {
            setActiveButton(homeButton);
        }
    }

    public void setMainPanelController(MainPanelController mainPanelController) {
        this.mainPanelController = mainPanelController;
    }

    private JFXButton createSecondaryMenuButton(String text, Runnable action) {
        JFXButton button = new JFXButton(text);

        // --- 终极方案的关键代码 ---
        // 强制按钮的最大高度为无穷大，使其能够自动拉伸并填满父容器（顶栏）的高度
        button.setMaxHeight(Double.MAX_VALUE);
        // --- 结束关键代码 ---

        button.getStyleClass().add("top-bar-menu-button");

        button.setOnAction(event -> {
            if (button.getParent() != null) {
                button.getParent().getChildrenUnmodifiable().forEach(node ->
                        node.getStyleClass().remove("active"));
            }
            button.getStyleClass().add("active");

            if (action != null) {
                action.run();
            }
        });
        return button;
    }

    private void setActiveButton(JFXButton newActiveButton) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("active");
        }
        if (newActiveButton != null) {
            newActiveButton.getStyleClass().add("active");
            activeButton = newActiveButton;
        }
    }

    private void switchView(String fxmlPath, String title, List<Node> menuItems) {
        mainPanelController.toggleNavRail();
        mainPanelController.switchView(fxmlPath, title, menuItems);
    }

    @FXML
    private void handleHome() {
        setActiveButton(homeButton);
        switchView("/app/vcampus/client/scene/sub/HomeView.fxml", "主页", List.of());
    }

    @FXML
    private void handleStudentStatus() {
        setActiveButton(studentStatusButton);
        JFXButton infoButton = createSecondaryMenuButton("我的学籍", () -> System.out.println("查看学籍信息"));
        JFXButton applyButton = createSecondaryMenuButton("学籍异动", () -> System.out.println("申请学籍异动"));
        infoButton.getStyleClass().add("active");
        switchView("/app/vcampus/client/scene/sub/StudentStatusView.fxml", "学籍管理", List.of(infoButton, applyButton));
    }

    @FXML
    private void handleTeachingAffairs() {
        setActiveButton(teachingAffairsButton);
        JFXButton courseSelectionButton = createSecondaryMenuButton("在线选课", () -> System.out.println("进入选课界面"));
        JFXButton gradesButton = createSecondaryMenuButton("成绩查询", () -> System.out.println("查询个人成绩"));
        JFXButton scheduleButton = createSecondaryMenuButton("我的课表", () -> System.out.println("查看本学期课表"));
        courseSelectionButton.getStyleClass().add("active");
        switchView("/app/vcampus/client/scene/sub/TeachingAffairsView.fxml", "教务系统", List.of(courseSelectionButton, gradesButton, scheduleButton));
    }

    @FXML
    private void handleLibrary() {
        setActiveButton(libraryButton);
        JFXButton searchButton = createSecondaryMenuButton("书籍检索", () -> System.out.println("执行书籍检索"));
        JFXButton historyButton = createSecondaryMenuButton("借阅历史", () -> System.out.println("查看借阅历史"));
        searchButton.getStyleClass().add("active");
        switchView("/app/vcampus/client/scene/sub/LibraryView.fxml", "图书馆", List.of(searchButton, historyButton));
    }

    @FXML
    private void handleShop() {
        setActiveButton(shopButton);
        JFXButton myCartButton = createSecondaryMenuButton("购物车", () -> System.out.println("打开购物车"));
        JFXButton myOrdersButton = createSecondaryMenuButton("我的订单", () -> System.out.println("查看历史订单"));
        myCartButton.getStyleClass().add("active");
        switchView("/app/vcampus/client/scene/sub/ShopView.fxml", "网上商店", List.of(myCartButton, myOrdersButton));
    }

    @FXML
    private void handleFinance() {
        setActiveButton(financeButton);
        JFXButton detailsButton = createSecondaryMenuButton("消费明细", () -> System.out.println("查询消费明细"));
        JFXButton rechargeButton = createSecondaryMenuButton("一卡通充值", () -> System.out.println("进入充值页面"));
        detailsButton.getStyleClass().add("active");
        switchView("/app/vcampus/client/scene/sub/FinanceView.fxml", "财务中心", List.of(detailsButton, rechargeButton));
    }

    @FXML
    private void handleAdmin() {
        setActiveButton(adminButton);
        JFXButton userManagementButton = createSecondaryMenuButton("用户管理", () -> System.out.println("管理系统用户"));
        JFXButton systemLogButton = createSecondaryMenuButton("系统日志", () -> System.out.println("查看系统日志"));
        userManagementButton.getStyleClass().add("active");
        switchView("/app/vcampus/client/scene/sub/AdminView.fxml", "系统管理", List.of(userManagementButton, systemLogButton));
    }

    @FXML
    private void handleGpt() {
        setActiveButton(gptButton);
        switchView("/app/vcampus/client/scene/sub/GptView.fxml", "VCampus GPT", List.of());
    }
}