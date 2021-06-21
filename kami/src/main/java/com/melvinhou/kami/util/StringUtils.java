package com.melvinhou.kami.util;


import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p/>
 * = 版 权 所 有：melvinhou@163.com
 * <p/>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p/>
 * = 时 间：2016/5/5 17:46
 * <p/>
 * = 分 类 说 明：字符串工具类
 * ================================================
 */
public class StringUtils {


    //非空判断*/
    public static boolean noNull(String str) {
        if (str == null || str.equals("") || str.equals("null"))
            return false;
        return true;
    }

    public static boolean isEmpty(String str) {
        return TextUtils.isEmpty(str);
    }

    //流转字符串
    public static String bytes2hex02(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        String tmp = null;
        for (byte b : bytes) {
            // 将每个字节与0xFF进行与运算，然后转化为10进制，然后借助于Integer再转化为16进制
            tmp = Integer.toHexString(0xFF & b);
            if (tmp.length() == 1)// 每个字节8为，转为16进制标志，2个16进制位
            {
                tmp = "0" + tmp;
            }
            sb.append(tmp);
        }

        return sb.toString();

    }

    //替换操作*/
    public static String rereplace(@NonNull String root, @NonNull String oldS, String newS) {
        return root.replace(oldS, newS);
    }

    //替换操作*/
    public static boolean equals(@NonNull String oldS, @NonNull String newS) {
        return oldS.equals(newS);
    }


    //利用签名辅助类，将字符串字节数组
    public static String md5(String str) {
        byte[] digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            digest = md.digest(str.getBytes());
            return bytes2hex02(digest);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    //根据明文生成md5密文
    public static String encode(String str) {
        return encode(str, "UTF-8");
    }

    public static String encode(String str, String charset) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes(charset));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] byteArray = messageDigest.digest();

        StringBuffer md5StrBuff = new StringBuffer();

        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                md5StrBuff.append("0").append(
                        Integer.toHexString(0xFF & byteArray[i]));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
        }

        return md5StrBuff.toString();
    }


    //string转float
    public static float getFloat(String number) {
        if (StringUtils.noNull(number))
            return Float.valueOf(number);
        else return -1;
    }

    //string转int*/
    public static int getInteger(String number, int defaultValue) {
        int value = defaultValue;
        try {
            if (StringUtils.noNull(number))
                value = Integer.valueOf(number);
        } finally {
            return value;
        }
    }


    /**
     * 方法用途: 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序），并且生成url参数串<br>
     * 实现步骤: <br>
     *
     * @param paraMap    要排序的Map对象
     * @param urlEncode  是否需要URLENCODE
     * @param keyToLower 是否需要将Key转换为全小写
     *                   true:key转化成小写，false:不转化
     * @return
     */
    public static String formatUrlMap(Map<String, String> paraMap,
                                      boolean urlEncode, boolean keyToLower) {

        String buff = null;
        Map<String, String> tmpMap = paraMap;
        try {
//            List<Map.Entry<String, String>> infoIds = new ArrayList<>(tmpMap.entrySet());
            List<Map.Entry<String, String>> infoIds = new ArrayList<>();
            for (Map.Entry<String, String> entry : tmpMap.entrySet()) {
                infoIds.add(entry);
            }
            // 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序）***需要TreeMap支持
            Collections.sort(infoIds, new Comparator<Map.Entry<String, String>>() {
                @Override
                public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                    return o1.getKey().compareTo(o2.getKey());
                }
            });

            // 构造URL 键值对的格式
            StringBuilder buf = new StringBuilder();
            for (Map.Entry<String, String> item : infoIds) {
                if (noNull(item.getKey())) {
                    String key = item.getKey();
                    String val = item.getValue();
                    if (urlEncode) {
                        val = URLEncoder.encode(val, "utf-8");
                    }
                    if (keyToLower) {
                        buf.append(key.toLowerCase() + "=" + val);
                    } else {
                        buf.append(key + "=" + val);
                    }
                    buf.append("&");
                }
            }
            buff = buf.toString();
            Log.e("formatUrlMap", "buff ：" + buff);
            if (buff.isEmpty() == false) {
                buff = buff.substring(0, buff.length() - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return buff;
        }
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
}
