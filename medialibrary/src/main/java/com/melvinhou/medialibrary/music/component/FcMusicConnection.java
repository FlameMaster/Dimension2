package com.melvinhou.medialibrary.music.component;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.medialibrary.music.util.MusicUtil;

import java.util.List;
import java.util.function.Consumer;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import kotlin.Suppress;

import static androidx.media.MediaBrowserServiceCompat.BrowserRoot.EXTRA_RECENT;
import static com.melvinhou.medialibrary.music.component.FcMusicService.NETWORK_FAILURE;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/3/6 0006 15:24
 * <p>
 * = 分 类 说 明：连接ui和服务
 * ================================================
 */
public class FcMusicConnection {

    private static FcMusicConnection instance = null;

    public static synchronized FcMusicConnection getInstance(Context context) {
        //ComponentName(context, MusicService::class.java)
        if (instance == null) {
            ComponentName serviceComponent = new ComponentName(FcUtils.getContext(), FcMusicService.class);
            instance = new FcMusicConnection(FcUtils.getContext(), serviceComponent);
        }
        return instance;
    }

    public static synchronized void disconnect() {
        if (instance != null) {
            instance.mediaController.unregisterCallback(instance.mediaControllerCallback);
            if (instance.mediaBrowser != null && instance.mediaBrowser.isConnected()) {
                instance.mediaBrowser.disconnect();
                instance.mediaBrowser = null;
            }
            instance = null;
            //停止音乐播放服务
            FcUtils.getContext().stopService(
                    new Intent(FcUtils.getContext(), FcMusicService.class));
        }
    }


    @Suppress(names = "PropertyName")
    PlaybackStateCompat EMPTY_PLAYBACK_STATE = new PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
            .build();
    @Suppress(names = "PropertyName")
    MediaMetadataCompat NOTHING_PLAYING = new MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, null)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
//            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, MusicUtil.getDeftAlbumArtUri())
//            .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, MusicUtil.getDeftAlbumArtUri())
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "FcMusic")
            .build();

    private Context mContext;
    public MutableLiveData<Boolean> isConnected = new MutableLiveData(false);
    public MutableLiveData networkFailure = new MutableLiveData(false);
    public String rootMediaId;
    public MutableLiveData<PlaybackStateCompat> playbackState = new MutableLiveData(EMPTY_PLAYBACK_STATE);
    public MutableLiveData<MediaMetadataCompat> nowPlaying = new MutableLiveData<>(NOTHING_PLAYING);
    //控制器
    private MediaControllerCompat mediaController;
    //指令控制
    public MediaControllerCompat.TransportControls transportControls;
    //客户端
    private MediaBrowserCompat mediaBrowser;
    //当前列表
    public MutableLiveData<List<MediaSessionCompat.QueueItem>> nowPlayQueue = new MutableLiveData<>();

    //媒体会话的状态或元数据每次发生更改时从媒体会话接收回调
    private MediaControllerCompat.Callback mediaControllerCallback =
            new MediaControllerCompat.Callback() {
                //音乐播放状态改变的回调，例如播放模式，播放、暂停，进度条等
                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    playbackState.postValue(state != null ? state : EMPTY_PLAYBACK_STATE);
                }

                //播放音乐改变的回调
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    if (metadata != null && metadata.getDescription() != null && metadata.getDescription().getMediaId() != null) {
                        nowPlaying.postValue(metadata);
                    } else {
                        nowPlaying.postValue(NOTHING_PLAYING);
                    }
                }

                @Override
                public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
                    nowPlayQueue.postValue(queue);
                }

                @Override
                public void onSessionEvent(String event, Bundle extras) {
                    super.onSessionEvent(event, extras);
                    if (NETWORK_FAILURE.equals(event)) {
                        networkFailure.postValue(true);
                    }
                }

                @Override
                public void onSessionDestroyed() {
                    mediaBrowserConnectionCallback.onConnectionSuspended();
                }

            };

    //客户端和服务端的连接回调,保存指向控制器的链接，以便处理媒体按钮
    private MediaBrowserCompat.ConnectionCallback mediaBrowserConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    rootMediaId = mediaBrowser.getRoot();
                    // 获取MediaSession的令牌，创建MediaControllerCompat
                    mediaController = new MediaControllerCompat(mContext, mediaBrowser.getSessionToken());
                    transportControls = mediaController.getTransportControls();
                    // 注册一个Callback以保持同步
                    mediaController.registerCallback(mediaControllerCallback);
                    //连接成功
                    isConnected.postValue(true);
                }

                @Override
                public void onConnectionSuspended() {
                    // 服务崩溃了。禁用传输控制，直到它自动重新连接
                    isConnected.postValue(false);
                }

                @Override
                public void onConnectionFailed() {
                    // 服务处拒绝了我们的连接
                    isConnected.postValue(false);
                }
            };


    private FcMusicConnection(Context context, ComponentName serviceComponent) {
        mContext = context;
        Bundle bundle = new Bundle();
        bundle.putBoolean(EXTRA_RECENT, true);
        mediaBrowser = new MediaBrowserCompat(context, serviceComponent,
                mediaBrowserConnectionCallback, bundle);
        mediaBrowser.connect();
    }

    public void subscribe(String parentId, MediaBrowserCompat.SubscriptionCallback callback) {
        mediaBrowser.subscribe(parentId, callback);
    }

    public void unsubscribe(String parentId, MediaBrowserCompat.SubscriptionCallback callback) {
        if (callback != null)
            mediaBrowser.unsubscribe(parentId, callback);
        else mediaBrowser.unsubscribe(parentId);
    }

    public boolean sendCommand(String command, Bundle parameters) {
        return sendCommand(command, parameters, null);
    }

    public boolean sendCommand(String command, Bundle parameters, ResultReceiver receiver) {
        if (mediaBrowser.isConnected()) {
            mediaController.sendCommand(command, parameters, receiver);
            return true;
        }
        return false;
    }

    public void addPlayQueue(@NonNull List<MediaBrowserCompat.MediaItem> list) {
        for (MediaBrowserCompat.MediaItem item : list) {
            mediaController.addQueueItem(item.getDescription());
        }
    }

    public void addPlayQueueItem(MediaDescriptionCompat description) {
        mediaController.addQueueItem(description);
    }

    public void removePlayQueueItem(MediaDescriptionCompat description) {
        mediaController.removeQueueItem(description);
    }

    public void clearPlayQueue() {
        if (mediaController.getQueue() != null)
            mediaController.getQueue().clear();
    }
}
