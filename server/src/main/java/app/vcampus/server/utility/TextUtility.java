package app.vcampus.server.utility;

/**
 * 文本工具类。
 */
public class TextUtility {
    /**
     * 将数字转换为中文星期。
     *
     * @param num 要转换的数字 (1-7)。
     * @return 中文星期字符串 ("一"-"日")。
     */
    public static String intToChineseWeek(Integer num) {
        return switch (num) {
            case 1 -> "一";
            case 2 -> "二";
            case 3 -> "三";
            case 4 -> "四";
            case 5 -> "五";
            case 6 -> "六";
            case 7 -> "日";
            default -> "";
        };
    }
}