package com.melvinhou.kami;

import android.util.Log;

import com.melvinhou.kami.util.AppUtils;
import com.melvinhou.kami.util.FcUtils;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2017/5/24 14:49
 * <p>
 * = 分 类 说 明：整个应用的异常捕获
 * ================================================
 */
public class BaseException implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable ex) {

        //对错误进行处理
        try {
           // 此方法在捕获异常后去做调用
            Log.e(getClass().toString(), "全局异常捕获");
            // 打印异常
            ex.printStackTrace();

//            final String name = "ERROR_YOULIAO_" + IOUtil.getFileNameForTime();
//            //获取错误日志
//            String path = IOUtil.getDirPath(IOUtil.ROOT_NAME, IOUtil.TYPE_PATH_LOG) + File.separator + name + ".log";

//            final File log = new File(path);
            // 将错误日志写入到sd卡中
//            PrintWriter printWriter = new PrintWriter(log);
//            ex.printStackTrace(printWriter);
//            printWriter.close();

            //上传错误日志
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            Log.e(FcUtils.getContext().getPackageName(),"退出程序");
            //退出程序
            AppUtils.closeApp();
        }

    }
}
