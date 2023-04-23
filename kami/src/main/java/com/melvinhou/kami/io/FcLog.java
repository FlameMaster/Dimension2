package com.melvinhou.kami.io;

import android.text.TextUtils;
import android.util.Log;

import com.melvinhou.kami.BuildConfig;

public class FcLog {
    private static final String TAG = FcLog.class.getSimpleName();

    public static final int LOG_LEVEL_OFF = 0;
    public static final int LOG_LEVEL_VERBOSE = 2;
    public static final int LOG_LEVEL_DEBUG = 3;
    public static final int LOG_LEVEL_INFO = 4;
    public static final int LOG_LEVEL_WARN = 5;
    public static final int LOG_LEVEL_ERROR = 6;


    /**
     * 打印INFO级别日志
     *
     * @param strTag  TAG
     * @param strInfo 消息
     */
    public static void v(String strTag, String strInfo) {
        log(LOG_LEVEL_VERBOSE, strTag, strInfo);
    }

    /**
     * 打印DEBUG级别日志
     *
     * @param strTag  TAG
     * @param strInfo 消息
     */
    public static void d(String strTag, String strInfo) {
        log(LOG_LEVEL_DEBUG, strTag, strInfo);
    }

    /**
     * 打印INFO级别日志
     *
     * @param strTag  TAG
     * @param strInfo 消息
     */
    public static void i(String strTag, String strInfo) {
        log(LOG_LEVEL_INFO, strTag, strInfo);
    }

    /**
     * 打印WARN级别日志
     *
     * @param strTag  TAG
     * @param strInfo 消息
     */
    public static void w(String strTag, String strInfo) {
        log(LOG_LEVEL_WARN, strTag, strInfo);
    }

    /**
     * 打印WARN级别日志
     *
     * @param strTag  TAG
     * @param strInfo 消息
     */
    public static void w(String strTag, String strInfo, Throwable e) {
        w(strTag, strInfo + e.getMessage());
    }

    /**
     * 打印ERROR级别日志
     *
     * @param strTag  TAG
     * @param strInfo 消息
     */
    public static void e(String strTag, String strInfo) {
        log(LOG_LEVEL_ERROR, strTag, strInfo);
    }

    private static void log(int logLevel, String strTag, String strInfo) {
        if (logLevel < LOG_LEVEL_OFF || logLevel > LOG_LEVEL_ERROR) {
            e(TAG, "invalid logLevel： " + logLevel);
            return;
        }

        if (TextUtils.isEmpty(strTag)) {
            e(TAG, "empty logTag");
            return;
        }

        if (TextUtils.isEmpty(strInfo)) {
            e(TAG, "empty logContent");
            return;
        }

        if (BuildConfig.DEBUG)
            nativeWriteLog(logLevel, strTag, "", 0, strInfo);
    }

    /**
     * 打印异常堆栈信息
     *
     * @param strTag
     * @param strInfo
     * @param tr
     */
    public static void writeException(String strTag, String strInfo, Throwable tr) {
        Log.e(strTag, strInfo + " exception : " + Log.getStackTraceString(tr));
    }

//    protected static native void nativeWriteLog(int level, String fileName, String funcName, int line, String logContent);
    protected static void nativeWriteLog(int level, String fileName, String funcName, int line, String logContent){
        Log.println(level,fileName,logContent);
    }
}
