package com.melvinhou.medialibrary.video.listener;

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
public interface MediaController {

    void start();
    void pause();
    void stop();
    void reset();
    void release();


    boolean isPlaying();
    void seekTo(long time);
    long getDuration();
    long getCurrentPosition();
}
