package com.melvinhou.dimension2.media.video.ijk;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jeffmony.videocache.model.VideoCacheInfo;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.media.video.FCVidoeView;
import com.melvinhou.dimension2.media.video.MediaController;
import com.melvinhou.kami.util.DeviceUtils;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.IOUtils;
import com.melvinhou.kami.util.ResourcesUtils;
import com.melvinhou.kami.util.StringUtils;
import com.melvinhou.kami.view.BaseActivity;

import java.io.File;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.ActionBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.IjkTimedText;

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
public class IjkVideoActivity extends BaseActivity {

    {
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
    }

    //默认属性
    public static final int SCREEN_DIRECTION_UNDEFINED = 404;


    private IjkVideoView mVidoe;
    private Group mVideoToolsGroup;
    private ImageView mPlayButton;
    private SeekBar mProgress;
    private TextView mTitle, mTextProgress, mTextMaxProgress;
    private View mBack, mRotationButton, mVideoShade, mLoadProgress;
    //显示工具条计时，开始进度条计时
    private Disposable mChangeOrientationDisposable, mProgressDisposable;
    //屏幕旋转监听的参数
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private SensorEventListener mSensorEventListener;
    /*屏幕方向开关变化的监听*/
    private ContentObserver mScreenRotateObserver;

    //控制器
    private MediaController mMediaController;


    //屏幕方向
    int mScreenDirection;
    //是否横屏显示,是否开启电影模式
    boolean isLandscape = false;

    @Override
    protected void initWindowUI() {
        getWindow().getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |//粘性沉浸模式
//                View.SYSTEM_UI_FLAG_IMMERSIVE |//沉浸模式
                //两行全屏模式
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        //以下防止布局随着系统栏的隐藏和显示调整大小
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        //带有layout的是伪全屏，会覆盖在视图上
        getWindow().setStatusBarColor(0x40000000);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        super.onConfigurationChanged(newConfig);
//        if (mVidoe != null) {
//            mVidoe.postInvalidate();//更新屏幕
//        }
    }

    @Override
    protected int getLayoutID() {
        return R.layout.activity_video_ijk;
    }

    @Override
    protected void initView() {
        mVidoe = findViewById(R.id.video);
        mVideoToolsGroup = findViewById(R.id.video_tools_group);
        mRotationButton = findViewById(R.id.video_change);
        mPlayButton = findViewById(R.id.video_play);
        mVideoShade = findViewById(R.id.video_shade);
        mLoadProgress = findViewById(R.id.video_load_progress);
        mBack = findViewById(R.id.back);
        mTitle = findViewById(R.id.video_title);
        mProgress = findViewById(R.id.video_progress);
        mTextProgress = findViewById(R.id.video_progress_text);
        mTextMaxProgress = findViewById(R.id.video_progress_max_text);

        //控制器
        mMediaController = mVidoe.getController();

        //前置遮罩置于状态栏下
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) mVideoShade.getLayoutParams();
        lp.topMargin = DimenUtils.getStatusHeight();
        mVideoShade.setLayoutParams(lp);
    }

    @Override
    protected void initListener() {
        mBack.setOnClickListener(this::back);
        mRotationButton.setOnClickListener(v -> rotationVideo(v));
        mPlayButton.setOnClickListener(this::changePlay);
        //拖动进度条
        mProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 不是用户发起的变更，则不处理
                if (!fromUser) return;
                // 跳转播放进度
                mMediaController.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                cleaToolsHideTimer();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                startHideTimer();
            }
        });

        mVidoe.setPlayerListener(mListener);

        //初始化屏幕旋转的参数
        initGravitySener();

        //状态栏变化的监听
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> {
            Log.e("状态栏变化", "visibility=" + visibility);
            cleaToolsHideTimer();
            if (visibility == View.SYSTEM_UI_FLAG_VISIBLE) {//状态栏显示
                mVideoToolsGroup.setVisibility(View.VISIBLE);
                startHideTimer();
            } else {//状态栏隐藏
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
        //标题
        mTitle.setText(getIntent().getStringExtra("title"));
        //加载传入的数据
        String url = getIntent().getStringExtra("url");
        if (StringUtils.noNull(url)) {
            //视频加载
            mVidoe.setLocalProxyEnable(true);
            mVidoe.setVideoPath(url);
            changePlay(null);
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
        if (mVidoe != null) {
            mPlayButton.setImageResource(R.drawable.ic_tv_pause);
            mMediaController.start();
        }
        //开启屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mVidoe != null) {
            mMediaController.pause();
            mPlayButton.setImageResource(R.drawable.ic_tv_play);
        }
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
        if (mVidoe != null) mMediaController.release();
        super.onDestroy();
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
     * 清空UI隐藏的计时器
     */
    private void cleaToolsHideTimer() {
        if (mChangeOrientationDisposable != null) mChangeOrientationDisposable.dispose();
    }

    private void startHideTimer() {
        mChangeOrientationDisposable = Observable.timer(3, TimeUnit.SECONDS)
                .compose(IOUtils.setThread())
                .subscribe(aLong -> initWindowUI());
    }

    /**
     * 返回
     *
     * @param view
     */
    public void back(View view) {
        finish();
    }


    /**
     * 旋转方向
     */
    public void rotationVideo(View view) {
        if (isLandscape) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            DisplayMetrics outMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getRealMetrics(outMetrics);
        }
    }

    /**
     * 切换播放状态
     */
    public void changePlay(View view) {
        if (mMediaController.isPlaying()) {
            mMediaController.pause();
            mPlayButton.setImageResource(R.drawable.ic_tv_play);
        } else {
            mMediaController.start();
            mPlayButton.setImageResource(R.drawable.ic_tv_pause);
        }
    }


    /**
     * 更新进度条
     */
    private void updataProgress() {
        int progress = (int) mMediaController.getCurrentPosition();
        mProgress.setProgress(progress);
        mTextProgress.setText(StringUtils.formatDuration(progress));
    }

    /**
     * 更新进度条
     */
    private void updataSecondaryProgress(int percentage) {
        int progress = (int) ((float) percentage / 100f * mProgress.getMax());
        mProgress.setSecondaryProgress(progress);
    }


//////////////////////////////////////////操作工具////////////////////////////////////////////////////

    /**
     * 视频资源加载完成
     */
    private void onResourcePrepared() {
        int maxProgress = (int) mMediaController.getDuration();
        mProgress.setMax(maxProgress);
        mTextMaxProgress.setText("/" + StringUtils.formatDuration(maxProgress));
        mProgress.setProgress(0);
        mProgress.setSecondaryProgress(0);
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
//                Log.e("监听内容的变化", "newOrientation=" + newOrientation + "mScreenDirection=" + mScreenDirection);
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
        if (DeviceUtils.isOpenScreenRotate())
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
        cleaToolsHideTimer();
        startHideTimer();
        if (requestedOrientation == SCREEN_DIRECTION_UNDEFINED | requestedOrientation == mScreenDirection)
            return;
        super.setRequestedOrientation(requestedOrientation);
        mScreenDirection = requestedOrientation;
    }


    /**
     * ijk播放器的监听器
     */
    IjkVideoView.IjkPlayerListener mListener = new IjkVideoView.IjkPlayerListener() {

        /**
         * 缓存监听器
         * @param cacheFile
         * @param url
         * @param percentsAvailable 进度
         */
        @Override
        public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {
            Log.e("IJK播放器", "onCacheAvailable=" + cacheFile.getAbsolutePath() + "\r\t" + percentsAvailable);
            updataSecondaryProgress(percentsAvailable);
        }

        @Override
        public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int width, int height, int i2, int i3) {
            Log.e("IJK播放器", "onVideoSizeChanged=" + width + "\r\t" + i2);

        }

        @Override
        public void onTimedText(IMediaPlayer iMediaPlayer, IjkTimedText ijkTimedText) {
            Log.e("IJK播放器", "onTimedText=" + ijkTimedText.getText());

        }

        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
            mPlayButton.setImageResource(R.drawable.ic_tv_play);
            //关闭屏幕常亮
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        @Override
        public void onSeekComplete(IMediaPlayer iMediaPlayer) {

        }

        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
//            mVidoe.setScreenRate(4f/3f);
            onResourcePrepared();
            mLoadProgress.setVisibility(View.GONE);
        }

        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
            Log.e("IJK播放器", "onInfo=" + what + "\r\t" + extra);
            if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_START) {
            } else if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_END) {
            } else if (what == IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED) {
                //这里返回了视频旋转的角度，根据角度旋转视频到正确的画面
            }
            return false;
        }

        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
            return false;
        }

        @Override
        public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {
            Log.e("IJK播放器", "onBufferingUpdate=" + i);
        }



//视频缓冲器的监听


        @Override
        public void onCacheStart(VideoCacheInfo cacheInfo) {

        }

        @Override
        public void onCacheProgress(VideoCacheInfo cacheInfo) {
            Log.e("IJK播放器", "onCacheProgress=" + cacheInfo.getPercent() + "\r\t" + cacheInfo.getPercent());
            int progress = (int) (cacheInfo.getPercent() * mProgress.getMax());
            mProgress.setSecondaryProgress(progress);
        }

        @Override
        public void onCacheError(VideoCacheInfo cacheInfo, int errorCode) {

        }

        @Override
        public void onCacheForbidden(VideoCacheInfo cacheInfo) {

        }

        @Override
        public void onCacheFinished(VideoCacheInfo cacheInfo) {

        }
    };
}
