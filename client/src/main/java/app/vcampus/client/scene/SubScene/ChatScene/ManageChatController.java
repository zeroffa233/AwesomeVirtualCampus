package app.vcampus.client.scene.SubScene.ChatScene;

import app.vcampus.client.viewmodel.ManageChatViewModel;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * 聊天管理控制器。
 * 负责处理聊天管理界面的UI逻辑，包括搜索和删除消息/评论。
 */
public class ManageChatController implements Initializable {

    @FXML
    private JFXComboBox<String> typeComboBox;
    /**
     * 搜索按钮。
     */
    @FXML
    private JFXButton searchButton;
    /**
     * 删除按钮。
     */
    @FXML
    private JFXButton deleteButton;
    /**
     * 昵称复选框。
     */
    @FXML
    private JFXCheckBox nicknameCheckBox;
    /**
     * 昵称文本字段。
     */
    @FXML
    private JFXTextField nicknameTextField;
    /**
     * 卡号复选框。
     */
    @FXML
    private JFXCheckBox cardNumCheckBox;
    /**
     * 卡号文本字段。
     */
    @FXML
    private JFXTextField cardNumTextField;
    /**
     * 内容复选框。
     */
    @FXML
    private JFXCheckBox contentCheckBox;
    /**
     * 内容文本字段。
     */
    @FXML
    private JFXTextField contentTextField;
    /**
     * 结果列表视图。
     */
    @FXML
    private JFXListView<ManageChatViewModel.SearchResultItem> resultListView;
    /**
     * 错误信息标签。
     */
    @FXML
    private Label errorLabel;

    /**
     * 聊天管理视图模型。
     */
    private final ManageChatViewModel viewModel = new ManageChatViewModel();

    /**
     * 初始化方法，在FXML文件加载完成后自动调用。
     *
     * @param location  URL定位资源。
     * @param resources 资源包。
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        typeComboBox.getItems().addAll("Message", "Comment");
        viewModel.searchType.bind(typeComboBox.valueProperty());

        viewModel.useNickname.bind(nicknameCheckBox.selectedProperty());
        viewModel.nickname.bind(nicknameTextField.textProperty());
        nicknameTextField.disableProperty().bind(nicknameCheckBox.selectedProperty().not());

        viewModel.useCardNum.bind(cardNumCheckBox.selectedProperty());
        viewModel.cardNum.bind(cardNumTextField.textProperty());
        cardNumTextField.disableProperty().bind(cardNumCheckBox.selectedProperty().not());

        viewModel.useContent.bind(contentCheckBox.selectedProperty());
        viewModel.content.bind(contentTextField.textProperty());
        contentTextField.disableProperty().bind(contentCheckBox.selectedProperty().not());

        resultListView.setItems(viewModel.searchResults);
        errorLabel.textProperty().bind(viewModel.errorMessage);
        resultListView.setCellFactory(param -> new ChatItemCell(viewModel));
        searchButton.setOnAction(event -> viewModel.search());

        searchButton.setOnAction(event -> viewModel.search());
    }

    /**
     * 自定义列表单元格，用于显示聊天项目并提供删除按钮。
     */
    private static class ChatItemCell extends ListCell<ManageChatViewModel.SearchResultItem> {
        /**
         * 水平布局容器。
         */
        private final HBox hbox = new HBox(10);
        /**
         * 标签。
         */
        private final Label label = new Label();
        /**
         * 删除按钮。
         */
        private final JFXButton deleteButton = new JFXButton("删除");
        /**
         * 聊天管理视图模型。
         */
        private final ManageChatViewModel viewModel;

        /**
         * 构造函数。
         *
         * @param viewModel 聊天管理视图模型。
         */
        public ChatItemCell(ManageChatViewModel viewModel) {
            this.viewModel = viewModel;

            label.setWrapText(true);
            label.setMaxWidth(Double.MAX_VALUE);
            deleteButton.setStyle("-fx-background-color: #D32F2F; -fx-font-size: 12px;");
            deleteButton.setTextFill(Color.WHITE);
            deleteButton.setButtonType(JFXButton.ButtonType.RAISED);

            HBox.setHgrow(label, Priority.ALWAYS);
            hbox.getChildren().addAll(label, deleteButton);
            hbox.setAlignment(Pos.CENTER_LEFT);
        }

        /**
         * 更新列表项。
         *
         * @param item 搜索结果项。
         * @param empty 是否为空。
         */
        @Override
        protected void updateItem(ManageChatViewModel.SearchResultItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                label.setText(item.toString());
                deleteButton.setOnAction(event -> {
                    viewModel.deleteItem(item);
                });
                setGraphic(hbox);
            }
        }
    }
}