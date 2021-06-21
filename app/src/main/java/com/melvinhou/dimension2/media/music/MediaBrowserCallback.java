package com.melvinhou.dimension2.media.music;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.util.List;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/4/6 19:58
 * <p>
 * = 分 类 说 明：播放器客户端的监听器
 * ================================================
 */
public interface MediaBrowserCallback {

    /**
     * 媒体加载完成
     * @param list
     */
    void onMediaListLoaded(List<MediaBrowserCompat.MediaItem> list);

    /**
     * 播放状态改变的回调
     * @param playbackState
     */
    void onPlaybackStateChanged(PlaybackStateCompat playbackState);

    /**
     * 媒体信息改变的回调
     * @param mediaMetadata
     */
    void onMetadataChanged(MediaMetadataCompat mediaMetadata);

    /**
     * 播放进度改变的回调
     * @param progress
     */
    void onPlayProgressChanged(int progress);
}
