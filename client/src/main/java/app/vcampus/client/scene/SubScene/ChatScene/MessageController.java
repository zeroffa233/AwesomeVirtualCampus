package app.vcampus.client.scene.SubScene.ChatScene;

import app.vcampus.client.viewmodel.ChatViewModel;
import app.vcampus.client.viewmodel.CommentViewModel;
import app.vcampus.client.viewmodel.MessageViewModel;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * 消息控制器。
 * 负责展示单条消息及其关联的评论，并处理点赞和发表评论等用户交互。
 */
public class MessageController {

    @FXML private VBox rootMessagePane;
    /**
     * 用户名标签。
     */
    @FXML private Label usernameLabel;
    /**
     * 时间戳标签。
     */
    @FXML private Label timestampLabel;
    /**
     * 内容标签。
     */
    @FXML private Label contentLabel;
    /**
     * 点赞按钮。
     */
    @FXML private JFXButton likeButton;
    /**
     * 点赞图标文本。
     */
    @FXML private Text likeIconText;
    /**
     * 点赞计数标签。
     */
    @FXML private Label likeCountLabel;
    /**
     * 评论VBox。
     */
    @FXML private VBox commentsVBox;
    /**
     * 新评论文本字段。
     */
    @FXML private JFXTextField newCommentTextField;
    /**
     * 发表评论按钮。
     */
    @FXML private JFXButton postCommentButton;

    /**
     * 消息视图模型。
     */
    private MessageViewModel messageViewModel;
    /**
     * 聊天视图模型。
     */
    private ChatViewModel chatViewModel;

    /**
     * 设置此控制器所需的数据模型。
     *
     * @param mvm 消息视图模型。
     * @param cvm 聊天视图模型。
     */
    public void setData(MessageViewModel mvm, ChatViewModel cvm) {
        this.messageViewModel = mvm;
        this.chatViewModel = cvm;

        setupBindings();
        setupListeners();
        updateLikeButtonState(mvm.isLikedByMeProperty().get());
        loadComments();
    }

    private void setupBindings() {
        usernameLabel.textProperty().bind(messageViewModel.uploaderNameProperty());
        timestampLabel.textProperty().bind(messageViewModel.timestampProperty());
        contentLabel.textProperty().bind(messageViewModel.contentProperty());
        likeCountLabel.textProperty().bind(messageViewModel.likeCountProperty().asString());
    }

    /**
     * 设置监听器。
     */
    private void setupListeners() {
        messageViewModel.isLikedByMeProperty().addListener((obs, oldVal, newVal) -> updateLikeButtonState(newVal));

        messageViewModel.getComments().addListener((ListChangeListener<CommentViewModel>) c -> {
            if (!c.getList().isEmpty() || commentsVBox.getChildren().size() > 0) {
                loadComments();
            }
        });

        newCommentTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                onPostCommentClicked();
            }
        });
    }

    /**
     * 更新点赞按钮状态。
     *
     * @param isLiked 是否已点赞。
     */
    private void updateLikeButtonState(boolean isLiked) {
        if (isLiked) {
            likeIconText.setFill(Color.web("#607830"));
        } else {
            likeIconText.setFill(Color.GRAY);
        }
    }

    /**
     * 加载评论。
     */
    private void loadComments() {
        commentsVBox.getChildren().clear();
        for (CommentViewModel cvm : messageViewModel.getComments()) {
            commentsVBox.getChildren().add(createCommentNode(cvm));
        }
    }

    /**
     * 创建评论节点。
     *
     * @param cvm 评论视图模型。
     * @return 评论节点。
     */
    private Node createCommentNode(CommentViewModel cvm) {
        HBox commentBox = new HBox(5);
        commentBox.setAlignment(Pos.CENTER_LEFT);

        Label authorLabel = new Label();
        authorLabel.textProperty().bind(cvm.uploaderNameProperty());
        authorLabel.setStyle("-fx-font-weight: bold;");

        Text contentText = new Text(": ");
        Text contentValueText = new Text();
        contentValueText.textProperty().bind(cvm.contentProperty());
        contentValueText.setWrappingWidth(500);

        JFXButton commentLikeButton = new JFXButton();
        Text commentIconText = new Text("❤");
        commentIconText.setStyle("-fx-font-size: 14px;");
        commentLikeButton.setGraphic(commentIconText);
        commentLikeButton.setOnAction(e -> chatViewModel.toggleCommentLike(cvm));

        Label commentLikeCount = new Label();
        commentLikeCount.textProperty().bind(cvm.likeCountProperty().asString());

        cvm.isLikedByMeProperty().addListener((obs, oldVal, isNowLiked) -> {
            if (isNowLiked) {
                commentIconText.setFill(Color.RED);
            } else {
                commentIconText.setFill(Color.GRAY);
            }
        });

        boolean isInitiallyLiked = cvm.isLikedByMeProperty().get();
        if (isInitiallyLiked) {
            commentIconText.setFill(Color.RED);
        } else {
            commentIconText.setFill(Color.GRAY);
        }

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        commentBox.getChildren().addAll(authorLabel, contentText, contentValueText, spacer, commentLikeButton, commentLikeCount);
        return commentBox;
    }

    /**
     * 点赞按钮点击事件处理。
     */
    @FXML
    private void onLikeButtonClicked() {
        chatViewModel.toggleMessageLike(messageViewModel);
    }

    /**
     * 发表评论按钮点击事件处理。
     */
    @FXML
    private void onPostCommentClicked() {
        String content = newCommentTextField.getText().trim();
        if (!content.isEmpty()) {
            chatViewModel.postComment(messageViewModel, content);
            newCommentTextField.clear();
        }
    }
}