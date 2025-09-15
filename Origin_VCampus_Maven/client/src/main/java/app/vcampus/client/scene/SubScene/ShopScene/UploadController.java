package app.vcampus.client.scene.SubScene.ShopScene;

import app.vcampus.client.gateway.ImageClient;
import app.vcampus.client.gateway.StoreClient;
import app.vcampus.client.repository.FakeRepository;
import app.vcampus.server.entity.StoreItem;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

public class UploadController {

    // 1. FXML 注入
    @FXML private JFXTextField itemNameField;
    @FXML private JFXTextField priceField;
    @FXML private JFXTextField stockField;
    @FXML private JFXTextField barcodeField;
    @FXML private JFXTextField imagePathField;
    @FXML private JFXButton chooseImageButton;
    @FXML private JFXTextArea descriptionArea;
    @FXML private JFXButton submitButton;

    // 用于存储用户选择的图片文件的二进制数据
    private byte[] selectedImageData;
    private String imageKey; // 用于存储图片的哈希值 (Key)

    @FXML
    public void initialize() {
        initializeColor();
        // 在这里可以添加输入验证逻辑，例如只允许价格和库存输入数字
        // ... (我们稍后可以添加)
    }

    private void initializeColor() {
        // 【核心修改】在这里，我们用Java代码来定义和应用颜色

        // 1. 定义我们想要的颜色
        final String focusColor = "#728748";
        // 最好也定义一个非焦点颜色，否则线条会从绿色跳回默认的灰色
        final String unfocusColor = "#BDBDBD"; // 一个标准的中性灰色

        // 2. 将样式应用到每一个 JFoenix 输入控件上
        itemNameField.setStyle(
                "-jfx-focus-color: " + focusColor + "; " +
                        "-jfx-unfocus-color: " + unfocusColor + ";"
        );

        priceField.setStyle(
                "-jfx-focus-color: " + focusColor + "; " +
                        "-jfx-unfocus-color: " + unfocusColor + ";"
        );

        stockField.setStyle(
                "-jfx-focus-color: " + focusColor + "; " +
                        "-jfx-unfocus-color: " + unfocusColor + ";"
        );

        barcodeField.setStyle(
                "-jfx-focus-color: " + focusColor + "; " +
                        "-jfx-unfocus-color: " + unfocusColor + ";"
        );

        imagePathField.setStyle(
                "-jfx-focus-color: " + focusColor + "; " +
                        "-jfx-unfocus-color: " + unfocusColor + ";"
        );

        descriptionArea.setStyle(
                "-jfx-focus-color: " + focusColor + "; " +
                        "-jfx-unfocus-color: " + unfocusColor + ";"
        );
    }

    /**
     * 当用户点击“选择图片”按钮时调用。
     */
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
                // 1. 在UI上显示选择的文件路径
                imagePathField.setText(selectedFile.getAbsolutePath());

                // 2. 读取文件的二进制数据并存储
                selectedImageData = Files.readAllBytes(selectedFile.toPath());

                // 3. 【核心】计算图片的哈希值作为 Key
                imageKey = ImageClient.calculateSHA256(selectedImageData);
                System.out.println("选择的图片 Key (SHA-256): " + imageKey);

            } catch (Exception e) {
                // 处理文件读取错误
                System.err.println("读取图片文件失败: " + e.getMessage());
                imagePathField.setText("文件读取错误！");
                selectedImageData = null;
                imageKey = null;
            }
        }
    }

    /**
     * 当用户点击“添加商品”按钮时调用。
     */
    @FXML
    private void handleSubmit() {
        // 1. 输入验证 (简单示例)
        if (itemNameField.getText().isEmpty() || priceField.getText().isEmpty() || selectedImageData == null) {
            System.out.println("错误：商品名称、价格和图片不能为空！");
            // 在这里可以显示一个错误提示对话框
            return;
        }

        // 2. 显示一个加载指示 (例如，禁用按钮)
        submitButton.setDisable(true);
        submitButton.setText("正在上传...");

        // 3. 将上传操作放到一个后台线程中，以避免UI卡顿
        new Thread(() -> {
            try {
                // a. 首先，上传图片到我们的图床系统
                boolean imageUploadSuccess = ImageClient.addOrUpdateImage(imageKey, selectedImageData);
                if (!imageUploadSuccess) {
                    throw new Exception("图片上传到图床失败！");
                }

                // b. 图片上传成功后，创建一个新的 StoreItem 实体
                StoreItem newItem = new StoreItem();
                newItem.uuid = UUID.randomUUID();
                newItem.itemName = itemNameField.getText();
                // 【注意】价格处理：将用户输入的“元”转换为“分”存储
                newItem.price = (int) (Double.parseDouble(priceField.getText()) * 100);
                newItem.stock = Integer.parseInt(stockField.getText());
                newItem.barcode = barcodeField.getText();
                newItem.description = descriptionArea.getText();
                newItem.pictureLink = imageKey; // 【核心】使用图片的哈希值作为链接

                // c. 调用 StoreClient 将新的商品信息发送到服务器
                boolean itemAddSuccess = StoreClient.addItem(FakeRepository.handler, newItem);
                if (!itemAddSuccess) {
                    throw new Exception("添加商品信息失败！");
                }

                // d. 在UI线程中报告成功并清空表单
                javafx.application.Platform.runLater(() -> {
                    System.out.println("商品添加成功！");
                    // 在这里可以显示一个成功提示
                    clearForm();
                });

            } catch (Exception e) {
                // e. 在UI线程中报告错误
                javafx.application.Platform.runLater(() -> {
                    System.err.println("添加商品时出错: " + e.getMessage());
                    // 在这里显示一个错误对话框
                });
            } finally {
                // f. 无论成功还是失败，最后都在UI线程中恢复按钮状态
                javafx.application.Platform.runLater(() -> {
                    submitButton.setDisable(false);
                    submitButton.setText("添加商品");
                });
            }
        }).start();
    }

    /**
     * 一个清空所有输入字段的辅助方法。
     */
    private void clearForm() {
        itemNameField.clear();
        priceField.clear();
        stockField.clear();
        barcodeField.clear();
        imagePathField.clear();
        descriptionArea.clear();
        selectedImageData = null;
        imageKey = null;
    }
}
