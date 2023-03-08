package com.melvinhou.medialibrary.music.model;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.util.ArrayList;
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
    //维护一个播放队列
    private List<MediaSessionCompat.QueueItem> mPlayQueue = new ArrayList<>();
//    private Map<Long,MediaMetadataCompat> mPlayQueue = new ArrayMap<>();

    //当前播放模式:重复
    private int mCurrentRepeatMode = PlaybackStateCompat.REPEAT_MODE_INVALID;
    //当前播放模式:随机
    private int mCurrentShuffleMode = PlaybackStateCompat.SHUFFLE_MODE_NONE;

    //当前角标
    private int mQueueIndex = -1;


    //当前播放的数据
    public MutableLiveData<MediaSessionCompat.QueueItem> currentMediaMetadat = new MutableLiveData();


    public class Callback extends MediaSessionCompat.Callback {

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

        }

        /**
         * 改变列表播放模式
         *
         * @param repeatMode
         */
        @Override
        public void onSetRepeatMode(int repeatMode) {
            mCurrentRepeatMode = repeatMode;
        }

        /**
         * 随机播放模式设置
         *
         * @param shuffleMode
         */
        @Override
        public void onSetShuffleMode(int shuffleMode) {
            mCurrentShuffleMode = shuffleMode;
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
            if (mQueueIndex + 1 < mPlayQueue.size()) {
                long id = mPlayQueue.get(mQueueIndex + 1).getQueueId();
                onSkipToQueueItem(id);
            }
        }

        @Override
        public void onSkipToPrevious() {
            if (mQueueIndex - 1 >= 0) {
                long id = mPlayQueue.get(mQueueIndex - 1).getQueueId();
                onSkipToQueueItem(id);
            }
        }

        @Override
        public void onSkipToQueueItem(long id) {

        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            Uri uri = Uri.parse(FcMusicLibrary.instance().getMusicUrl(mediaId));
            mPlayer.setDataSource(getBaseContext(), uri);
            for (MediaSessionCompat.QueueItem item :mPlayQueue){
                if (item.getDescription()!=null && mediaId.equals(item.getDescription().getMediaId())){

                }
            }
        }

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            mPlayer.setDataSource(getBaseContext(), uri);
        }

        @Override
        public void onPlayFromSearch(String query, Bundle extras) {
        }

        /**
         * 倒带
         */
        @Override
        public void onRewind() {
        }

    }


}
