package app.vcampus.client.scene;

import javafx.fxml.FXML;

public class NavRailController {

    private MainPanelController mainPanelController;

    public void setMainPanelController(MainPanelController mainPanelController) {
        this.mainPanelController = mainPanelController;
    }

    private void switchView(String fxmlPath, String title) {
        mainPanelController.toggleNavRail();
        mainPanelController.loadView(fxmlPath, title);
    }

    @FXML
    private void handleHome() {
        switchView("/app/vcampus/client/scene/sub/HomeView.fxml", "主页");
    }

    @FXML
    private void handleStudentStatus() {
        switchView("/app/vcampus/client/scene/sub/StudentStatusView.fxml", "学籍管理");
    }

    @FXML
    private void handleTeachingAffairs() {
        switchView("/app/vcampus/client/scene/sub/TeachingAffairsView.fxml", "教务系统");
    }

    @FXML
    private void handleLibrary() {
        switchView("/app/vcampus/client/scene/sub/LibraryView.fxml", "图书馆");
    }

    @FXML
    private void handleShop() {
        switchView("/app/vcampus/client/scene/sub/ShopView.fxml", "网上商店");
    }

    @FXML
    private void handleFinance() {
        switchView("/app/vcampus/client/scene/sub/FinanceView.fxml", "财务中心");
    }

    @FXML
    private void handleAdmin() {
        switchView("/app/vcampus/client/scene/sub/AdminView.fxml", "系统管理");
    }

    @FXML
    private void handleGpt() {
        switchView("/app/vcampus/client/scene/sub/GptView.fxml", "VCampus GPT");
    }
}
