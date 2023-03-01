package com.melvinhou.medialibrary.music.proxy;

import android.content.Context;
import android.net.Uri;
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

    void setDataSource(Context context, Uri uri) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    void prepareAsync();

    void release();

    void start();

    void stop();

    void pause();

    boolean isPlaying();

    int getVideoWidth();

    int getVideoHeight();

    void seekTo(int progress);
    int getCurrentPosition();
    int getDuration();




    void setOnPreparedListener(final OnPreparedListener l);

    void setOnErrorListener(final OnErrorListener l);

    void setOnCompletionListener(final OnCompletionListener l);

    void setOnSeekCompleteListener(final OnSeekCompleteListener l);

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
        void OnSeekComplete(IMusicPlayer mp);
    }
}
