package com.melvinhou.medialibrary.music.ui;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.melvinhou.medialibrary.R;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;
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
 * = 分 类 说 明：播放器对通知栏的连接
 * ================================================
 */
public class FcMusicNotificationManager {
    private static final String TAG = FcMusicNotificationManager.class.getSimpleName();

//    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);

    private Context mContext;
    private String mChannelId;

    private NotificationCompat.Builder mBuilder;
    private final NotificationCompat.Action mPlayAction;
    private final NotificationCompat.Action mPauseAction;
    private final NotificationCompat.Action mNextAction;
    private final NotificationCompat.Action mPrevAction;

    public FcMusicNotificationManager(@NonNull Context context, @NonNull String channelId) {
        mContext = context;
        mChannelId = channelId;
        mPlayAction =
                new NotificationCompat.Action(
                        android.R.drawable.ic_media_play,
                        "play",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                context,
                                PlaybackStateCompat.ACTION_PLAY));
        mPauseAction =
                new NotificationCompat.Action(
                        android.R.drawable.ic_media_pause,
                        "pause",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                context,
                                PlaybackStateCompat.ACTION_PAUSE));
        mNextAction =
                new NotificationCompat.Action(
                        android.R.drawable.ic_media_next,
                        "next",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                context,
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT));
        mPrevAction =
                new NotificationCompat.Action(
                        android.R.drawable.ic_media_previous,
                        "previous",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                context,
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));
        //androidX的Action意图处理
        //在配置文件中添加MediaButtonReceiver
        //在配置文件中为MusicService添加MEDIA_BUTTON意图
        //MusicService中通过onStartCommand获取传递的意图

        initBuilder();
    }

    private void initBuilder() {
        // 取消所有通知，以处理服务被系统终止并重新启动的情况。
        NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancelAll();

        //android8以上的通知栏需要申请连接
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel("FC音乐播放器", "音乐播放的通知");
        }
        mBuilder = new NotificationCompat.Builder(mContext, mChannelId)

                // 在删除通知时停止服务
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(mContext,
                        PlaybackStateCompat.ACTION_STOP))

                // 即使用户隐藏敏感内容，也可以在锁定屏幕上显示控件。
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                // 添加一个应用程序图标，并设置其强调色注意颜色
                .setSmallIcon(R.mipmap.ic_music_notification)
                .setColor(mContext.getColor(R.color.colorPrimaryDark))
                .setColorized(true)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_LOW)

        //自定义时间
//                .setWhen(SystemClock.currentThreadTimeMillis())
//                .setShowWhen(false)

        // 当用户单击通知时触发的意图。
//                .setContentIntent(createContentIntent())
        ;
    }

    public Notification build() {
        return mBuilder.build();
    }

    public Notification build(MediaSessionCompat mediaSession) {
        try {
            // 给定一个媒体会话及其上下文(通常是包含会话的组件)
            // 创建NotificationCompat。构建器

            // 获取会话的元数据
            MediaControllerCompat controller = mediaSession.getController();
            MediaMetadataCompat mediaMetadata = controller.getMetadata();
            MediaDescriptionCompat description = mediaMetadata.getDescription();
            PlaybackStateCompat state = controller.getPlaybackState();
            //
            mBuilder
                    // 添加当前播放曲目的元数据
                    .setContentTitle(description.getTitle())// Title - 歌曲名称
                    .setContentText(description.getSubtitle())// Subtitle - 作者
                    .setSubText(description.getDescription())
                    .setLargeIcon(description.getIconBitmap())

                    // 当用户单击通知时触发的意图。
//                    .setContentIntent(controller.getSessionActivity())
                    .setContentIntent(createContentIntent());

            // 利用MediaStyle的特性
            MediaStyle style = new MediaStyle()
                    .setMediaSession(mediaSession.getSessionToken())
                    .setShowActionsInCompactView(0, 1, 2)//紧凑ui时显示哪几个button
                    // 添加取消按钮,向后兼容Android L和更早
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(mContext,
                            PlaybackStateCompat.ACTION_STOP));
            //紧凑ui时显示哪几个button
            if (state.getCustomActions().size() > 2) style.setShowActionsInCompactView(0, 1, 2);
            mBuilder.setStyle(style);

            //按钮
            mBuilder.clearActions();
            // 如果已启用“跳转到下一步”。
            if ((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0) {
                mBuilder.addAction(mPrevAction);
            }

            //播放暂停按钮
            mBuilder.addAction(state.getState() == PlaybackStateCompat.STATE_PLAYING ? mPauseAction : mPlayAction);

            // 如果“跳转到上一步”已启用。
            if ((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0) {
                mBuilder.addAction(mNextAction);
            }

        } finally {

            return mBuilder.build();
        }
    }


    /**
     * ,早于版本O不会执行
     *
     * @param channelName
     * @param description
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel(CharSequence channelName, String description) {
        NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm.getNotificationChannel(mChannelId) == null) {
            int importance = NotificationManager.IMPORTANCE_LOW;//重要性级别：中
            NotificationChannel channel = new NotificationChannel(mChannelId, channelName, importance);
            // 配置通知通道。
            channel.setDescription(description);
//            channel.enableLights(true);
            // 如果设备支持此功能，则为发送到此通道的通知设置通知光的颜色。
//            channel.setLightColor(Color.RED);
//            channel.enableVibration(true);
//            channel.enableLights(false);//如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
//            channel.setShowBadge(true);//是否显示角标
//            channel.setVibrationPattern(
//                    new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            nm.createNotificationChannel(channel);
            Log.i(TAG, "createChannel: New channel created");
        } else {
            Log.i(TAG, "createChannel: Existing channel reused");
        }
    }

    //点击时打开页面
    private PendingIntent createContentIntent() {
        Intent startActivityIntent = new Intent(mContext, FcMusicActivity.class);
        PendingIntent processInfoIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            //31，Android11以上系统
            processInfoIntent = PendingIntent.getActivity(mContext, 0, startActivityIntent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            processInfoIntent = PendingIntent.getActivity(mContext, 0, startActivityIntent, PendingIntent.FLAG_ONE_SHOT);
        }
        return processInfoIntent;
    }

}
