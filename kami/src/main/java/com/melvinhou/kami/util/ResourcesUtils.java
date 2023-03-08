package com.melvinhou.kami.util;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/6/24 19:45
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class ResourcesUtils {

    /**
     * 获取资源文件管理者
     *
     * @return 本应用的Resources
     */
    public static Resources getResources() {
        return FcUtils.getContext().getResources();
    }

    /**
     * 获取资源目录的字符串
     *
     * @param R_ID 字符串的id
     * @return String字符串
     */
    public static String getString(int R_ID) {
        return getResources().getString(R_ID);
    }

    /**
     * 获取资源目录的字符串组
     *
     * @param R_ID 字符串组的id
     * @return String字符串组
     */
    public static String[] getStringArray(int R_ID) {
        return getResources().getStringArray(R_ID);
    }

    /**
     * 获取资源目录的颜色值
     *
     * @param R_ID 资源目录id
     * @return int类型的色值
     */
    public static int getColor(int R_ID) {
        return getResources().getColor(R_ID);
    }

    /**
     * 获取资源目录的相应主题的颜色值
     *
     * @param currentTheme 使用的主题
     * @param R_ID         资源id
     * @return int类型的色值
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static int getThemeColor(Resources.Theme currentTheme, int R_ID) {
        return getResources().getColor(R_ID, currentTheme);
    }

    /**
     * 获取资源目录的颜色状态选择器
     *
     * @param R_ID 资源id
     * @return 颜色状态选择器
     */
    public static ColorStateList getColorStateList(int R_ID) {
        return getResources().getColorStateList(R_ID);
    }

    /**
     * 获取资源目录的相应主题的颜色选择器
     *
     * @param currentTheme 使用的主题
     * @param R_ID         资源id
     * @return 颜色状态选择器
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static ColorStateList getThemeColorStateList(Resources.Theme currentTheme, int R_ID) {
        return getResources().getColorStateList(R_ID, currentTheme);
    }

    /**
     * 获取资源目录的尺寸
     *
     * @param R_ID 资源id
     * @return int类型的尺寸的像素值
     */
    public static int getDimen(int R_ID) {
        return getResources().getDimensionPixelSize(R_ID);
    }

    /**
     * 获取资源目录的drawable图片资源
     *
     * @param R_ID 资源id
     * @return 资源目录的drawable
     */
    public static Drawable getDrawable(int R_ID) {
        return getResources().getDrawable(R_ID);
    }

    /**
     * 获取资源目录相应主题的drawable图片
     *
     * @param currentTheme 相应主题
     * @param R_ID         资源id
     * @return 资源目录的drawable
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Drawable getThemeDrawable(Resources.Theme currentTheme, int R_ID) {
        return getResources().getDrawable(R_ID, currentTheme);
    }

    /**
     * 获取资源文件的uri
     * @param id
     * @return
     */
    public static  Uri getResourceUri(int id) {
        try {
            return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                    + getResources().getResourcePackageName(id) + '/'
                    + getResources().getResourceTypeName(id) + '/'
                    + getResources().getResourceEntryName(id));
        } catch (Resources.NotFoundException e) {
            return null;
        }
    }
}
