package com.melvinhou.dimension2.function.screenrecord;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.media.video.VideoActivity2;
import com.melvinhou.kami.adapter.RecyclerAdapter;
import com.melvinhou.kami.adapter.RecyclerAdapter2;
import com.melvinhou.kami.adapter.RecyclerHolder;
import com.melvinhou.kami.util.DimenUtils;
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
    //浮动窗口申请回调
    private final static int FLOAT_WINDOW_REQUEST_CODE = 10071;
    //权限请求
    public static final int REQUEST_CODE_PERMISSIONS = 2112;
    //权限列表：录屏和文件
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    //浮动窗口
    private WindowManager.LayoutParams mParams;
    private WindowManager mWindowManager;
    private TextView mStopView;
    private TextView mDownTimerView;
    private  Intent data;


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
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        findViewById(R.id.start).setOnClickListener(v -> launchRecord());
        mRecycler.setLayoutManager(new LinearLayoutManager(FcUtils.getContext()));
        mAdapter = new MyAdapter();
        mRecycler.setAdapter(mAdapter);
        mRecycler.addItemDecoration(new RecyclerView.ItemDecoration(){
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                //设定底部边距为
                int height = DimenUtils.dp2px(10);
                outRect.set(0,0, 0, height);
            }
        });

        mAdapter.setOnItemClickListener((viewHolder, position, data) -> {
            Intent intent = new Intent(ScreenRecordActivity.this, VideoActivity2.class);
            intent.putExtra("url", data);
            intent.putExtra("title", "录屏");
            intent.putExtra("mode", true);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (allPermissionsGranted(REQUIRED_PERMISSIONS))
            loadData();
    }

    @Override
    protected void initData() {
        View view = LayoutInflater.from(FcUtils.getContext()).inflate(R.layout.item_loadmore, mRecycler, false);
        mAdapter.addTailView(view);
        view.setVisibility(View.INVISIBLE);
        //权限申请
        if (allPermissionsGranted(REQUIRED_PERMISSIONS))
            loadData();
        else
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
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
                launchRecord();
            } else if (requestCode == RECORD_REQUEST_CODE) {
                this.data = data;
                start();
//                startRecord();
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

    /**
     * 启动录制
     */
    private void launchRecord() {
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


    /**
     * 浮动窗口
     */
    private void start() {
        //设置允许弹出悬浮窗口的权限
        requestWindowPermission();
        //悬浮窗布局参数
        initFloatWindowParams();
        //倒计时
        showDownTimer();
        //跳转桌面
        toDesktop();
    }

    /**
     * android 6.0或者之后的版本需要发一个intent让用户授权
     */
    private void requestWindowPermission() {
        if (!Settings.canDrawOverlays(getApplicationContext())) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, FLOAT_WINDOW_REQUEST_CODE);
        }
    }

    /**
     * 浮动按钮的参数
     */
    private void initFloatWindowParams() {
        //创建窗口布局参数
        mParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, 0, 0, PixelFormat.TRANSPARENT);
        //设置悬浮窗坐标
//        mParams.x=100;
//        mParams.y=100;
        //表示该Window无需获取焦点，也不需要接收输入事件
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mParams.gravity = Gravity.CENTER;
        //设置window 类型
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//API Level 26
            mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        }
    }

    /**
     * 倒计时的悬浮窗
     */
    private void showDownTimer() {
        //创建倒计时的悬浮窗
        if (null == mDownTimerView) {
            mDownTimerView = new TextView(FcUtils.getContext());
            mDownTimerView.setText("3");
            mDownTimerView.setTextSize(72);
            mDownTimerView.setTextColor(Color.RED);
            mWindowManager.addView(mDownTimerView, mParams);
        }
        //启动倒计时
        new CountDownTimer(3000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                mDownTimerView.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                if (mWindowManager != null && mDownTimerView != null)
                    mWindowManager.removeView(mDownTimerView);
                mDownTimerView = null;
                //显示停止键
                showStopButton();
                //开始录制
                startRecord();
            }
        }.start();
    }

    /**
     * 结束录制按钮
     */
    @SuppressLint("ClickableViewAccessibility")
    private void showStopButton() {
        if (null == mStopView) {
            mParams.gravity = Gravity.TOP | Gravity.LEFT;
            mStopView = new TextView(FcUtils.getContext());
            mStopView.setText("3");
            int paddingh = DimenUtils.dp2px(8);
            int paddingv = DimenUtils.dp2px(2);
            mStopView.setPadding(paddingh, paddingv, paddingh, paddingv);
            mStopView.setBackgroundColor(Color.parseColor("#eeeeee"));
            mStopView.setText("点击停止");
            mStopView.setTextColor(Color.BLACK);
            mWindowManager.addView(mStopView, mParams);
            mStopView.setOnClickListener(v -> {
                if (null != mStopView) {
                    if (mWindowManager != null && mStopView != null)
                        mWindowManager.removeView(mStopView);
                    mStopView = null;
                    Log.e("录屏", "结束录屏");
                    //结束
                    if (mRecordBinder != null) mRecordBinder.stop();
                    //停止服务
//        unbindService(mServiceConnection);
                    stopService(new Intent(ScreenRecordActivity.this, ScreenRecordService.class));
                }
            });
        }
    }

    /**
     * 跳转桌面
     */
    private void toDesktop() {
        Intent mIntent = new Intent(Intent.ACTION_MAIN);
        mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mIntent.addCategory(Intent.CATEGORY_HOME);
        startActivity(mIntent);
    }

    /**
     * 开始录制
     */
    private void startRecord(){
        Log.e("录屏","开始录屏");
        Intent intent = new Intent(this, ScreenRecordService.class);
        intent.putExtra("resultCode",RESULT_OK);
        intent.putExtra("data",data);
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
            img = itemView.findViewById(R.id.img);
        }

        public void update(String data) {
            File file = new File(data);
            title.setText(file.getName());
            text.setText(data);
            Glide.with(FcUtils.getContext())
                    .load(file)
                    .apply(new RequestOptions()
                            .override(100, 100)
                            .placeholder(R.mipmap.fc)
                            .error(R.mipmap.fc)
                            .diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(img);
        }
    }

}
