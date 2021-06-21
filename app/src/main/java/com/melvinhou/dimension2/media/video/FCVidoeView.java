package com.melvinhou.dimension2.media.video;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

import com.melvinhou.kami.model.EventMessage;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.rxjava.RxBus;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2019/5/14 19:55
 * <p>
 * = 分 类 说 明：自定义视频播放器，使用TextureView来显示，MediaPlayer控制媒体
 * ============================================================
 */
public class FCVidoeView extends TextureView implements TextureView.SurfaceTextureListener {


    public static final int STATE_ERROR = -1;          // 播放错误
    public static final int STATE_IDLE = 0;            // 播放未开始
    public static final int STATE_PREPARING = 1;       // 播放准备中
    public static final int STATE_PREPARED = 2;        // 播放准备就绪
    public static final int STATE_PLAYING = 3;         // 正在播放
    public static final int STATE_PAUSED = 4;          // 暂停播放
    // 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，缓冲区数据足够后恢复播放)
    public static final int STATE_BUFFERING_PLAYING = 5;
    // 正在缓冲(播放器正在播放时，缓冲区数据不足，进行缓冲，此时暂停播放器，继续缓冲，缓冲区数据足够后恢复暂停)
    public static final int STATE_BUFFERING_PAUSED = 6;
    public static final int STATE_COMPLETED = 7;       // 播放完成


    public static final int PLAYER_NORMAL = 10;        // 普通播放器
    public static final int PLAYER_FULL_SCREEN = 11;   // 全屏播放器
    public static final int PLAYER_TINY_WINDOW = 12;   // 小窗口播放器

    public static final int TYPE_DISPLAYER_TENSILE = 0;       // 默认拉伸
    public static final int TYPE_DISPLAYER_INSIDE = 1;        // 居中原比例
    public static final int TYPE_DISPLAYER_CROP = 2;          // 裁剪居中
    public static final int TYPE_DISPLAYER_RATIO = 3;         // 按比例拉伸

    //存储位置：私有目录无法存储目录文件
//    public static final int PATH_ROOT = FileUtils.MEMORY_ADDRESS_EMULATED;
    //子路径
    private final static String PATH_VIDEO = "video";

    private MediaPlayer mMediaPlayer;
    private Uri mUri, mNativeUri;
    private int mCurrentState, mPlayerState, mBufferPercentage;
    private int mDisplayerType;
    private int mVideoWidth, mVideoHeight;
    private boolean isLooping, isOpenSound;
    private CurrentStateChangeListener mCurrentStateChangeListener;

    public void setCurrentStateChangeListener(CurrentStateChangeListener listener) {
        this.mCurrentStateChangeListener = listener;
    }

    /**
     * 设置初始化时显示模式，不能直接设置比例模式
     *
     * @param displayerType
     */
    public void setInitDisplayerType(int displayerType) {
        this.mDisplayerType = displayerType;
    }

    /**
     * 当前播放状态
     *
     * @return
     */
    public int getCurrentState() {
        return mCurrentState;
    }

    public FCVidoeView(Context context) {
        this(context, null);
    }

    public FCVidoeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FCVidoeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mCurrentState = STATE_IDLE;
        mPlayerState = PLAYER_NORMAL;
        mDisplayerType = TYPE_DISPLAYER_INSIDE;
        isLooping = true;
        isOpenSound = true;
        mBufferPercentage = 0;
        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int width = getDefaultSize(-1, widthMeasureSpec);
//        int height = getDefaultSize(-1, heightMeasureSpec);
//        setMeasuredDimension(width,height);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 初始化控制器
     */
    private void initMediaPlayer() {
        if (mMediaPlayer == null) {
            try {
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setScreenOnWhilePlaying(true);
                mMediaPlayer.setLooping(isLooping);
                mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
                mMediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
                mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
                mMediaPlayer.setOnErrorListener(mOnErrorListener);
                mMediaPlayer.setOnInfoListener(mOnInfoListener);
                mMediaPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        setSurfaceTextureListener(this);
    }

    /**
     * 更新当前状态
     *
     * @param state
     */
    private void updataCurrentState(int state) {
        mCurrentState = state;
        if (mCurrentStateChangeListener != null)
            mCurrentStateChangeListener.onChanged(mCurrentState);
//            mController.setControllerState(mPlayerState, mCurrentState);
//            RxBus.get().post(new EventMessage("FCVidoe_Complete",getId()));
    }

/////////////////////////———————————————————外部操作类——————————————————————/////////////////////////

    /**
     * 获取进度
     * @return
     */
    public int getProgress(){
        return mMediaPlayer.getCurrentPosition();
    }
    public int getMaxProgress(){
        return mMediaPlayer.getDuration();
    }

    /**
     * 设置播放的uri
     *
     * @param uri
     */
    public void setVideoURI(Uri uri) {
        if (uri == null || (!uri.equals(mUri)))
            mNativeUri = null;
        mUri = uri;
        //本地缓存
        /*
        if (mUri != null && !SharePrefUtil.getBoolean(mUri.getPath(), false))
            FileUtils.downFile(uri.toString(), PATH_ROOT,
                    PATH_VIDEO, StringUtils.md5(uri.getPath()), new Observer<String>() {
                        @Override
                        public void onSubscribe(Disposable d) {
//                        SharePrefUtil.saveBoolean(uri.getPath(), true);
                        }

                        @Override
                        public void onNext(String s) {
                            //下载成功,存储状态
                            SharePrefUtil.saveBoolean(uri.getPath(), true);//当前地址是否存在
//                        SharePrefUtil.saveString(KEY_ZIP_PATH, s);//当前文件的路径
                        }

                        @Override
                        public void onError(Throwable e) {
                            //出现异常时弃置
                            SharePrefUtil.saveBoolean(uri.getPath(), false);
                            e.printStackTrace();
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        */
    }

    /**
     * 开始播放
     */
    public void start() {
        if (mUri == null) return;
        //重新播放
        if (mMediaPlayer == null) {
            play();
        }
        //开始播放
        else if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.setLooping(isLooping);
            mMediaPlayer.setVolume(isOpenSound ? 1 : 0f, isOpenSound ? 1 : 0f);
            mMediaPlayer.start();
            updataCurrentState(STATE_PLAYING);
        }
    }

    /**
     * 手动播放
     */
    public void play() {
        if (mUri == null) return;
        SurfaceTexture surface = getSurfaceTexture();
        if (surface == null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                surface = new SurfaceTexture(false);
            } else {
                surface = new SurfaceTexture(0);
            }
        openMediaPlayer(surface);
    }

    public void changeSound(boolean isOpen) {
        isOpenSound = isOpen;
    }

    /**
     * 切换屏幕显示模式
     *
     * @param type   类型模式
     * @param ratioW 比例宽
     * @param ratioH 比例高，平时没用，只有有用
     */
    public void changeDisplayerType(int type, float ratioW, float ratioH) {
        mDisplayerType = type;
        if (type == TYPE_DISPLAYER_INSIDE)
            updateTextureViewSizeCenter();
        else if (type == TYPE_DISPLAYER_TENSILE)
            updateTextureViewSizefitXY();
        else if (type == TYPE_DISPLAYER_CROP)
            updateTextureViewSizeCenterCrop();
        else if (type == TYPE_DISPLAYER_RATIO)
            updateTextureViewSizeRatio(ratioW, ratioH);
    }

    public void changeDisplayerType(int type) {
        changeDisplayerType(type, 0, 0);
    }

    /**
     * 暂停
     */
    public void pause() {
        if (mMediaPlayer == null) return;
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            updataCurrentState(STATE_PAUSED);
        }
    }

    /**
     * 停止
     */
    public void stop() {
        if (mMediaPlayer == null) return;
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            updataCurrentState(STATE_COMPLETED);
        }
    }

    /**
     * 跳转播放
     * @param sec
     */
    public void seekTo(int sec){
        mMediaPlayer.seekTo(sec);
    }

    /**
     * 清除数据
     */
    public void clear() {
        if (mMediaPlayer == null) return;
        stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    /**
     * 设置循环播放
     *
     * @param looping
     */
    public void setLooping(boolean looping) {
        isLooping = looping;
    }

    /**
     * 获取播放的uri
     *
     * @return
     */
    public Uri getUri() {
        /*
        if (mUri != null
                && mNativeUri == null
                && SharePrefUtil.getBoolean(mUri.getPath(), false)) {
            File file = FileUtils.getFilePath(PATH_VIDEO, StringUtils.md5(mUri.getPath()));
            if (file.exists())
                mNativeUri = Uri.fromFile(file);
        }
        */
        //先从本地获取
        if (mNativeUri != null)
            return mNativeUri;
        return mUri;
    }

/////////////////////////———————————————————监听——————————————————————/////////////////////////

    /**
     * SurfaceTexture准备就绪
     *
     * @param surface
     * @param width
     * @param height
     */
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//        FCVidoeView.this.surface = new Surface(surface);
        openMediaPlayer(surface);
    }

    /**
     * SurfaceTexture缓冲大小变化
     *
     * @param surface
     * @param width
     * @param height
     */
    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    /**
     * SurfaceTexture即将被销毁
     *
     * @param surface
     * @return
     */
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//        FCVidoeView.this.surface = null;
        clear();
        return true;
//        return false;
    }

    /**
     * SurfaceTexture通过updateImage更新
     *
     * @param surface
     */
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

/////////////////////////———————————————————其它——————————————————————/////////////////////////

    /**
     * 开始播放
     *
     * @param surface
     */
    private void openMediaPlayer(SurfaceTexture surface) {
        if (mMediaPlayer == null) {
            initMediaPlayer();
        }
        try {
            mMediaPlayer.reset();//把各项参数恢复到初始状态
            mMediaPlayer.setDataSource(FcUtils.getContext(), getUri());
            mMediaPlayer.setSurface(new Surface(surface));
            mMediaPlayer.prepareAsync();
//                          mMediaPlayer.prepare();    //进行缓冲
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 重新计算video的显示位置，裁剪后全屏显示
     */
    private void updateTextureViewSizeCenterCrop() {

        float sx = (float) getMeasuredWidth() / (float) mVideoWidth;
        float sy = (float) getMeasuredHeight() / (float) mVideoHeight;

        Matrix matrix = new Matrix();
        float maxScale = Math.max(sx, sy);

        //第1步:把视频区移动到View区,使两者中心点重合.
        matrix.preTranslate((getMeasuredWidth() - mVideoWidth) / 2, (getMeasuredHeight() - mVideoHeight) / 2);

        //第2步:因为默认视频是fitXY的形式显示的,所以首先要缩放还原回来.
        matrix.preScale(mVideoWidth / (float) getMeasuredWidth(), mVideoHeight / (float) getMeasuredHeight());

        //第3步,等比例放大或缩小,直到视频区的一边超过View一边, 另一边与View的另一边相等. 因为超过的部分超出了View的范围,所以是不会显示的,相当于裁剪了.
        matrix.postScale(maxScale, maxScale, getMeasuredWidth() / 2, getMeasuredHeight() / 2);//后两个参数坐标是以整个View的坐标系以参考的

        setTransform(matrix);
        postInvalidate();
    }

    /**
     * 适应屏幕（原比例）
     */
    private void updateTextureViewSizeCenter() {

        float sx = (float) getWidth() / (float) mVideoWidth;
        float sy = (float) getHeight() / (float) mVideoHeight;

        Matrix matrix = new Matrix();

        //第1步:把视频区移动到View区,使两者中心点重合.
        matrix.preTranslate((getWidth() - mVideoWidth) / 2, (getHeight() - mVideoHeight) / 2);

        //第2步:因为默认视频是fitXY的形式显示的,所以首先要缩放还原回来.
        matrix.preScale(mVideoWidth / (float) getWidth(), mVideoHeight / (float) getHeight());

        //第3步,等比例放大或缩小,直到视频区的一边和View一边相等.如果另一边和view的一边不相等，则留下空隙
        if (sx >= sy) {
            matrix.postScale(sy, sy, getWidth() / 2, getHeight() / 2);
        } else {
            matrix.postScale(sx, sx, getWidth() / 2, getHeight() / 2);
        }

        setTransform(matrix);
        postInvalidate();
    }

    /**
     * 拉伸
     */
    private void updateTextureViewSizefitXY() {
        Matrix matrix = new Matrix();
        matrix.preScale(1, 1);
        setTransform(matrix);
        postInvalidate();
    }

    /**
     * 按比例拉伸
     */
    private void updateTextureViewSizeRatio(float ratioW, float ratioH) {

        float width = getWidth();
        float height = getHeight();
        if (ratioW / ratioH < getWidth() / getHeight())
            width = height * ratioW / ratioH;
        else height = width * ratioH / ratioW;

        float sx = width / (float) mVideoWidth;
        float sy = height / (float) mVideoHeight;

        Matrix matrix = new Matrix();
        matrix.preTranslate((getWidth() - mVideoWidth) / 2, (getHeight() - mVideoHeight) / 2);
        matrix.preScale(mVideoWidth / (float) getWidth(), mVideoHeight / (float) getHeight());
        matrix.postScale(sx, sy, getWidth() / 2, getHeight() / 2);
        setTransform(matrix);
        postInvalidate();
    }

/////////////////////////———————————————————播放监听——————————————————————/////////////////////////

    private MediaPlayer.OnPreparedListener mOnPreparedListener
            = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            RxBus.get().post(new EventMessage("FCVidoe_Start_" + getId(), mp.getDuration()));
            mMediaPlayer.setLooping(isLooping);
            mMediaPlayer.setVolume(isOpenSound ? 1 : 0f, isOpenSound ? 1 : 0f);
            mp.start();
//            play();
            updataCurrentState(STATE_PREPARED);
        }
    };

    private MediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener
            = new MediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            mVideoHeight = mMediaPlayer.getVideoHeight();
            mVideoWidth = mMediaPlayer.getVideoWidth();

            if (mDisplayerType == TYPE_DISPLAYER_INSIDE)
                updateTextureViewSizeCenter();
            else if (mDisplayerType == TYPE_DISPLAYER_TENSILE)
                updateTextureViewSizefitXY();
            else if (mDisplayerType == TYPE_DISPLAYER_CROP)
                updateTextureViewSizeCenterCrop();
        }
    };

    private MediaPlayer.OnCompletionListener mOnCompletionListener
            = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            updataCurrentState(STATE_COMPLETED);
        }
    };

    private MediaPlayer.OnErrorListener mOnErrorListener
            = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            updataCurrentState(STATE_ERROR);
            play();
            return false;
        }
    };

    private MediaPlayer.OnInfoListener mOnInfoListener
            = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                // 播放器渲染第一帧
                updataCurrentState(STATE_PLAYING);
            } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                // MediaPlayer暂时不播放，以缓冲更多的数据
                if (mCurrentState == STATE_PAUSED || mCurrentState == STATE_BUFFERING_PAUSED) {
                    updataCurrentState(STATE_BUFFERING_PAUSED);
                } else {
                    updataCurrentState(STATE_BUFFERING_PLAYING);
                }
            } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                // 填充缓冲区后，MediaPlayer恢复播放/暂停
                if (mCurrentState == STATE_BUFFERING_PLAYING) {
                    updataCurrentState(STATE_PLAYING);
                }
                if (mCurrentState == STATE_BUFFERING_PAUSED) {
                    updataCurrentState(STATE_PAUSED);
                }
            } else {
            }
            return true;
        }
    };

    private MediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener
            = new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            mBufferPercentage = percent;
        }
    };

    /**
     * 切换状态监听
     */
    public interface CurrentStateChangeListener {
        void onChanged(int currentState);
    }
}
