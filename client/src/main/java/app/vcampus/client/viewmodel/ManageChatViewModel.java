
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

public class ManageChatViewModel {
    // 搜索条件属性，用于与Controller中的UI控件绑定
    public final StringProperty searchType = new SimpleStringProperty();
    public final BooleanProperty useNickname = new SimpleBooleanProperty();
    public final StringProperty nickname = new SimpleStringProperty();
    public final BooleanProperty useCardNum = new SimpleBooleanProperty();
    public final StringProperty cardNum = new SimpleStringProperty();
    public final BooleanProperty useContent = new SimpleBooleanProperty();
    public final StringProperty content = new SimpleStringProperty();
    public final StringProperty errorMessage = new SimpleStringProperty("");

    // 列表视图的数据源和当前选中项
    public final ObservableList<SearchResultItem> searchResults = FXCollections.observableArrayList();
    // public final ObjectProperty<SearchResultItem> selectedItem = new SimpleObjectProperty<>();

    /**
     * 内部类，用于封装显示在ListView中的每一项的数据。
     */
    public static class SearchResultItem {
        public final String type; // "message" or "comment"
        public final UUID id;
        public final String displayContent;

        public SearchResultItem(String type, UUID id, String displayContent) {
            this.type = type;
            this.id = id;
            this.displayContent = displayContent;
        }

        @Override
        public String toString() {
            // ListView 默认调用此方法来显示内容
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
            // MODIFIED: 更新错误信息属性，而不是打印到控制台
            errorMessage.set("请选择搜索类型 (消息 或 评论)");
            return;
        }

        // 从绑定的属性中构建搜索条件Map
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
            // 调用网关，直接获取封装好的 SearchResult 对象
            SearchResult result = ChatClient.getInstance().search(type.toLowerCase(), criteria);

            Map<Integer, String> userMap = result.getUserMap();
            searchResults.clear();

            if (result.getResults().isEmpty()) {
                errorMessage.set("未找到符合条件的结果");
            }

            // 遍历已解析好的对象列表，并转换为UI上要显示的 SearchResultItem
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
            // 可以在此显示一个UI错误提示
        }
    }

    /**
     * MODIFIED: 删除一个指定的项。
     * @param itemToDelete 要删除的 SearchResultItem 对象。
     */
    public void deleteItem(SearchResultItem itemToDelete) {
        errorMessage.set("");

        if (itemToDelete == null) {
            errorMessage.set("内部错误: 尝试删除一个空项目。");
            return;
        }

        try {
            // 调用网关删除后端的记录
            ChatClient.getInstance().delete(itemToDelete.type, itemToDelete.id);
            // 从界面上的列表移除该项
            searchResults.remove(itemToDelete);
        } catch (IOException e) {
            e.printStackTrace();
            errorMessage.set("删除失败: " + e.getMessage());
        }
    }

    /**
     * 辅助方法，用于格式化最终显示在列表中的字符串。
     * @param typeLabel "消息" 或 "评论"
     * @param cardNum 发送者卡号
     * @param userMap 卡号到用户名的映射
     * @param content 内容
     * @param timestamp 时间戳
     * @return 格式化后的字符串
     */
    private String formatDisplay(String typeLabel, Integer cardNum, Map<Integer, String> userMap, String content, long timestamp) {
        String userName = userMap.getOrDefault(cardNum, "未知用户");
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));
        // 限制内容长度，防止UI过长
        String truncatedContent = content.length() > 100 ? content.substring(0, 100) + "..." : content;
        return String.format("[%s] %s (%d) 于 %s:\n%s", typeLabel, userName, cardNum, date, truncatedContent);
    }
}