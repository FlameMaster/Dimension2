package com.melvinhou.medialibrary.video.proxy;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
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
 * = 分 类 说 明：播放器的选择
 * ================================================
 */
public class VideoPlayerProxy implements IPlayer {

    private static final String TAG = VideoPlayerProxy.class.getSimpleName();

    private IPlayer mMediaPlayer;

    public VideoPlayerProxy() {
        try {
            Class.forName("tv.danmaku.ijk.media.player.IjkMediaPlayer").newInstance();
            mMediaPlayer = new IjkMediaPlayerWrapper();
        } catch (Exception e) {
            mMediaPlayer = new SystemMediaPlayerWrapper();
        }
        Log.i(TAG, "use mMediaPlayer: " + mMediaPlayer);
    }

    @Override
    public void setOnPreparedListener(final OnPreparedListener l) {
        mMediaPlayer.setOnPreparedListener(l);
    }

    @Override
    public void setOnErrorListener(final OnErrorListener l) {
        mMediaPlayer.setOnErrorListener(l);
    }

    @Override
    public void setOnCompletionListener(final OnCompletionListener l) {
        mMediaPlayer.setOnCompletionListener(l);
    }

    @Override
    public void setOnVideoSizeChangedListener(final OnVideoSizeChangedListener l) {
        mMediaPlayer.setOnVideoSizeChangedListener(l);
    }

    @Override
    public void setOnSeekCompleteListener(OnSeekCompleteListener l) {
        mMediaPlayer.setOnSeekCompleteListener(l);
    }

    @Override
    public void setOnInfoListener(final OnInfoListener l) {
        mMediaPlayer.setOnInfoListener(l);
    }

    @Override
    public void setDisplay(SurfaceHolder sh) {
        mMediaPlayer.setDisplay(sh);
    }

    @Override
    public void setSurface(Surface sh) {
        mMediaPlayer.setSurface(sh);
    }

    @Override
    public void setDataSource(Context context, Uri uri) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mMediaPlayer.setDataSource(context, uri);
    }

    @Override
    public void prepareAsync() {
        mMediaPlayer.prepareAsync();
    }

    @Override
    public void release() {
        mMediaPlayer.release();
    }

    @Override
    public void start() {
        mMediaPlayer.start();
    }

    @Override
    public void stop() {
        mMediaPlayer.stop();
    }

    @Override
    public void pause() {
        mMediaPlayer.pause();
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    @Override
    public int getVideoWidth() {
        return mMediaPlayer.getVideoWidth();
    }

    @Override
    public int getVideoHeight() {
        return mMediaPlayer.getVideoHeight();
    }

    @Override
    public void seekTo(int progress) {
        mMediaPlayer.seekTo(progress);
    }

    @Override
    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return mMediaPlayer.getDuration();
    }
}
