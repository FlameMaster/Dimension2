package com.melvinhou.medialibrary.music.proxy;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.io.IOException;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/3/1 0001 16:16
 * <p>
 * = 分 类 说 明：音乐播放器播放实现
 * ================================================
 */
public class MusicPlayerProxy implements IMusicPlayer {

    private static final String TAG = MusicPlayerProxy.class.getSimpleName();

    //媒体播放器对象
    private MediaPlayer mMediaPlayer;

    //当前状态
    @PlaybackStateCompat.State
    private int mCurrentState = PlaybackStateCompat.STATE_NONE;

    //播放器监听
    private IMusicPlayer.OnPreparedListener mOutOnPreparedListener;
    private IMusicPlayer.OnErrorListener mOutOnErrorListener;
    private IMusicPlayer.OnCompletionListener mOutOnCompletionListener;
    private IMusicPlayer.OnSeekCompleteListener mOutOnSeekCompleteListener;
    private IMusicPlayer.OnPlaybackStateListener mOnPlaybackStateListener;
    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            Log.i(TAG, "onPrepared");
            updateState(PlaybackStateCompat.STATE_STOPPED);
            pause();
            if (mOutOnPreparedListener != null) {
                mOutOnPreparedListener.onPrepared(MusicPlayerProxy.this);
            }
        }
    };
    private MediaPlayer.OnErrorListener mOnErrorListener = new MediaPlayer.OnErrorListener() {
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.w(TAG, "onError: what/extra: " + what + "/" + extra);
            updateState(PlaybackStateCompat.STATE_ERROR);
            release();
            if (mOutOnErrorListener != null) {
                mOutOnErrorListener.onError(MusicPlayerProxy.this, what, extra);
            }
            return true;
        }
    };
    private MediaPlayer.OnInfoListener mOnInfoListener = new MediaPlayer.OnInfoListener() {
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            Log.w(TAG, "onInfo: what/extra: " + what + "/" + extra);
            return false;
        }
    };
    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            Log.i(TAG, "onCompletion");
            updateState(PlaybackStateCompat.STATE_STOPPED);
            if (mOutOnCompletionListener != null) {
                mOutOnCompletionListener.onCompletion(MusicPlayerProxy.this);
            }
        }
    };

    private MediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mp) {
            Log.i(TAG, "onSeekComplete：position = " + mp.getCurrentPosition());
            if (mOutOnSeekCompleteListener != null) {
                mOutOnSeekCompleteListener.onSeekComplete(MusicPlayerProxy.this);
            }

        }
    };


    public MusicPlayerProxy() {
        //播放器
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
        mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
        mMediaPlayer.setOnErrorListener(mOnErrorListener);
        mMediaPlayer.setOnInfoListener(mOnInfoListener);
        mMediaPlayer.setOnSeekCompleteListener(mOnSeekCompleteListener);
    }

    // 这是玩家状态机的主要减速机。
    private void updateState(@PlaybackStateCompat.State int newPlayerState) {
        mCurrentState = newPlayerState;
        if (mOnPlaybackStateListener != null) {
            mOnPlaybackStateListener.onPlaybackStateChange(newPlayerState);
        }
    }

    @Override
    public void setDataSource(Context context, Uri uri) {
        Log.i(TAG, "setDataSource：" + uri.toString());
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.reset();//把各项参数恢复到初始状态
                mMediaPlayer.setDataSource(context, uri);
                prepareAsync();
            } catch (Exception ex) {
                Log.w(TAG, "setDataSource: ex = " + ex.getMessage());
                ex.printStackTrace();
                updateState(PlaybackStateCompat.STATE_ERROR);
            }
        }
    }

    @Override
    public void prepareAsync() {
        if (mMediaPlayer != null) {
            mMediaPlayer.prepareAsync();
        }
    }

    @Override
    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            updateState(PlaybackStateCompat.STATE_NONE);
        }
        Log.i(TAG, "onRelease");
    }

    @Override
    public void start() {
        if (mMediaPlayer != null && !isPlaying()) {
            mMediaPlayer.start();
            updateState(PlaybackStateCompat.STATE_PLAYING);
        }
    }

    @Override
    public void stop() {
        // 无论MediaPlayer是否已创建/启动，都必须更新状态，以便MediaNotificationManager可以关闭通知。
        updateState(PlaybackStateCompat.STATE_STOPPED);
        release();
    }

    @Override
    public void pause() {
        if (mMediaPlayer != null && isPlaying()) {
            mMediaPlayer.pause();
            updateState(PlaybackStateCompat.STATE_PAUSED);
        }
    }

    @Override
    public boolean isPlaying() {
        boolean isplaying = false;
        try {
            isplaying = mMediaPlayer != null && mMediaPlayer.isPlaying();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return isplaying;
        }
    }

    @Override
    public void seekTo(int progress) {
        if (mMediaPlayer != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mMediaPlayer.seekTo(progress, MediaPlayer.SEEK_CLOSEST);
            } else {
                mMediaPlayer.seekTo(progress);
            }
            // 设置状态(为当前状态)，因为位置改变了，应该报告给客户端。
            updateState(mCurrentState);
        }
    }

    @Override
    public int getCurrentPosition() {
        int position = 0;
        try {
            position = mMediaPlayer != null ? mMediaPlayer.getCurrentPosition() : 0;
        } finally {
            return position;
        }
    }

    @Override
    public int getDuration() {
        return mMediaPlayer != null ? mMediaPlayer.getDuration() : 0;
    }

    /**
     * 设置声音大小
     *
     * @param volume
     */
    @Override
    public void setVolume(float volume) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(volume, volume);
        }
    }

    /**
     * 播放倍率
     *
     * @return
     */
    @Override
    public float getSpeed() {
        float speed = 1.0f;
        try {
            if (mMediaPlayer.getPlaybackParams() != null)
                speed = mMediaPlayer.getPlaybackParams().getSpeed();
        } finally {
            return speed;
        }
    }

    @Override
    public void setSpeed(float speed) {
        if (mMediaPlayer != null) {
            PlaybackParams params = mMediaPlayer.getPlaybackParams();
            params.setSpeed(speed);
            mMediaPlayer.setPlaybackParams(params);
        }
        updateState(getCurrentPosition());
    }

    @Override
    public void setOnPreparedListener(OnPreparedListener l) {
        mOutOnPreparedListener = l;
    }

    @Override
    public void setOnErrorListener(OnErrorListener l) {
        mOutOnErrorListener = l;

    }

    @Override
    public void setOnCompletionListener(OnCompletionListener l) {
        mOutOnCompletionListener = l;

    }

    @Override
    public void setOnSeekCompleteListener(OnSeekCompleteListener l) {
        mOutOnSeekCompleteListener = l;
    }

    @Override
    public void setOnPlaybackStateListener(OnPlaybackStateListener l) {
        mOnPlaybackStateListener = l;
    }

}
