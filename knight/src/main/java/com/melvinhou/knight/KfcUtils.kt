package com.melvinhou.knight

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import androidx.core.app.NotificationManagerCompat
import com.melvinhou.kami.util.FcUtils
import java.text.DecimalFormat


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/1/30 0030 15:42
 * <p>
 * = 分 类 说 明：适用于kotlin的工具类
 * ================================================
 */
object KfcUtils {


    /**
     * 获取渠道名
     *
     * @return 如果没有获取成功，那么返回值为空
     */
    fun getChannelName(key: String): String? {
        var channelName: String? = null
        try {
            val packageManager = FcUtils.getContext().packageManager
            if (packageManager != null) {
                //注意此处为ApplicationInfo 而不是 ActivityInfo,因为友盟设置的meta-data是在application标签中，
                // 而不是某activity标签中，所以用ApplicationInfo
                val applicationInfo = packageManager.getApplicationInfo(
                    FcUtils.getContext().packageName, PackageManager.GET_META_DATA
                )
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        channelName = applicationInfo.metaData.getString(key)
                    }
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } finally {
            return channelName
        }
    }


    /**
     * 系统层面通知开关有没有开启
     * Build.VERSION.SDK_INT >= 24
     * Build.VERSION.SDK_INT >= 19
     *
     * @param mContext
     * @return
     */
    fun checkNotifyPermission(context: Context): Boolean {
        val manager: NotificationManagerCompat = NotificationManagerCompat.from(context)
        return manager.areNotificationsEnabled()
    }

    /**
     * 如果通知未打开 跳转到通知设定界面
     * @param mContext
     */
    fun tryJumpNotifyPage(context: Context) {
        val intent = Intent()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                intent.putExtra("app_package", context.packageName)
                intent.putExtra("app_uid", context.applicationInfo.uid)
            } else {
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.data = Uri.parse("package:" + context.packageName)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            context.startActivity(intent)
        }
    }


    /**
     * 获取自己应用内部的版本号
     */
    fun getVersionCode(context: Context): Int {
        val manager: PackageManager = context.packageManager
        var code = 0
        try {
            val info: PackageInfo = manager.getPackageInfo(context.packageName, 0)
            code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                info.longVersionCode.toInt()
            else
                info.versionCode

        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return code
    }


    /**
     * 获取自己应用内部的版本名
     */
    fun getVersionName(context: Context): String {
        val manager: PackageManager = context.packageManager
        var code = ""
        try {
            val info: PackageInfo = manager.getPackageInfo(context.packageName, 0)
            code = info.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return code
    }

    //手机号脱敏
    fun getMaskMobile(mobileStr: String?): String {
        var mobile = "*********"
        mobileStr?.let {
            mobile = it
            if (it.length > 10) {
                mobile = "${it.substring(0, 3)}****${
                    it.substring(7, it.length)
                }"
            }
        }
        return mobile
    }


    //价格
    fun getPrice(price: Double, isUnit: Boolean): String {
        val format = DecimalFormat("##0.00")
        if (isUnit)
            return "¥ ${format.format(price)}"
        else
            return format.format(price)
    }

    //价格
    fun getPrice(price: Float, isUnit: Boolean): String {
        val format = DecimalFormat("##0.00")
        if (isUnit)
            return "¥ ${format.format(price)}"
        else
            return format.format(price)
    }

    //价格
    fun getPrice(priceStr: String?, isUnit: Boolean): String {
        var price = 0f
        try {
            price = priceStr?.toFloat() ?: 0f
        } finally {
            return getPrice(price, isUnit)
        }
    }

    //价格
    fun getBackCardNumber(numberStr: String?): String? {
        if (TextUtils.isEmpty(numberStr)) return numberStr
        val buffer = StringBuffer()
        val size = numberStr!!.length
        for (i in 0..numberStr.length / 4) {
            val start = i * 4
            val end = if (start + 4 >= numberStr.length) numberStr.length else start + 4
            buffer
                .append(numberStr.substring(start, end))
                .append(" ")
        }
        return buffer.substring(0, buffer.length - 2)
    }


    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @return true 表示开启
     */
    fun isOpenLocation(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // 经过GPS卫星定位，定位级别能够精确到街（经过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        val gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        // 经过WLAN或移动网络(3G/2G)肯定的位置（也称做AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        val network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return gps || network
    }


    /**
     * 显示时间
     * @param 时间（秒）
     */
    fun getTime(timeMs: Long): String {
        val day = (timeMs / 60 / 60 / 24).toInt()
        val hour = (timeMs / 60 / 60 % 24).toInt()
        val min = (timeMs / 60 % 60).toInt()
        val s = (timeMs % 60).toInt()
        return "${String.format("%02d", hour)}：${String.format("%02d", min)}：${
            String.format("%02d", s)
        }"
    }

    /**
     * 显示时间
     * @param 时间（秒）
     */
    fun getTime1(timeMs: Long): String {
        val day = (timeMs / 60 / 60 / 24).toInt()
        val hour = (timeMs / 60 / 60 % 24).toInt()
        val min = (timeMs / 60 % 60).toInt()
        val s = (timeMs % 60).toInt()
        return "${day}天${String.format("%02d", hour)}小时${String.format("%02d", min)}分${
            String.format("%02d", s)
        }秒"
    }
}