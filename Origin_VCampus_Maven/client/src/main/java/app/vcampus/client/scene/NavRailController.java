package app.vcampus.client.scene;

import javafx.fxml.FXML;

public class NavRailController {

    private MainPanelController mainPanelController;

    public void setMainPanelController(MainPanelController mainPanelController) {
        this.mainPanelController = mainPanelController;
    }

    private void switchView(String fxmlPath) {
        mainPanelController.toggleNavRail();
        mainPanelController.loadView(fxmlPath);
    }

    @FXML
    private void handleHome() {
        switchView("/app/vcampus/client/scene/subscene/HomeView.fxml");
    }

    @FXML
    private void handleStudentStatus() {
        System.out.println("Student Status button clicked");
        // switchView("/app/vcampus/client/scene/subscene/StudentStatusView.fxml");
    }

    @FXML
    private void handleTeachingAffairs() {
        System.out.println("Teaching Affairs button clicked");
        // switchView("/app/vcampus/client/scene/subscene/TeachingAffairsView.fxml");
    }

    @FXML
    private void handleLibrary() {
        switchView("/app/vcampus/client/scene/subscene/LibraryView.fxml");
    }

    @FXML
    private void handleShop() {
        System.out.println("Shop button clicked");
        // switchView("/app/vcampus/client/scene/subscene/ShopView.fxml");
    }

    @FXML
    private void handleFinance() {
        System.out.println("Finance button clicked");
        mainPanelController.loadView("/app/vcampus/client/scene/sub/FinanceView.fxml");
    }

    @FXML
    private void handleAdmin() {
        System.out.println("Admin button clicked");
    }

    @FXML
    private void handleGpt() {
        System.out.println("GPT button clicked");
        mainPanelController.loadView("/app/vcampus/client/scene/sub/GptView.fxml");
    }
}
