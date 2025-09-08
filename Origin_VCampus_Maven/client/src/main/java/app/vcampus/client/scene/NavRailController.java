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

    /**
     * 【关键改动】创建二级菜单按钮的辅助方法。
     * 它现在接收一个 FXML 路径，并为按钮绑定加载该路径的动作。
     * @param text 按钮显示的文本
     * @param subViewFxmlPath 点击后要加载的子视图 FXML 路径
     * @return 配置好的 JFXButton
     */
    private JFXButton createSecondaryMenuButton(String text, String subViewFxmlPath) {
        JFXButton button = new JFXButton(text);
        button.setMaxHeight(Double.MAX_VALUE); // 确保按钮填满顶栏高度
        button.getStyleClass().add("top-bar-menu-button");

        button.setOnAction(event -> {
            // 1. 调用 MainPanelController 加载子视图
            mainPanelController.loadSubView(subViewFxmlPath);

            // 2. 管理高亮状态
            if (button.getParent() != null) {
                button.getParent().getChildrenUnmodifiable().forEach(node ->
                        node.getStyleClass().remove("active"));
            }
            button.getStyleClass().add("active");
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
        JFXButton infoButton = createSecondaryMenuButton("我的学籍", "/app/vcampus/client/scene/sub/studentstatus/InfoView.fxml");
        JFXButton applyButton = createSecondaryMenuButton("学籍异动", "/app/vcampus/client/scene/sub/studentstatus/ApplyView.fxml");
        infoButton.getStyleClass().add("active"); // 默认高亮第一个
        // 初始加载的父视图
        switchView("/app/vcampus/client/scene/sub/StudentStatusView.fxml", "学籍管理", List.of(infoButton, applyButton));
    }

    @FXML
    private void handleTeachingAffairs() {
        setActiveButton(teachingAffairsButton);
        JFXButton courseSelectionButton = createSecondaryMenuButton("在线选课", "/app/vcampus/client/scene/sub/teachingaffairs/CourseSelectionView.fxml");
        JFXButton gradesButton = createSecondaryMenuButton("成绩查询", "/app/vcampus/client/scene/sub/teachingaffairs/GradesView.fxml");
        JFXButton scheduleButton = createSecondaryMenuButton("我的课表", "/app/vcampus/client/scene/sub/teachingaffairs/ScheduleView.fxml");
        courseSelectionButton.getStyleClass().add("active");
        // 初始加载的父视图
        switchView("/app/vcampus/client/scene/sub/TeachingAffairsView.fxml", "教务系统", List.of(courseSelectionButton, gradesButton, scheduleButton));
    }

    @FXML
    private void handleLibrary() {
        setActiveButton(libraryButton);
        JFXButton searchButton = createSecondaryMenuButton("书籍检索", "/app/vcampus/client/scene/sub/LibraryView.fxml");
        JFXButton historyButton = createSecondaryMenuButton("借阅历史", "/app/vcampus/client/scene/sub/LibraryView.fxml");
        searchButton.getStyleClass().add("active");
        // 初始加载的父视图
        switchView("/app/vcampus/client/scene/sub/LibraryView.fxml", "图书馆", List.of(searchButton, historyButton));
    }

    // ... 对 handleShop, handleFinance, handleAdmin 等方法进行类似的修改 ...
    @FXML
    private void handleShop() {
        setActiveButton(shopButton);
        // ... 创建二级菜单按钮
        switchView("/app/vcampus/client/scene/sub/ShopView.fxml", "网上商店", List.of(/* ... 按钮列表 ... */));
    }

    @FXML
    private void handleFinance() {
        setActiveButton(financeButton);
        // ... 创建二级菜单按钮
        switchView("/app/vcampus/client/scene/sub/FinanceView.fxml", "财务中心", List.of(/* ... 按钮列表 ... */));
    }

    @FXML
    private void handleAdmin() {
        setActiveButton(adminButton);
        // ... 创建二级菜单按钮
        switchView("/app/vcampus/client/scene/sub/AdminView.fxml", "系统管理", List.of(/* ... 按钮列表 ... */));
    }

    @FXML
    private void handleGpt() {
        setActiveButton(gptButton);
        switchView("/app/vcampus/client/scene/sub/GptView.fxml", "VCampus GPT", List.of());
    }
}