package com.melvinhou.kami.util;

import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/6/24 20:01
 * <p>
 * = 分 类 说 明：终端工具
 * ================================================
 */
public class DeviceUtils {


    /*华为全面屏判断*/
    public static boolean hasNotchAtHuawei() {
        boolean ret = false;
        try {
            ClassLoader classLoader = FcUtils.getContext().getClassLoader();
            Class HwNotchSizeUtil = classLoader.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method get = HwNotchSizeUtil.getMethod("hasNotchInScreen");
            ret = (boolean) get.invoke(HwNotchSizeUtil);
        } catch (ClassNotFoundException e) {
            Log.e("Notch", "hasNotchAtHuawei ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            Log.e("Notch", "hasNotchAtHuawei NoSuchMethodException");
        } catch (Exception e) {
            Log.e("Notch", "hasNotchAtHuawei Exception");
        } finally {
            return ret;
        }
    }

    //获取刘海尺寸：width、height
    //int[0]值为刘海宽度 int[1]值为刘海高度
    public static int[] getNotchSizeAtHuawei() {
        int[] ret = new int[]{0, 0};
        try {
            ClassLoader cl = FcUtils.getContext().getClassLoader();
            Class HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method get = HwNotchSizeUtil.getMethod("getNotchSize");
            ret = (int[]) get.invoke(HwNotchSizeUtil);
        } catch (ClassNotFoundException e) {
            Log.e("Notch", "getNotchSizeAtHuawei ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            Log.e("Notch", "getNotchSizeAtHuawei NoSuchMethodException");
        } catch (Exception e) {
            Log.e("Notch", "getNotchSizeAtHuawei Exception");
        } finally {
            return ret;
        }
    }

    public static final int VIVO_NOTCH = 0x00000020;//是否有刘海
    public static final int VIVO_FILLET = 0x00000008;//是否有圆角

    //vivo
    public static boolean hasNotchAtVivo() {
        boolean ret = false;
        try {
            ClassLoader classLoader = FcUtils.getContext().getClassLoader();
            Class FtFeature = classLoader.loadClass("android.util.FtFeature");
            Method method = FtFeature.getMethod("isFeatureSupport", int.class);
            ret = (boolean) method.invoke(FtFeature, VIVO_NOTCH);
        } catch (ClassNotFoundException e) {
            Log.e("Notch", "hasNotchAtVivo ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            Log.e("Notch", "hasNotchAtVivo NoSuchMethodException");
        } catch (Exception e) {
            Log.e("Notch", "hasNotchAtVivo Exception");
        } finally {
            return ret;
        }
    }

    //oppo
    public static boolean hasNotchAtOPPO() {
        return FcUtils.getContext().getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
    }

    //小米
    public static boolean hasNotchAtXiaoMi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.Global.getInt(FcUtils.getContext().getContentResolver(), "force_black", 0) == 1;
        }
        return false;
    }

    //小米刘海高度
    public static int getStatusBarHeight() {
        int statusBarHeight = 0;
        int resourceId = FcUtils.getContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = FcUtils.getContext().getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }


    /*获取系统用户是否开启屏幕旋转：1表示已开启，0表示未开启*/
    public static boolean isOpenScreenRotate() {
        boolean isOpen = false;
        try {
            int screenchange = Settings.System.getInt(FcUtils.getContext().getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION);

            isOpen = screenchange != 0;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        } finally {
            return isOpen;
        }
    }
}
