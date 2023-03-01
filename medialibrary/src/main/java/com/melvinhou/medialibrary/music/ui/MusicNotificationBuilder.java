package com.melvinhou.medialibrary.music.ui;

import android.content.Context;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.melvinhou.medialibrary.R;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/3/1 0001 17:24
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class MusicNotificationBuilder  extends NotificationCompat.Builder{

//    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);


    public MusicNotificationBuilder(@NonNull Context context, @NonNull String channelId) {
        super(context, channelId);
    }

    public void fc(@NonNull Context context, MediaSessionCompat mediaSession){

        // Given a media session and its context (usually the component containing the session)
        // Create a NotificationCompat.Builder

        // Get the session's metadata
        MediaControllerCompat controller = mediaSession.getController();
        MediaMetadataCompat mediaMetadata = controller.getMetadata();
        MediaDescriptionCompat description = mediaMetadata.getDescription();

        //
        this
                // 添加当前播放曲目的元数据
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setSubText(description.getDescription())
                .setLargeIcon(description.getIconBitmap())

                // 启用启动播放器点击通知
                .setContentIntent(controller.getSessionActivity())

                // 在删除通知时停止服务
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                        PlaybackStateCompat.ACTION_STOP))

                // 使运输控制在锁定屏幕上可见
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                // 添加一个应用程序图标，并设置其强调色注意颜色
                .setSmallIcon(R.drawable.ic_star_01_h)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))

                // 添加暂停按钮
                .addAction(new NotificationCompat.Action(
                        R.drawable.ic_media_pause_02, "暂停",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                                PlaybackStateCompat.ACTION_PLAY_PAUSE)))

                // 利用MediaStyle的特性
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0)

                        // 添加取消按钮
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                                PlaybackStateCompat.ACTION_STOP)));

    }

}
