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
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Files;
import java.util.UUID;
import com.jfoenix.validation.RegexValidator;
import com.jfoenix.validation.RequiredFieldValidator;
import com.jfoenix.validation.base.ValidatorBase;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.util.Duration;

public class UploadController {

    // 1. FXML 注入
    @FXML private JFXTextField itemNameField;
    @FXML private JFXTextField priceField;
    @FXML private JFXTextField stockField;
    @FXML private JFXTextField imagePathField;
    @FXML private JFXButton chooseImageButton;
    @FXML private JFXTextArea descriptionArea;
    @FXML private JFXButton submitButton;
    @FXML private Label errorMessageLabel;

    // 用于存储用户选择的图片文件的二进制数据
    private byte[] selectedImageData;
    private String imageKey; // 用于存储图片的哈希值 (Key)
    private String selectedFileExtension;

    @FXML
    public void initialize() {
        final String focusColor = "#728748";
        final String unfocusColor = "#BDBDBD";
        initializeColor(focusColor, unfocusColor);
        setupValidators();
    }

    private void initializeColor(String focusColor, String unfocusColor) {
        String style = "-jfx-focus-color: " + focusColor + "; -jfx-unfocus-color: " + unfocusColor + ";";
        itemNameField.setStyle(style);
        priceField.setStyle(style);
        stockField.setStyle(style);
        imagePathField.setStyle(style);
        descriptionArea.setStyle(style);
    }

    private void setupValidators() {
        // --- 通用验证器 ---
        RequiredFieldValidator requiredValidator = new RequiredFieldValidator("此字段不能为空");

        // --- 商品名称验证 ---
        ValidatorBase nameLengthValidator = new ValidatorBase("商品名称不能超过30个字") {
            @Override
            protected void eval() {
                // JFXTextField, JFXTextArea 都继承自 TextInputControl
                javafx.scene.control.TextInputControl field = (javafx.scene.control.TextInputControl) srcControl.get();
                if (field.getText() != null && field.getText().length() > 30) {
                    hasErrors.set(true);
                } else {
                    hasErrors.set(false);
                }
            }
        };
        itemNameField.getValidators().addAll(requiredValidator, nameLengthValidator);

        // --- 商品价格验证 ---
        RegexValidator priceValidator = new RegexValidator("价格格式不正确 (例如: 99 或 99.99)");
        priceValidator.setRegexPattern("^\\d+(\\.\\d{1,2})?$");
        priceField.getValidators().addAll(requiredValidator, priceValidator);

        // --- 商品数量验证 ---
        RegexValidator stockNumberValidator = new RegexValidator("库存必须是大于0的整数");
        stockNumberValidator.setRegexPattern("^[1-9]\\d*$");
        stockField.getValidators().addAll(requiredValidator, stockNumberValidator);

        // --- 商品图片验证 ---
        ValidatorBase imageValidator = new ValidatorBase("必须选择一个 .png 格式的图片") {
            @Override
            protected void eval() {
                // 检查文件是否被选择，并且扩展名是否是 "png"
                if (selectedImageData == null || !"png".equalsIgnoreCase(selectedFileExtension)) {
                    hasErrors.set(true);
                } else {
                    hasErrors.set(false);
                }
            }
        };
        imagePathField.getValidators().add(imageValidator);

        // --- 添加监听器，在用户失去焦点时自动触发验证 ---
        addFocusLostValidationListener(itemNameField);
        addFocusLostValidationListener(priceField);
        addFocusLostValidationListener(stockField);
        // imagePathField 的验证由按钮点击触发，不需要焦点监听
    }
    private void addFocusLostValidationListener(JFXTextField field) {
        field.focusedProperty().addListener((o, oldVal, newVal) -> {
            if (!newVal) { // 当失去焦点时
                field.validate();
            }
        });
    }


    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择商品图片");
        // 设置文件类型过滤器
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("图片文件", "*.png", "*.jpg", "*.gif"),
                new FileChooser.ExtensionFilter("所有文件", "*.*")
        );

        // 显示文件选择对话框
        File selectedFile = fileChooser.showOpenDialog(chooseImageButton.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // ... (读取文件、计算哈希的代码不变)
                imagePathField.setText(selectedFile.getAbsolutePath());
                selectedImageData = Files.readAllBytes(selectedFile.toPath());
                imageKey = ImageClient.calculateSHA256(selectedImageData);

                // 【核心修正】在这里，我们从文件名中提取扩展名，并赋给我们的新成员变量
                String name = selectedFile.getName();
                // 确保文件名中有 . 符号，避免出错
                if (name.lastIndexOf(".") != -1 && name.lastIndexOf(".") != 0) selectedFileExtension = name.substring(name.lastIndexOf(".") + 1);
                else selectedFileExtension = "";


                // 用户选择文件后，立即触发一次验证
                imagePathField.validate();

            } catch (Exception e) {
                // 处理文件读取错误
                System.err.println("读取图片文件失败: " + e.getMessage());
                imagePathField.setText("文件读取错误！");
                selectedImageData = null;
                imageKey = null;
                return;
            }
            imagePathField.validate();
        }
    }



    @FXML
    private void handleSubmit() {
        System.out.println("handleSubmit() called");
        boolean isAllValid = itemNameField.validate() &
                priceField.validate() &
                stockField.validate() &
                imagePathField.validate();

        // 【第3步】【核心修改】如果验证失败，执行我们的新 feature
        if (!isAllValid) {
            System.out.println("Validation failed.");

            // a. 抖动按钮
            shakeNode(submitButton);

            // b. 显示错误信息，并在3秒后自动隐藏
            showAndHideErrorMessage();

            return; // 立即终止方法，不执行后续上传逻辑
        }

        // --- 只有在所有验证都通过后，才继续执行上传逻辑 ---
        // ... (后续的后台上传逻辑，保持我们之前的版本不变)


        submitButton.setDisable(true);
        submitButton.setText("正在上传...");

        new Thread(() -> {
            try {
                // a. 上传图片
                boolean imageUploadSuccess = ImageClient.addOrUpdateImage(imageKey, selectedImageData);
                if (!imageUploadSuccess) {
                    throw new Exception("图片上传到图床失败！");
                }

                // b. 创建 StoreItem 实体
                StoreItem newItem = new StoreItem();
                newItem.uuid = UUID.randomUUID();
                newItem.itemName = itemNameField.getText();
                newItem.price = (int) (Double.parseDouble(priceField.getText()) * 100);
                newItem.stock = Integer.parseInt(stockField.getText());
                String description = descriptionArea.getText();
                newItem.description = description.isEmpty() ? null : description;
                newItem.pictureLink = imageKey;
                // 注意：您的 StoreItem 实体还有一个 barcode 字段，这里我们先设为空字符串
                newItem.barcode = "";

                // c. 【核心修正】调用 StoreClient.addItem 而不是 addOrUpdateImage
                // addItem 是专门用于添加新商品的，更符合这里的业务逻辑
                boolean itemAddSuccess = StoreClient.addItem(FakeRepository.handler, newItem);
                if (!itemAddSuccess) {
                    throw new Exception("添加商品信息失败！");
                }

                javafx.application.Platform.runLater(this::clearFormAndShowSuccess);

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    System.err.println("添加商品时出错: " + e.getMessage());
                    // 在这里可以显示一个错误对话框
                });
            } finally {
                javafx.application.Platform.runLater(() -> {
                    submitButton.setDisable(false);
                    submitButton.setText("添加商品");
                });
            }
        }).start();
    }

    private void clearFormAndShowSuccess() {
        clearForm();
        System.out.println("商品添加成功！");
        // 这里可以弹出一个成功的提示框
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

        // 重置所有验证状态
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

        PauseTransition delay = new PauseTransition(Duration.seconds(3)); // 错误信息显示 3 秒
        delay.setOnFinished(event -> errorMessageLabel.setVisible(false));
        delay.play();
    }
}
