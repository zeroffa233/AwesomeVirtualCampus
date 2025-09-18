package app.vcampus.server.utility;

import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.AllArgsConstructor;

/**
 * 一个数据传输对象（DTO），用于封装从 ChatClient.search() 返回的复杂结果。
 */
@Data
@AllArgsConstructor
public class SearchResult {
    // 使用通配符 ? 来存储 Message 或 Comment 列表
    private final List<?> results;

    // 预先处理好的用户映射表 (CardNum -> UserName)
    private final Map<Integer, String> userMap;
}