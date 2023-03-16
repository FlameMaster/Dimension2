package com.melvinhou.medialibrary.music.model;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.melvinhou.kami.util.FcUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import androidx.collection.ArrayMap;
import androidx.lifecycle.MutableLiveData;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/3/8 0008 17:21
 * <p>
 * = 分 类 说 明：存储播放媒体数据
 * ================================================
 */
public class FcMusicModel extends MediaSessionCompat.Callback {

    //再次下拉状态
    public static final String KEY_COMMAND_STATE_UPDATE = "update_state";

    //维护一个播放队列
    private List<MediaSessionCompat.QueueItem> mPlayQueue = new ArrayList<>();
    private List<MediaSessionCompat.QueueItem> mPlayShuffle = new ArrayList<>();

    //当前播放模式:重复
    private int mCurrentRepeatMode = PlaybackStateCompat.REPEAT_MODE_INVALID;
    //当前播放模式:随机
    private int mCurrentShuffleMode = PlaybackStateCompat.SHUFFLE_MODE_INVALID;

    //当前角标
    private int mQueueIndex = -1;


    protected void onPrepare(MediaSessionCompat.QueueItem item) {
        FcUtils.showToast("播放：" + item.getDescription().getTitle());
    }

    private void onPrepare(int position) {
        mQueueIndex = position;
        MediaSessionCompat.QueueItem item = mPlayQueue.get(position);
        if (mCurrentShuffleMode >= 0) {
            item = mPlayShuffle.get(position);
        }
        onPrepare(item);
    }

    protected void onChangeQueue(List<MediaSessionCompat.QueueItem> list) {

    }

    /**
     * 自定义指令处理
     *
     * @param command The command name.
     * @param extras  Optional parameters for the command, may be null.
     * @param cb      A result receiver to which a result may be sent by the command, may be null.
     */
    @Override
    public void onCommand(String command, Bundle extras, ResultReceiver cb) {
    }

    /**
     * 按键意图处理
     *
     * @param mediaButtonEvent The media button event intent.
     * @return
     */
    @Override
    public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
        return super.onMediaButtonEvent(mediaButtonEvent);
    }

    @Override
    public void onPrepare() {
        if (mPlayQueue.size() > 0) {
            onSetRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE);
            onPrepare(0);
        }
    }

    /**
     * 改变列表播放模式
     *
     * @param repeatMode
     */
    @Override
    public void onSetRepeatMode(int repeatMode) {
        if (mCurrentRepeatMode == PlaybackStateCompat.REPEAT_MODE_INVALID) {
            //通知列表变更
            onChangeQueue(mPlayQueue);
            if (mQueueIndex >= 0) {
                mQueueIndex = mPlayQueue.indexOf(mPlayShuffle.get(mQueueIndex));
            }
        }
        mCurrentRepeatMode = repeatMode;
        mCurrentShuffleMode = PlaybackStateCompat.SHUFFLE_MODE_INVALID;
    }

    /**
     * 随机播放模式设置
     *
     * @param shuffleMode
     */
    @Override
    public void onSetShuffleMode(int shuffleMode) {
        if (mCurrentShuffleMode == PlaybackStateCompat.SHUFFLE_MODE_INVALID) {
            mPlayShuffle = new ArrayList<>(mPlayQueue);
            Collections.shuffle(mPlayShuffle);
            //通知列表变更
            onChangeQueue(mPlayShuffle);
            if (mQueueIndex >= 0) {
                mQueueIndex = mPlayShuffle.indexOf(mPlayQueue.get(mQueueIndex));
            }
        }
        mCurrentShuffleMode = shuffleMode;
        mCurrentRepeatMode = PlaybackStateCompat.REPEAT_MODE_INVALID;
    }

    /**
     * 添加到队列尾部
     *
     * @param description
     */
    @Override
    public void onAddQueueItem(MediaDescriptionCompat description) {
        mPlayQueue.add(new MediaSessionCompat.QueueItem(description, description.hashCode()));
    }

    @Override
    public void onAddQueueItem(MediaDescriptionCompat description, int index) {
        mPlayQueue.add(index, new MediaSessionCompat.QueueItem(description, description.hashCode()));
    }

    @Override
    public void onRemoveQueueItem(MediaDescriptionCompat description) {
        mPlayQueue.remove(description);
    }

    @Override
    public void onSkipToNext() {
        if (mCurrentRepeatMode == PlaybackStateCompat.REPEAT_MODE_NONE
                || mCurrentShuffleMode == PlaybackStateCompat.SHUFFLE_MODE_NONE) {
            return;
        }
        if (mQueueIndex + 1 < mPlayQueue.size()) {
            onPrepare(mQueueIndex + 1);
            onPlay();
        } else if (mPlayQueue.size() > 0 &&(mCurrentRepeatMode == PlaybackStateCompat.REPEAT_MODE_GROUP
                || mCurrentShuffleMode == PlaybackStateCompat.SHUFFLE_MODE_GROUP)) {
            onPrepare(0);
            onPlay();
        }
    }

    @Override
    public void onSkipToPrevious() {
        if (mQueueIndex - 1 >= 0) {
            onPrepare(mQueueIndex - 1);
            onPlay();
        } else if (mPlayQueue.size() > 0 &&(mCurrentRepeatMode == PlaybackStateCompat.REPEAT_MODE_GROUP
                || mCurrentShuffleMode == PlaybackStateCompat.SHUFFLE_MODE_GROUP)) {
            onPrepare(mPlayQueue.size() - 1);
            onPlay();
        }
    }

    @Override
    public void onSkipToQueueItem(long id) {
        for (MediaSessionCompat.QueueItem item : mPlayQueue) {
            if (item.getQueueId() == id) {
                int position = mPlayQueue.indexOf(item);
                if (position >= 0) {
                    onPrepare(position);
                }
                return;
            }
        }
    }

    @Override
    public void onPlayFromMediaId(String mediaId, Bundle extras) {
        for (MediaSessionCompat.QueueItem item : mPlayQueue) {
            if (item.getDescription() != null && mediaId.equals(item.getDescription().getMediaId())) {
                int position = mPlayQueue.indexOf(item);
                if (position >= 0) {
                    onPrepare(position);
                }
                return;
            }
        }
    }

    @Override
    public void onPlayFromUri(Uri uri, Bundle extras) {
        for (MediaSessionCompat.QueueItem item : mPlayQueue) {
            if (item.getDescription() != null && uri == item.getDescription().getMediaUri()) {
                int position = mPlayQueue.indexOf(item);
                if (position >= 0) {
                    onPrepare(position);
                }
                return;
            }
        }
    }

    @Override
    public void onPlayFromSearch(String query, Bundle extras) {
        for (MediaSessionCompat.QueueItem item : mPlayQueue) {
            if (item.getDescription() != null && item.getDescription().getTitle().toString().contains(query)) {
                int position = mPlayQueue.indexOf(item);
                if (position >= 0) {
                    onPrepare(position);
                }
                return;
            }
        }
    }

    /**
     * 倒带
     */
    @Override
    public void onRewind() {
    }


}
