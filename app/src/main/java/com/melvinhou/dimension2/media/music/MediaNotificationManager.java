/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.melvinhou.dimension2.media.music;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.melvinhou.dimension2.R;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.media.app.NotificationCompat.MediaStyle;
import androidx.media.session.MediaButtonReceiver;


/**
 * Keeps track of a notification and updates it automatically for a given MediaSession.
 * This is required so that the music service don't get killed during playback.
 *
 * 跟踪通知并针对给定的MediaSession自动更新它。
 * 这是必须的，这样音乐服务就不会在播放过程中被杀死
 */
public class MediaNotificationManager {

    public static final int NOTIFICATION_ID = 412;

    private static final String TAG = MediaNotificationManager.class.getSimpleName();
    private static final String CHANNEL_ID = "com.example.android.musicplayer.channel";
    private static final int REQUEST_CODE = 501;

    private final MusicService mService;

    private final NotificationCompat.Action mPlayAction;
    private final NotificationCompat.Action mPauseAction;
    private final NotificationCompat.Action mNextAction;
    private final NotificationCompat.Action mPrevAction;
    private final NotificationManager mNotificationManager;

    public MediaNotificationManager(MusicService service) {
        mService = service;

        mNotificationManager =
                (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);

        //TODO PendingIntent暂未处理
        mPlayAction =
                new NotificationCompat.Action(
                        android.R.drawable.ic_media_play,
                        "play",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                mService,
                                PlaybackStateCompat.ACTION_PLAY));
        mPauseAction =
                new NotificationCompat.Action(
                        android.R.drawable.ic_media_pause,
                        "pause",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                mService,
                                PlaybackStateCompat.ACTION_PAUSE));
        mNextAction =
                new NotificationCompat.Action(
                        android.R.drawable.ic_media_next,
                        "next",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                mService,
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT));
        mPrevAction =
                new NotificationCompat.Action(
                        android.R.drawable.ic_media_previous,
                        "previous",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                mService,
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));

        // 取消所有通知，以处理服务被系统终止并重新启动的情况。
        mNotificationManager.cancelAll();
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
    }

    public NotificationManager getNotificationManager() {
        return mNotificationManager;
    }

    public Notification getNotification(MediaMetadataCompat metadata,
                                        @NonNull PlaybackStateCompat state,
                                        MediaSessionCompat.Token token) {
        boolean isPlaying = state.getState() == PlaybackStateCompat.STATE_PLAYING;
        MediaDescriptionCompat description = metadata.getDescription();
        NotificationCompat.Builder builder =
                buildNotification(state, token, isPlaying, description);
        return builder.build();
    }

    private NotificationCompat.Builder buildNotification(@NonNull PlaybackStateCompat state,
                                                         MediaSessionCompat.Token token,
                                                         boolean isPlaying,
                                                         MediaDescriptionCompat description) {

        // 在Android O上运行时创建(强制性的)通知通道。
        if (isAndroidOOrHigher()) {
            createChannel();
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mService, CHANNEL_ID);
        builder.setStyle(
                new MediaStyle()
                        .setMediaSession(token)
                        .setShowActionsInCompactView(0, 1, 2)
                        // 向后兼容Android L和更早。
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(
                                MediaButtonReceiver.buildMediaButtonPendingIntent(
                                        mService,
                                        PlaybackStateCompat.ACTION_STOP)))
                .setColor(ContextCompat.getColor(mService, R.color.colorAccent))
                .setSmallIcon(R.mipmap.ic_launcher)
                // 当用户单击通知时触发的意图。
                .setContentIntent(createContentIntent())
                // Title - 歌曲名称.
                .setContentTitle(description.getTitle())
                // Subtitle - 作者.
                .setContentText(description.getSubtitle())
                .setLargeIcon(MusicLibrary.getAlbumBitmap(mService, description.getMediaId()))
                // 当通知被删除时(当回放被暂停并且通知可以被删除时)用ACTION_STOP触发MediaButtonPendingIntent。
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(
                        mService, PlaybackStateCompat.ACTION_STOP))
                // 即使用户隐藏敏感内容，也可以在锁定屏幕上显示控件。
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        // 如果已启用“跳转到下一步”。
        if ((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0) {
            builder.addAction(mPrevAction);
        }

        builder.addAction(isPlaying ? mPauseAction : mPlayAction);

        // 如果“跳转到上一步”已启用。
        if ((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0) {
            builder.addAction(mNextAction);
        }

        return builder;
    }

    // 早于版本O不会执行.
    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        if (mNotificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            // The user-visible name of the channel.
            CharSequence name = "MediaSession";
            // The user-visible description of the channel.
            String description = "MediaSession and MediaPlayer";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            // Configure the notification channel.
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            // 如果设备支持此功能，则为发送到此通道的通知设置通知光的颜色。
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(
                    new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mNotificationManager.createNotificationChannel(mChannel);
            Log.d(TAG, "createChannel: New channel created");
        } else {
            Log.d(TAG, "createChannel: Existing channel reused");
        }
    }

    //版本判断
    private boolean isAndroidOOrHigher() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    //点击时打开页面
    private PendingIntent createContentIntent() {
        //TODO 有报错懒得改
        Intent openUI = new Intent(mService, MusicPlayerActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(
                mService, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT|PendingIntent.FLAG_MUTABLE);
    }

}