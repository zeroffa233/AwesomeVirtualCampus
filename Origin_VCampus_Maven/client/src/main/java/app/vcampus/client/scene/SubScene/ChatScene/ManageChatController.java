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

import java.awt.*;
import java.net.URL;
import java.util.ResourceBundle;

public class ManageChatController implements Initializable {

    @FXML
    private JFXComboBox<String> typeComboBox;
    @FXML
    private JFXButton searchButton;
    @FXML
    private JFXButton deleteButton;
    @FXML
    private JFXCheckBox nicknameCheckBox;
    @FXML
    private JFXTextField nicknameTextField;
    @FXML
    private JFXCheckBox cardNumCheckBox;
    @FXML
    private JFXTextField cardNumTextField;
    @FXML
    private JFXCheckBox contentCheckBox;
    @FXML
    private JFXTextField contentTextField;
    @FXML
    private JFXListView<ManageChatViewModel.SearchResultItem> resultListView;
    @FXML
    private Label errorLabel;

    private final ManageChatViewModel viewModel = new ManageChatViewModel();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化搜索类型下拉框
        typeComboBox.getItems().addAll("Message", "Comment");
        viewModel.searchType.bind(typeComboBox.valueProperty());

        // 绑定搜索条件的启用复选框和输入框
        viewModel.useNickname.bind(nicknameCheckBox.selectedProperty());
        viewModel.nickname.bind(nicknameTextField.textProperty());
        nicknameTextField.disableProperty().bind(nicknameCheckBox.selectedProperty().not());

        viewModel.useCardNum.bind(cardNumCheckBox.selectedProperty());
        viewModel.cardNum.bind(cardNumTextField.textProperty());
        cardNumTextField.disableProperty().bind(cardNumCheckBox.selectedProperty().not());

        viewModel.useContent.bind(contentCheckBox.selectedProperty());
        viewModel.content.bind(contentTextField.textProperty());
        contentTextField.disableProperty().bind(contentCheckBox.selectedProperty().not());

        // 绑定列表视图的数据源和选中项
        resultListView.setItems(viewModel.searchResults);
        errorLabel.textProperty().bind(viewModel.errorMessage);
        resultListView.setCellFactory(param -> new ChatItemCell(viewModel));
        searchButton.setOnAction(event -> viewModel.search());

        // 绑定按钮的点击事件到ViewModel中的方法
        searchButton.setOnAction(event -> viewModel.search());

    }
    private static class ChatItemCell extends ListCell<ManageChatViewModel.SearchResultItem> {
        private final HBox hbox = new HBox(10); // 10px spacing
        private final Label label = new Label();
        private final JFXButton deleteButton = new JFXButton("删除");
        private final ManageChatViewModel viewModel;

        public ChatItemCell(ManageChatViewModel viewModel) {
            this.viewModel = viewModel;

            // 配置布局和样式
            label.setWrapText(true);
            label.setMaxWidth(Double.MAX_VALUE);
            deleteButton.setStyle("-fx-background-color: #D32F2F; -fx-font-size: 12px;");
            deleteButton.setTextFill(Color.WHITE);
            deleteButton.setButtonType(JFXButton.ButtonType.RAISED);

            // HBox 内部布局：让 label 占据所有可用空间，将按钮推到最右侧
            HBox.setHgrow(label, Priority.ALWAYS);
            hbox.getChildren().addAll(label, deleteButton);
            hbox.setAlignment(Pos.CENTER_LEFT);
        }

        @Override
        protected void updateItem(ManageChatViewModel.SearchResultItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                // 如果单元格为空，则不显示任何内容
                setGraphic(null);
            } else {
                // 如果单元格有内容，则更新显示
                label.setText(item.toString());
                deleteButton.setOnAction(event -> {
                    // 为按钮设置点击事件，调用 ViewModel 的 deleteItem 方法
                    viewModel.deleteItem(item);
                });
                setGraphic(hbox);
            }
        }
    }
}