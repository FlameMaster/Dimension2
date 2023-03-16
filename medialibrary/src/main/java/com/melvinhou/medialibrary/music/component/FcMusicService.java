package com.melvinhou.medialibrary.music.component;

import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import com.melvinhou.medialibrary.music.model.FcMusicLibrary;
import com.melvinhou.medialibrary.music.model.FcMusicModel;
import com.melvinhou.medialibrary.music.proxy.IMusicPlayer;
import com.melvinhou.medialibrary.music.proxy.MusicPlayerProxy;
import com.melvinhou.medialibrary.music.ui.FcMusicNotificationManager;

import static androidx.media.MediaBrowserServiceCompat.BrowserRoot.EXTRA_RECENT;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/3/1 0001 16:27
 * <p>
 * = 分 类 说 明：音乐播放器的服务
 * ================================================
 */
public class FcMusicService extends MediaBrowserServiceCompat {

    public static final String NETWORK_FAILURE = "com.melvinhou.media.session.NETWORK_FAILURE";

    private static final String TAG = FcMusicService.class.getSimpleName();
    private static final String MY_MEDIA_ROOT_ID = "__RECENT__";
    private static final String MY_BROWSABLE_ROOT = "/";
    private static final String MY_EMPTY_MEDIA_ROOT_ID = "@empty@";

    private final static int NOTIFICATION_ID = 14650;
    //连接当前通知栏的id
    private static final String NOTIFICATION_CHANNEL_ID = FcMusicService.class.getSimpleName();


    //配对码
    private MediaSessionCompat mMediaSession;
    //播放器
    private IMusicPlayer mPlayer;
    private FcMusicNotificationManager mNotificationConnect;
    //是否获取播放焦点
    private boolean mResumeOnFocusGain = false;
    //焦点申请，android9之后使用
    private AudioFocusRequest mAudioFocusRequest;
    //是否注册广播
    private boolean mAudioNoisyReceiverRegistered = false;

    //音频焦点转移监听
    public final AudioManager.OnAudioFocusChangeListener mAfChangeListener
            = new AudioManager.OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(int focusChange) {
            boolean isPlaying = mPlayer != null && mPlayer.isPlaying();
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (mResumeOnFocusGain && !isPlaying) {
//                        mPlayer.start();
                    } else if (isPlaying) {
                        mPlayer.setVolume(IMusicPlayer.MEDIA_VOLUME_DEFAULT);
                    }
                    mResumeOnFocusGain = false;
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    mPlayer.setVolume(IMusicPlayer.MEDIA_VOLUME_DUCK);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (isPlaying) {
                        mResumeOnFocusGain = true;
                        mMediaSession.getController().getTransportControls().pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    abandonAudioFocus();
                    mResumeOnFocusGain = false;
                    mMediaSession.getController().getTransportControls().pause();
                    break;
            }
        }
    };

    //todo 这个还么调整完
    //播放控制器回调
    private final MediaSessionCompat.Callback mMediaSessionCallback
            = new FcMusicModel() {

        @Override
        public void onCommand(String command, Bundle extras, ResultReceiver cb) {
            super.onCommand(command, extras, cb);
            if (FcMusicModel.KEY_COMMAND_STATE_UPDATE.contains(command)) {
                PlaybackStateCompat state = mMediaSession.getController().getPlaybackState();
                mMediaSession.setPlaybackState(state);
            }
        }

        @Override
        protected void onPrepare(MediaSessionCompat.QueueItem item) {
            MediaDescriptionCompat index = item.getDescription();
            mMediaSession.setMetadata(FcMusicLibrary.instance().getMetadata(index.getMediaId()));
            mPlayer.setDataSource(getBaseContext(), index.getMediaUri());
            //通知桌面控件
            Intent intent = new Intent();
            intent.putExtra("title", index.getTitle());
            intent.putExtra("icon", index.getIconUri());
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            sendBroadcast(intent);
        }

        @Override
        protected void onChangeQueue(List<MediaSessionCompat.QueueItem> list) {
            mMediaSession.setQueue(list);
        }

        @Override
        public void onSetRepeatMode(int repeatMode) {
            super.onSetRepeatMode(repeatMode);
            mMediaSession.setRepeatMode(repeatMode);
        }

        @Override
        public void onSetShuffleMode(int shuffleMode) {
            super.onSetShuffleMode(shuffleMode);
            mMediaSession.setShuffleMode(shuffleMode);
        }

        @Override
        public void onSeekTo(long pos) {
            mPlayer.seekTo((int) pos);
        }

        @Override
        public void onPlay() {
            //可以播放
            if (requestAudioFocus()) {
                // 启动服务
//                startService(new Intent(getBaseContext(), MediaBrowserService.class));
                // 将会话设置为活动的(并更新元数据和状态)
                mMediaSession.setActive(true);
                // 启动播放器(自定义调用)
                mPlayer.start();
                // 注册 BECOME_NOISY BroadcastReceiver
                registerAudioNoisyReceiver();
                // 把服务放在前台，发布通知
                startForeground(NOTIFICATION_ID, mNotificationConnect.build(mMediaSession));
            }
        }

        @Override
        public void onStop() {
            // 放弃音频对焦
            abandonAudioFocus();
            unregisterAudioNoisyReceiver();
            // 停止服务
//            stopSelf();
            // 将会话设置为非活动(并更新元数据和状态)
            mMediaSession.setActive(false);
            // 停止播放器(自定义调用)
            mPlayer.stop();
            // 将服务移出前台
            stopForeground(false);
        }

        @Override
        public void onPause() {
            // 放弃音频对焦
            if (!mResumeOnFocusGain) {
                abandonAudioFocus();
            }
            // 更新元数据并暂停播放器状态(自定义调用)
            mPlayer.pause();
            // 注销 BECOME_NOISY BroadcastReceiver
            unregisterAudioNoisyReceiver();
            // 将服务移出前台，保留通知
            stopForeground(false);
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(NOTIFICATION_ID, mNotificationConnect.build(mMediaSession));
        }
    };

    //耳机拔出的广播接收器
    private final FcNoisyAudioReceiver mNoisyAudioStreamReceiver
            = new FcNoisyAudioReceiver() {
        @Override
        void dispose() {
            boolean isPlaying = mPlayer != null && mPlayer.isPlaying();
            if (isPlaying) {
                mPlayer.pause();
            }
        }
    };

    //播放器状态变化监听，更新可接收的控制器命令
    private final IMusicPlayer.OnPlaybackStateListener mPlaybackStateListener
            = new IMusicPlayer.OnPlaybackStateListener() {
        @Override
        public void onPlaybackStateChange(int state) {
            long actions = PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                    | PlaybackStateCompat.ACTION_PLAY_FROM_URI
                    | PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                    | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
            switch (state) {
                case PlaybackStateCompat.STATE_STOPPED:
                    actions |= PlaybackStateCompat.ACTION_PLAY
                            | PlaybackStateCompat.ACTION_PAUSE;
                    break;
                case PlaybackStateCompat.STATE_PLAYING:
                    actions |= PlaybackStateCompat.ACTION_STOP
                            | PlaybackStateCompat.ACTION_PAUSE
                            | PlaybackStateCompat.ACTION_SEEK_TO;
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                    actions |= PlaybackStateCompat.ACTION_PLAY
                            | PlaybackStateCompat.ACTION_STOP;
                    break;
                default:
                    actions |= PlaybackStateCompat.ACTION_PLAY
                            | PlaybackStateCompat.ACTION_PLAY_PAUSE
                            | PlaybackStateCompat.ACTION_STOP
                            | PlaybackStateCompat.ACTION_PAUSE;
            }
            //当前进度
            long reportPosition = mPlayer.getCurrentPosition();
            float speed = mPlayer.getSpeed();
            //用ACTION_PLAY设置初始的PlaybackState，这样媒体按钮就可以启动播放器
            final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
            stateBuilder.setActions(actions);
            stateBuilder.setState(state, reportPosition, speed, SystemClock.elapsedRealtime());
            PlaybackStateCompat stateCompat = stateBuilder.build();
            mMediaSession.setPlaybackState(stateCompat);
            //通知桌面控件
            Intent intent = new Intent();
            intent.putExtra("state", state);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            sendBroadcast(intent);
        }
    };
    //播放完成的监听
    private final IMusicPlayer.OnCompletionListener mPlayCompletionListener = new IMusicPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMusicPlayer mp) {
            int repeatMode = mMediaSession.getController().getRepeatMode();
            int shuffleMode = mMediaSession.getController().getShuffleMode();
            if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE) {
                //重复播放
                mMediaSession.getController().getTransportControls().play();
            } else if (repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL
                    || shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL
                    || repeatMode == PlaybackStateCompat.REPEAT_MODE_GROUP
                    || shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_GROUP) {
                //下一首
                mMediaSession.getController().getTransportControls().skipToNext();
            }
        }
    };


//*******************服务本身逻辑*******************************//

    @Override
    public void onCreate() {
        super.onCreate();

        // 创建MediaSessionCompat
        mMediaSession = new MediaSessionCompat(getBaseContext(), TAG);

        // 启用MediaButtons和TransportControls的回调
        // 指明支持的按键信息类型
        // MediaSessionCompat允许与媒体控制器、音量键、媒体按钮和传输控制进行交互
        // FLAG_HANDLES_MEDIA_BUTTONS 控制媒体按钮
        // FLAG_HANDLES_TRANSPORT_CONTROLS 控制传输命令
        // FLAG_EXCLUSIVE_GLOBAL_PRIORITY 优先级最高的，会在activity处理之前先处理，
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS | //队列处理
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // MySessionCallback()具有处理媒体控制器回调的方法
        mMediaSession.setCallback(mMediaSessionCallback);

        // 设置会话的令牌，以便客户端活动可以与其通信。
        // 设置token后会触发MediaBrowserCompat.ConnectionCallback的回调方法
        // 表示MediaBrowser与MediaBrowserService连接成功
        setSessionToken(mMediaSession.getSessionToken());

        //播放器
        mPlayer = new MusicPlayerProxy();
        mPlayer.setOnPlaybackStateListener(mPlaybackStateListener);
        mPlayer.setOnCompletionListener(mPlayCompletionListener);

        //通知栏
        mNotificationConnect = new FcMusicNotificationManager(this, NOTIFICATION_CHANNEL_ID);
    }


    //处理按钮的意图传递
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        int code = keyEvent.getKeyCode();
        if (code == PlaybackStateCompat.toKeyCode(PlaybackStateCompat.ACTION_PLAY)) {
            //确保能正确执行播放（service未激活前台MediaButtonReceiver可能不生效
            mMediaSession.getController().getTransportControls().play();
        } else if (code == PlaybackStateCompat.toKeyCode(PlaybackStateCompat.ACTION_PAUSE) ||
                code == PlaybackStateCompat.toKeyCode(PlaybackStateCompat.ACTION_PLAY_PAUSE)) {
            mMediaSession.getController().getTransportControls().pause();
        } else if (code == PlaybackStateCompat.toKeyCode(PlaybackStateCompat.ACTION_STOP)) {
            mMediaSession.getController().getTransportControls().stop();
        } else if (code == PlaybackStateCompat.toKeyCode(PlaybackStateCompat.ACTION_SKIP_TO_NEXT)) {
            mMediaSession.getController().getTransportControls().skipToNext();
        } else if (code == PlaybackStateCompat.toKeyCode(PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)) {
            mMediaSession.getController().getTransportControls().skipToPrevious();
        } else {
            //确保根据传入的KeyEvent触发对MediaSessionCompat.Callback的正确回调
            MediaButtonReceiver.handleIntent(mMediaSession, intent);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    /**
     * 启动该服务的context执行onDestroy会执行当前
     */
    @Override
    public void onDestroy() {
        mPlayer.release();
        mMediaSession.release();
        stopForeground(true);
        stopSelf();
        Log.d(TAG, "onDestroy: MediaPlayerAdapter stopped, and MediaSession released");
        super.onDestroy();
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
        // (可选)控制指定包名的访问级别。为此，您需要编写自己的逻辑。
        if (allowBrowsing(clientPackageName, clientUid)) {
            // 返回一个根ID，客户端可以使用onLoadChildren()检索内容层次结构。
            boolean isRecentRequest = rootHints != null && rootHints.getBoolean(EXTRA_RECENT);
            return new BrowserRoot(isRecentRequest ? MY_MEDIA_ROOT_ID : MY_BROWSABLE_ROOT, null);
        } else {
            // 客户端可以连接，但是这个BrowserRoot是一个空的层次结构，
            // 所以onLoadChildren没有返回任何东西。这将禁用浏览内容的能力。
            return new BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null);
        }
    }

    /**
     * 当前服务的访问判断
     *
     * @param clientPackageName
     * @param clientUid
     * @return
     */
    private boolean allowBrowsing(String clientPackageName, int clientUid) {
        if (clientPackageName.contains("melvinhou")) return true;
        return false;
    }


    /**
     * 与客户端通信
     * 接收来自客户端的不同请求，此时需要通过客户端发送过来的parentMediaId，
     * 服务端根据parentMediaId来返回不同的结果给客户端
     * onLoadChildren在执行完之前必须调用detach()或sendResult()
     *
     * @param parentMediaId
     * @param result
     */
    @Override
    public void onLoadChildren(@NonNull String parentMediaId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        //将信息从当前线程中移除，允许后续调用sendResult方法
        result.detach();

        //  不允许浏览
        if (TextUtils.equals(MY_EMPTY_MEDIA_ROOT_ID, parentMediaId)) {
            result.sendResult(null);
            return;
        }

        // 检查这是否是根菜单:
        if (MY_MEDIA_ROOT_ID.equals(parentMediaId)) {
            // 构建顶层的MediaItem对象，并将它们放在mediaItems列表中……
            if (FcMusicLibrary.instance().loadData(false)) {
                List<MediaBrowserCompat.MediaItem> entity = FcMusicLibrary.instance().getMediaItems();
                result.sendResult(entity);
                Log.d(TAG, "数据加载完：共" + entity.size() + "首");
            }
        } else {
            // 检查传递的parentMediaId以查看我们在哪个子菜单中，并将该菜单的子菜单放在mediaItems列表中……
            List<MediaSessionCompat.QueueItem> list = mMediaSession.getController().getQueue();
            if (list != null) {
                List<MediaBrowserCompat.MediaItem> entity = new ArrayList<>();
                for (MediaSessionCompat.QueueItem item : list) {
                    entity.add(new MediaBrowserCompat.MediaItem(item.getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
                }
                result.sendResult(entity);
            } else {
                mMediaSession.sendSessionEvent(NETWORK_FAILURE, null);
                result.sendResult(null);
            }
        }
    }

    @Override
    public void onSearch(@NonNull String query, Bundle extras, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        super.onSearch(query, extras, result);
    }


//*******************配件*******************************//


    /**
     * 申请音频播放权，注册焦点转移监听
     *
     * @return 是否获取到播放焦点
     */
    private boolean requestAudioFocus() {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes attrs = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            mAudioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(mAfChangeListener)
                    .setAudioAttributes(attrs)
                    .build();
            result = am.requestAudioFocus(mAudioFocusRequest);
        } else {
            result = am.requestAudioFocus(mAfChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    /**
     * 放弃音频播放权
     */
    private void abandonAudioFocus() {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mAudioFocusRequest != null)
                am.abandonAudioFocusRequest(mAudioFocusRequest);
        } else {
            if (mAfChangeListener != null)
                am.abandonAudioFocus(mAfChangeListener);
        }
    }


    /**
     * 注册音频广播监听
     */
    private void registerAudioNoisyReceiver() {
        if (!mAudioNoisyReceiverRegistered) {
            registerReceiver(mNoisyAudioStreamReceiver, FcNoisyAudioReceiver.AUDIO_NOISY_INTENT_FILTER);
            mAudioNoisyReceiverRegistered = true;
        }
    }

    /**
     * 注销音频广播监听
     */
    private void unregisterAudioNoisyReceiver() {
        if (mAudioNoisyReceiverRegistered) {
            unregisterReceiver(mNoisyAudioStreamReceiver);
            mAudioNoisyReceiverRegistered = false;
        }
    }
}
