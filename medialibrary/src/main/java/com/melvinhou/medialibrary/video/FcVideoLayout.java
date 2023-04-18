package com.melvinhou.medialibrary.video;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.melvinhou.kami.io.IOUtils;
import com.melvinhou.kami.util.DateUtils;
import com.melvinhou.medialibrary.R;
import com.melvinhou.medialibrary.video.proxy.IPlayer;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
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
 * = 时 间：2023/2/28 0028 14:57
 * <p>
 * = 分 类 说 明：基础播放功能的播放器
 * ================================================
 */
public class FcVideoLayout extends ConstraintLayout {
    public FcVideoLayout(@NonNull Context context) {
        super(context);
    }

    public FcVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FcVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //布局所有控件
    private View mLoadingView, mForegroundView, mControllerGroup;
    private TextView mCurrentPositionText, mDurationText, mMessageView;
    private SeekBar mProgressBar;
    private FcVideoView mVideoView;
    private ImageView mPlayButton, mFullScreenButton, mBackgroundView;
    //显示工具条计时，开始进度条计时
    private Disposable mChangeOrientationDisposable, mProgressDisposable;
    /**
     * 手势检测
     */
    private GestureDetector mGestureDetector;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initLayout();
    }

    private void initLayout() {
        int resLayout = R.layout.inset_video_group;
        findView();
        addListener();
    }

    private void findView() {
        mVideoView = findViewById(R.id.video);
        mPlayButton = findViewById(R.id.video_play);
        mControllerGroup = findViewById(R.id.video_tools);
        mFullScreenButton = findViewById(R.id.video_full_screen);
        mLoadingView = findViewById(R.id.video_loading);
        mMessageView = findViewById(R.id.video_error_message);
        mCurrentPositionText = findViewById(R.id.video_progress_text);
        mDurationText = findViewById(R.id.video_progress_max_text);
        mProgressBar = findViewById(R.id.video_progress);
        mBackgroundView = findViewById(R.id.video_background);
        mForegroundView = findViewById(R.id.video_foreground);
    }

    private void addListener() {
        if (mVideoView != null) {
            mVideoView.setOnPreparedListener(mOnPreparedListener);
            mVideoView.setOnSeekCompleteListener(onSeekCompleteListener);
            mVideoView.setOnErrorListener(mOnErrorListener);
            mVideoView.setOnCompletionListener(mOnCompletionListener);
        }
        //界面的一些手势监听
        mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {//单击确认
                if (isPlaying()) {
                    if (mForegroundView.getVisibility() != VISIBLE) {
                        showController();
                        startToolsTimer();
                    } else hideController();
                }
                return true;
            }

            //双击
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                changePlay();
                return true;
            }
        });
        setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mGestureDetector != null) mGestureDetector.onTouchEvent(event);
                return true;
            }
        });
        //进度跳转
        if (mProgressBar != null)
            mProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // 不是用户发起的变更，则不处理
                    if (!fromUser) return;
                    // 跳转播放进度
                    seekTo(progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    cleaHideTimer();
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    startToolsTimer();
                }
            });
        //播放
        if (mPlayButton != null) mPlayButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                changePlay();
            }
        });
        //全屏
        if (mFullScreenButton != null) mFullScreenButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isFull = !view.isSelected();
                view.setSelected(isFull);
                if (mVideoControllerUIListener != null)
                    mVideoControllerUIListener.onFullScreen(isFull);
            }
        });
    }


    /**
     * 隐藏控制器ui
     */
    public void hideController() {
        if (mForegroundView != null) mForegroundView.setVisibility(GONE);
        if (mControllerGroup != null) mControllerGroup.setVisibility(GONE);
        if (mPlayButton != null) mPlayButton.setVisibility(GONE);
        if (mVideoControllerUIListener != null)
            mVideoControllerUIListener.onHideControllerUI();
    }

    /**
     * 显示控制器ui
     */
    public void showController() {
        if (mForegroundView != null) mForegroundView.setVisibility(VISIBLE);
        if (mControllerGroup != null) mControllerGroup.setVisibility(VISIBLE);
        if (mPlayButton != null && mVideoView.isPrepared()) mPlayButton.setVisibility(VISIBLE);
        if (mVideoControllerUIListener != null)
            mVideoControllerUIListener.onShowControllerUI();
    }

    private void showLoading() {
        if (mLoadingView != null) mLoadingView.setVisibility(VISIBLE);
    }

    private void hideLoading() {
        if (mLoadingView != null) mLoadingView.setVisibility(GONE);
    }

    private void showMessage() {
        if (mMessageView != null) mMessageView.setVisibility(VISIBLE);
    }

    private void hideMessage() {
        if (mMessageView != null) mMessageView.setVisibility(GONE);
    }


    private IPlayer.OnPreparedListener mOutOnPreparedListener;
    private IPlayer.OnErrorListener mOutOnErrorListener;
    private IPlayer.OnCompletionListener mOutOnCompletionListener;
    private IPlayer.OnSeekCompleteListener mOnSeekCompleteListener;
    //准备好的监听
    private IPlayer.OnPreparedListener mOnPreparedListener = new IPlayer.OnPreparedListener() {
        public void onPrepared(IPlayer mp) {
            hideLoading();
            if (mPlayButton != null) mPlayButton.setVisibility(VISIBLE);
            pause();
            int maxProgress = getDuration();
            if (mProgressBar != null) {
                mProgressBar.setMax(maxProgress);
                mProgressBar.setProgress(0);
                mProgressBar.setSecondaryProgress(0);
            }
            if (mDurationText != null)
                mDurationText.setText("/" + DateUtils.formatDuration(maxProgress));
            //开始更新进度条
            if (mProgressDisposable != null) mProgressDisposable.dispose();
            mProgressDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
                    .compose(IOUtils.setThread())
                    .subscribe(residueTime -> {
                        //更新进度条
                        updataProgress();
                    });

            //外部监听
            if (mOutOnPreparedListener != null) {
                mOutOnPreparedListener.onPrepared(mp);
            }
        }
    };
    //错误的监听
    private IPlayer.OnErrorListener mOnErrorListener = new IPlayer.OnErrorListener() {
        public boolean onError(IPlayer mp, int what, int extra) {
            hideLoading();
            hideController();
            showMessage();
            if (mOutOnErrorListener != null) {
                mOutOnErrorListener.onError(mp, what, extra);
            }
            return true;
        }
    };
    //播放完的监听
    private IPlayer.OnCompletionListener mOnCompletionListener = new IPlayer.OnCompletionListener() {
        public void onCompletion(IPlayer mp) {
            //播放完
            if (mPlayButton != null) {
                mPlayButton.setVisibility(VISIBLE);
                mPlayButton.setSelected(false);
            }
            if (mOutOnCompletionListener != null) {
                mOutOnCompletionListener.onCompletion(mp);
            }
        }
    };

    //进度跳转的监听
    private IPlayer.OnSeekCompleteListener onSeekCompleteListener = new IPlayer.OnSeekCompleteListener() {
        @Override
        public void OnSeekComplete(IPlayer mp) {
            if (mOnSeekCompleteListener != null) {
                mOnSeekCompleteListener.OnSeekComplete(mp);
            }
        }
    };

    private VideoControllerUIListener mVideoControllerUIListener;


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

    public void setVideoControllerUIListener(VideoControllerUIListener l) {
        mVideoControllerUIListener = l;
    }

    public void setPlayerStateListener(FcVideoView.PlayerStateListener l) {
        if (mVideoView != null)
            mVideoView.setPlayerStateListener(l);
    }


    public void setVideoURI(Uri uri) {
        if (mVideoView != null) mVideoView.setVideoURI(uri);
        showLoading();
    }

    public boolean start() {
        if (mPlayButton != null) mPlayButton.setSelected(true);
        if (mVideoView != null) return mVideoView.start();
        return false;
    }

    public boolean stop() {
        if (mPlayButton != null) mPlayButton.setSelected(false);
        if (mVideoView != null) return mVideoView.stop();
        return false;
    }

    public boolean pause() {
        if (mPlayButton != null) mPlayButton.setSelected(false);
        if (mVideoView != null) return mVideoView.pause();
        return false;
    }

    public void stop_l() {
        if (mPlayButton != null) mPlayButton.setSelected(false);
        if (mVideoView != null) mVideoView.stop_l();
    }

    public boolean isPlaying() {
        if (mVideoView != null) return mVideoView.isPlaying();
        return false;
    }

    public void seekTo(int progress) {
        if (mVideoView != null) mVideoView.seekTo(progress);
    }

    public boolean isPrepared() {
        if (mVideoView != null) return mVideoView.isPrepared();
        return false;
    }

    public int getCurrentPosition() {
        if (mVideoView != null) return mVideoView.getCurrentPosition();
        return 0;
    }

    public int getDuration() {
        if (mVideoView != null) return mVideoView.getDuration();
        return 0;
    }


    /**
     * 更新进度条
     */
    private void updataProgress() {
        int progress = getCurrentPosition();
        if (mProgressBar != null) mProgressBar.setProgress(progress);
        if (mCurrentPositionText != null)
            mCurrentPositionText.setText(DateUtils.formatDuration(progress));
    }


    public void setBackground(Drawable background) {
        if (mBackgroundView != null) {
            mBackgroundView.setImageDrawable(background);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N && background != null) {
            super.setBackgroundDrawable(background);
        }
    }


    /**
     * 清空UI隐藏的计时器
     */
    private void cleaHideTimer() {
        if (mChangeOrientationDisposable != null) mChangeOrientationDisposable.dispose();
    }

    //隐藏ui的计时器
    private void startToolsTimer() {
        cleaHideTimer();
        mChangeOrientationDisposable = Observable.timer(3, TimeUnit.SECONDS)
                .compose(IOUtils.setThread())
                .subscribe(aLong -> {
                    if (isPlaying()) {
                        hideController();
                    }
                });
    }

    //切换播放状态
    private void changePlay() {
        if (isPlaying()) {
            pause();
            showController();
        } else {
            start();
            startToolsTimer();
        }
    }

    //清空
    public void cancel() {
        cleaHideTimer();
        if (mProgressDisposable != null) mProgressDisposable.dispose();
        stop_l();
    }

    /**
     * 屏幕是否全屏
     *
     * @param isFull
     */
    public void setFullScreen(boolean isFull) {
        if (mFullScreenButton != null)
            mFullScreenButton.setSelected(isFull);
    }


    /**
     * 当前布局的ui变化监听
     */
    public interface VideoControllerUIListener {
        void onShowControllerUI();

        void onHideControllerUI();

        void onFullScreen(boolean isFull);
    }

}
