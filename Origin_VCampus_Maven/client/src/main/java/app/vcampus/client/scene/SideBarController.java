package app.vcampus.client.scene;

import app.vcampus.client.repository.FakeRepository;
import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Setter;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import app.vcampus.server.utility.Request;

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

        // Bind managed property to visible property to remove from layout when not visible
        adminButton.managedProperty().bind(adminButton.visibleProperty());
        studentStatusButton.managedProperty().bind(studentStatusButton.visibleProperty());

        // Default to hiding permissioned buttons
        adminButton.setVisible(false);
        studentStatusButton.setVisible(false);

        // Set visibility based on user roles
        if (FakeRepository.user != null) {
            List<String> roles = Arrays.asList(FakeRepository.user.getRoles());
            boolean isAdmin = roles.contains("admin");
            boolean isStudent = roles.contains("student");

            adminButton.setVisible(isAdmin);
            studentStatusButton.setVisible(isAdmin || isStudent);
        }

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
        // if a user is a student
        if (FakeRepository.user != null && Arrays.asList(FakeRepository.user.getRoles()).contains("student")) {
            switchView("/app/vcampus/client/scene/SubScene/StudentScene/StudentStatusView.fxml", "我的学籍", List.of());
        }
        // if a user is an administrator
        else {
            switchView("/app/vcampus/client/scene/SubScene/StudentScene/StudentStatusManagementView.fxml", "学籍管理", List.of());
        }
    }

    @FXML
    private void handleTeachingAffairs() {
        setActiveButton(courseButton);

        List<Node> menuItems = new ArrayList<>();
        if(FakeRepository.user != null && Arrays.asList(FakeRepository.user.getRoles()).contains("student")) {
            JFXButton courseSelectionButton = createSecondaryMenuButton("在线选课", "/app/vcampus/client/scene/SubScene/CourseScene/choose_class.fxml");
            //JFXButton gradesButton = createSecondaryMenuButton("成绩查询", "/app/vcampus/client/scene/SubScene/teachingaffairs/GradesView.fxml");
            JFXButton scheduleButton = createSecondaryMenuButton("我的课表", "/app/vcampus/client/scene/SubScene/CourseScene/MySchedule.fxml");
            menuItems.add(courseSelectionButton);
            menuItems.add(scheduleButton);
        }
        if(FakeRepository.user != null && Arrays.asList(FakeRepository.user.getRoles()).contains("teacher")) {
            JFXButton classButton = createSecondaryMenuButton("我的课堂", "/app/vcampus/client/scene/SubScene/CourseScene/MyClassSubscene.fxml");
            menuItems.add(classButton);
        }// 初始加载的父视图
        switchView("/app/vcampus/client/scene/SubScene/CourseScene/TeachingAffairsView.fxml", "教务系统",menuItems);
    }

    @FXML
    private void handleLibrary() {
        setActiveButton(libraryButton);

        List<Node> menuItems = new ArrayList<>();
        JFXButton searchBookButton = createSecondaryMenuButton("图书检索", "/app/vcampus/client/scene/SubScene/LibraryScene/LibraryView.fxml");
        searchBookButton.getStyleClass().add("active");
        menuItems.add(searchBookButton);

        boolean isAdmin = false;
        if (FakeRepository.user != null && FakeRepository.user.getRoles() != null) {
            if (Arrays.asList(FakeRepository.user.getRoles()).contains("admin")) {
                isAdmin = true;
            }
        }

        if (isAdmin) {
            JFXButton borrowBookButton = createSecondaryMenuButton("办理借书", "/app/vcampus/client/scene/SubScene/LibraryScene/LibraryBorrowBookView.fxml");
            JFXButton returnBookButton = createSecondaryMenuButton("办理还书", "/app/vcampus/client/scene/SubScene/LibraryScene/LibraryReturnBookView.fxml");
            JFXButton updateBookButton = createSecondaryMenuButton("修改图书信息", "/app/vcampus/client/scene/SubScene/LibraryScene/LibraryDeleteBookView.fxml");
            JFXButton addBookButton = createSecondaryMenuButton("添加图书", "/app/vcampus/client/scene/SubScene/LibraryScene/LibraryAddBookView.fxml");
            menuItems.add(borrowBookButton);
            menuItems.add(returnBookButton);
            menuItems.add(updateBookButton);
            menuItems.add(addBookButton);
        } else {
            JFXButton historyButton = createSecondaryMenuButton("我的借阅", "/app/vcampus/client/scene/SubScene/LibraryScene/LibraryHistoryView.fxml");
            JFXButton borrowBookButton = createSecondaryMenuButton("借阅图书", "/app/vcampus/client/scene/SubScene/LibraryScene/UserBorrowBookView.fxml");
            menuItems.add(historyButton);
            menuItems.add(borrowBookButton);
        }

        switchView("/app/vcampus/client/scene/SubScene/LibraryScene/LibraryView.fxml", "书籍检索", menuItems);
    }

    // ... 对 handleShop, handleFinance, handleAdmin 等方法进行类似的修改 ...
    @FXML
    private void handleShop() {
        setActiveButton(shopButton);
        // ... 创建二级菜单按钮
        JFXButton shopButton = createSecondaryMenuButton("购物页面", "/app/vcampus/client/scene/SubScene/ShopScene/ShopView.fxml");
        JFXButton orderButton = createSecondaryMenuButton("我的订单", "/app/vcampus/client/scene/SubScene/ShopScene/OrderView.fxml");
        shopButton.getStyleClass().add("active");
        switchView("/app/vcampus/client/scene/SubScene/ShopScene/ShopView.fxml", "网上商店", List.of(shopButton, orderButton));
    }
    
    @FXML
    private void handleFinance() {
        setActiveButton(financeButton);
        JFXButton personalButton = createSecondaryMenuButton("个人财务管理", "/app/vcampus/client/scene/SubScene/FinanceScene/PersonalFinanceView.fxml");
        JFXButton manageButton = createSecondaryMenuButton("一卡通管理", "/app/vcampus/client/scene/SubScene/FinanceScene/ManageFinanceView.fxml");
        personalButton.getStyleClass().add("active");
        // ... 创建二级菜单按钮
        switchView("/app/vcampus/client/scene/SubScene/FinanceScene/PersonalFinanceView.fxml", "财务中心", List.of(personalButton, manageButton));
    }

    @FXML
    private void handleAdmin() {
        setActiveButton(adminButton);
        JFXButton userManagementButton = createSecondaryMenuButton("用户管理", "/app/vcampus/client/scene/SubScene/AdminScene/AdminView.fxml");
        userManagementButton.getStyleClass().add("active");
        switchView("/app/vcampus/client/scene/SubScene/AdminScene/AdminView.fxml", "系统管理", List.of(userManagementButton));
    }

    @FXML
    private void handleGpt() {
        setActiveButton(gptButton);
        switchView("/app/vcampus/client/scene/SubScene/LlmScene/GptView.fxml", "VCampus GPT", List.of());
    }
}
