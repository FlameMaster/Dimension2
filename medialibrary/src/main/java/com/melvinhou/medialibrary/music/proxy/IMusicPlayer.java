package com.melvinhou.medialibrary.music.proxy;

import android.content.Context;
import android.media.PlaybackParams;
import android.net.Uri;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;

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
 * = 分 类 说 明：播放器的功能接口
 * ================================================
 */
public interface IMusicPlayer {
    //普通音量
    float MEDIA_VOLUME_DEFAULT = 1.0f;//AudioAttributes.USAGE_MEDIA
    //后台音量
    float MEDIA_VOLUME_DUCK = 0.2f;

//    void setDataSource(Context context, Uri uri) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;
    void setDataSource(Context context, Uri uri);

    void prepareAsync();

    void release();

    void start();

    void stop();

    void pause();

    boolean isPlaying();

    void setVolume(float volume);

    void seekTo(int progress);

    int getCurrentPosition();

    int getDuration();


    void setOnPreparedListener(final OnPreparedListener l);

    void setOnErrorListener(final OnErrorListener l);

    void setOnCompletionListener(final OnCompletionListener l);

    void setOnSeekCompleteListener(final OnSeekCompleteListener l);

    void setOnPlaybackStateListener(final OnPlaybackStateListener l);

    float getSpeed();

    void setSpeed(float speed);

    //准备好的监听
    interface OnPreparedListener {
        void onPrepared(IMusicPlayer mp);
    }

    //错误的监听
    interface OnErrorListener {
        boolean onError(IMusicPlayer mp, int what, int extra);
    }

    //播放完的监听
    interface OnCompletionListener {
        void onCompletion(IMusicPlayer mp);
    }

    //进度跳转的监听
    interface OnSeekCompleteListener {
        void onSeekComplete(IMusicPlayer mp);
    }

    //进度跳转的监听
    interface OnPlaybackStateListener {
        void onPlaybackStateChange(@PlaybackStateCompat.State int state);
    }
}
