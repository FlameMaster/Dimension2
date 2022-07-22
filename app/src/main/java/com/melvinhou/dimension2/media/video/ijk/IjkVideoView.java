package com.melvinhou.dimension2.media.video.ijk;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;
import com.jeffmony.videocache.listener.IVideoCacheListener;
import com.jeffmony.videocache.utils.ProxyCacheUtils;
import com.melvinhou.dimension2.media.video.LocalProxyVideoHelper;
import com.melvinhou.dimension2.media.video.MediaController;

import java.io.IOException;
import java.util.List;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
 * = 时 间：2022/7/18 0018 15:51
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class IjkVideoView extends ViewGroup {


    /**
     * 由ijkplayer提供，用于播放视频，需要给他传入一个surfaceView
     */
    private IMediaPlayer mMediaPlayer = null;

    //视频文件地址
    private Uri mUri, mNativeUri;

    //视频缓存
    private HttpProxyCacheServer mProxy;
    private LocalProxyVideoHelper mProxyHelper;
    //画面容器
    private SurfaceView mSurfaceView;
    //控制器
    private VideoController mController;

    public MediaController getController() {
        return mController;
    }

    //当前屏幕比例
    private float mScreenRate = 16f / 9f;

    /**
     * 视频比例
     *
     * @param rate
     */
    public void setScreenRate(float rate) {
        mScreenRate = rate;
        invalidate();
    }

    //合并监听
    private IjkPlayerListener listener;

    /**
     * 播放器的监听
     *
     * @param listener
     */
    public void setPlayerListener(IjkPlayerListener listener) {
        this.listener = listener;
    }

    private Context mContext;


    public IjkVideoView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public IjkVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public IjkVideoView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //测量所有子控件的宽和高,只有先测量了所有子控件的尺寸，后面才能使用child.getMeasuredWidth()
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//        super.onLayout(changed, left, top, right, bottom);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                int childLeft = left;
                int childTop = top;
                int width = right - left;
                int height = bottom - top;
                //显示器
                if (child instanceof SurfaceView) {
                    float windowRate = (float) width / (float) height;
                    if (mScreenRate > windowRate) {
                        height = (int) (width / mScreenRate);
                    } else {
                        width = (int) (height * mScreenRate);
                    }
                    childLeft = (right - left - width) / 2;
                    childTop = (bottom - top - height) / 2;
                }
                child.layout(childLeft, childTop, childLeft + width, childTop + height);
            }
        }
    }

    private void init(Context context) {
        mContext = context;
        mController = new VideoController();
        mProxyHelper = LocalProxyVideoHelper.getInstance();
    }

    /**
     * 创建播放器的控件
     */
    private void createPlayerChild() {
        //生成一个新的surface view
        mSurfaceView = new SurfaceView(mContext);
        mSurfaceView.getHolder().addCallback(new LmnSurfaceCallback());
//        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT
//                , LayoutParams.MATCH_PARENT, Gravity.CENTER);
//        mSurfaceView.setLayoutParams(layoutParams);
        addView(mSurfaceView);
//        SeekBar seekBar = new SeekBar(mContext);
//        LayoutParams layoutParams2 = new LayoutParams(LayoutParams.MATCH_PARENT
//                , LayoutParams.WRAP_CONTENT, Gravity.CENTER);
//        seekBar.setLayoutParams(layoutParams2);
//        addView(seekBar);
    }


    /**
     * 获取播放的uri
     *
     * @return
     */
    public Uri getUri() {
        //先从本地获取
        if (mNativeUri != null)
            return mNativeUri;
        return mUri;
    }

    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }

    /**
     * 设置播放的uri
     *
     * @param uri
     */
    public void setVideoURI(Uri uri) {
        if (uri == null) return;
        Log.e("IJK播放器", "url=" + uri);
        //获取uri参数
        List<String> pathSegList = uri.getPathSegments();
//        if (pathSegList != null && pathSegList.size() > 0)
//            fileName = pathSegList.get(pathSegList.size() - 1);
        String scheme = uri.getScheme();
        if ("http".equals(scheme) || "https".equals(scheme)) {
            //videochahe
//            mProxy = VideoCacheHelper.getProxy();
//            String path = uri.toString();
//            if (listener != null) {
//                mProxy.registerCacheListener(listener, path);
//            }
//            mNativeUri = Uri.parse(mProxy.getProxyUrl(path));

            //构建本地代理url
            String playUrl = ProxyCacheUtils.getProxyUrl(uri.toString(), null, null);
            mNativeUri = Uri.parse(playUrl);
            //请求放在客户端,便于控制
            mProxyHelper.startRequestVideoInfo(uri.toString(),listener);
        } else {
            mNativeUri = uri;
        }
        mUri = uri;

        //首次加载
        if (getChildCount() == 0) createPlayerChild();
        else openMediaPlayer();
    }

    /**
     * 播放准备
     */
    private void openMediaPlayer() {

        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.setDisplay(null);
            mMediaPlayer.release();
        } else createMediaPlayer();

        //监听
        if (listener != null) {
            mMediaPlayer.setOnInfoListener(listener);//有信息或者警告,获取视频相关的元信息里视频旋转角度
            mMediaPlayer.setOnSeekCompleteListener(listener);//拖动进度条
            mMediaPlayer.setOnBufferingUpdateListener(listener);//当网络播放的流发生变化时
            mMediaPlayer.setOnErrorListener(listener);//发生错误
            mMediaPlayer.setOnTimedTextListener(listener);//媒体时间数据
            mMediaPlayer.setOnVideoSizeChangedListener(listener);//初次获取视频大小或者发生变化
            mMediaPlayer.setOnCompletionListener(listener);//播放完
        }

        try {
            mMediaPlayer.setDataSource(mContext, getUri());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setDisplay(mSurfaceView.getHolder());//给mediaPlayer设置视图
        mMediaPlayer.prepareAsync();//异步准备
    }

    /**
     * 创建一个新的player
     */
    private void createMediaPlayer() {
        IjkMediaPlayer ijkMediaPlayer = new IjkMediaPlayer();
        ijkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_DEBUG);

        //常亮
        ijkMediaPlayer.setScreenOnWhilePlaying(true);
//        //开启硬解码
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
        //重连模式
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1);
        // 视频的话，设置100帧即开始播放
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 100);
        //设置缓冲区为100KB
//        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", 1000 * 1024);

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 25 * 1024);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "threads", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync-av-start", 0);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
        // 属性设置支持，转入我们自定义的播放类
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "ijkio,crypto,file,http,https,tcp,tls,udp");

//        ijkMediaPlayer.setAndroidIOCallback(IAndroidIO.getInstance());

        mMediaPlayer = ijkMediaPlayer;

        //播放准备完毕监听
        mMediaPlayer.setOnPreparedListener(iMediaPlayer -> {
            setScreenRate((float) iMediaPlayer.getVideoWidth() / (float) iMediaPlayer.getVideoHeight());
            if (listener!=null) listener.onPrepared(iMediaPlayer);
        });
    }

    /**
     * surfaceView的监听器
     */
    private class LmnSurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            //surfaceview创建成功后，加载视频
            openMediaPlayer();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    }

    /**
     * 合并监听
     */
    public abstract static class IjkPlayerListener implements IMediaPlayer.OnPreparedListener,
            IMediaPlayer.OnInfoListener,
            IMediaPlayer.OnSeekCompleteListener,
            IMediaPlayer.OnBufferingUpdateListener,
            IMediaPlayer.OnErrorListener,
            IMediaPlayer.OnTimedTextListener,
            IMediaPlayer.OnVideoSizeChangedListener,
            IMediaPlayer.OnCompletionListener,
            CacheListener, IVideoCacheListener {
    }

    /**
     * 控制器
     */
    private class VideoController implements MediaController {

        @Override
        public void start() {
            if (mMediaPlayer != null)
                mMediaPlayer.start();
            mProxyHelper.resumeLocalProxyTask();
        }

        @Override
        public void pause() {
            if (mMediaPlayer != null)
                mMediaPlayer.pause();
            mProxyHelper.pauseLocalProxyTask();
        }

        @Override
        public void stop() {
            if (mMediaPlayer != null)
                mMediaPlayer.stop();
        }

        @Override
        public void reset() {
            if (mMediaPlayer != null)
                mMediaPlayer.reset();
        }

        @Override
        public void release() {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
            if (null != listener && mProxy != null)
                mProxy.unregisterCacheListener(listener);

            mProxyHelper.releaseLocalProxyResources();
        }

        @Override
        public boolean isPlaying() {
            return mMediaPlayer != null && mMediaPlayer.isPlaying();
        }

        @Override
        public void seekTo(long msec) {

            // 拖动进度条
            long totalDuration = mMediaPlayer.getDuration();
            if (totalDuration > 0) {
                float percent = msec * 1.0f / totalDuration;
                mProxyHelper.seekToCachePosition(percent);
            }


            if (mMediaPlayer != null)
                mMediaPlayer.seekTo(msec);
        }

        @Override
        public long getDuration() {
            if (mMediaPlayer != null)
                return mMediaPlayer.getDuration();
            return 0;
        }

        @Override
        public long getCurrentPosition() {
            if (mMediaPlayer != null)
                return mMediaPlayer.getCurrentPosition();
            return 0;
        }
    }
}
