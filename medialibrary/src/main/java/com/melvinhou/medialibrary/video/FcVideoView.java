package com.melvinhou.medialibrary.video;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import com.melvinhou.medialibrary.video.proxy.IPlayer;
import com.melvinhou.medialibrary.video.proxy.VideoPlayerProxy;
import com.melvinhou.medialibrary.video.util.ScreenUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class FcVideoView extends TextureView {

    private static final String TAG = FcVideoView.class.getSimpleName();

    @IntDef({
            STATE_ERROR,
            STATE_IDLE,
            STATE_PREPARING,
            STATE_PREPARED,
            STATE_PLAYING,
            STATE_PAUSED,
            STATE_PLAYBACK_COMPLETED,
            STATE_STOPPED,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface PlayerState {
    }

    //播放器状态:错误-1 默认0 准备中1 准备完毕2 播放中3 暂停4 播放完毕5 停止6
    public static final int STATE_ERROR = -1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_PREPARING = 1;
    public static final int STATE_PREPARED = 2;
    public static final int STATE_PLAYING = 3;
    public static final int STATE_PAUSED = 4;
    public static final int STATE_PLAYBACK_COMPLETED = 5;
    public static final int STATE_STOPPED = 6;

    //当前状态
    private int mCurrentState = STATE_IDLE;

    private Context mContext;
    //显示器
    private Surface mSurface;
    //播放器
    private VideoPlayerProxy mMediaPlayer;

    //地址
    private Uri mUri;
    //宽高和旋转参数
    private int mVideoWidth;
    private int mVideoHeight;
    private int mVideoRotationDegree;

    //播放器监听
    private IPlayer.OnPreparedListener mOutOnPreparedListener;
    private IPlayer.OnErrorListener mOutOnErrorListener;
    private IPlayer.OnCompletionListener mOutOnCompletionListener;
    private IPlayer.OnSeekCompleteListener mOnSeekCompleteListener;
    private IPlayer.OnPreparedListener mOnPreparedListener = new IPlayer.OnPreparedListener() {
        public void onPrepared(IPlayer mp) {
            updateState(STATE_PREPARED);
            mVideoHeight = mp.getVideoHeight();
            mVideoWidth = mp.getVideoWidth();
            Log.i(TAG, "onPrepared mVideoWidth: " + mVideoWidth
                    + " mVideoHeight: " + mVideoHeight
                    + " mVideoRotationDegree: " + mVideoRotationDegree);
            if (mOutOnPreparedListener != null) {
                mOutOnPreparedListener.onPrepared(mp);
            }
        }
    };
    private IPlayer.OnErrorListener mOnErrorListener = new IPlayer.OnErrorListener() {
        public boolean onError(IPlayer mp, int what, int extra) {
            Log.w(TAG, "onError: what/extra: " + what + "/" + extra);
            updateState(STATE_ERROR);
            stop_l();
            if (mOutOnErrorListener != null) {
                mOutOnErrorListener.onError(mp, what, extra);
            }
            return true;
        }
    };
    private IPlayer.OnInfoListener mOnInfoListener = new IPlayer.OnInfoListener() {
        public void onInfo(IPlayer mp, int what, int extra) {
            Log.w(TAG, "onInfo: what/extra: " + what + "/" + extra);
            if (what == 10001) { // IJK: MEDIA_INFO_VIDEO_ROTATION_CHANGED
                // 有些视频拍摄的时候有角度，需要做旋转，默认ijk是不会做的，这里自己实现
                mVideoRotationDegree = extra;
                setRotation(mVideoRotationDegree);
                requestLayout();
            }
        }
    };
    private IPlayer.OnCompletionListener mOnCompletionListener = new IPlayer.OnCompletionListener() {
        public void onCompletion(IPlayer mp) {
            Log.i(TAG, "onCompletion");
            updateState(STATE_PLAYBACK_COMPLETED);
            if (mOutOnCompletionListener != null) {
                mOutOnCompletionListener.onCompletion(mp);
            }
        }
    };
    private IPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener = new IPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IPlayer mp, int width, int height) {
            // TUIChatLog.i(TAG, "onVideoSizeChanged width: " + width + " height: " + height);
        }
    };

    private IPlayer.OnSeekCompleteListener onSeekCompleteListener = new IPlayer.OnSeekCompleteListener() {
        @Override
        public void OnSeekComplete(IPlayer mp) {
            if (mOnSeekCompleteListener != null) {
                mOnSeekCompleteListener.OnSeekComplete(mp);
            }
        }
    };

    //显示器监听
    private SurfaceTextureListener mSurfaceTextureListener = new SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.i(TAG, "onSurfaceTextureAvailable");
            mSurface = new Surface(surface);
            openVideo();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.i(TAG, "onSurfaceTextureSizeChanged");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            Log.i(TAG, "onSurfaceTextureDestroyed");
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            // TUIChatLog.i(TAG,"onSurfaceTextureUpdated");
        }
    };


    public void setOnPreparedListener(IPlayer.OnPreparedListener l) {
        mOutOnPreparedListener = l;
    }

    public void setOnSeekCompleteListener(IPlayer.OnSeekCompleteListener l) {
        mOnSeekCompleteListener = l;
    }

    public void setOnErrorListener(IPlayer.OnErrorListener l) {
        mOutOnErrorListener = l;
    }

    public void setOnCompletionListener(IPlayer.OnCompletionListener l) {
        mOutOnCompletionListener = l;
    }


    public FcVideoView(Context context) {
        super(context);
        initVideoView(context);
    }

    public FcVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVideoView(context);
    }

    public FcVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVideoView(context);
    }

    private void initVideoView(Context context) {
        Log.i(TAG, "initVideoView");
        mContext = context;
        setSurfaceTextureListener(mSurfaceTextureListener);
        updateState(STATE_IDLE);
    }

    /**
     * 根据视频宽高控制控件大小
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //  TUIChatLog.i(TAG, "onMeasure(" + MeasureSpec.toString(widthMeasureSpec) + ", "
        //        + MeasureSpec.toString(heightMeasureSpec) + ")"
        //        + " mVideoWidth: " + mVideoWidth
        //        + " mVideoHeight: " + mVideoHeight);

        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize;
                height = heightSpecSize;

                // for compatibility, we adjust size based on aspect ratio
                if (mVideoWidth * height < width * mVideoHeight) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * mVideoWidth / mVideoHeight;
                } else if (mVideoWidth * height > width * mVideoHeight) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / mVideoWidth;
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize;
                height = width * mVideoHeight / mVideoWidth;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize;
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize;
                width = height * mVideoWidth / mVideoHeight;
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize;
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = mVideoWidth;
                height = mVideoHeight;
                if (heightSpecMode == MeasureSpec.AT_MOST) {
                    //视频宽高都比屏幕小的时候
                    if (height < heightSpecSize && width < widthSpecSize) {
                        float ws = (float)width/(float)widthSpecSize;
                        float hs = (float)height/(float)heightSpecSize;
                        if (ws>hs){
                            width = widthSpecSize;
                            height = width * mVideoHeight / mVideoWidth;
                        }else {
                            height = heightSpecSize;
                            width = height * mVideoWidth / mVideoHeight;
                        }
                    } else {
                        if (height > heightSpecSize) {
                            // too tall, decrease both width and height
                            height = heightSpecSize;
                            width = height * mVideoWidth / mVideoHeight;
                        }
                        if (width > widthSpecSize) {
                            // too wide, decrease both width and height
                            width = widthSpecSize;
                            height = width * mVideoHeight / mVideoWidth;
                        }
                    }
                }
            }
        } else {
            // no size yet, just adopt the given spec sizes
        }
        Log.i(TAG, "onMeasure width: " + width + " height: " + height + " rotation degree: " + mVideoRotationDegree);
        setMeasuredDimension(width, height);
        if ((mVideoRotationDegree + 180) % 180 != 0) {
            // 画面旋转之后需要缩放，而且旋转之后宽高的计算都要换为高宽。
            int[] size = ScreenUtil.scaledSize(widthSpecSize, heightSpecSize, height, width);
            Log.i(TAG, "onMeasure scaled width: " + size[0] + " height: " + size[1]);
            setScaleX(size[0] / ((float) height));
            setScaleY(size[1] / ((float) width));
        }
    }

    public void setVideoURI(Uri uri) {
        mUri = uri;
        openVideo();
    }

    public void resetVideo() {
        openVideo();
    }

    private void openVideo() {
        if (mUri == null) {
            Log.e(TAG, "openVideo: mUri is null ");
            return;
        }
        Log.i(TAG, "openVideo: mUri: " + mUri.getPath() + " mSurface: " + mSurface);
        if (mSurface == null) {
            Log.e(TAG, "openVideo: mSurface is null ");
            return;
        }

        stop_l();
        try {
            mMediaPlayer = new VideoPlayerProxy();
            mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
            mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
            mMediaPlayer.setOnErrorListener(mOnErrorListener);
            mMediaPlayer.setOnInfoListener(mOnInfoListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
            mMediaPlayer.setOnSeekCompleteListener(mOnSeekCompleteListener);
            mMediaPlayer.setSurface(mSurface);
            mMediaPlayer.setDataSource(getContext(), mUri);
            mMediaPlayer.prepareAsync();
            updateState(STATE_PREPARING);
        } catch (Exception ex) {
            Log.w(TAG, "ex = " + ex.getMessage());
            updateState(STATE_ERROR);
        }

    }

    public boolean start() {
        Log.i(TAG, "start mCurrentState:" + mCurrentState);
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
            updateState(STATE_PLAYING);
        }
        return true;
    }

    public boolean stop() {
        Log.i(TAG, "stop mCurrentState:" + mCurrentState);
        stop_l();
        return true;
    }

    public boolean pause() {
        Log.i(TAG, "pause mCurrentState:" + mCurrentState);
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
            updateState(STATE_PAUSED);
        }
        return true;
    }

    public void stop_l() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            updateState(STATE_STOPPED);
            mMediaPlayer.release();
            mMediaPlayer = null;
            updateState(STATE_IDLE);
        }
    }

    public boolean isPlaying() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    public void seekTo(int progress) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(progress);
        }
    }

    public boolean isPrepared() {
        if (mUri == null) {
            Log.e(TAG, "isPrepared: mUri is null ");
            return false;
        }
        Log.i(TAG, "isPrepared: mUri: " + mUri.getPath() + " mSurface: " + mSurface);
        if (mSurface == null) {
            Log.e(TAG, "isPrepared: mSurface is null ");
            return false;
        }

        return true;
    }

    public int getCurrentPosition() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    private void updateState(@PlayerState int state) {
        mCurrentState = state;
        if (mPlayerStateListener != null)
            mPlayerStateListener.onChanged(state);
    }

    public int getState() {
        return mCurrentState;
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N && background != null) {
            super.setBackgroundDrawable(background);
        }
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);
    }


    /**
     * 播放器状态变化的监听
     */
    public interface PlayerStateListener {
        void onChanged(@PlayerState int state);
    }

    private PlayerStateListener mPlayerStateListener;

    public void setPlayerStateListener(PlayerStateListener l) {
        mPlayerStateListener = l;
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
    }
}
