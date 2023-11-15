package com.melvinhou.kami.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/6/24 19:37
 * <p>
 * = 分 类 说 明：时间工具类
 * ================================================
 */
public class DateUtils {

    /**
     * @return 获取当前时间
     */
    public static long getCurrentTime() {
        return System.currentTimeMillis();
//        return new Date().getTime();
    }


    /**
     * 格式化时间：1970年0点为0
     * @param date 时间
     * @param pattern 转换后的格式
     * @return
     */
    public static String formatDuration(long date, String pattern) {
        return new SimpleDateFormat(pattern)
                .format(date);
    }


    /**
     * 格式化时间：1970年0点为0
     * @param pattern 转换后的格式
     * @return
     */
    public static String formatDuration( String pattern) {
        return new SimpleDateFormat(pattern)
                .format(getCurrentTime());
    }


    private static final int HOUR = 60 * 60 * 1000;
    private static final int MIN = 60 * 1000;
    private static final int SEC = 1000;

    /**
     * 将时间戳转换为 01:01:01 或 01:01 的格式
     */
    public static String formatDuration(int duartion) {
        // 计算小时数
        int hour = duartion / HOUR;

        // 计算分钟数
        int min = duartion % HOUR / MIN;

        // 计算秒数
        int sec = duartion % MIN / SEC;

        // 生成格式化字符串
        if (hour == 0) {
            // 不足一小时 01：01
            return String.format("%02d:%02d", min, sec);
        } else if (hour < 24) {
            // 大于一小时 01:01:01
            return String.format("%02d:%02d:%02d", hour, min, sec);
        } else
            return "max";//超出24小时的一般都是直播
    }

    /**
     * 计算年龄
     * @param birthday
     * @return
     */
    public static int getAge(long birthday){
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTimeInMillis(System.currentTimeMillis());
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.setTimeInMillis(birthday);//这个解析传进来的时间戳
        if (currentCalendar.get(Calendar.MONTH) >= targetCalendar.get(Calendar.MONTH)) {//如果现在的月份大于生日的月份
            return currentCalendar.get(Calendar.YEAR) - targetCalendar.get(Calendar.YEAR);//那就直接减,因为现在的年月都大于生日的年月
        } else {
            return currentCalendar.get(Calendar.YEAR) - targetCalendar.get(Calendar.YEAR) - 1;//否则,减掉一年
        }
    }
}
