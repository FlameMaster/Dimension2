package com.melvinhou.dimension2.media.music;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.melvinhou.dimension2.R;
import com.melvinhou.kami.util.ImageUtils;
import com.melvinhou.kami.util.ResourcesUtils;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;

import androidx.media.session.MediaButtonReceiver;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/12/16 18:56
 * <p>
 * = 分 类 说 明：控制音乐播放桌面控件
 * ================================================
 */
public class MusicWidgetProvider extends AppWidgetProvider {

    private final String TAG = "MusicWidgetProvider";

    public static final String ACTION_APPWIDGET_UPDATE = "android.appwidget.action.APPWIDGET_UPDATE";
    /*小控件的管理者*/
    private AppWidgetManager mWidgetManager;
    private boolean mIsPlaying;
    private String mTitle;
    private String mArtist;
    private Uri mCoverUri;

    /**
     * 没接收一次广播消息就调用一次，使用频繁
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        // <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />

        String action = intent.getAction();
        if (ACTION_APPWIDGET_UPDATE.equals(action)) {
            int state = intent.getIntExtra("state", -1);
            switch (state) {
                case PlaybackStateCompat.STATE_PLAYING:
                    mIsPlaying = true;
                    mTitle = intent.getStringExtra("title");
                    mArtist = intent.getStringExtra("artist");
                    mCoverUri = intent.getParcelableExtra("cover");
                    updateAllAppWidgets(context);
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                    mIsPlaying = false;
                    mTitle = intent.getStringExtra("title");
                    mArtist = intent.getStringExtra("artist");
                    mCoverUri = intent.getParcelableExtra("cover");
                    updateAllAppWidgets(context);
                    break;
                case PlaybackStateCompat.STATE_STOPPED:
                    mIsPlaying = false;
                    mTitle = null;
                    mArtist = null;
                    mCoverUri = null;
                    updateAllAppWidgets(context);
                    break;
            }
        }

    }

    /**
     * 每次更新都调用一次该方法，使用频繁
     * 平时半个小时更新一次
     */
    @Override
    public void onUpdate(final Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    /**
     * 没删除一个就调用一次
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    /**
     * 当该Widget第一次添加到桌面是调用该方法，可添加多次但只第一次调用
     */
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        updateAllAppWidgets(context);
    }

    /**
     * 更新ui
     *
     * @param context
     */
    private void updateAllAppWidgets(Context context) {
        // 桌面小控件的管理者
        mWidgetManager = AppWidgetManager.getInstance(context);

        // 初始化一个远程的view Remote 远程
        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.wiget_music_tools);
        // 设置文本
        views.setTextViewText(R.id.title, TextUtils.isEmpty(mTitle)
                ? new StringBuffer("聆听你的声音-").append(ResourcesUtils.getString(R.string.app_name))
                : new StringBuffer(mTitle).append(" - ").append(mArtist));
        //封面
        if (mCoverUri != null) views.setImageViewBitmap(R.id.cover,
                getAlbumBitmap(context, mCoverUri, 128, 128));
        else views.setImageViewResource(R.id.cover, R.mipmap.fc);
        //图标
        views.setImageViewResource(R.id.tab_play, mIsPlaying
                ? android.R.drawable.ic_media_pause
                : android.R.drawable.ic_media_play);


        //TODO 没有设置MusicService未启动的意图
        //打开app
        Intent intent = new Intent();
        intent.setClass(context, MusicListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, 0);
        // 设置点击事件
        views.setOnClickPendingIntent(R.id.cover, pendingIntent);
        views.setOnClickPendingIntent(R.id.tab_play,
                MediaButtonReceiver.buildMediaButtonPendingIntent(context, mIsPlaying
                        ? PlaybackStateCompat.ACTION_PAUSE
                        : PlaybackStateCompat.ACTION_PLAY));
        views.setOnClickPendingIntent(R.id.tab_previous,
                MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));
        views.setOnClickPendingIntent(R.id.tab_next,
                MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT));

        // 第一个参数表示上下文，第二个参数表示当前有哪一个广播进行去处理当前的桌面小控件
        ComponentName provider = new ComponentName(
                context, MusicWidgetProvider.class);
        // 更新桌面
        mWidgetManager.updateAppWidget(provider, views);

    }

    /**
     * 当最后一个该Widget删除是调用该方法，注意是最后一个
     */
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }


    /**
     * 获取封面
     *
     * @param context
     * @param uri
     * @return
     */
    public Bitmap getAlbumBitmap(Context context, Uri uri,
                                   int reqWidth, int reqHeight) {
        Bitmap bm = null;
        try {
            FileDescriptor fd = null;
            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
            if (pfd != null) fd = pfd.getFileDescriptor();
            bm = ImageUtils.decodeBitmapFromFileDescriptor(fd, reqWidth, reqHeight);
            if (bm == null)
                bm = ImageUtils.getBitmapResources(R.mipmap.fc, reqWidth, reqHeight);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            bm = ImageUtils.getBitmapResources(R.mipmap.fc, reqWidth, reqHeight);
        } finally {
            return bm;
        }
    }
}
