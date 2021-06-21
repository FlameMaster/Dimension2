package com.melvinhou.dimension2.media.video;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Binder;
import android.util.Log;
import android.view.SurfaceHolder;

import com.melvinhou.kami.model.EventMessage;
import com.melvinhou.kami.util.IOUtils;
import com.melvinhou.rxjava.RxBus;

import java.io.Serializable;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2018/12/10 14:54
 * <p>
 * = 分 类 说 明：媒体播放辅助类
 * ============================================================
 */
public class MediaBinder extends Binder implements Serializable, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    /*媒体列表*/
    private List<MediaModel> mMedias;
    /*播放器*/
    private MediaPlayer mMediaPlayer;
    /*当前位置*/
    private int nowMediaPosition, nowProgress;

    public MediaBinder() {
        if (mMediaPlayer != null) mMediaPlayer.reset();
        mMediaPlayer = new MediaPlayer();
        nowMediaPosition = 0;
        nowProgress = 0;
    }


    /*释放资源*/
    public void release() {
        nowMediaPosition = 0;
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mMedias != null) {
            mMedias.clear();
            mMedias = null;
        }
    }

    /*设置播放列表*/
    public void setMedias(List<MediaModel> datas) {
        mMedias = datas;
    }

    /*设置视频播放的holder*/
    public void setDisplay(SurfaceHolder holder) {
        mMediaPlayer.setDisplay(holder);
    }

    /*播放完成后*/
    @Override
    public void onCompletion(MediaPlayer mp) {
        //判断是否播放完，误差2秒
        if (Math.abs(mp.getCurrentPosition() - mp.getDuration()) < 2000) {
            nowProgress = 0;
        } else {
            //没播放完，断点续播
            Log.e("没播放完毕", "总共：" + mp.getDuration() + "、、、进度：" + mp.getCurrentPosition());
            nowProgress = mp.getCurrentPosition();
            play(nowMediaPosition);
            return;
        }
//        // 自动播放下一首
//        next();
        //通知ui变更
        RxBus.get().post(new EventMessage(EventMessage.EventType.ALL, "tv_completion", nowMediaPosition));
    }

    /*媒体准备完成*/
    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.e("onPrepared", "媒体准备完成");
        start();//开始播放
        if (nowProgress > 0) {    //如果不是从头播放
            seekTo(nowProgress);
        }
        RxBus.get().post(new EventMessage(EventMessage.EventType.ALL, "tv_loading", false));
    }

    /*如果正在播放则返回true*/
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    /*播放*/
    @SuppressLint("CheckResult")
    public void play(int position) {
        RxBus.get().post(new EventMessage(EventMessage.EventType.ALL, "tv_loading", true));

        //列表循环
        if (position < 0) position = mMedias.size() - 1;
        else if (position >= mMedias.size()) position = 0;

        nowMediaPosition = position;
        //通知ui变更
        RxBus.get().post(new EventMessage(EventMessage.EventType.ALL, "tv_play", position));

        String url = mMedias.get(position).getUrl();
        //判断是否过期
        Observable
                .create((ObservableOnSubscribe<Boolean>) emitter -> {
                    emitter.onNext(IOUtils.isHttpHas(url));
                    emitter.onComplete();
                })
                .compose(IOUtils.setThread())
                .subscribe(ishas -> {
                    if (ishas) {
                        try {
                            mMediaPlayer.reset();//把各项参数恢复到初始状态
                            mMediaPlayer.setDataSource(url);
                            mMediaPlayer.prepareAsync();
//                          mMediaPlayer.prepare();    //进行缓冲
                            mMediaPlayer.setOnCompletionListener(this);
                            mMediaPlayer.setOnPreparedListener(this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        mMediaPlayer.stop();
                        //通知ui变更
                        RxBus.get().post(new EventMessage(EventMessage.EventType.ALL, "tv_expired"));
                    }
                });
    }

    /*暂停*/
    public void pause() {
        if (mMediaPlayer != null && isPlaying()) {
            //通知ui变更
            RxBus.get().post(new EventMessage(EventMessage.EventType.ALL, "tv_pause"));
            mMediaPlayer.pause();
        }
    }

    /*开始播放*/
    public void start() {
        if (mMediaPlayer != null && !isPlaying()) {
            mMediaPlayer.start();
            //通知ui变更
            RxBus.get().post(new EventMessage(EventMessage.EventType.ALL, "tv_start"));
        }
    }

    /*停止*/
    public void stop() {
        nowProgress = 0;
        if (mMediaPlayer != null) {
            //通知ui变更
            mMediaPlayer.stop();
            try {
                RxBus.get().post(new EventMessage(EventMessage.EventType.ALL, "tv_stop"));
                mMediaPlayer.prepare(); // 在调用stop后如果需要再次通过start进行播放,需要之前调用prepare函数
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*跳转播放进度*/
    public void seekTo(int mSec) {
        mMediaPlayer.seekTo(mSec);
    }

    /*下一首*/
    public void next() {
        nowProgress = 0;
        play(nowMediaPosition + 1);
    }

    /*上一首*/
    public void previous() {
        nowProgress = 0;
        play(nowMediaPosition - 1);
    }

    /* 返回当前的总时长*/
    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    /*返回当前已播放时间*/
    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    /*获取当前位置*/
    public int getNowMediaPosition() {
        return nowMediaPosition;
    }

    /*获取对应位置的数据*/
    public MediaModel getMediaData(int position) {
        return mMedias.get(position);
    }

    /*获取视频宽度*/
    public int getVideoWidth() {
        if (mMediaPlayer == null)
            return 0;
        return mMediaPlayer.getVideoWidth();
    }

    /*获取视频高度*/
    public int getVideoHeight() {
        if (mMediaPlayer == null)
            return 0;
        return mMediaPlayer.getVideoHeight();
    }
}
