package com.melvinhou.medialibrary.music.ui;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.melvinhou.medialibrary.R;

import androidx.media.session.MediaButtonReceiver;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/3/15 0015 9:19
 * <p>
 * = 分 类 说 明：桌面小组件
 * ================================================
 */
public class FcMusicWidgetProvider extends AppWidgetProvider {

    /**
     * 接收窗口小部件点击时发送的广播
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        @PlaybackStateCompat.State
        int state = intent.getIntExtra("state", -1);
        String title = intent.getStringExtra("title");
        Uri icon = intent.getParcelableExtra("icon");
        //更新
        updateWidget(context, title, icon, state == PlaybackStateCompat.STATE_PLAYING);
    }

    /**
     * 每次窗口小部件被更新都调用一次该方法（创建、时间到更新周期都会调起这里）
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetIds
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }


    /**
     * 删除一次窗口小部件就调用一次
     *
     * @param context
     * @param appWidgetIds
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    /**
     * 当最后一个该窗口小部件删除时调用该方法
     *
     * @param context
     */
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    /**
     * 当该窗口小部件第一次添加到桌面时调用该方法
     *
     * @param context
     */
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    /**
     * 当小部件大小改变时
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     * @param newOptions
     */
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    /**
     * 当小部件从备份恢复时调用该方法
     *
     * @param context
     * @param oldWidgetIds
     * @param newWidgetIds
     */
    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }

    /**
     * 更新控件
     *
     * @param context
     * @param title     标题
     * @param icon       图标
     * @param isPlaying 是否播放中
     */
    private void updateWidget(Context context, String title, Uri icon, boolean isPlaying) {
        //初始化RemoteViews
        ComponentName componentName = new ComponentName(context, FcMusicWidgetProvider.class);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.wiget_music_fc);
        //点击事件，点击跳转到FcMusicActivity页面
        Intent startActivityIntent = new Intent(context, FcMusicActivity.class);
        PendingIntent processInfoIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            //31，Android11以上系统
            processInfoIntent = PendingIntent.getActivity(context, 0, startActivityIntent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            processInfoIntent = PendingIntent.getActivity(context, 0, startActivityIntent, PendingIntent.FLAG_ONE_SHOT);
        }
        views.setOnClickPendingIntent(R.id.iv_cover, processInfoIntent);
        views.setOnClickPendingIntent(R.id.player_play,
                MediaButtonReceiver.buildMediaButtonPendingIntent(context, isPlaying
                        ? PlaybackStateCompat.ACTION_PAUSE
                        : PlaybackStateCompat.ACTION_PLAY));
        views.setOnClickPendingIntent(R.id.player_previous,
                MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));
        views.setOnClickPendingIntent(R.id.player_next,
                MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT));

        //更新数据
        if (!TextUtils.isEmpty(title)) views.setTextViewText(R.id.tv_title, title);
        if (icon != null) views.setImageViewUri(R.id.iv_cover, icon);
        views.setImageViewResource(R.id.player_play, isPlaying
                ? android.R.drawable.ic_media_pause
                : android.R.drawable.ic_media_play);
        //开始更新视图
        AppWidgetManager awm = AppWidgetManager.getInstance(context);
        awm.updateAppWidget(componentName, views);
    }
}
