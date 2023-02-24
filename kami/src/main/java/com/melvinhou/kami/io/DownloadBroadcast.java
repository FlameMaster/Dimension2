package com.melvinhou.kami.io;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
 * = 时 间：2019/12/25 17:50
 * <p>
 * = 分 类 说 明：下载广播完成或点击广播
 * ============================================================
 */
public class DownloadBroadcast extends BroadcastReceiver {

    private final File mFile;
    private String TAG = DownloadBroadcast.class.getName();

    public DownloadBroadcast(File file) {
        mFile = file;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        //在广播中取出下载任务的id
        long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        Log.d(TAG, "action:" + action + "\ndownloadId:" + downloadId);

        //任务完成
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            openFile(downloadId);
        }
        //任务中被点击
        else if (intent.getAction().equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
            //处理 如果还未完成下载，用户点击Notification ，跳转到下载中心
            Intent viewDownloadIntent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
            viewDownloadIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(viewDownloadIntent);
        }
    }


    private void openFile(long downloadId) {
        //setFilterById根据下载id进行过滤
        DownloadManager downloadManager = (DownloadManager) FcUtils.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);

        Cursor cursor = null;
        try {
            cursor = downloadManager.query(query);
            if (cursor != null && cursor.moveToFirst()) {
                //获取文件下载路径
//                String filename = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_FILENAME));
                int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                int fileUriIdx = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI);
                String fileUri = cursor.getString(fileUriIdx);
                //下载完成
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    FileUtils.openFile(fileUri);
                }
                //下载失败
                else if (status == DownloadManager.STATUS_FAILED) {
                }
                //下载暂停
                else if (status == DownloadManager.STATUS_PAUSED) {
                }
                //下载中
                else if (status == DownloadManager.STATUS_RUNNING) {
                }
                //下载延迟
                else if (status == DownloadManager.STATUS_PENDING) {
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
