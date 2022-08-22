package com.melvinhou.dimension2.media.music;

import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import com.melvinhou.kami.util.FcUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/4/2 19:45
 * <p>
 * = 分 类 说 明：musicbrowser的助手类，通过简化的回调处理连接、断开连接和基本浏览。
 * ================================================
 */
public class MediaBrowserHelper {

    private static final String TAG = MediaBrowserHelper.class.getSimpleName();

    private static MediaBrowserHelper mMusicBrowserHelper;


    private Context mContext;

    //客户端
    private MediaBrowserCompat mMediaBrowser;
    //服务端
    private final Class<? extends MediaBrowserServiceCompat> mMediaBrowserServiceClass;
    //客户端和服务端的连接回调
    private final MediaBrowserConnectionCallback mMediaBrowserConnectionCallback;
    //控制器
    @Nullable
    private MediaControllerCompat mMediaController;
    //控制器回调
    private final MediaControllerCallback mMediaControllerCallback;
    //浏览器订阅的接口，数据的回调
    private final MediaBrowserSubscriptionCallback mMediaBrowserSubscriptionCallback;

    /**
     * 数据列表
     */
    private List<MediaBrowserCompat.MediaItem> mediaItems;
    /**
     * 回调列表，用于绑定多个页面
     */
    private final List<MediaBrowserCallback> mCallbackList = new ArrayList<>();


    /**
     * 单例对象
     *
     * @return
     */
    private static MediaBrowserHelper getInstance() {
        if (mMusicBrowserHelper == null)
            mMusicBrowserHelper = new MediaBrowserHelper(FcUtils.getContext(), MusicService.class);
        return mMusicBrowserHelper;
    }

    /**
     * 发送指令的对象
     *
     * @return
     */
    public static MediaControllerCompat.TransportControls getTransportControls() {
        if (getInstance().mMediaController == null) {
            throw new IllegalStateException("MediaController is null!");
        }
        return getInstance().mMediaController.getTransportControls();
    }

    /**
     * 注册用于更新数据的回调
     *
     * @param callback
     */
    public static void registerCallback(MediaBrowserCallback callback) {
        if (callback != null) {
            getInstance().mCallbackList.add(callback);
            //开启
            if (getInstance().mCallbackList.size() > 0) getInstance().onStart();
            // 更新最新的元数据/回放状态。
            getInstance().initCallbackMetadata(callback);
        }
    }

    public static void unregisterCallback(MediaBrowserCallback callback) {
        if (callback != null) {
            getInstance().mCallbackList.remove(callback);
            if (getInstance().mCallbackList.size() == 0) {
                getInstance().onStop();
                mMusicBrowserHelper = null;
            }
        }
    }


    private MediaBrowserHelper(Context context,
                               Class<? extends MediaBrowserServiceCompat> serviceClass) {
        mContext = context;
        mMediaBrowserServiceClass = serviceClass;
        mMediaBrowserConnectionCallback = new MediaBrowserConnectionCallback();
        mMediaControllerCallback = new MediaControllerCallback();
        mMediaBrowserSubscriptionCallback = new MediaBrowserSubscriptionCallback();
    }

    /**
     * 创建browser并连接
     */
    public void onStart() {
        if (mMediaBrowser == null) {
            mMediaBrowser =
                    new MediaBrowserCompat(
                            mContext,
                            new ComponentName(mContext, mMediaBrowserServiceClass),
                            mMediaBrowserConnectionCallback,
                            null);
            //Browser发送连接请求
            mMediaBrowser.connect();
        }
    }

    /**
     * 释放MediaController，断开与browser的连接
     */
    public void onStop() {
        if (mMediaController != null) {
            mMediaController.unregisterCallback(mMediaControllerCallback);
            mMediaController = null;
        }
        if (mMediaBrowser != null && mMediaBrowser.isConnected()) {
            mMediaBrowser.disconnect();
            mMediaBrowser = null;
        }
        resetState();
    }

    /**
     * 连接成功回调
     */
    protected void onConnected(@NonNull MediaControllerCompat mediaController) {

    }

    /**
     * MusicService加载玩音乐列表后的回调，可用于更新播放列表数据
     *
     * @param parentId The media ID of the parent item.
     * @param children List (possibly empty) of child items.
     */
    protected void onChildrenLoaded(@NonNull String parentId,
                                    @NonNull List<MediaBrowserCompat.MediaItem> children) {
        mediaItems = children;
        // 将这个简单示例的所有媒体项排队。
        for (final MediaBrowserCompat.MediaItem mediaItem : children) {
            mMediaController.addQueueItem(mediaItem.getDescription());
        }

        // 现在准备好了，按下播放键就可以播放了。
        mMediaController.getTransportControls().prepare();
        performOnAllCallbacks(new CallbackCommand() {
            @Override
            public void perform(@NonNull MediaBrowserCallback callback) {
                callback.onMediaListLoaded(children);
            }
        });
    }

    /**
     * 当{@link MediaBrowserServiceCompat} 连接丢失时调用。
     */
    protected void onDisconnected() {

    }

    /**
     * 在通过{@link MediaSessionCompat}连接到{@link MusicService}之前，应用程序的内部状态需要恢复到它启动时的状态。
     */
    private void resetState() {
        performOnAllCallbacks(new CallbackCommand() {
            @Override
            public void perform(@NonNull MediaBrowserCallback callback) {
                callback.onPlaybackStateChanged(null);
            }
        });
    }

    /**
     * 初始化元数据
     *
     * @param callback
     */
    private void initCallbackMetadata(MediaBrowserCallback callback) {
        // 更新最新的数据状态
        if (mMediaController != null) {
            final MediaMetadataCompat metadata = mMediaController.getMetadata();
            if (metadata != null) {
                callback.onMetadataChanged(metadata);
            }
            final PlaybackStateCompat playbackState = mMediaController.getPlaybackState();
            if (playbackState != null) {
                callback.onPlaybackStateChanged(playbackState);
            }
            if (mediaItems!=null)
                callback.onMediaListLoaded(mediaItems);
            callback.onPlayProgressChanged(0);
        }
    }

    /**
     * 通知所有监听器
     *
     * @param command
     */
    private void performOnAllCallbacks(@NonNull CallbackCommand command) {
        for (MediaBrowserCallback callback : mCallbackList) {
            if (callback != null) {
                command.perform(callback);
            }
        }
    }


    /**
     * 监听客户端。
     */
    private interface CallbackCommand {
        void perform(@NonNull MediaBrowserCallback callback);
    }

    /**
     * 当MusicService加载了准备播放的新媒体时，接收来自MediaBrowser的回调。
     */
    public class MediaBrowserSubscriptionCallback extends MediaBrowserCompat.SubscriptionCallback {

        @Override
        public void onChildrenLoaded(@NonNull String parentId,
                                     @NonNull List<MediaBrowserCompat.MediaItem> children) {
            //children 即为Service发送回来的媒体数据集合
            //在onChildrenLoaded可以执行刷新列表UI的操作
            MediaBrowserHelper.this.onChildrenLoaded(parentId, children);
        }
    }

    /**
     * 当成功连接到MusicService时，接收来自MediaBrowser的回调
     */
    private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {

        //连接成功回调
        @Override
        public void onConnected() {

            //必须在确保连接成功的前提下执行订阅的操作
//            if (mMediaBrowser.isConnected()) {
            try {
                // 为MediaSession获取一个MediaController控制器并注册一个回调
                mMediaController =
                        new MediaControllerCompat(mContext, mMediaBrowser.getSessionToken());
                mMediaController.registerCallback(mMediaControllerCallback);

                // 将现有的MediaSession状态同步到UI。
                mMediaControllerCallback.onMetadataChanged(mMediaController.getMetadata());
                mMediaControllerCallback.onPlaybackStateChanged(
                        mMediaController.getPlaybackState());

                MediaBrowserHelper.this.onConnected(mMediaController);
            } catch (Exception e) {//RemoteException
                Log.d(TAG, String.format("onConnected: Problem: %s", e.toString()));
                throw new RuntimeException(e);
            }

//            mMediaBrowser.unsubscribe(mMediaBrowser.getRoot());
            //mediaId即为MediaBrowserService.onGetRoot的返回值
            String mediaId = mMediaBrowser.getRoot();
            mMediaBrowser.subscribe(mediaId, mMediaBrowserSubscriptionCallback);
        }
    }

    /**
     * 接收来自MediaController控制器的回调并更新UI状态
     * <p>
     * 自定义进度条更新
     */
    private class MediaControllerCallback extends MediaControllerCompat.Callback
            implements ValueAnimator.AnimatorUpdateListener {
        private ValueAnimator mProgressAnimator;
        //进度条最大值
        private int max = 0;

        //媒体切换
        @Override
        public void onMetadataChanged(final MediaMetadataCompat metadata) {
            max = metadata != null
                    ? (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
                    : 0;
            performOnAllCallbacks(new CallbackCommand() {
                @Override
                public void perform(@NonNull MediaBrowserCallback callback) {
                    callback.onMetadataChanged(metadata);
                    callback.onPlayProgressChanged(0);
                }
            });
        }

        //播放状态切换
        @Override
        public void onPlaybackStateChanged(@Nullable final PlaybackStateCompat state) {
            performOnAllCallbacks(new CallbackCommand() {
                @Override
                public void perform(@NonNull MediaBrowserCallback callback) {
                    callback.onPlaybackStateChanged(state);
                }
            });
            //进度条相关
            startProgress(state);

        }

        //进度条数值改变
        private void onPlayProgressChanged(int progress) {
            performOnAllCallbacks(new CallbackCommand() {
                @Override
                public void perform(@NonNull MediaBrowserCallback callback) {
                    callback.onPlayProgressChanged(progress);
                }
            });
        }

        /**
         * 创建动画用于更新进度条
         *
         * @param state
         */
        private void startProgress(PlaybackStateCompat state) {
            //先清除
            if (mProgressAnimator != null) {
                mProgressAnimator.cancel();
                mProgressAnimator = null;
            }
            final int progress = state != null
                    ? (int) state.getPosition()
                    : 0;
            onPlayProgressChanged(progress);
            // 创建一个伪进度更新进度条
            if (state != null && state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                final int timeToEnd = (int) ((max - progress) / state.getPlaybackSpeed());
                mProgressAnimator = ValueAnimator.ofInt(progress, max)
                        .setDuration(timeToEnd);
                mProgressAnimator.setInterpolator(new LinearInterpolator());
                mProgressAnimator.addUpdateListener(this);
                mProgressAnimator.start();
            }
        }

        @Override
        public void onAnimationUpdate(final ValueAnimator valueAnimator) {
            //用动画数值来更新进度
            final int animatedIntValue = (int) valueAnimator.getAnimatedValue();
            onPlayProgressChanged(animatedIntValue);
        }

        //当Activity在前台并且onStart()被调用(但没有onStop())时，这可能会发生。
        @Override
        public void onSessionDestroyed() {
            resetState();
            onPlaybackStateChanged(null);

            MediaBrowserHelper.this.onDisconnected();
        }
    }


}
