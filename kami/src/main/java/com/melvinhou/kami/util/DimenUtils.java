package com.melvinhou.kami.util;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import java.lang.reflect.Method;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/6/24 19:36
 * <p>
 * = 分 类 说 明：尺寸相关工具类
 * ================================================
 */
public class DimenUtils {


    /**
     * dp值转像素值
     *
     * @param dp dp值
     * @return 像素值
     */
    public static int dp2px(int dp) {
        return (int) (dp * ResourcesUtils.getResources().getDisplayMetrics().density + 0.5f);
    }

    /**
     * sp值转像素值
     *
     * @param sp sp值
     * @return 像素值
     */
    public static int sp2px(int sp) {
        return (int) (sp * ResourcesUtils.getResources().getDisplayMetrics().scaledDensity + 0.5f);
    }

    /**
     * 像素值转大潘值
     *
     * @param px 像素值
     * @return dp值
     */
    public static float px2dp(int px) {
        return px / ResourcesUtils.getResources().getDisplayMetrics().density;
    }



    /**
     * @return 获得屏幕的宽度高度,不一定准确
     */
    public static int[] getScreenSize() {

        WindowManager wm = (WindowManager) FcUtils.getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealMetrics(outMetrics);
        }else wm.getDefaultDisplay().getMetrics(outMetrics);

        int[] pos = new int[2];
        pos[0] = outMetrics.widthPixels;
        pos[1] = outMetrics.heightPixels;

        return pos;
    }


    /**
     * 获取状态栏高度
     *
     * @return
     */
    public static int getStatusBarHeight() {
        int result = 0;
        int resourceId = FcUtils.getContext().getResources().getIdentifier("status_bar_height", "dimen",
                "android");
        if (resourceId > 0) {
            result = FcUtils.getContext().getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 获取导航栏高度
     *
     * @return
     */
    public static int getNavigationHeight() {
        if (!DeviceUtils.hasNavBar())return 0;
//        TypedArray actionbarSizeTypedArray = FcUtils.getContext()
//                    .obtainStyledAttributes(new int[]{android.R.attr.actionBarSize});
//            float h = actionbarSizeTypedArray.getDimension(0, 0);
        int resourceId;
        int rid = FcUtils.getContext().getResources().getIdentifier("config_showNavigationBar",
                "bool", "android");
        if (rid != 0) {
            resourceId = FcUtils.getContext().getResources().getIdentifier("navigation_bar_height",
                    "dimen", "android");
            //全面屏判断
            String manufacturer = Build.MANUFACTURER;
            // 这个字符串可以自己定义,例如判断华为就填写huawei,魅族就填写meizu
            if ("xiaomi".equalsIgnoreCase(manufacturer)) {
                // true 是手势，默认是 false
                boolean isFull = false;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    isFull = Settings.Global.getInt(FcUtils.getContext().getContentResolver(), "force_fsg_nav_bar", 0) != 0;
                }
                if (isFull) return 0;
            }
            return FcUtils.getContext().getResources().getDimensionPixelSize(resourceId);
        } else
            return 0;
    }

    /**
     * 获取BAR高度
     *
     * @return
     */
    public static int getActionBarSize() {
        TypedValue typedValue = new TypedValue();
        if (FcUtils.getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true))
            return TypedValue.complexToDimensionPixelSize(typedValue.data, ResourcesUtils.getResources().getDisplayMetrics());
        return dp2px(48);
    }
}
