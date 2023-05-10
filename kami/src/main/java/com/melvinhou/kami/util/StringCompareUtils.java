package com.melvinhou.kami.util;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class StringCompareUtils {

    //正则表达式：验证用户名*/
    public static final String REGEX_USERNAME = "^[a-zA-Z]\\w{5,17}$";
    // 正则表达式：验证密码*/
//    public static final String REGEX_PASSWORD = "^[a-zA-Z0-9]{6,16}$";
    public static final String REGEX_PASSWORD = "^[!-~]{6,20}$";
    // 正则表达式：验证手机号*/
    public static final String REGEX_MOBILE = "^((13[0-9])|(15[^4,\\D])|(17[0-9])|(18[0-9]))\\d{8}$";
    //正则表达式：验证邮箱*/
    public static final String REGEX_EMAIL = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
    //正则表达式：验证汉字*/
    public static final String REGEX_CHINESE = "^[\\u4e00-\\u9fa5],{0,}$";
    //正则表达式：验证身份证*/
    public static final String REGEX_ID_CARD = "(^[1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$)|(^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}$)";
    //正则表达式：验证URL*/
    public static final String REGEX_URL = "http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?";
    //正则表达式：验证IP地址*/
    public static final String REGEX_IP_ADDR = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)";
    public static final String REGEX_IP_ADDR2 = "(?<=(\\b|\\D))(((\\d{1,2})|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))\\.){3}((\\d{1,2})|(1\\d{2})|(2[0-4]\\d)|(25[0-5]))(?=(\\b|\\D))";
    //正则表达式：企业信用代码
    public static final String  REGEX_QYCODE = "[1-9NY]{1}[1-9]{1}[1-6]{1}[0-9]{5}[0123456789ABCDEFGHJKLMNPQRTUWXYabcsefghjklmnpqrtuwxy]{10}";



    /**
     * 判断字符串是否为null或长度为0
     *
     * @param s 待校验字符串
     * @return {@code true}: 空<br> {@code false}: 不为空
     */
    public static boolean isEmpty(CharSequence s) {
        return s == null || s.length() == 0;
    }

    /**
     * 判断字符串是否为null或全为空格
     *
     * @param s 待校验字符串
     * @return {@code true}: null或全空格<br> {@code false}: 不为null且不全空格
     */
    public static boolean isSpace(String s) {
        return (s == null || s.trim().length() == 0 || s.equals("") || s.equals("null"));
    }
    //非空判断*/
    public static boolean nonEmpty(String str) {
        return StringUtils.nonEmpty(str);
    }

    public static boolean isEmpty(String str) {
        return StringUtils.isEmpty(str);
    }

    // 判断两个字符串的大小，前>后返回true
    public static boolean compare(String noumenon, String comparison) {
        float i, j;
        i = StringUtils.getFloat(noumenon);
        j = StringUtils.getFloat(comparison);
        return i > j;
    }


//***********************************输入字符校验*********************************************//



    //校验特殊字符
    public static boolean isConSpeChar(String str) {
        if (TextUtils.isEmpty(str)) return false;
        String regEx = "[`~!@#$%^&*()+=|{}':;'\\[\\]<>/?~！@#￥%……&*——+|{}【】○●★☆☉♀♂※¤╬の〆]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.find();
    }

    //用正则表达式判断密码格式
    public static boolean isPassword(String str) {
        return Pattern.matches(REGEX_PASSWORD, str);
    }

    //验证邮箱
    public static boolean isEmail(String email) {
        boolean flag = false;
        try {
            Pattern regex = Pattern.compile(REGEX_EMAIL);
            Matcher matcher = regex.matcher(email);
            flag = matcher.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    //用正则表达式判断手机号码
    public static boolean isPhone(String str) {
        return Pattern.matches(REGEX_MOBILE, str);
    }

    //用正则表达式判断身份证格式
    public static boolean isIDCard(String str) {
        return Pattern.matches(REGEX_ID_CARD, str);
    }

    //用正则表达式判断企业信用码
    public static boolean isQyCode(String str) {
        return Pattern.matches(REGEX_QYCODE, str);
    }

    //用正则表达式判断汉字
    public static boolean isChinese(String str) {
        return Pattern.matches(REGEX_CHINESE, str);
    }

    //是否是汉字
    public static boolean isChinese2(String str) {
        int n = 0;
        for (int i = 0; i < str.length(); i++) {
            n = (int) str.charAt(i);
            if (!(19968 <= n && n < 40869)) {
                return false;
            }
        }
        return true;
    }

    //判断是否为汉字
    public static boolean isChinese3(String str) {

        char[] chars = str.toCharArray();
        boolean isGB2312 = false;
        for (int i = 0; i < chars.length; i++) {
            byte[] bytes = ("" + chars[i]).getBytes();
            if (bytes.length == 2) {
                int[] ints = new int[2];
                ints[0] = bytes[0] & 0xff;
                ints[1] = bytes[1] & 0xff;
                if (ints[0] >= 0x81 && ints[0] <= 0xFE && ints[1] >= 0x40
                        && ints[1] <= 0xFE) {
                    isGB2312 = true;
                    break;
                }
            }
        }
        return isGB2312;
    }

    //判断是否有汉字
    public static boolean hasChinese(String countname) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(countname);
        if (m.find()) {
            return true;
        }
        return false;
    }

    //判断字符串是否仅为数字*/
    public static boolean isNumeric1(String str) {
        for (int i = str.length(); --i >= 0; ) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    //用正则表达式判断字符串是否仅为数字
    public static boolean isNumeric2(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    //用ascii码判断字符串是否仅为数字*/
    public static boolean isNumeric3(String str) {
        for (int i = str.length(); --i >= 0; ) {
            int chr = str.charAt(i);
            if (chr < 48 || chr > 57)
                return false;
        }
        return true;
    }

    //判断一个字符串的首字符是否为字母
    public static boolean test(String s) {
        char c = s.charAt(0);
        int i = (int) c;
        if ((i >= 65 && i <= 90) || (i >= 97 && i <= 122)) {
            return true;
        } else {
            return false;
        }
    }

    //判断一个字符串的首字符是否为字母
    public static boolean check(String fstrData) {
        char c = fstrData.charAt(0);
        if (((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) {
            return true;
        } else {
            return false;
        }
    }

    //用正则表达式判断ip地址格式
    public static boolean isIp(String str) {
        return Pattern.matches(REGEX_IP_ADDR2, str);
    }



//***********************************文件格式判断*********************************************//



    //判断url地址是否是图片*/
    public static boolean isImageFile(String path) {
        if (!nonEmpty(path)) return false;
        else if (path.toLowerCase().endsWith(".jpg")
                | path.toLowerCase().endsWith(".png")
                | path.toLowerCase().endsWith(".jpeg")
                | path.toLowerCase().endsWith(".gif")
                | path.toLowerCase().endsWith(".bmp")
                | path.toLowerCase().endsWith(".svg")
                | path.toLowerCase().endsWith(".webp")) return true;
        return false;
    }

    public static boolean isTxtFile(String path) {
        if (TextUtils.isEmpty(path)) return false;
        else if (path.toLowerCase().endsWith(".txt")) return true;
        return false;
    }

    public static boolean isApkFile(String path) {
        if (TextUtils.isEmpty(path)) return false;
        else if (path.toLowerCase().endsWith(".apk")) return true;
        return false;
    }

    public static boolean isVideoFile(String path) {
        if (TextUtils.isEmpty(path)) return false;
        else if (path.toLowerCase().endsWith(".mp4")
                | path.toLowerCase().endsWith(".3gp")
                | path.toLowerCase().endsWith(".rmvb")
                | path.toLowerCase().endsWith(".mkv")
                | path.toLowerCase().endsWith(".avi")
                | path.toLowerCase().endsWith(".mov")) return true;
        return false;
    }

    public static boolean isAudioFile(String path) {
        if (TextUtils.isEmpty(path)) return false;
        else if (path.toLowerCase().endsWith(".mp3")
                | path.toLowerCase().endsWith(".m4a")
                | path.toLowerCase().endsWith(".aac")
                | path.toLowerCase().endsWith(".wav")
                | path.toLowerCase().endsWith(".wma")) return true;
        return false;
    }

    public static boolean isHtmlFile(String path) {
        if (TextUtils.isEmpty(path)) return false;
        else if (path.toLowerCase().endsWith(".html")) return true;
        return false;
    }

    public static boolean isPdfFile(String path) {
        if (TextUtils.isEmpty(path)) return false;
        else if (path.toLowerCase().endsWith(".pdf")) return true;
        return false;
    }

    public static boolean isExcelFile(String path) {
        if (TextUtils.isEmpty(path)) return false;
        else if (path.toLowerCase().endsWith(".xls")
                | path.toLowerCase().endsWith(".xlsx")) return true;
        return false;
    }

    public static boolean isWordFile(String path) {
        if (TextUtils.isEmpty(path)) return false;
        else if (path.toLowerCase().endsWith(".doc")
                | path.toLowerCase().endsWith(".docx")) return true;
        return false;
    }
}
