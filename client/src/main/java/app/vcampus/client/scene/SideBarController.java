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

/**
 * 侧边栏控制器。
 * 负责主导航栏的按钮交互、视图切换以及根据用户角色动态生成二级菜单。
 */
public class SideBarController implements Initializable {

    @FXML
    private JFXButton homeButton;
    /**
     * 学籍管理按钮。
     */
    @FXML
    private JFXButton studentStatusButton;
    /**
     * 课程管理按钮。
     */
    @FXML
    private JFXButton courseButton;
    /**
     * 图书馆按钮。
     */
    @FXML
    private JFXButton libraryButton;
    /**
     * 网上商店按钮。
     */
    @FXML
    private JFXButton shopButton;
    /**
     * 财务中心按钮。
     */
    @FXML
    private JFXButton financeButton;
    /**
     * 管理员按钮。
     */
    @FXML
    private JFXButton adminButton;
    /**
     * GPT按钮。
     */
    @FXML
    private JFXButton gptButton;
    /**
     * 聊天按钮。
     */
    @FXML
    private JFXButton chatButton;

    /**
     * 主场景控制器。
     */
    @Setter
    private MainSceneController mainSceneController;
    /**
     * 当前激活的按钮。
     */
    private JFXButton activeButton;

    /**
     * 初始化方法，在FXML文件加载完成后自动调用。
     *
     * @param location  URL定位资源。
     * @param resources 资源包。
     */
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
        chatButton.setUserData("chat");

        adminButton.managedProperty().bind(adminButton.visibleProperty());
        studentStatusButton.managedProperty().bind(studentStatusButton.visibleProperty());

        adminButton.setVisible(false);
        studentStatusButton.setVisible(false);

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

    private JFXButton createSecondaryMenuButton(String text, String subViewFxmlPath) {
        JFXButton button = new JFXButton(text);
        button.setMaxHeight(Double.MAX_VALUE);
        button.getStyleClass().add("top-bar-menu-button");

        button.setOnAction(event -> {
            mainSceneController.loadSubScene(subViewFxmlPath);

            if (button.getParent() != null) {
                button.getParent().getChildrenUnmodifiable().forEach(node ->
                        node.getStyleClass().remove("active"));
            }
            button.getStyleClass().add("active");
        });
        return button;
    }

    /**
     * 设置当前激活的按钮。
     *
     * @param newActiveButton 新的激活按钮。
     */
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
            updateButtonIcon(newActiveButton, true);
            activeButton = newActiveButton;
        }
    }

    /**
     * 更新按钮图标。
     *
     * @param button 按钮。
     * @param isActive 是否激活。
     */
    private void updateButtonIcon(JFXButton button, boolean isActive) {
        if (button == null || button.getGraphic() == null) return;

        String baseName = (String) button.getUserData();
        if (baseName == null || baseName.isEmpty()) {
            System.err.println("错误：按钮 [" + button.getText() + "] 没有在 initialize 方法中设置 userData!");
            return;
        }

        String targetFilename = isActive ? baseName + "_white.png" : baseName + ".png";

        String newClassPath = "/images/" + targetFilename;
        java.io.InputStream resourceStream = getClass().getResourceAsStream(newClassPath);

        if (resourceStream == null) {
            System.err.println("错误：找不到图标资源文件: " + newClassPath);
            return;
        }

        Node graphic = button.getGraphic();
        if (graphic instanceof ImageView imageView) {
            imageView.setImage(new Image(resourceStream));
        }
    }

    /**
     * 切换视图。
     *
     * @param fxmlPath FXML文件路径。
     * @param title 标题。
     * @param menuItems 菜单项。
     */
    private void switchView(String fxmlPath, String title, List<Node> menuItems) {
        mainSceneController.toggleNavRail();
        mainSceneController.switchView(fxmlPath, title, menuItems);
    }

    /**
     * 处理主页按钮点击事件。
     */
    @FXML
    private void handleHome() {
        setActiveButton(homeButton);

        String fxmlPath;
        if (FakeRepository.user != null && FakeRepository.user.getRoles() != null) {
            List<String> roles = Arrays.asList(FakeRepository.user.getRoles());
            if (roles.contains("admin")) {
                fxmlPath = "/app/vcampus/client/scene/SubScene/HomeScene/HomeViewAdmin.fxml";
            } else if (roles.contains("teacher")) {
                fxmlPath = "/app/vcampus/client/scene/SubScene/HomeScene/HomeViewTeacher.fxml";
            } else {
                fxmlPath = "/app/vcampus/client/scene/SubScene/HomeScene/HomeViewStudent.fxml";
            }
        } else {
            fxmlPath = "/app/vcampus/client/scene/SubScene/HomeScene/HomeViewStudent.fxml";
        }

        switchView(fxmlPath, "主页", List.of());
    }

    /**
     * 处理学籍管理按钮点击事件。
     */
    @FXML
    private void handleStudentStatus() {
        setActiveButton(studentStatusButton);
        List<Node> menuItems = new ArrayList<>();
        if (FakeRepository.user != null && Arrays.asList(FakeRepository.user.getRoles()).contains("student")) {
            JFXButton studentButton = createSecondaryMenuButton("我的学籍", "/app/vcampus/client/scene/SubScene/StudentScene/StudentStatusView.fxml");
            menuItems.add(studentButton);
            studentButton.getStyleClass().add("active");
            switchView("/app/vcampus/client/scene/SubScene/StudentScene/StudentStatusView.fxml", "我的学籍", menuItems);
            }
        if(FakeRepository.user != null && Arrays.asList(FakeRepository.user.getRoles()).contains("admin")) {
            JFXButton studentmButton = createSecondaryMenuButton("修改学籍", "/app/vcampus/client/scene/SubScene/StudentScene/StudentStatusManagementView.fxml");
            menuItems.add(studentmButton);
            studentmButton.getStyleClass().add("active");
            switchView("/app/vcampus/client/scene/SubScene/StudentScene/StudentStatusManagementView.fxml", "修改学籍", menuItems);
        }

    }

    /**
     * 处理教务系统按钮点击事件。
     */
    @FXML
    private void handleTeachingAffairs() {
        setActiveButton(courseButton);

        List<Node> menuItems = new ArrayList<>();
        if(FakeRepository.user != null && Arrays.asList(FakeRepository.user.getRoles()).contains("student")) {
            JFXButton courseSelectionButton = createSecondaryMenuButton("在线选课", "/app/vcampus/client/scene/SubScene/CourseScene/choose_class.fxml");
            JFXButton scheduleButton = createSecondaryMenuButton("我的课表", "/app/vcampus/client/scene/SubScene/CourseScene/MySchedule.fxml");
            menuItems.add(courseSelectionButton);
            menuItems.add(scheduleButton);

        }
        if(FakeRepository.user != null && Arrays.asList(FakeRepository.user.getRoles()).contains("teacher")) {
            JFXButton classButton = createSecondaryMenuButton("我的课堂", "/app/vcampus/client/scene/SubScene/CourseScene/MyClassSubscene.fxml");
            menuItems.add(classButton);

        }
        if(FakeRepository.user != null && Arrays.asList(FakeRepository.user.getRoles()).contains("admin"))
        {
            JFXButton addcourseButton = createSecondaryMenuButton("添加课程", "/app/vcampus/client/scene/SubScene/CourseScene/add_course.fxml");
            JFXButton addclassButton = createSecondaryMenuButton("添加班级", "/app/vcampus/client/scene/SubScene/CourseScene/add_teaching_class.fxml");
            menuItems.add(addcourseButton);
            menuItems.add(addclassButton);
        }
        switchView("/app/vcampus/client/scene/SubScene/CourseScene/TeachingAffairsView.fxml", "教务系统",menuItems);
    }

    /**
     * 处理图书馆按钮点击事件。
     */
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

    /**
     * 处理网上商店按钮点击事件。
     */
    @FXML
    private void handleShop() {
        setActiveButton(shopButton);
        JFXButton shopButton = createSecondaryMenuButton("购物页面", "/app/vcampus/client/scene/SubScene/ShopScene/ShopView.fxml");
        JFXButton orderButton = createSecondaryMenuButton("我的订单", "/app/vcampus/client/scene/SubScene/ShopScene/OrderView.fxml");
        JFXButton uploadButton = createSecondaryMenuButton("上传商品", "/app/vcampus/client/scene/SubScene/ShopScene/UploadView.fxml");
        shopButton.getStyleClass().add("active");
        switchView("/app/vcampus/client/scene/SubScene/ShopScene/ShopView.fxml", "网上商店", List.of(shopButton, orderButton, uploadButton));
    }
    
    /**
     * 处理财务中心按钮点击事件。
     */
    @FXML
    private void handleFinance() {
        List<Node> menuItems = new ArrayList<>();
        if(FakeRepository.user != null && Arrays.asList(FakeRepository.user.getRoles()).contains("admin")) {
            JFXButton personalButton = createSecondaryMenuButton("个人财务管理", "/app/vcampus/client/scene/SubScene/FinanceScene/PersonalFinanceView.fxml");
            JFXButton manageButton = createSecondaryMenuButton("一卡通管理", "/app/vcampus/client/scene/SubScene/FinanceScene/ManageFinanceView.fxml");
            menuItems.add(personalButton);
            menuItems.add(manageButton);
            personalButton.getStyleClass().add("active");
        }
        switchView("/app/vcampus/client/scene/SubScene/FinanceScene/PersonalFinanceView.fxml", "财务中心", menuItems);
    }

    /**
     * 处理系统管理按钮点击事件。
     */
    @FXML
    private void handleAdmin() {
        setActiveButton(adminButton);
        JFXButton userManagementButton = createSecondaryMenuButton("用户管理", "/app/vcampus/client/scene/SubScene/AdminScene/AdminView.fxml");
        userManagementButton.getStyleClass().add("active");
        switchView("/app/vcampus/client/scene/SubScene/AdminScene/AdminView.fxml", "系统管理", List.of(userManagementButton));
    }

    /**
     * 处理GPT按钮点击事件。
     */
    @FXML
    private void handleGpt() {
        setActiveButton(gptButton);
        switchView("/app/vcampus/client/scene/SubScene/LlmScene/GptView.fxml", "VCampus GPT", List.of());
    }

    /**
     * 处理聊天按钮点击事件。
     */
    @FXML
    private void handleChat() {
        List<Node> menuItems = new ArrayList<>();
        if(FakeRepository.user != null && Arrays.asList(FakeRepository.user.getRoles()).contains("admin")) {
            JFXButton ctButton = createSecondaryMenuButton("聊天", "/app/vcampus/client/scene/SubScene/ChatScene/ChatView.fxml");
            JFXButton mgButton = createSecondaryMenuButton("管理", "/app/vcampus/client/scene/SubScene/ChatScene/ManageChatView.fxml");
            menuItems.add(ctButton);
            menuItems.add(mgButton);
            ctButton.getStyleClass().add("active");
        }
        setActiveButton(chatButton);
        switchView("/app/vcampus/client/scene/SubScene/ChatScene/ChatView.fxml", "VCampus BBS", menuItems);
    }

}