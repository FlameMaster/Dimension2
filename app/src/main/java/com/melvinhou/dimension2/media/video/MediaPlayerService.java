package com.melvinhou.dimension2.media.video;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2018/12/10 15:59
 * <p>
 * = 分 类 说 明：媒体播放的服务
 * ============================================================
 */
public class MediaPlayerService extends Service {

    private MediaBinder mMediaBinder;

    @Override
    public IBinder onBind(Intent intent) {
        return mMediaBinder;
    }


    @Override
    public void onCreate() {
        mMediaBinder = new MediaBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        mMediaBinder.release();
        mMediaBinder=null;
        Log.e("结束服务了","mMediaBinder："+mMediaBinder);
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("解除绑定","intent："+intent);
        mMediaBinder.pause();
        return super.onUnbind(intent);
    }

}
