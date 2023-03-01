package com.melvinhou.dimension2.media.music;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.melvinhou.dimension2.media.music.proxy.PlaybackInfoListener;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.io.IOUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.media.MediaBrowserServiceCompat;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/4/2 19:30
 * <p>
 * = 分 类 说 明：音乐播放服务，专心处理音乐播放
 * ================================================
 */
public class MusicService extends MediaBrowserServiceCompat {

    private static final String TAG = MusicService.class.getSimpleName();

    //配对码
    private MediaSessionCompat mSession;
    //播放器
    private PlayerAdapter mPlayback;
    //状态栏工具
    private MediaNotificationManager mMediaNotificationManager;
    //播放控制器回调
    private MediaSessionCallback mCallback;
    private boolean mServiceInStartedState;

    //当前播放模式
    private int mCurrentRepeatMode = PlaybackStateCompat.REPEAT_MODE_INVALID;


    @Override
    public void onCreate() {
        super.onCreate();

        // 新建MediaSession.
        mSession = new MediaSessionCompat(this, "MusicService");
        mCallback = new MediaSessionCallback();
        mSession.setCallback(mCallback);
        //指明支持的按键信息类型
        //MediaSessionCompat允许与媒体控制器、音量键、媒体按钮和传输控制进行交互
        // FLAG_HANDLES_MEDIA_BUTTONS 控制媒体按钮
        // FLAG_HANDLES_TRANSPORT_CONTROLS 控制传输命令
        // FLAG_EXCLUSIVE_GLOBAL_PRIORITY 优先级最高的，会在activity处理之前先处理，
        mSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        //设置token后会触发MediaBrowserCompat.ConnectionCallback的回调方法
        //表示MediaBrowser与MediaBrowserService连接成功
        setSessionToken(mSession.getSessionToken());

        mMediaNotificationManager = new MediaNotificationManager(this);

        mPlayback = new PlayerAdapter(this, new PlayerListener());
        Log.d(TAG, "onCreate: MusicService creating MediaSession, and MediaNotificationManager");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        mMediaNotificationManager.onDestroy();
        mPlayback.stop();
        mSession.release();
        Log.d(TAG, "onDestroy: MediaPlayerAdapter stopped, and MediaSession released");
    }

    /**
     * 控制对服务的访问
     * 在客户端声明MediaBrowserCompat的时候，向服务端发起了初次连接请求。
     * 此时，服务端会在onGetRoot方法中收到请求，此时返回一个rootId就好了，如果方法返回null，则拒绝连接
     *
     * @param clientPackageName
     * @param clientUid
     * @param rootHints
     * @return
     */
    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot(MusicLibrary.getRoot(), null);
    }

    /**
     * 与客户端通信
     * 接收来自客户端的不同请求，此时需要通过客户端发送过来的parentMediaId，
     * 服务端根据parentMediaId来返回不同的结果给客户端
     *
     * @param parentMediaId
     * @param result
     */
    @SuppressLint("CheckResult")
    @Override
    public void onLoadChildren(
            @NonNull final String parentMediaId,
            @NonNull final Result<List<MediaBrowserCompat.MediaItem>> result) {
        //将信息从当前线程中移除，允许后续调用sendResult方法
        result.detach();

        //此时应该异步从网络或者本地读取数据，然后向Browser发送数据
        Observable.create((ObservableOnSubscribe<List<MediaBrowserCompat.MediaItem>>) emitter -> {
            List<MediaBrowserCompat.MediaItem> entity = null;
            if (MusicLibrary.loadNativeData(FcUtils.getContext())) {
                entity = MusicLibrary.getMediaItems();
                emitter.onNext(entity);
            }
            emitter.onComplete();
        })
                .compose(IOUtils.setThread())
                .subscribe(list -> {
                    //向Browser发送数据
                    result.sendResult(list);
                    Log.d(TAG,"数据加载完：共"+list.size()+"首");
                });
    }


    /**
     * 响应控制器指令的回调
     */
    public class MediaSessionCallback extends MediaSessionCompat.Callback {
        //QueueItem是播放队列一部分的单个项目。它包含项目的描述及其在队列中的id。
        private final List<MediaSessionCompat.QueueItem> mPlaylist = new ArrayList<>();
        private int mQueueIndex = -1;
        private MediaMetadataCompat mPreparedMedia;

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            return super.onMediaButtonEvent(mediaButtonEvent);
        }

        /**
         * 自定义指令处理
         *
         * @param command
         * @param extras
         * @param cb
         */
        @Override
        public void onCommand(String command, Bundle extras, ResultReceiver cb) {
            super.onCommand(command, extras, cb);
        }

        /**
         * 添加到队列尾部
         *
         * @param description
         */
        @Override
        public void onAddQueueItem(MediaDescriptionCompat description) {
            mPlaylist.add(new MediaSessionCompat.QueueItem(description, description.hashCode()));
            mQueueIndex = (mQueueIndex == -1) ? 0 : mQueueIndex;
            mSession.setQueue(mPlaylist);
        }

        /**
         * 删除指定的条目
         *
         * @param description
         */
        @Override
        public void onRemoveQueueItem(MediaDescriptionCompat description) {
            mPlaylist.remove(new MediaSessionCompat.QueueItem(description, description.hashCode()));
            mQueueIndex = (mPlaylist.isEmpty()) ? -1 : mQueueIndex;
            mSession.setQueue(mPlaylist);
        }

        /**
         * 准备播放
         */
        @Override
        public void onPrepare() {
            if (mQueueIndex < 0 && mPlaylist.isEmpty()) {
                // 无法播放
                return;
            }

            final String mediaId = mPlaylist.get(mQueueIndex).getDescription().getMediaId();
            mPreparedMedia = MusicLibrary.getMetadata(MusicService.this, mediaId);
            mSession.setMetadata(mPreparedMedia);

            if (!mSession.isActive()) {
                mSession.setActive(true);
            }
        }

        /**
         * 指定播放
         *
         * @param mediaId
         * @param extras
         */
        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);
            try {
                for (int i = 0; i < mPlaylist.size(); i++) {
                    if (mediaId.equals(mPlaylist.get(i).getDescription().getMediaId())) {
                        mQueueIndex = i;
                        mPreparedMedia = null;
                        onPlay();
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPlay() {
            if (!isReadyToPlay()) {
                // 无法播放
                return;
            }

            if (mPreparedMedia == null) {
                onPrepare();
            }

            mPlayback.playFromMedia(mPreparedMedia);
            Log.d(TAG, "onPlayFromMediaId: MediaSession active");
        }

        @Override
        public void onPause() {
            mPlayback.pause();
        }

        @Override
        public void onStop() {
            mPlayback.stop();
            mSession.setActive(false);
        }

        @Override
        public void onSkipToNext() {
            mQueueIndex = (++mQueueIndex % mPlaylist.size());
            mPreparedMedia = null;
            onPlay();
        }

        @Override
        public void onSkipToPrevious() {
            mQueueIndex = mQueueIndex > 0 ? mQueueIndex - 1 : mPlaylist.size() - 1;
            mPreparedMedia = null;
            onPlay();
        }

        @Override
        public void onSeekTo(long pos) {
            mPlayback.seekTo(pos);
        }

        private boolean isReadyToPlay() {
            return (!mPlaylist.isEmpty());
        }

        /**
         * 改变列表播放模式
         *
         * @param repeatMode
         */
        @Override
        public void onSetRepeatMode(int repeatMode) {
            super.onSetRepeatMode(repeatMode);
            mCurrentRepeatMode = repeatMode;
        }

        /**
         * 判断是否为列表最后一个
         * @return
         */
        public boolean isLastMedia() {
            return mPlaylist.size() == 0 || mQueueIndex == mPlaylist.size() - 1;
        }

        /**
         * 播放指定id
         *
         * @param id
         */
        @Override
        public void onSkipToQueueItem(long id) {
            super.onSkipToQueueItem(id);
            String mediaId = String.valueOf(id);
            onPlayFromMediaId(mediaId,null);
        }

        /**
         * 随机播放模式设置
         *
         * @param shuffleMode
         */
        @Override
        public void onSetShuffleMode(int shuffleMode) {
            super.onSetShuffleMode(shuffleMode);
        }

        /**
         * 倒带
         */
        @Override
        public void onRewind() {
            super.onRewind();
        }
    }


    /**
     * 实现播放器的状态监听器
     */
    public class PlayerListener extends PlaybackInfoListener {

        private final ServiceManager mServiceManager;

        PlayerListener() {
            mServiceManager = new ServiceManager();
        }

        @Override
        public void onPlaybackStateChange(PlaybackStateCompat state) {
            // 向媒体报告情况。
            mSession.setPlaybackState(state);

            // 管理此服务的启动状态。
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_PLAYING:
                    mServiceManager.moveServiceToStartedState(state);
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                    mServiceManager.updateNotificationForPause(state);
                    break;
                case PlaybackStateCompat.STATE_STOPPED:
                    mServiceManager.moveServiceOutOfStartedState(state);
                    break;
            }
        }

        /**
         * 播放完成
         */
        @Override
        public void onPlaybackCompleted() {
            switch (mCurrentRepeatMode) {
                case PlaybackStateCompat.REPEAT_MODE_INVALID://默认播放完停止
                    break;
                case PlaybackStateCompat.REPEAT_MODE_NONE://播放一次列表全部
                    if (!mCallback.isLastMedia()) mCallback.onSkipToNext();
                    break;
                case PlaybackStateCompat.REPEAT_MODE_ONE://单曲循环
                    mCallback.onPlay();
                    break;
                case PlaybackStateCompat.REPEAT_MODE_ALL://全体循环
                    mCallback.onSkipToNext();
                    break;
                case PlaybackStateCompat.REPEAT_MODE_GROUP:
                    break;
            }

        }

        /**
         * 对状态栏控件和桌面控件的管理
         */
        class ServiceManager {

            private void moveServiceToStartedState(PlaybackStateCompat state) {
                Notification notification =
                        mMediaNotificationManager.getNotification(
                                mPlayback.getCurrentMedia(), state, getSessionToken());

                if (!mServiceInStartedState) {
                    ContextCompat.startForegroundService(
                            MusicService.this,
                            new Intent(MusicService.this, MusicService.class));
                    mServiceInStartedState = true;
                }

                startForeground(MediaNotificationManager.NOTIFICATION_ID, notification);
                //通知桌面控件
                Intent intent = new Intent();
                intent.setAction(MusicWidgetProvider.ACTION_APPWIDGET_UPDATE);
                intent.putExtra("state",PlaybackStateCompat.STATE_PLAYING);
                intent.putExtra("title",mPlayback.getCurrentMedia().getDescription().getTitle());
                intent.putExtra("artist",mPlayback.getCurrentMedia().getDescription().getSubtitle());
                intent.putExtra("cover",mPlayback.getCurrentMedia().getDescription().getIconUri());
                sendBroadcast(intent);
            }

            private void updateNotificationForPause(PlaybackStateCompat state) {
                stopForeground(false);
                Notification notification =
                        mMediaNotificationManager.getNotification(
                                mPlayback.getCurrentMedia(), state, getSessionToken());
                mMediaNotificationManager.getNotificationManager()
                        .notify(MediaNotificationManager.NOTIFICATION_ID, notification);
                //通知桌面控件
                Intent intent = new Intent();
                intent.setAction(MusicWidgetProvider.ACTION_APPWIDGET_UPDATE);
                intent.putExtra("state",PlaybackStateCompat.STATE_PAUSED);
                intent.putExtra("title",mPlayback.getCurrentMedia().getDescription().getTitle());
                intent.putExtra("artist",mPlayback.getCurrentMedia().getDescription().getSubtitle());
                intent.putExtra("cover",mPlayback.getCurrentMedia().getDescription().getIconUri());
                sendBroadcast(intent);
            }

            private void moveServiceOutOfStartedState(PlaybackStateCompat state) {
                stopForeground(true);
                stopSelf();
                mServiceInStartedState = false;
                //通知桌面控件
                Intent intent = new Intent();
                intent.setAction(MusicWidgetProvider.ACTION_APPWIDGET_UPDATE);
                intent.putExtra("state",PlaybackStateCompat.STATE_STOPPED);
                sendBroadcast(intent);
            }
        }

    }
}
