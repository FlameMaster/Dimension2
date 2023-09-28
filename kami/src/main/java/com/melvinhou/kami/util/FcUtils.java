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
import android.widget.TextView;
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
 * = 分 类 说 明：常用的工具类
 * ================================================
 */
public class FcUtils {

    /**
     * 本应用的全局上下文
     */
    private static Context mContext;
    /**
     * 主线程已经初始化之后的handler
     */
    private static Handler mHandler;
    /**
     * 主线程id
     */
    private static int mMainThreadID;
    /**
     * 输入帮助
     */
    private static InputMethodManager mInputMannager;


    /**
     * 使用工具类需要先注入全局数据
     * 防止使用别人application时参数无法调用
     *
     * @param context
     * @param mainThreadId
     * @param handler
     * @param inputMethodManager
     */
    public static void inject(Context context, int mainThreadId, Handler handler, InputMethodManager inputMethodManager) {
        mContext = context;
        mMainThreadID = mainThreadId;
        mHandler = handler;
        mInputMannager = inputMethodManager;
    }


    /**
     * @return 获取全局的Context
     */
    public static Context getContext() {
        return mContext;
    }

    /**
     * @return 获取主线程的handler
     */
    public static Handler getHandler() {
        return mHandler;
    }

    /**
     * 获取主线程id
     *
     * @return 主线程id
     */
    public static int getMainThreadID() {
        return mMainThreadID;
    }

    /**
     * 打印一个toast
     *
     * @param tost
     */
    public static void showToast(String tost) {
        runOnUiThread(() -> {
            Toast toast = Toast.makeText(getContext(), tost, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);//设置位置
            toast.show();
        });
    }

    /**
     * 打印toast
     * @param message
     * @param isLong
     */
    private static void showToast(final String message, boolean isLong) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(getContext(), message,
                        isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
                // 解决各个手机系统 toast 文字对齐方式不一致的问题
                View view = toast.getView();
                // 红米手机上可能为空
                if (view != null) {
                    TextView textView = view.findViewById(android.R.id.message);
                    if (textView != null) {
                        textView.setGravity(Gravity.CENTER);
                    }
                }
                toast.show();
            }
        });
    }

    /**
     * 获取输入管理
     *
     * @return
     */
    public static InputMethodManager getmInputMannager() {
        return mInputMannager;
    }

    /**
     * 判断当前线程是否是主线程
     *
     * @return 判断结果
     */
    public static boolean isrunOnUiThread() {

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
    public static void runOnUiThread(Runnable r) {
        if (isrunOnUiThread()) {
            // 已经是主线程, 直接运行
            r.run();
        } else {
            // 如果是子线程, 借助handler让其运行在主线程
            getHandler().post(r);
        }
    }
}
