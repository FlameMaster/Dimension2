package com.melvinhou.medialibrary.record;

import android.Manifest;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.melvinhou.kami.io.FileUtils;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.view.activities.BaseActivity;

import java.io.File;

import androidx.core.app.NotificationManagerCompat;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/4/18 0018 13:41
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public abstract class SecreenRecordView extends BaseActivity {
    private static final String TAG = SecreenRecordView.class.getName();
    private ScreenRecordService.RecordBinder mRecordBinder;

    protected static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS};
    protected static final String[] REQUIRED_PERMISSIONS_33 = {
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS};

    /**
     * 存储位置
     *
     * @return
     */
    protected File getRecordFilesDir() {
        File folderFile = new File(FileUtils.getAppFileDir(FileUtils.RECORD_DIR_SUFFIX));
        if (!folderFile.exists()) {
            folderFile.mkdirs();
        }
        return folderFile;
    }

    @Override
    protected void onDestroy() {
        //停止服务
        stopRecord();
        super.onDestroy();
    }


    /**
     * 开始录制
     */
    protected void startRecord(Intent data) {
        Log.e(TAG, "开始录屏");
        Intent intent = new Intent(this, ScreenRecordService.class);
        intent.putExtra("resultCode", RESULT_OK);
        intent.putExtra("data", data);
        //屏幕宽高
        int[] size = DimenUtils.getScreenSize();
        int displayWidth = size[0];
        int displayHeight = size[1];
        intent.putExtra("width", displayWidth);
        intent.putExtra("height", displayHeight);
        //存储位置
        String path = new File(getRecordFilesDir(), "REC_" + FileUtils.getFileNameForDate() + ".mp4").getAbsolutePath();
        intent.putExtra("path", path);
//            intent.putExtra("surface",surface); // Surface 用于显示录屏的数据
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent); // 启动前台服务
        } else {
            startService(intent);
        }
//            bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
    }

    /**
     * 停止录制
     */
    protected void stopRecord() {
        if (mRecordBinder != null) mRecordBinder.stop();
        stopService(new Intent(this, ScreenRecordService.class));
    }


    @Override
    protected void onPermissionGranted() {
        super.onPermissionGranted();
        //权限申请成功
        initData();
    }

    @Override
    protected void initData() {
        checkPermissions();
    }


    protected boolean checkPermissions() {


        //普通权限
        String[] permissions = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? REQUIRED_PERMISSIONS_33 : REQUIRED_PERMISSIONS;
        // 请求权限
        if (!checkPermission(permissions)) {
            requestPermissions(permissions);
            return false;
        }

        //危险权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {//以上
            //文件管理权限
//            if (!Environment.isExternalStorageManager()) {
//                showCheckView(new DialogCheckBuilder("权限提醒",
//                        "存储录制视频需要文件管理权限，是否授予权限？",
//                        "授权", "取消") {
//                    @Override
//                    public void confirm() {
//                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
//                        intent.setData(Uri.fromParts("package", getPackageName(), null));
//                        toResultActivity(intent, result -> {
//                            if (Environment.isExternalStorageManager())
//                                onPermissionGranted();
//                            else onPermissionCancel();
//                        });
//                    }
//                    @Override
//                    public void cancel() {
//                        onPermissionCancel();
//                    }
//                });
//                return false;
//            }
            //通知权限
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                showCheckView("权限提醒",
                        "录制视频时需要通知权限，是否授予权限？",
                        "授权", "取消", data -> {
                            if (data) {
                                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                                intent.putExtra(Settings.EXTRA_CHANNEL_ID, getApplicationInfo().uid);
                                toResultActivity(intent, result -> {
                                    if (NotificationManagerCompat.from(SecreenRecordView.this).areNotificationsEnabled())
                                        onPermissionGranted();
                                    else onPermissionCancel();
                                });
                            }else {
                                onPermissionCancel();
                            }
                        });
                return false;
            }
        }

        return true;
    }


    /**
     * 启动录制
     */
    private void launchRecord() {
        //录制
        MediaProjectionManager manager =
                (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        Intent screenCaptureIntent = manager.createScreenCaptureIntent();
        toResultActivity(screenCaptureIntent, callback -> {
            startRecord(callback.getData());
        });
    }


}
