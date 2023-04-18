package com.melvinhou.medialibrary.video;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.melvinhou.kami.util.DeviceUtils;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.ResourcesUtils;
import com.melvinhou.kami.util.StringUtils;
import com.melvinhou.kami.view.activities.BaseActivity;
import com.melvinhou.medialibrary.R;
import com.melvinhou.medialibrary.video.proxy.IPlayer;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

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
 * = 分 类 说 明：Ijk的视频播放页
 * ================================================
 */
public class FcVideoActivity extends BaseActivity {

    {
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
    }

    //默认属性
    public static final int SCREEN_DIRECTION_UNDEFINED = 404;


    private WindowInsetsControllerCompat windowInsetsController;
    private FcVideoLayout mVidoeLayout;
    private TextView mTitle;
    private View mBack, mBarLayout;
    //屏幕旋转监听的参数
    private SensorManager mSensorManager;
    private Sensor mSensor;
    //传感器的监听
    private SensorEventListener mSensorEventListener;
    //屏幕方向开关变化的监听
    private ContentObserver mScreenRotateObserver;
    //准备好的监听
    private IPlayer.OnPreparedListener mOnPreparedListener = new IPlayer.OnPreparedListener() {
        public void onPrepared(IPlayer mp) {
        }
    };
    //错误的监听
    private IPlayer.OnErrorListener mOnErrorListener = new IPlayer.OnErrorListener() {
        public boolean onError(IPlayer mp, int what, int extra) {
            //int MEDIA_INFO_VIDEO_RENDERING_START = 3;//视频准备渲染
            //int MEDIA_INFO_BUFFERING_START = 701;//开始缓冲
            //int MEDIA_INFO_BUFFERING_END = 702;//缓冲结束
            //int MEDIA_INFO_VIDEO_ROTATION_CHANGED = 10001;//视频选择信息
            //int MEDIA_ERROR_SERVER_DIED = 100;//视频中断，一般是视频源异常或者不支持的视频类型。
            //int MEDIA_ERROR_IJK_PLAYER = -10000,//一般是视频源有问题或者数据格式不支持，比如音频不是AAC之类的
            //int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;//数据错误没有有效的回收
            Log.e("IJK播放器", "onInfo=" + what + "\r\t" + extra);
            if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_START) {
            } else if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_END) {
            } else if (what == IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED) {
                //这里返回了视频旋转的角度，根据角度旋转视频到正确的画面
            }
            return true;
        }
    };
    //播放完的监听
    private IPlayer.OnCompletionListener mOnCompletionListener = new IPlayer.OnCompletionListener() {
        public void onCompletion(IPlayer mp) {
            //关闭屏幕常亮
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    };
    //进度跳转的监听
    private IPlayer.OnSeekCompleteListener onSeekCompleteListener = new IPlayer.OnSeekCompleteListener() {
        @Override
        public void OnSeekComplete(IPlayer mp) {
        }
    };
    //当前布局的ui变化监听
    private FcVideoLayout.VideoControllerUIListener mVideoControllerUIListener = new FcVideoLayout.VideoControllerUIListener() {
        @Override
        public void onShowControllerUI() {
            if (windowInsetsController != null) {
                windowInsetsController.show(WindowInsetsCompat.Type.statusBars());
            }
            if (mBarLayout != null) mBarLayout.setVisibility(View.VISIBLE);
        }

        @Override
        public void onHideControllerUI() {
            if (mVidoeLayout.isPlaying()) {
                if (windowInsetsController != null) {
                    windowInsetsController.hide(WindowInsetsCompat.Type.statusBars());
                }
                if (mBarLayout != null) mBarLayout.setVisibility(View.GONE);
            }
        }

        @Override
        public void onFullScreen(boolean isFull) {
            fullScreen(isFull);
        }
    };


    //屏幕方向
    int mScreenDirection;
    //是否横屏显示
    boolean isLandscape = false;

    @Override
    protected void initWindowUI() {

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |//粘性沉浸模式
                        View.SYSTEM_UI_FLAG_IMMERSIVE |//沉浸模式
                        //两行全屏
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        //以下防止布局随着系统栏的隐藏和显示调整大小
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        getWindow().setStatusBarColor(Color.parseColor("#40000000"));
        //刘海屏适配
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lp.layoutInDisplayCutoutMode
                    = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(lp);
        }
        //控制系统界面的工具
        windowInsetsController
                = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView().getRootView());
        windowInsetsController.setAppearanceLightStatusBars(false);
        windowInsetsController.setAppearanceLightNavigationBars(false);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        super.onConfigurationChanged(newConfig);
        if (mVidoeLayout != null) mVidoeLayout.setFullScreen(isLandscape);
//        if (mVidoe != null) {
//            mVidoe.postInvalidate();//更新屏幕
//        }
    }

    @Override
    protected int getLayoutID() {
        return R.layout.activity_video_fc;
    }

    @Override
    protected void initView() {
        //设置主题，当前页面的icon都是theme里面的
        setTheme(R.style.KamiTheme);
        //
        mVidoeLayout = findViewById(R.id.video_layout);
        mBack = findViewById(R.id.back);
        mBarLayout = findViewById(R.id.video_tools_top);
        mTitle = findViewById(R.id.video_title);

        //前置遮罩置于状态栏下
        mBarLayout.setPadding(0, DimenUtils.getStatusBarHeight(), 0, 0);
        if (mVidoeLayout != null) {
            mVidoeLayout.showController();
            mVideoControllerUIListener.onShowControllerUI();
        }
    }

    @Override
    protected void initListener() {
        mBack.setOnClickListener(view -> onActivityBack(2));
        if (mVidoeLayout != null) {
            mVidoeLayout.setOnCompletionListener(mOnCompletionListener);
            mVidoeLayout.setVideoControllerUIListener(mVideoControllerUIListener);
            mVidoeLayout.setOnErrorListener(mOnErrorListener);
        }

        //初始化屏幕旋转的参数
        initGravitySener();

        //todo 状态栏变化的监听,暂时不使用
        if (mVidoeLayout == null)
            getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> {
                Log.e("状态栏变化", "visibility=" + visibility);
                if (visibility == View.SYSTEM_UI_FLAG_VISIBLE) {//状态栏显示SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                showToolsUI();
                } else {//状态栏隐藏
//                hideToolsUI();
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
        if (StringUtils.nonEmpty(url)) {
            //视频加载
//            mVidoe.setLocalProxyEnable(true);//开启缓存
            if (mVidoeLayout != null)
                mVidoeLayout.setVideoURI(Uri.parse(url));
        }
    }

    @Override
    protected void onActivityBack(int type) {
        if (isLandscape){
            fullScreen(false);
        }else super.onActivityBack(type);
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
        if (mVidoeLayout != null) {
            mVidoeLayout.start();
            //开启屏幕常亮
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mVidoeLayout != null) {
            mVidoeLayout.pause();
            //关闭屏幕常亮
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            IjkMediaPlayer.native_profileEnd();
        }
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorEventListener);
        getContentResolver().unregisterContentObserver(mScreenRotateObserver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mVidoeLayout != null) mVidoeLayout.cancel();
        super.onDestroy();
    }


//////////////////////////////////////////操作工具////////////////////////////////////////////////////


    /**
     * 旋转方向
     */
    public void fullScreen(boolean isFull) {
        if (isFull) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            DisplayMetrics outMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getRealMetrics(outMetrics);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
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
        if (requestedOrientation == SCREEN_DIRECTION_UNDEFINED | requestedOrientation == mScreenDirection)
            return;
        super.setRequestedOrientation(requestedOrientation);
        mScreenDirection = requestedOrientation;
    }
}
