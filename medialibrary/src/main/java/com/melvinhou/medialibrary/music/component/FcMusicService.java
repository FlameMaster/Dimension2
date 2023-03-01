package com.melvinhou.medialibrary.music.component;

import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import com.melvinhou.medialibrary.music.ui.MusicNotificationBuilder;

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
 * = 时 间：2023/3/1 0001 16:27
 * <p>
 * = 分 类 说 明：音乐播放器的服务
 * ================================================
 */
public class FcMusicService extends MediaBrowserServiceCompat {

    private static final String TAG = FcMusicService.class.getSimpleName();
    private static final String MY_MEDIA_ROOT_ID = "media_root_id";
    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";


    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;

    private MediaSessionCompat.Callback mMediaSessionCallback = new MediaSessionCompat.Callback() {
    };

    @Override
    public void onCreate() {
        super.onCreate();

        // 创建MediaSessionCompat
        mediaSession = new MediaSessionCompat(getBaseContext(), TAG);

        // 启用MediaButtons和TransportControls的回调
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // 用ACTION_PLAY设置初始的PlaybackState，这样媒体按钮就可以启动播放器
        stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mediaSession.setPlaybackState(stateBuilder.build());

        // MySessionCallback()具有处理媒体控制器回调的方法
        mediaSession.setCallback(mMediaSessionCallback);

        // 设置会话的令牌，以便客户端活动可以与其通信。
        setSessionToken(mediaSession.getSessionToken());
    }

    //控制客户端连接
    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        // (可选)控制指定包名的访问级别。为此，您需要编写自己的逻辑。
        if (allowBrowsing(clientPackageName, clientUid)) {
            // 返回一个根ID，客户端可以使用onLoadChildren()检索内容层次结构。
            return new BrowserRoot(MY_MEDIA_ROOT_ID, null);
        } else {
            // 客户端可以连接，但是这个BrowserRoot是一个空的层次结构，
            // 所以onLoadChildren没有返回任何东西。这将禁用浏览内容的能力。
            return new BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null);
        }
    }

    private boolean allowBrowsing(String clientPackageName, int clientUid) {
        if (clientPackageName.contains("melvinhou")) return true;
        return false;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

        //  不允许浏览
        if (TextUtils.equals(MY_EMPTY_MEDIA_ROOT_ID, parentId)) {
            result.sendResult(null);
            return;
        }

        // 例如，假设已经加载/缓存了音乐目录。

        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        // 检查这是否是根菜单:
        if (MY_MEDIA_ROOT_ID.equals(parentId)) {
            // 构建顶层的MediaItem对象，并将它们放在mediaItems列表中……
        } else {
            // 检查传递的parentMediaId以查看我们在哪个子菜单中，并将该菜单的子菜单放在mediaItems列表中……
        }
        result.sendResult(mediaItems);

        MusicNotificationBuilder builder = new MusicNotificationBuilder(getBaseContext(),parentId);
        builder.fc(getBaseContext(),mediaSession);
        // 显示通知并将服务放在前台
        startForeground(NOTIFICATION_ID, builder.build());
    }

    private final static int NOTIFICATION_ID = 14650;
}
