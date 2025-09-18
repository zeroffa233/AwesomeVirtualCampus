package app.vcampus.client.viewmodel;

import app.vcampus.client.gateway.ChatClient;
import app.vcampus.server.utility.SearchResult;
import app.vcampus.server.entity.Comment;
import app.vcampus.server.entity.Message;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 聊天管理视图模型。
 * 负责处理聊天管理界面的逻辑，如搜索和删除消息或评论。
 */
public class ManageChatViewModel {
    /**
     * 搜索类型属性。
     */
    public final StringProperty searchType = new SimpleStringProperty();
    /**
     * 是否使用昵称搜索的布尔属性。
     */
    public final BooleanProperty useNickname = new SimpleBooleanProperty();
    /**
     * 昵称输入框的属性。
     */
    public final StringProperty nickname = new SimpleStringProperty();
    /**
     * 是否使用卡号搜索的布尔属性。
     */
    public final BooleanProperty useCardNum = new SimpleBooleanProperty();
    /**
     * 卡号输入框的属性。
     */
    public final StringProperty cardNum = new SimpleStringProperty();
    /**
     * 是否使用内容搜索的布尔属性。
     */
    public final BooleanProperty useContent = new SimpleBooleanProperty();
    /**
     * 内容输入框的属性。
     */
    public final StringProperty content = new SimpleStringProperty();
    /**
     * 错误消息文本属性。
     */
    public final StringProperty errorMessage = new SimpleStringProperty("");

    /**
     * 搜索结果列表。
     */
    public final ObservableList<SearchResultItem> searchResults = FXCollections.observableArrayList();

    /**
     * 搜索结果项的内部类。
     * 用于封装显示在ListView中的每一项的数据。
     */
    public static class SearchResultItem {
        /**
         * 类型，"message" 或 "comment"。
         */
        public final String type;
        /**
         * 唯一标识符。
         */
        public final UUID id;
        /**
         * 用于显示的内容。
         */
        public final String displayContent;

        /**
         * 构造函数。
         *
         * @param type           类型。
         * @param id             ID。
         * @param displayContent 显示内容。
         */
        public SearchResultItem(String type, UUID id, String displayContent) {
            this.type = type;
            this.id = id;
            this.displayContent = displayContent;
        }

        /**
         * 返回用于在ListView中显示的字符串。
         *
         * @return 显示内容。
         */
        @Override
        public String toString() {
            return displayContent;
        }
    }

    /**
     * 执行搜索操作。
     */
    public void search() {
        errorMessage.set("");

        String type = searchType.get();
        if (type == null || type.isEmpty()) {
            errorMessage.set("请选择搜索类型 (消息 或 评论)");
            return;
        }

        Map<String, String> criteria = new HashMap<>();
        if (useNickname.get() && nickname.get() != null && !nickname.get().isEmpty()) {
            criteria.put("nickname", nickname.get());
        }
        if (useCardNum.get() && cardNum.get() != null && !cardNum.get().isEmpty()) {
            criteria.put("cardNum", cardNum.get());
        }
        if (useContent.get() && content.get() != null && !content.get().isEmpty()) {
            criteria.put("content", content.get());
        }

        try {
            SearchResult result = ChatClient.getInstance().search(type.toLowerCase(), criteria);

            Map<Integer, String> userMap = result.getUserMap();
            searchResults.clear();

            if (result.getResults().isEmpty()) {
                errorMessage.set("未找到符合条件的结果");
            }

            for (Object item : result.getResults()) {
                if (item instanceof Message) {
                    Message msg = (Message) item;
                    String formatted = formatDisplay("消息", msg.getUploaderCardNum(), userMap, msg.getContent(), msg.getTimestamp());
                    searchResults.add(new SearchResultItem("message", msg.getId(), formatted));
                } else if (item instanceof Comment) {
                    Comment cmt = (Comment) item;
                    String formatted = formatDisplay("评论", cmt.getUploaderCardNum(), userMap, cmt.getContent(), cmt.getTimestamp());
                    searchResults.add(new SearchResultItem("comment", cmt.getId(), formatted));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            errorMessage.set("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 删除一个指定的项。
     *
     * @param itemToDelete 要删除的 SearchResultItem 对象。
     */
    public void deleteItem(SearchResultItem itemToDelete) {
        errorMessage.set("");

        if (itemToDelete == null) {
            errorMessage.set("内部错误: 尝试删除一个空项目。");
            return;
        }

        try {
            ChatClient.getInstance().delete(itemToDelete.type, itemToDelete.id);
            searchResults.remove(itemToDelete);
        } catch (IOException e) {
            e.printStackTrace();
            errorMessage.set("删除失败: " + e.getMessage());
        }
    }

    /**
     * 格式化最终显示在列表中的字符串。
     *
     * @param typeLabel "消息" 或 "评论"。
     * @param cardNum   发送者卡号。
     * @param userMap   卡号到用户名的映射。
     * @param content   内容。
     * @param timestamp 时间戳。
     * @return 格式化后的字符串。
     */
    private String formatDisplay(String typeLabel, Integer cardNum, Map<Integer, String> userMap, String content, long timestamp) {
        String userName = userMap.getOrDefault(cardNum, "未知用户");
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));
        String truncatedContent = content.length() > 100 ? content.substring(0, 100) + "..." : content;
        return String.format("[%s] %s (%d) 于 %s:\n%s", typeLabel, userName, cardNum, date, truncatedContent);
    }
}