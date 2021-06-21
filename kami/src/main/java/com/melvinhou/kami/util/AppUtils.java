package com.melvinhou.kami.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/6/24 19:35
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class AppUtils {


    /**
     * @return 获取当前app信息
     */
    public static PackageInfo getAppInfo() {
        PackageInfo info = null;
        try {
            PackageManager packageManager = FcUtils.getContext().getPackageManager();
            info = packageManager.getPackageInfo(FcUtils.getContext().getPackageName(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return info;
        }
    }

    /**
     * @return 获取当前应用的版本号
     */
    public static int getVersion() {
        PackageInfo info = getAppInfo();
        if (info != null)
            return info.versionCode;
        return -1;
    }

    /**
     * @return获取当前版本
     */
    public static String getAppVersionName() {
        PackageInfo info = getAppInfo();
        if (info != null)
            return info.versionName;
        return null;
    }


    /**
     * 获取设备唯一标示码
     *
     * @return
     */
    public static String getMachineID() {
        String id = null;
        if (!FcUtils.lacksPermission(Manifest.permission.READ_PHONE_STATE)) {
            id = getDeviceId();
        } else {
            id = Settings.System.getString(FcUtils.getContext().getContentResolver(), Settings.System.ANDROID_ID);
            if (TextUtils.isEmpty(id))
                id = Settings.System.getString(FcUtils.getContext().getContentResolver(), Build.SERIAL);
            if (TextUtils.isEmpty(id))
                id = Settings.System.getString(FcUtils.getContext().getContentResolver(), Build.FINGERPRINT);
        }
        return "Android#" + android.os.Build.BRAND + "#" + android.os.Build.MODEL + "#" + id;
    }

    /**
     * 获取deviceID
     * @return
     */
    @SuppressLint("MissingPermission")
    public static String getDeviceId() {
        TelephonyManager tm = (TelephonyManager) FcUtils.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }


    /**
     * 获取渠道名
     *
     * @return 如果没有获取成功，那么返回值为空
     */
    public static String getChannelName(String key) {
        String channelName = null;
        try {
            PackageManager packageManager = FcUtils.getContext().getPackageManager();
            if (packageManager != null) {
                //注意此处为ApplicationInfo 而不是 ActivityInfo,因为友盟设置的meta-data是在application标签中，
                // 而不是某activity标签中，所以用ApplicationInfo
                ApplicationInfo applicationInfo =
                        packageManager.getApplicationInfo(
                                FcUtils.getContext().getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        channelName = applicationInfo.metaData.getString(key);
                    }
                }

            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } finally {
            return channelName;
        }
    }
}
