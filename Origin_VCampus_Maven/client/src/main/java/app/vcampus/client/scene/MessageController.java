package app.vcampus.client.scene;

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

public class MessageController {

    @FXML private VBox rootMessagePane;
    @FXML private Label usernameLabel;
    @FXML private Label timestampLabel;
    @FXML private Label contentLabel;
    @FXML private JFXButton likeButton;
    @FXML private Text likeIconText; // 引用 FXML 中的 Text 节点 for Message
    @FXML private Label likeCountLabel;
    @FXML private VBox commentsVBox;
    @FXML private JFXTextField newCommentTextField;
    @FXML private JFXButton postCommentButton;

    private MessageViewModel messageViewModel;
    private ChatViewModel chatViewModel;

    public void setData(MessageViewModel mvm, ChatViewModel cvm) {
        this.messageViewModel = mvm;
        this.chatViewModel = cvm;

        setupBindings();
        setupListeners();
        updateLikeButtonState(mvm.isLikedByMeProperty().get()); // 设置初始的点赞按钮状态
        loadComments(); // 立即加载一次已有的评论
    }

    private void setupBindings() {
        usernameLabel.textProperty().bind(messageViewModel.uploaderNameProperty());
        timestampLabel.textProperty().bind(messageViewModel.timestampProperty());
        contentLabel.textProperty().bind(messageViewModel.contentProperty());
        likeCountLabel.textProperty().bind(messageViewModel.likeCountProperty().asString());
    }

    private void setupListeners() {
        // 监听消息点赞状态的变化，并更新UI
        messageViewModel.isLikedByMeProperty().addListener((obs, oldVal, newVal) -> updateLikeButtonState(newVal));

        // 监听评论列表的变化
        messageViewModel.getComments().addListener((ListChangeListener<CommentViewModel>) c -> {
            // 在重新加载评论时，确保在JavaFX应用线程上执行
            // 以避免在后台线程（例如轮询线程）直接修改UI
            if (!c.getList().isEmpty() || commentsVBox.getChildren().size() > 0) {
                loadComments();
            }
        });

        // 为评论输入框添加回车键监听
        newCommentTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                onPostCommentClicked();
            }
        });
    }

    /**
     * 【修改点 1】: 更新消息点赞按钮的状态 (👍)
     * 通过改变颜色来清晰地表示用户是否已点赞。
     * @param isLiked 用户是否点赞了这条消息
     */
    private void updateLikeButtonState(boolean isLiked) {
        System.out.println("Trigger like:"+isLiked);
        if (isLiked) {
            likeIconText.setFill(Color.web("#4285F4")); // 点赞后为蓝色 (Google Blue)
        } else {
            likeIconText.setFill(Color.GRAY); // 未点赞为灰色
        }
    }

    private void loadComments() {
        commentsVBox.getChildren().clear();
        for (CommentViewModel cvm : messageViewModel.getComments()) {
            commentsVBox.getChildren().add(createCommentNode(cvm));
        }
    }

    /**
     * 【修改点 2】: 创建评论节点，实现空心/实心爱心效果 (♡/❤)
     * @param cvm 评论的ViewModel
     * @return 代表一条评论的UI节点
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

        // 评论的点赞按钮
        JFXButton commentLikeButton = new JFXButton();
        Text commentIconText = new Text(); // 初始文本在下面设置
        commentIconText.setText("❤");
        commentIconText.setStyle("-fx-font-size: 14px;"); // 稍微放大爱心图标
        commentLikeButton.setGraphic(commentIconText);
        commentLikeButton.setOnAction(e -> chatViewModel.toggleCommentLike(cvm));

        Label commentLikeCount = new Label();
        commentLikeCount.textProperty().bind(cvm.likeCountProperty().asString());

        // 核心逻辑：监听评论的点赞状态，并切换图标和颜色
        cvm.isLikedByMeProperty().addListener((obs, oldVal, isNowLiked) -> {
            if (isNowLiked) {
                commentIconText.setFill(Color.RED);
            } else {
                commentIconText.setFill(Color.GRAY);
            }
        });

        // 初始化第一次加载时的状态
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

    @FXML
    private void onLikeButtonClicked() {
        chatViewModel.toggleMessageLike(messageViewModel);
    }

    @FXML
    private void onPostCommentClicked() {
        String content = newCommentTextField.getText().trim();
        if (!content.isEmpty()) {
            chatViewModel.postComment(messageViewModel, content);
            newCommentTextField.clear();
        }
    }
}