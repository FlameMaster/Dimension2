package com.melvinhou.medialibrary.record;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.medialibrary.R;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/4/18 0018 14:15
 * <p>
 * = 分 类 说 明：屏幕录制的通知
 * ================================================
 */
public class RecordNotificationMannager {
    private static final String TAG = RecordNotificationMannager.class.getSimpleName();
    public final static int NOTIFICATION_ID = 13568;

    private NotificationCompat.Builder mBuilder;
    private Context mContext;
    private String mChannelId;

    RecordNotificationMannager(@NonNull Context context,@NonNull String channelId){
        mContext = context;
        mChannelId = channelId;
        initBuilder();
    }

    private void initBuilder() {
        // 取消所有通知，以处理服务被系统终止并重新启动的情况。
        NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancelAll();

        //android8以上的通知栏需要申请连接
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel("FC屏幕录制", "FC屏幕录制工具");
        }

        //停止录制的意图
        Intent intent = new Intent(mContext, ScreenRecordReceiver.class);
        intent.setAction("stop");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                mContext, 0, intent, PendingIntent.FLAG_ONE_SHOT|PendingIntent.FLAG_IMMUTABLE);
        mBuilder = new NotificationCompat.Builder(mContext, mChannelId)
                .setContentTitle("FC屏幕录制")
                .setContentText("正在录制，点击停止录制")
                // 当用户单击通知时触发的意图。
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)//点击后关闭通知

                // 在删除通知时停止服务
                .setDeleteIntent(pendingIntent)

                // 添加一个应用程序图标
                .setSmallIcon(R.drawable.ic_video_record)

        //自定义时间
//                .setWhen(SystemClock.currentThreadTimeMillis())
//                .setShowWhen(false)
        ;
    }

    public Notification build() {
        return mBuilder.build();
    }

    public void stop(){
//            mNotificationManager.cancel(NOTIFICATION_ID);
        NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancelAll();
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
            int importance = NotificationManager.IMPORTANCE_DEFAULT;//重要性级别：中
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
}
