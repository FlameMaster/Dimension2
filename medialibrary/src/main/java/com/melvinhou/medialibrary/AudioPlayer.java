package com.melvinhou.medialibrary;


import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.text.TextUtils;

import com.melvinhou.kami.io.FcLog;
import com.melvinhou.kami.io.FileUtils;
import com.melvinhou.kami.util.FcUtils;


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/2/20 0020 13:58
 * <p>
 * = 分 类 说 明：音频播放器-录音机
 * ================================================
 */
public class AudioPlayer {

    private static final String TAG = AudioPlayer.class.getSimpleName();

    public final static int DEFAULT_AUDIO_RECORD_MAX_TIME = 60;
    private static AudioPlayer sInstance = new AudioPlayer();
    private static String CURRENT_RECORD_FILE = FileUtils.getAppFileDir(FileUtils.RECORD_DIR_SUFFIX) + "auto_";
    private static int MAGIC_NUMBER = 500;
    private static int MIN_RECORD_DURATION = 1000;
    private Callback mRecordCallback;
    private Callback mPlayCallback;

    private String mAudioRecordPath;
    private MediaPlayer mPlayer;
    private MediaRecorder mRecorder;
    private Handler mHandler;

    private AudioPlayer() {
        mHandler = new Handler();
    }

    public static AudioPlayer getInstance() {
        return sInstance;
    }


    public void startRecord(Callback callback) {
        mRecordCallback = callback;
        try {
            mAudioRecordPath = CURRENT_RECORD_FILE + System.currentTimeMillis() + ".m4a";
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            // 使用mp4容器并且后缀改为.m4a，来兼容小程序的播放
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setOutputFile(mAudioRecordPath);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mRecorder.prepare();
            mRecorder.start();
            // 最大录制时间之后需要停止录制
            mHandler.removeCallbacksAndMessages(null);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopInternalRecord();
                    onRecordCompleted(true);
                    mRecordCallback = null;
                    FcUtils.showToast("语音文件已损坏或不存在");
                }
            }, DEFAULT_AUDIO_RECORD_MAX_TIME * 1000);
            updateMicStatus();
        } catch (Exception e) {
            FcLog.w(TAG, "startRecord failed", e);
            stopInternalRecord();
            onRecordCompleted(false);
        }
    }

    public void stopRecord() {
        stopInternalRecord();
        onRecordCompleted(true);
        mRecordCallback = null;
    }

    private void stopInternalRecord() {
        mHandler.removeCallbacksAndMessages(null);
        if (mRecorder == null) {
            return;
        }
        mRecorder.release();
        mRecorder = null;
    }

    public void startPlay(String filePath, Callback callback) {
        mAudioRecordPath = filePath;
        mPlayCallback = callback;
        try {
            mPlayer = new MediaPlayer();
            mPlayer.setDataSource(filePath);
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopInternalPlay();
                    onPlayCompleted(true);
                }
            });
            mPlayer.prepare();
            mPlayer.start();
        } catch (Exception e) {
            FcLog.w(TAG, "startPlay failed", e);
            FcUtils.showToast("语音文件已损坏或不存在");
            stopInternalPlay();
            onPlayCompleted(false);
        }
    }

    public void stopPlay() {
        stopInternalPlay();
        onPlayCompleted(false);
        mPlayCallback = null;
    }

    private void stopInternalPlay() {
        if (mPlayer == null) {
            return;
        }
        mPlayer.release();
        mPlayer = null;
    }

    public boolean isPlaying() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            return true;
        }
        return false;
    }

    private void onPlayCompleted(boolean success) {
        if (mPlayCallback != null) {
            mPlayCallback.onCompletion(success);
        }
        mPlayer = null;
    }

    private void onRecordCompleted(boolean success) {
        if (mRecordCallback != null) {
            mRecordCallback.onCompletion(success);
        }
        mRecorder = null;
    }

    public String getPath() {
        return mAudioRecordPath;
    }

    public int getDuration() {
        if (TextUtils.isEmpty(mAudioRecordPath)) {
            return 0;
        }
        int duration = 0;
        // 通过初始化播放器的方式来获取真实的音频长度
        try {
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(mAudioRecordPath);
            mp.prepare();
            duration = mp.getDuration();
            // 语音长度如果是59s多，因为外部会/1000取整，会一直显示59'，所以这里对长度进行处理，达到四舍五入的效果
            if (duration < MIN_RECORD_DURATION) {
                duration = 0;
            } else {
                duration = duration + MAGIC_NUMBER;
            }
        } catch (Exception e) {
            FcLog.w(TAG, "getDuration failed", e);
        }
        if (duration < 0) {
            duration = 0;
        }
        return duration;
    }

    private void updateMicStatus() {
        if (mRecorder != null) {
            double ratio = (double) mRecorder.getMaxAmplitude() / 1;   // 参考振幅为 1
            double db = 0;// 分贝
            if (ratio > 1) {
                db = 20 * Math.log10(ratio);
            }
            FcLog.d(TAG, "计算分贝值 = " + db + "dB");
            if (mRecordCallback != null) {
                mRecordCallback.onVoiceDb(db);
            }
            mHandler.postDelayed(mUpdateMicStatusTimer, 100); // 间隔取样时间为100秒
        }
    }

    private Runnable mUpdateMicStatusTimer = new Runnable() {
        public void run() {
            updateMicStatus();
        }
    };

    public interface Callback {
        void onCompletion(Boolean success);
        void onVoiceDb(double db);
    }

}
