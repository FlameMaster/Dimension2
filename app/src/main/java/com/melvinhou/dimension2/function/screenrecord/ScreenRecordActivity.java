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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.media.video.VideoActivity2;
import com.melvinhou.kami.adapter.RecyclerAdapter;
import com.melvinhou.kami.adapter.RecyclerAdapter2;
import com.melvinhou.kami.adapter.RecyclerHolder;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.IOUtils;
import com.melvinhou.kami.view.BaseActivity;

import java.io.File;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
    //录屏回调
    private final static int RECORD_REQUEST_CODE = 10012;
    //权限请求
    public static final int REQUEST_CODE_PERMISSIONS = 2112;
    //权限列表：录屏和文件
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.INTERACT_ACROSS_PROFILES,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};


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


    private RecyclerView mRecycler;
    private MyAdapter mAdapter;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_screenrecord;
    }

    @Override
    protected void initView() {
        mRecycler = findViewById(R.id.list);
    }

    @Override
    protected void initListener() {
        findViewById(R.id.start).setOnClickListener(v -> start());
        mRecycler.setLayoutManager(new LinearLayoutManager(FcUtils.getContext()));
        mAdapter = new MyAdapter();
        mRecycler.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener((viewHolder, position, data) -> {
            Intent intent = new Intent(ScreenRecordActivity.this, VideoActivity2.class);
            intent.putExtra("url", data);
            intent.putExtra("title", "录屏");
            intent.putExtra("mode", true);
            startActivity(intent);
        });
    }

    @Override
    protected void initData() {
        View view = LayoutInflater.from(FcUtils.getContext()).inflate(R.layout.item_loadmore, mRecycler, false);
        mAdapter.addTailView(view);
        view.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (allPermissionsGranted( REQUIRED_PERMISSIONS)) loadData();
        else ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
    }

    /**
     * 加载数据
     */
    private void loadData() {
        mAdapter.clearData();
        File dir = getRecordFilesDir();
        if (dir.isDirectory()) {// 处理目录
            File files[] = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
//                File file = new File(files[i].getAbsolutePath());
                mAdapter.addData(files[i].getAbsolutePath());
            }
        }
    }

    @Override
    protected void onPermissionGranted(int requestCode) {
        if (requestCode == REQUEST_CODE_PERMISSIONS)
            loadData();
    }

    @Override
    protected void onPermissionCancel(int requestCode) {
        if (requestCode == REQUEST_CODE_PERMISSIONS)
            FcUtils.showToast("没有权限读取录屏资料");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == MICROPHONE_REQUEST_CODE) {
                start();
            } else if (requestCode == RECORD_REQUEST_CODE) {
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
        startActivityForResult(screenCaptureIntent, RECORD_REQUEST_CODE);
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


    class MyAdapter extends RecyclerAdapter<String, MyHolder> {
        @Override
        public void bindData(MyHolder viewHolder, int position, String data) {
            viewHolder.update(data);
        }

        @Override
        public int getItemLayoutId(int viewType) {
            return R.layout.item_screen_record;
        }

        @Override
        protected MyHolder onCreate(View View, int viewType) {
            return new MyHolder(View);
        }
    }

    class MyHolder extends RecyclerHolder {

        TextView title, text;
        ImageView img;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            text = itemView.findViewById(R.id.text);
        }

        public void update(String data) {
            text.setText(data);
        }
    }

}
