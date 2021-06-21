package com.melvinhou.dimension2.function.screenrecord;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.DisplayMetrics;

import com.melvinhou.dimension2.R;
import com.melvinhou.kami.util.IOUtils;
import com.melvinhou.kami.view.BaseActivity;

import java.io.File;
import java.util.concurrent.TimeUnit;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.reactivex.Observable;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/6/3 19:49
 * <p>
 * = 分 类 说 明：屏幕录制
 * ================================================
 */
public class ScreenRecordActivity extends BaseActivity {

    private final String TAG = ScreenRecordActivity.class.getName();
    private final static int MICROPHONE_REQUEST_CODE = 789;
    private final static int LOCAL_REQUEST_CODE = 10012;


    MediaProjectionManager mProjectionManager;
    ScreenRecordService.RecordBinder mRecordBinder;
    ServiceConnection mServiceConnection = new ServiceConnection() {
        @SuppressLint("CheckResult")
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mRecordBinder = (ScreenRecordService.RecordBinder) service;

            Observable.timer(2, TimeUnit.SECONDS)
                    .compose(IOUtils.setThread())
                    .subscribe(aLong -> {
                        mRecordBinder.start();
                    });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected int getLayoutID() {
        return R.layout.activity_screenrecord;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initListener() {
        findViewById(R.id.start).setOnClickListener(v -> start());
    }

    @Override
    protected void initData() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MICROPHONE_REQUEST_CODE) {
            start();
        }
        if (requestCode == LOCAL_REQUEST_CODE && resultCode == RESULT_OK) {
            Intent intent = new Intent(this, ScreenRecordService.class);
            //录屏申请数据
            intent.putExtra("data", data);
            intent.putExtra("resultCode", resultCode);
            //屏幕宽高
            int[] size = getDisplaySize();
            int displayWidth = size[0];
            int displayHeight = size[1];
            intent.putExtra("width", displayWidth);
            intent.putExtra("height", displayHeight);
            //存储位置
            intent.putExtra("path", getRecordFilesDir().getAbsolutePath());
//            intent.putExtra("surface",surface); // Surface 用于显示录屏的数据
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent); // 启动前台服务
            } else {
                startService(intent);
            }
//            bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onDestroy() {
        if (mRecordBinder != null) mRecordBinder.stop();
        //停止服务
//        unbindService(mServiceConnection);
        stopService(new Intent(ScreenRecordActivity.this, ScreenRecordService.class));
        super.onDestroy();
    }

    private void start() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MICROPHONE_REQUEST_CODE);
            return;
        }
        mProjectionManager
                = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent screenCaptureIntent = mProjectionManager.createScreenCaptureIntent();
        startActivityForResult(screenCaptureIntent, LOCAL_REQUEST_CODE);
    }


    /**
     * 获取屏幕宽高
     *
     * @return
     */
    private int[] getDisplaySize() {
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        return new int[]{outMetrics.widthPixels, outMetrics.heightPixels};
    }

    public File getRecordFilesDir() {
        File folderFile = new File(Environment.getExternalStorageDirectory().getPath()
                + File.separator + "Dimension2" + File.separator + "record");
        if (!folderFile.exists()) {
            folderFile.mkdirs();
        }
        return folderFile;
    }


}
