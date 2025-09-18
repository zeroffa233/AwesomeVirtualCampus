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
import javafx.scene.paint.Color; // 确保导入 Color

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
        // --- 通用验证器 ---
        RequiredFieldValidator requiredValidator = new RequiredFieldValidator("此字段不能为空");

        // --- 商品名称验证 ---
        ValidatorBase nameLengthValidator = new ValidatorBase("商品名称不能超过30个字") {
            @Override
            protected void eval() {
                // JFXTextField, JFXTextArea 都继承自 TextInputControl
                javafx.scene.control.TextInputControl field = (javafx.scene.control.TextInputControl) srcControl.get();
                hasErrors.set(field.getText() != null && field.getText().length() > 30);
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
                hasErrors.set(selectedImageData == null || !"png".equalsIgnoreCase(selectedFileExtension));
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
    private void handleSubmit() {
        // ... (验证逻辑保持不变) ...

        submitButton.setDisable(true);
        submitButton.setText("正在上传...");
        // 确保在后台线程启动前计算好所有需要的数据
        // 注意：selectedImageData 和 imageKey 必须是成员变量或 effectively final
        // 在你的代码里它们已经是成员变量了，所以没问题。
        imageKey = ImageClient.calculateSHA256(selectedImageData);

        new Thread(() -> {
            try {
                // --- Step 1: Handle Image Upload (Asynchronous) ---
                String base64ImageData = Base64.getEncoder().encodeToString(selectedImageData);

                // Call the async method and use .get() to wait for its future result.
                boolean imageUploadSuccess = ImageClient.addOrUpdateImage(imageKey, base64ImageData).get();

                if (!imageUploadSuccess) {
                    throw new Exception("图片上传到图床失败！服务器返回了错误状态。");
                }
                System.out.println("步骤 1/3: 图片成功上传到图床。");

                // --- Step 2: Handle Store Item Addition (Synchronous) ---
                StoreItem newItem = new StoreItem();
                newItem.uuid = UUID.randomUUID();
                newItem.itemName = itemNameField.getText();
                newItem.price = (int) (Double.parseDouble(priceField.getText()) * 100);
                newItem.stock = Integer.parseInt(stockField.getText());
                String description = descriptionArea.getText();
                newItem.description = description.isEmpty() ? null : description;
                newItem.pictureLink = imageKey;
                //barcode
                if (FakeRepository.user != null && FakeRepository.user.getCardNum() != null) {
                    newItem.barcode = String.valueOf(FakeRepository.user.getCardNum());
                } else {
                    // 添加一个备用逻辑，以防万一用户信息获取失败
                    newItem.barcode = "unknown_user";
                    System.err.println("警告: 无法获取当前用户信息，barcode 已被设置为 'unknown_user'");
                }

                // 【核心修正】
                // Call the synchronous method directly. No .get() is needed because it already returns a boolean.
                // We also need to pass the handler as required by the method signature.
                boolean itemAddSuccess = StoreClient.addItem(FakeRepository.handler, newItem);

                if (!itemAddSuccess) {
                    throw new Exception("添加商品信息失败！服务器返回了错误状态。");
                }
                System.out.println("步骤 2/3: 商品信息成功持久化到数据库。");

                // --- Step 3: Update UI on Success ---
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
    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择商品图片");
        // 设置文件类型过滤器
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("图片文件", "*.png"),
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

    private void handleUploadSuccess() {
        // 1. 清空表单并显示成功信息
        clearForm();
        errorMessageLabel.setText("上传成功！");
        errorMessageLabel.setTextFill(Color.GREEN); // 将提示信息变为绿色
        showAndHideErrorMessage();

        // 2. 【核心修改】通知 ImageCache 刷新
        // 我们调用 ImageCache 自己的 initOnce(force=true) 或一个专门的 refresh() 方法
        // (这里我们假设 ImageCache 有一个 refresh 方法来强制重新加载)
        if (ImageCache.getInstance() != null) {
            // 注意：这需要您在 ImageCache 中添加一个 public 的 refresh 方法
            ImageCache.getInstance().refresh();
            System.out.println("已通知 ImageCache 刷新。");
        }

        // 3. 【核心修改】通知 ShopController 刷新
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
