package app.vcampus.server.utility;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期工具类。
 * 提供日期和字符串之间的转换功能。
 */
public class DateUtility {
    /**
     * 将 Date 对象格式化为 "yyyy-MM-dd" 格式的字符串。
     *
     * @param date 要格式化的 Date 对象。
     * @return 格式化后的日期字符串；如果输入为 null，则返回空字符串。
     */
    public static String fromDate(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    /**
     * 使用指定的格式将 Date 对象格式化为字符串。
     *
     * @param date   要格式化的 Date 对象。
     * @param format 格式化字符串 (例如, "yyyy-MM-dd HH:mm:ss")。
     * @return 格式化后的日期字符串；如果输入日期为 null，则返回空字符串。
     */
    public static String fromDate(Date date, String format) {
        if (date == null) return "";
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * 将 "yyyy-MM-dd" 格式的字符串解析为 Date 对象。
     *
     * @param date 要解析的日期字符串。
     * @return 解析后的 Date 对象；如果解析失败，则返回 null。
     */
    public static Date toDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (Exception e) {
            return null;
        }
    }
}