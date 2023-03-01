package com.melvinhou.dimension2.media.video;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.melvinhou.dimension2.R;
import com.melvinhou.kami.util.DateUtils;
import com.melvinhou.kami.util.DeviceUtils;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.io.IOUtils;
import com.melvinhou.kami.util.ResourcesUtils;
import com.melvinhou.kami.util.StringUtils;
import com.melvinhou.kami.view.activities.BaseActivity;

import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.ActionBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/7/17 1:33
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class VideoActivity2 extends BaseActivity {

    //默认属性
    public static final int SCREEN_DIRECTION_UNDEFINED = 404;

    //竖屏的两种风格
    private static final int[] SYSTEM_UI_FLAG_PORTRAIT = {
            View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION,
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    };
    //横屏的两种风格
    private static final int[] SYSTEM_UI_FLAG_LANDSCAPE = {
//                  View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |//粘性沉浸模式
            View.SYSTEM_UI_FLAG_IMMERSIVE |//沉浸模式
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    //以下防止布局随着系统栏的隐藏和显示调整大小
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION,
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    };


    private TextureVidoeView mVidoe;
    private Group mVideoToolsGroup;
    private ImageView mPlayButton;
    private SeekBar mProgress;
    private TextView mTitle, mTextProgress, mTextMaxProgress;
    private View mBack, mVideoContainer, mRotationButton, mVideoShade, mLoadProgress;
    //显示工具条计时，开始进度条计时
    private Disposable mChangeOrientationDisposable, mProgressDisposable;
    //屏幕旋转监听的参数
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private SensorEventListener mSensorEventListener;
    /*屏幕方向开关变化的监听*/
    private ContentObserver mScreenRotateObserver;


    //屏幕方向
    int mScreenDirection;
    //横竖屏系统ui状态
    int mSystemUIFlag = 0;
    //是否横屏显示,是否开启电影模式
    boolean isLandscape = false, isMovieMode = false;

    @Override
    protected void initWindowUI() {
        if (isMovieMode || isLandscape) {
            getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_LANDSCAPE[mSystemUIFlag]);
            getWindow().setNavigationBarColor(0x40000000);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_PORTRAIT[mSystemUIFlag]);
            getWindow().setNavigationBarColor(0x40ffffff);
        }
        //带有layout的是伪全屏，会覆盖在视图上
        getWindow().setStatusBarColor(0x40000000);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        super.onConfigurationChanged(newConfig);
        initWindowUI();
    }

    @Override
    protected int getLayoutID() {
        return R.layout.activity_video2;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //是否开启电影模式
        isMovieMode = getIntent().getBooleanExtra("mode", true);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initView() {
        mVidoe = findViewById(R.id.video);
        mVideoToolsGroup = findViewById(R.id.video_tools_group);
        mVideoContainer = findViewById(R.id.video_container);
        mRotationButton = findViewById(R.id.video_change);
        mPlayButton = findViewById(R.id.video_play);
        mVideoShade = findViewById(R.id.video_shade);
        mLoadProgress = findViewById(R.id.video_load_progress);
        mBack = findViewById(R.id.back);
        mTitle = findViewById(R.id.video_title);
        mProgress = findViewById(R.id.video_progress);
        mTextProgress = findViewById(R.id.video_progress_text);
        mTextMaxProgress = findViewById(R.id.video_progress_max_text);


        //置于状态栏下
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) mVideoShade.getLayoutParams();
        lp.topMargin = DimenUtils.getStatusHeight();
        mVideoShade.setLayoutParams(lp);
    }

    @Override
    protected void initListener() {
        mBack.setOnClickListener(this::back);
        mRotationButton.setOnClickListener(v -> {
            if (isMovieMode) return;
            rotationVideo(v);
        });
        mVidoe.setOnClickListener(this::showVideoToolsGroup);
        mPlayButton.setOnClickListener(this::changePlay);
        //拖动进度条
        mProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 不是用户发起的变更，则不处理
                if (!fromUser) return;
                // 跳转播放进度
                mVidoe.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mVidoe.setCurrentStateChangeListener(this::onVideoStateChanged);

        //初始化屏幕旋转的参数
        initGravitySener();

        //状态栏变化的监听
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> {
            Log.e("状态栏变化", "visibility=" + visibility);
            if (visibility == View.SYSTEM_UI_FLAG_VISIBLE) {//状态栏显示
                if (mChangeOrientationDisposable != null) mChangeOrientationDisposable.dispose();
                mVideoToolsGroup.setVisibility(View.VISIBLE);
                mChangeOrientationDisposable = Observable.timer(3, TimeUnit.SECONDS)
                        .compose(IOUtils.setThread())
                        .subscribe(aLong -> {
                            mSystemUIFlag = 1;
                            showVideoToolsGroup(null);
                        });
            } else {//状态栏隐藏
                if (mChangeOrientationDisposable != null) mChangeOrientationDisposable.dispose();
                mVideoToolsGroup.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void initData() {
        //屏幕方向
        mScreenDirection = ResourcesUtils.getResources()
                .getConfiguration().orientation;
        isLandscape = ResourcesUtils.getResources()
                .getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        mVidoe.changeDisplayerType(TextureVidoeView.TYPE_DISPLAYER_INSIDE);
        //标题
        mTitle.setText(getIntent().getStringExtra("title"));
        //电影模式直接横屏
        if (isMovieMode) rotationVideo(null);
        //加载传入的数据
        String url = getIntent().getStringExtra("url");
        if (StringUtils.nonEmpty(url)) {
            //视频加载
            mVidoe.setVideoURI(Uri.parse(url));
            mVidoe.setLooping(false);
//            mVidoe.start();
            mVidoe.play();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        changeSensor();
        //注册屏幕变化开关的监听
        getContentResolver()
                .registerContentObserver(Settings.System
                                .getUriFor(Settings.System.ACCELEROMETER_ROTATION),
                        false, mScreenRotateObserver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mVidoe != null && mVidoe.getCurrentState() == TextureVidoeView.STATE_PAUSED)
            mVidoe.start();
        //开启屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mVidoe != null && mVidoe.getCurrentState() == TextureVidoeView.STATE_PLAYING)
            mVidoe.pause();
        //关闭屏幕常亮
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorEventListener);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVidoe != null) mVidoe.clear();
        if (mProgressDisposable != null) mProgressDisposable.dispose();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            back(null);
            return true;
        }
        return false;
    }

//////////////////////////////////////////操作工具////////////////////////////////////////////////////


    /**
     * 返回
     *
     * @param view
     */
    public void back(View view) {
        if (!isMovieMode && isLandscape) rotationVideo(null);
        else finish();
    }

    /**
     * 显示/隐藏操作栏
     */
    public void showVideoToolsGroup(View view) {
        Log.e("被点击了", "JOJO我不做人了");
        if (mSystemUIFlag == 0) {
            mSystemUIFlag = 1;
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) actionBar.show();
        } else mSystemUIFlag = 0;
        initWindowUI();
    }


    /**
     * 旋转方向
     */
    public void rotationVideo(View view) {
        if (mSystemUIFlag == 0) mSystemUIFlag = 1;
        else mSystemUIFlag = 0;
        StringBuffer buffer = new StringBuffer("H,");
        int marginRight;
        if (isLandscape) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            buffer.append(16).append(":").append(9);
            marginRight = 0;
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            DisplayMetrics outMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getRealMetrics(outMetrics);
            buffer.append(outMetrics.widthPixels).append(":").append(outMetrics.heightPixels);
            marginRight = DimenUtils.getNavigationHeight();
        }
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) mVideoContainer.getLayoutParams();
        ConstraintLayout.LayoutParams lp2 = (ConstraintLayout.LayoutParams) mRotationButton.getLayoutParams();
        lp.dimensionRatio = buffer.toString();
        lp2.rightMargin = marginRight;
        mVideoContainer.setLayoutParams(lp);
        mRotationButton.setLayoutParams(lp2);
    }

    /**
     * 切换播放状态
     */
    public void changePlay(View view) {
        if (mVidoe.getCurrentState() == TextureVidoeView.STATE_PLAYING) {
            mVidoe.pause();
        } else {
            mVidoe.start();
        }
    }


    /**
     * 更新进度条
     */
    private void updataProgress() {
        int progress = mVidoe.getProgress();
        mProgress.setProgress(progress);
        mTextProgress.setText(DateUtils.formatDuration(progress));
    }


//////////////////////////////////////////操作工具////////////////////////////////////////////////////

    /**
     * 播放状态监听
     *
     * @param currentState
     */
    private void onVideoStateChanged(int currentState) {
        if (currentState == TextureVidoeView.STATE_IDLE ||
                currentState == TextureVidoeView.STATE_PREPARING ||
                currentState == TextureVidoeView.STATE_BUFFERING_PLAYING ||
                currentState == TextureVidoeView.STATE_BUFFERING_PAUSED) {
            if (mLoadProgress.getVisibility() == View.GONE)
                mLoadProgress.setVisibility(View.VISIBLE);
        } else if (mLoadProgress.getVisibility() != View.GONE)
            mLoadProgress.setVisibility(View.GONE);

        //播放准备完毕
        if (currentState == TextureVidoeView.STATE_PREPARED)
            onResourcePrepared();

        //播放图标
        if (currentState == TextureVidoeView.STATE_PLAYING||
                currentState == TextureVidoeView.STATE_BUFFERING_PLAYING) {
            mPlayButton.setImageResource(R.drawable.ic_media_pause_02);
        } else
            mPlayButton.setImageResource(R.drawable.ic_media_play_02);
    }

    /**
     * 视频资源加载完成
     */
    private void onResourcePrepared() {
        int maxProgress = mVidoe.getMaxProgress();
        mProgress.setMax(maxProgress);
        mTextMaxProgress.setText("/" + DateUtils.formatDuration(maxProgress));
        //开始更新进度条
        if (mProgressDisposable != null) mProgressDisposable.dispose();
        mProgressDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
                .compose(IOUtils.setThread())
                .subscribe(residueTime -> {
                    //更新进度条
                    updataProgress();
                });
    }

    /**
     * 构造初始化重力重力感应
     */
    private void initGravitySener() {
        // 获取传感器管理器
        mSensorManager = (SensorManager) FcUtils.getContext()
                .getSystemService(Context.SENSOR_SERVICE);
        // 获取传感器类型
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        mSensorEventListener = new SensorEventListener() {
            /*感应检测到Sensor的值有变化*/
            @Override
            public void onSensorChanged(SensorEvent event) {

                //只需要重力传感器
                if (Sensor.TYPE_GRAVITY != event.sensor.getType()) return;

                //获取xy方向的偏移量
                float[] values = event.values;
                float x = values[0];
                float y = values[1];
//                Log.e("范老师mmp","z="+values[2]);

                //根据偏移量判断方向
                int newOrientation = SCREEN_DIRECTION_UNDEFINED;
                if (x < 4.5 && x >= -4.5 && y >= 4.5) {//竖屏
                    newOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                } else if (x >= 4.5 && y < 4.5 && y >= -4.5) {//横屏
                    newOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                } else if (x <= -4.5 && y < 4.5 && y >= -4.5) {//翻转横屏
                    newOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                } else if (x < 4.5 && x >= -4.5 && y < -4.5) {//翻转竖屏
                    newOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                }
//                if (x < 3.5 && x >= -3.5) {//竖屏，具体方向由系统判断
//                    newOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
//                } else if (y < 9 && y >= -9) {//横屏，具体方向由系统判断
//                    newOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
//                }

//                Log.e("监听内容的变化", "newOrientation=" + newOrientation + "____x=" + x + "____y=" + y);
                Log.e("监听内容的变化", "newOrientation=" + newOrientation + "mScreenDirection=" + mScreenDirection);
                setRequestedOrientation(newOrientation);

            }

            /*感应检测到Sensor的精密度有变化*/
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };


        mScreenRotateObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                changeSensor();
            }
        };
    }

    /**
     * 根据开关状态判断是否开启重力感应
     */
    protected void changeSensor() {
        //注册监听，第三个属性是延迟紧密度
        if (DeviceUtils.isOpenScreenRotate() && !isMovieMode)
            mSensorManager.registerListener(mSensorEventListener, mSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        else
            mSensorManager.unregisterListener(mSensorEventListener);
    }

    /**
     * 屏幕方向切换
     *
     * @param requestedOrientation
     */
    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        if (mChangeOrientationDisposable != null) mChangeOrientationDisposable.dispose();
        if (requestedOrientation == SCREEN_DIRECTION_UNDEFINED | requestedOrientation == mScreenDirection)
            return;
        super.setRequestedOrientation(requestedOrientation);
        mScreenDirection = requestedOrientation;
    }

}
