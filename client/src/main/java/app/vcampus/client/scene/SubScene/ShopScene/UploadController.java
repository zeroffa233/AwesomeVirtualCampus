package app.vcampus.client.scene.SubScene.ShopScene;

import app.vcampus.client.gateway.ImageClient;
import app.vcampus.client.gateway.StoreClient;
import app.vcampus.client.repository.FakeRepository;
import app.vcampus.server.entity.StoreItem;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.UUID;
import com.jfoenix.validation.RegexValidator;
import com.jfoenix.validation.RequiredFieldValidator;
import com.jfoenix.validation.base.ValidatorBase;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.util.Duration;
import app.vcampus.client.util.ImageCache;
import javafx.scene.paint.Color;

/**
 * 商品上传控制器。
 * 负责处理新商品上传界面的逻辑，包括图片选择、表单验证和提交。
 */
public class UploadController {

    @FXML private JFXTextField itemNameField;
    @FXML private JFXTextField priceField;
    @FXML private JFXTextField stockField;
    @FXML private JFXTextField imagePathField;
    @FXML private JFXButton chooseImageButton;
    @FXML private JFXTextArea descriptionArea;
    @FXML private JFXButton submitButton;
    @FXML private Label errorMessageLabel;

    private byte[] selectedImageData;
    private String imageKey;
    private String selectedFileExtension;

    /**
     * 初始化方法，在FXML文件加载完成后自动调用。
     */
    @FXML
    public void initialize() {
        initializeColor();
        setupValidators();
    }

    private void initializeColor() {
        String style = "-jfx-focus-color: " + "#728748" + "; -jfx-unfocus-color: " + "#BDBDBD" + ";";
        itemNameField.setStyle(style);
        priceField.setStyle(style);
        stockField.setStyle(style);
        imagePathField.setStyle(style);
        descriptionArea.setStyle(style);
    }

    private void setupValidators() {
        RequiredFieldValidator requiredValidator = new RequiredFieldValidator("此字段不能为空");

        ValidatorBase nameLengthValidator = new ValidatorBase("商品名称不能超过30个字") {
            @Override
            protected void eval() {
                javafx.scene.control.TextInputControl field = (javafx.scene.control.TextInputControl) srcControl.get();
                hasErrors.set(field.getText() != null && field.getText().length() > 30);
            }
        };
        itemNameField.getValidators().addAll(requiredValidator, nameLengthValidator);

        RegexValidator priceValidator = new RegexValidator("价格格式不正确 (例如: 99 或 99.99)");
        priceValidator.setRegexPattern("^\\d+(\\.\\d{1,2})?$");
        priceField.getValidators().addAll(requiredValidator, priceValidator);

        RegexValidator stockNumberValidator = new RegexValidator("库存必须是大于0的整数");
        stockNumberValidator.setRegexPattern("^[1-9]\\d*$");
        stockField.getValidators().addAll(requiredValidator, stockNumberValidator);

        ValidatorBase imageValidator = new ValidatorBase("必须选择一个 .png 格式的图片") {
            @Override
            protected void eval() {
                hasErrors.set(selectedImageData == null || !"png".equalsIgnoreCase(selectedFileExtension));
            }
        };
        imagePathField.getValidators().add(imageValidator);

        addFocusLostValidationListener(itemNameField);
        addFocusLostValidationListener(priceField);
        addFocusLostValidationListener(stockField);
    }

    private void addFocusLostValidationListener(JFXTextField field) {
        field.focusedProperty().addListener((o, oldVal, newVal) -> {
            if (!newVal) {
                field.validate();
            }
        });
    }

    /**
     * 处理提交按钮点击事件。
     * 验证表单，上传图片和商品信息。
     */
    @FXML
    private void handleSubmit() {
        submitButton.setDisable(true);
        submitButton.setText("正在上传...");
        imageKey = ImageClient.calculateSHA256(selectedImageData);

        new Thread(() -> {
            try {
                String base64ImageData = Base64.getEncoder().encodeToString(selectedImageData);
                boolean imageUploadSuccess = ImageClient.addOrUpdateImage(imageKey, base64ImageData).get();

                if (!imageUploadSuccess) throw new Exception("图片上传到图床失败！服务器返回了错误状态。");

                StoreItem newItem = new StoreItem();
                newItem.uuid = UUID.randomUUID();
                newItem.itemName = itemNameField.getText();
                newItem.price = (int) (Double.parseDouble(priceField.getText()) * 100);
                newItem.stock = Integer.parseInt(stockField.getText());
                String description = descriptionArea.getText();
                newItem.description = description.isEmpty() ? null : description;
                newItem.pictureLink = imageKey;
                if (FakeRepository.user != null && FakeRepository.user.getCardNum() != null) {
                    newItem.barcode = String.valueOf(FakeRepository.user.getCardNum());
                } else {
                    newItem.barcode = "unknown_user";
                    System.err.println("警告: 无法获取当前用户信息，barcode 已被设置为 'unknown_user'");
                }

                boolean itemAddSuccess = StoreClient.addItem(FakeRepository.handler, newItem);

                if (!itemAddSuccess) throw new Exception("添加商品信息失败！服务器返回了错误状态。");

                javafx.application.Platform.runLater(this::handleUploadSuccess);

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    System.err.println("添加商品时出错: " + e.getMessage());
                    e.printStackTrace();
                    errorMessageLabel.setText("上传失败，请查看日志！");
                    showAndHideErrorMessage();
                    shakeNode(submitButton);
                });
            } finally {
                javafx.application.Platform.runLater(() -> {
                    submitButton.setDisable(false);
                    submitButton.setText("添加商品");
                });
            }
        }).start();
    }

    /**
     * 处理选择图片按钮点击事件。
     * 打开文件选择器让用户选择图片。
     */
    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择商品图片");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("图片文件", "*.png"),
                new FileChooser.ExtensionFilter("所有文件", "*.*"));

        File selectedFile = fileChooser.showOpenDialog(chooseImageButton.getScene().getWindow());

        if (selectedFile != null) {
            try {
                imagePathField.setText(selectedFile.getAbsolutePath());
                selectedImageData = Files.readAllBytes(selectedFile.toPath());
                imageKey = ImageClient.calculateSHA256(selectedImageData);

                String name = selectedFile.getName();
                if (name.lastIndexOf(".") != -1 && name.lastIndexOf(".") != 0) selectedFileExtension = name.substring(name.lastIndexOf(".") + 1);
                else selectedFileExtension = "";

                imagePathField.validate();

            } catch (Exception e) {
                System.err.println("读取图片文件失败: " + e.getMessage());
                imagePathField.setText("文件读取错误！");
                selectedImageData = null;
                imageKey = null;
                return;
            }
            imagePathField.validate();
        }
    }

    private void handleUploadSuccess() {
        clearForm();
        errorMessageLabel.setText("上传成功！");
        errorMessageLabel.setTextFill(Color.GREEN);
        showAndHideErrorMessage();

        if (ImageCache.getInstance() != null) {
            ImageCache.getInstance().refresh();
            System.out.println("已通知 ImageCache 刷新。");
        }

        if (ShopController.getInstance() != null) {
            ShopController.getInstance().refreshData();
            System.out.println("已通知 ShopController 刷新数据。");
        } else {
            System.err.println("无法获取 ShopController 实例，商品列表未刷新。");
        }
    }

    private void clearForm() {
        itemNameField.clear();
        priceField.clear();
        stockField.clear();
        imagePathField.clear();
        descriptionArea.clear();
        selectedImageData = null;
        imageKey = null;
        selectedFileExtension = null;

        itemNameField.resetValidation();
        priceField.resetValidation();
        stockField.resetValidation();
        imagePathField.resetValidation();
    }

    private void shakeNode(Node node) {
        double SPEED = 1.2;
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(0*SPEED), new KeyValue(node.translateXProperty(), 0)),
                new KeyFrame(Duration.millis(50*SPEED), new KeyValue(node.translateXProperty(), -10)),
                new KeyFrame(Duration.millis(100*SPEED), new KeyValue(node.translateXProperty(), 10)),
                new KeyFrame(Duration.millis(150*SPEED), new KeyValue(node.translateXProperty(), -10)),
                new KeyFrame(Duration.millis(200*SPEED), new KeyValue(node.translateXProperty(), 10)),
                new KeyFrame(Duration.millis(250*SPEED), new KeyValue(node.translateXProperty(), 0))
        );
        timeline.play();
    }

    private void showAndHideErrorMessage() {
        errorMessageLabel.setVisible(true);

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> errorMessageLabel.setVisible(false));
        delay.play();
    }
}