package com.melvinhou.kami.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.melvinhou.kami.BaseApplication;

import androidx.core.content.ContextCompat;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/6/24 19:29
 * <p>
 * = 分 类 说 明：比较通用的工具类
 * ================================================
 */
public class FcUtils {

    /**
     * @return 获取全局的Context
     */
    public static Context getContext() {
        return BaseApplication.getContext();
    }

    /**
     * @return 获取主线程的handler
     */
    public static Handler getHandler() {
        return BaseApplication.getHandler();
    }

    /**
     * 获取主线程id
     *
     * @return 主线程id
     */
    public static int getMainThreadID() {
        return BaseApplication.getMainThreadID();
    }


    /**
     * 结束app
     */
    public static void closeApp() {
        BaseApplication.getInstance().exit();
    }

    /**
     * 打印一个toast
     *
     * @param tost
     */
    public static void showToast(String tost) {
        Toast toast = Toast.makeText(getContext(), tost, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);//设置位置
        toast.show();
    }

    /**
     * 获取输入管理
     *
     * @return
     */
    public static InputMethodManager getmInputMannager() {
        return BaseApplication.getmInputMannager();
    }

    /* 判断是否缺少权限*/
    public static boolean lacksPermission(String permission) {
        return ContextCompat.checkSelfPermission(getContext(), permission) ==
                PackageManager.PERMISSION_DENIED;
    }


    /**
     *判断是否有网
     * @return
     */
    public static boolean isNetworkConnected() {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            //mNetworkInfo.isAvailable();
            return true;//有网
        }
        return false;//没有网
    }

    /**
     * 判断当前线程是否是主线程
     *
     * @return 判断结果
     */
    public static boolean isRunOnUIThread() {

        // 获取当前线程id
        int myTid = android.os.Process.myTid();
        //如果当前线程id和主线程id相同, 那么当前就是主线程
        if (myTid == getMainThreadID()) {
            return true;
        }

        return false;
    }

    /**
     * 把runnable运行在主线程上
     *
     * @param r 需要运行的runnable
     */
    public static void runOnUIThread(Runnable r) {
        if (isRunOnUIThread()) {
            // 已经是主线程, 直接运行
            r.run();
        } else {
            // 如果是子线程, 借助handler让其运行在主线程
            getHandler().post(r);
        }
    }



    /**
     * 加载资源目录的布局转成view
     *
     * @param R_ID 布局id
     * @return view
     */
    public static View inflate(int R_ID) {
        return View.inflate(getContext(), R_ID, null);
    }
}
