package com.melvinhou.kami.helper;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;


import com.melvinhou.kami.util.FcUtils;

import java.io.File;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2019/12/26 15:49
 * <p>
 * = 分 类 说 明：下载帮助类
 * ============================================================
 */
public class DownloadHelper {

    private static final String TAG = "DownloadHelper";
    private boolean debug = false;
    /*系统下载管理器*/
    private DownloadManager mDownloadManager;
    /* 下载ID*/
    private long mDownloadId;
    /*文件名*/
    private String fileName;
    /*文件下载地址*/
    private String downloadUrl;
    private boolean downloading;

    /*通知栏点击事件，点击进入下载详情*/
    private BroadcastReceiver mDownloadDetailsReceiver;
    /*下载监听*/
    private DownloadListener downloadListener;


    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mQueryProgressRunnable = new Runnable() {
        @Override
        public void run() {
            //不停触发获取下载状态
            queryProgress();
            if (downloading) {
                mHandler.postDelayed(mQueryProgressRunnable, 1000);
            }
        }
    };

///////////////////////////////////////////////////////////////////////////////////////////////////

    private DownloadHelper(String fileName, String downloadUrl) {
        this.fileName = fileName;
        this.downloadUrl = downloadUrl;
    }

    /**
     * 保证每个创建都是唯一
     * @param fileName
     * @param downloadUrl
     * @return
     */
    public static DownloadHelper getInstance(String fileName, String downloadUrl) {
        DownloadHelper helper = new DownloadHelper(fileName, downloadUrl);
        helper.registerReceiver();
        return helper;
    }

    /**
     * 设置监听
     *
     * @param listener
     */
    public DownloadHelper setDownloadListener(DownloadListener listener) {
        this.downloadListener = listener;
        return this;
    }

    /**
     * 日志控制器
     * @param msg
     */
    private void logDebug(String msg) {
        if (debug) {
            Log.e(TAG, msg);
        }
    }

    /**
     * 是否正在下载
     * @return
     */
    public boolean isDownloading() {
        return downloading;
    }

    /**
     * 注销广播
     */
    public void clear() {
        try {
            stopQueryProgress(); //停止查询下载进度
            FcUtils.getContext().unregisterReceiver(mDownloadDetailsReceiver);
        } catch (Exception ex) {
            //java.lang.IllegalArgumentException: Receiver not registered:
        }
    }


//////////////////////////////////////////下载相关操作/////////////////////////////////////////////////////////

    /**
     * 注册广播
     */
    private void registerReceiver() {
        mDownloadDetailsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                //下载中被点击
                if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action)) {
                    //打开下载中心
                    showDownloadList();
                }
                //下载完成（跟下面重复就不写了
                else if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)){
                    //打开文件
                }
            }
        };

        //选择注册监听的范围
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        intentFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        intentFilter.addAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
        FcUtils.getContext().registerReceiver(mDownloadDetailsReceiver,intentFilter);
    }

    /**
     * 显示下载列表
     */
    public void showDownloadList() {
        Intent downloadIntent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
        if (downloadIntent.resolveActivity(FcUtils.getContext().getPackageManager()) != null) {
            FcUtils.getContext().startActivity(downloadIntent);
        }
    }

    /**
     * 开始下载
     */
    public void start() {
        if (downloadListener != null) downloadListener.onStart();

        mDownloadManager = (DownloadManager) FcUtils.getContext().getSystemService(
                FcUtils.getContext().DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
        request
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)//下载完成后不移除
//                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)//下载完成后任务栏移除图标
                .setTitle(fileName)
                .setDescription("正在下载" + fileName)
//              .setMimeType("application/vnd.android.package-archive")
                .setMimeType("file/*")
//                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
//                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI)
//                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)//公用文件下载位置
                .setDestinationInExternalFilesDir(FcUtils.getContext(),
                        Environment.DIRECTORY_DOWNLOADS, fileName)
//                .allowScanningByMediaScanner()//允许MediaScanner扫描到这个文件，默认不允许
        ;
        try {
            mDownloadId = mDownloadManager.enqueue(request); // 加入下载队列
            if (mDownloadId != 0) {
                startQueryProgress();
            }
        } catch (IllegalArgumentException e) {
            if (downloadListener != null) downloadListener.onFailed();
            // "更新失败", "请在设置中开启下载管理"
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + "com.android.providers.downloads"));
            if (intent.resolveActivity(FcUtils.getContext().getPackageManager()) != null) {
                FcUtils.getContext().startActivity(intent);
            }
        }
    }

    /**
     * 查询下载进度
     */
    private void queryProgress() {
        // 通过ID向下载管理查询下载情况，返回一个cursor
        Cursor c = mDownloadManager.query(new DownloadManager.Query().setFilterById(mDownloadId));
        if (c != null && c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            logDebug("下载状态：" + status);
            switch (status) {
                case DownloadManager.STATUS_PAUSED: //下载暂停， 由系统触发
                case DownloadManager.STATUS_PENDING: //下载延迟， 由系统触发
                    break;
                case DownloadManager.STATUS_RUNNING: //正在下载， 由系统触发
                    long soFarSize = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    long totalSize = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    if (totalSize > 0) {
                        logDebug(String.format("total:%s soFar:%s ", totalSize, soFarSize)
                                + soFarSize * 1.0f / totalSize);
                        if (downloadListener != null) {
                            downloadListener.onProgress(soFarSize, totalSize);
                        }
                    }
                    break;
                case DownloadManager.STATUS_SUCCESSFUL: //下载完成， 由系统触发
                    stopQueryProgress();
                    File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    totalSize = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    if (downloadListener != null) {
                        String fullName = downloadDir.getPath() + File.separator + fileName;
                        logDebug(fullName);
                        downloadListener.onFinish(fullName, totalSize);
                    }
                    break;
                case DownloadManager.STATUS_FAILED: //下载失败， 由系统触发
                    if (downloadListener != null) downloadListener.onFailed();
                    break;
            }
        } else {
            stopQueryProgress();
            if (downloadListener != null) downloadListener.onFailed();
        }
        closeCursor(c);
    }

    /**
     * 开始查询下载进度
     */
    private void startQueryProgress() {
        downloading = true;
        mHandler.post(mQueryProgressRunnable);
    }

    /**
     * 停止查询下载进度
     */
    private void stopQueryProgress() {
        downloading = false;
        mHandler.removeCallbacks(mQueryProgressRunnable);
    }

    /**
     * 关闭查询流
     * @param cursor
     */
    private void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    /**
     * 移除下载并删除下载文件
     */
    public void remove() {
        mDownloadManager.remove(mDownloadId);
        stopQueryProgress();
    }


    /**
     * 下载监听接口
     */
    public interface DownloadListener {

        /**
         * 开始下载
         */
        void onStart();

        /**
         * 进度
         * @param soFarSize
         * @param totalSize
         */
        void onProgress(long soFarSize, long totalSize);

        /**
         * 下载成功
         * @param fileFullPath
         * @param totalSize
         */
        void onFinish(String fileFullPath, long totalSize);

        /**
         * 下载失败
         */
        void onFailed();
    }
}