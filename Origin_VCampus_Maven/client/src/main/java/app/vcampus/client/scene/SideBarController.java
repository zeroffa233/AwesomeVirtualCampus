package app.vcampus.client.scene;

import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Setter;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SideBarController implements Initializable {

    // --- FXML 注入 ---
    @FXML
    private JFXButton homeButton;
    @FXML
    private JFXButton studentStatusButton;
    @FXML
    private JFXButton courseButton;
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

    @Setter
    private MainSceneController mainSceneController; // 这个变量将通过下面的方法被赋值
    private JFXButton activeButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        homeButton.setUserData("home");
        studentStatusButton.setUserData("student");
        courseButton.setUserData("course");
        libraryButton.setUserData("library");
        shopButton.setUserData("shop");
        financeButton.setUserData("finance");
        adminButton.setUserData("administrator");
        gptButton.setUserData("llm");
        if (homeButton != null) {
            setActiveButton(homeButton);
        }
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
            // 1. 调用 MainSceneController 加载子视图
            mainSceneController.loadSubScene(subViewFxmlPath);

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
        if (activeButton == newActiveButton) {
            return;
        }

        if (activeButton != null) {
            updateButtonIcon(activeButton, false);
            activeButton.getStyleClass().remove("active");
        }
        if (newActiveButton != null) {
            newActiveButton.getStyleClass().add("active");
            updateButtonIcon(newActiveButton, true); // 设置为高亮图标
            activeButton = newActiveButton;
        }
    }

    /**
     * 【新增】根据按钮的活动状态更新其图标的辅助方法
     * @param button 目标按钮
     * @param isActive 是否为活动状态
     */

    private void updateButtonIcon(JFXButton button, boolean isActive) {
        if (button == null || button.getGraphic() == null) return;

        // 直接从按钮的 userData 获取图标的基础名
        String baseName = (String) button.getUserData();
        if (baseName == null || baseName.isEmpty()) {
            System.err.println("错误：按钮 [" + button.getText() + "] 没有在 initialize 方法中设置 userData!");
            return;
        }

        // 根据 isActive 状态，构建出【目标文件名】
        String targetFilename = isActive ? baseName + "_white.png" : baseName + ".png";

        // 构建标准的类路径并加载图标
        String newClassPath = "/images/" + targetFilename;
        java.io.InputStream resourceStream = getClass().getResourceAsStream(newClassPath);

        if (resourceStream == null) {
            System.err.println("错误：找不到图标资源文件: " + newClassPath);
            return;
        }

        // 更新 ImageView
        Node graphic = button.getGraphic();
        if (graphic instanceof ImageView imageView) {
            imageView.setImage(new Image(resourceStream));
        }
    }

    private void switchView(String fxmlPath, String title, List<Node> menuItems) {
        mainSceneController.toggleNavRail();
        mainSceneController.switchView(fxmlPath, title, menuItems);
    }

    @FXML
    private void handleHome() {
        setActiveButton(homeButton);
        switchView("/app/vcampus/client/scene/SubScene/HomeScene/HomeView.fxml", "主页", List.of());
    }

    @FXML
    private void handleStudentStatus() {
        setActiveButton(studentStatusButton);
        // if 的条件为用户是学生
        if(true) {
            switchView("/app/vcampus/client/scene/SubScene/StudentScene/StudentStatusView.fxml", "我的学籍", List.of());
        }
        // if 的条件为用户是管理员
        else{
            switchView("/app/vcampus/client/scene/SubScene/StudentScene/StudentStatusManagementView.fxml", "学籍管理", List.of());
        }
    }

    @FXML
    private void handleTeachingAffairs() {
        setActiveButton(courseButton);
        JFXButton courseSelectionButton = createSecondaryMenuButton("在线选课", "/app/vcampus/client/scene/SubScene/teachingaffairs/CourseSelectionView.fxml");
        JFXButton gradesButton = createSecondaryMenuButton("成绩查询", "/app/vcampus/client/scene/SubScene/teachingaffairs/GradesView.fxml");
        JFXButton scheduleButton = createSecondaryMenuButton("我的课表", "/app/vcampus/client/scene/SubScene/teachingaffairs/ScheduleView.fxml");
        courseSelectionButton.getStyleClass().add("active");
        // 初始加载的父视图
        switchView("/app/vcampus/client/scene/SubScene/CourseScene/TeachingAffairsView.fxml", "教务系统", List.of(courseSelectionButton, gradesButton, scheduleButton));
    }

    @FXML
    private void handleLibrary() {
        setActiveButton(libraryButton);
        JFXButton searchButton = createSecondaryMenuButton("书籍检索", "/app/vcampus/client/scene/SubScene/LibraryScene/LibraryView.fxml");
        JFXButton historyButton = createSecondaryMenuButton("我的借阅", "/app/vcampus/client/scene/SubScene/LibraryScene/LibraryHistoryView.fxml");
        JFXButton addBookButton = createSecondaryMenuButton("添加图书", "/app/vcampus/client/scene/SubScene/LibraryScene/LibraryAddBookView.fxml");
        JFXButton borrowBookButton = createSecondaryMenuButton("办理借书", "/app/vcampus/client/scene/SubScene/LibraryScene/LibraryBorrowBookView.fxml");
        JFXButton returnBookButton = createSecondaryMenuButton("办理还书", "/app/vcampus/client/scene/SubScene/LibraryScene/LibraryReturnBookView.fxml");
        JFXButton updateBookButton = createSecondaryMenuButton("修改图书信息", "/app/vcampus/client/scene/SubScene/LibraryScene/LibraryDeleteBookView.fxml");
        switchView("/app/vcampus/client/scene/SubScene/LibraryScene/LibraryDefaultView.fxml", "图书馆", List.of(searchButton, historyButton, addBookButton, borrowBookButton, returnBookButton, updateBookButton));
    }

    // ... 对 handleShop, handleFinance, handleAdmin 等方法进行类似的修改 ...
    @FXML
    private void handleShop() {
        setActiveButton(shopButton);
        // ... 创建二级菜单按钮
        switchView("/app/vcampus/client/scene/SubScene/ShopScene/ShopView.fxml", "网上商店", List.of(/* ... 按钮列表 ... */));
    }

    @FXML
    private void handleFinance() {
        setActiveButton(financeButton);
        // ... 创建二级菜单按钮
        switchView("/app/vcampus/client/scene/SubScene/FinanceScene/FinanceView.fxml", "财务中心", List.of(/* ... 按钮列表 ... */));
    }

    @FXML
    private void handleAdmin() {
        setActiveButton(adminButton);
        // ... 创建二级菜单按钮
        switchView("/app/vcampus/client/scene/SubScene/AdministratorScene/AdminView.fxml", "系统管理", List.of(/* ... 按钮列表 ... */));
    }

    @FXML
    private void handleGpt() {
        setActiveButton(gptButton);
        switchView("/app/vcampus/client/scene/SubScene/LlmScene/GptView.fxml", "VCampus GPT", List.of());
    }
}
