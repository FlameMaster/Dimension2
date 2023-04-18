package com.melvinhou.medialibrary.record;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import com.melvinhou.kami.io.FileUtils;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.medialibrary.R;
import com.melvinhou.medialibrary.music.ui.FcMusicNotificationManager;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/6/3 20:10
 * <p>
 * = 分 类 说 明：录屏的前台服务
 * ================================================
 */
public class ScreenRecordService extends Service {

    private final String TAG = ScreenRecordService.class.getName();

    private RecordBinder mRecordBinder;
    private RecordNotificationMannager mNotificationConnect;

    private int mWidth, mHeight;//录制宽高
    private int mDpi;//帧速率
    private String mRecordPath;//存储位置


    private Surface mSurface;
    //进行音视频压缩编解码
    private MediaCodec mEncoder;
    //将音视频混合生成多媒体文件
    private MediaMuxer mMuxer;
    private boolean mMuxerStarted = false;
    private int mVideoTrackIndex = -1;
    private MediaProjectionManager mProjectionManager;
    //授予捕获屏幕或记录系统音频的功能
    private MediaProjection mMediaProjection;
    //捕获屏幕后将数据输出到投影仪 投影仪可以获取视频的信息，指定输出的位置等
    private VirtualDisplay mVirtualDisplay;
    private AtomicBoolean mQuit;
    private MediaCodec.BufferInfo mBufferInfo;
    //用于将音视频编码输出
    private MediaRecorder mMediaRecorder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mRecordBinder = new RecordBinder();
        return mRecordBinder;
    }


    @Override
    public void onDestroy() {
        if (mRecordBinder != null)
            mRecordBinder.stop();
        mRecordBinder = null;
        release();
        super.onDestroy();
    }

    public class RecordBinder extends Binder {

        public void start() {
            mMediaRecorder.start();
            recordVirtualDisplay();
        }

        public void paused() {
            mVirtualDisplay.setSurface(null);
        }

        public void stop() {
            Log.e(TAG, "停止录制");
            mQuit.set(true);
            mVirtualDisplay.setSurface(null);
            release();
            stopForeground(true);
            if (mNotificationConnect != null)
                mNotificationConnect.stop();
        }

        public void resume() {
            mVirtualDisplay.setSurface(mSurface);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return super.onStartCommand(intent, flags, startId);
        //参数配置
        initConfig(intent);
        //通知栏
        mNotificationConnect = new RecordNotificationMannager(this, getClass().getName());
        startForeground(RecordNotificationMannager.NOTIFICATION_ID, mNotificationConnect.build());

        // 获取 MediaProjection
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        int resultCode = intent.getIntExtra("resultCode", -1);
        Intent data = intent.getParcelableExtra("data");
        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        if (mMediaProjection != null) {
            try {
                initMediaRecorder();//开启声音录制1
                initMediaCodec();
                mMuxer = new MediaMuxer(mRecordPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            /**
             * 创建投影
             * name 本次虚拟显示的名称
             * flags VIRTUAL_DISPLAY_FLAG_PUBLIC 通用显示屏
             * Surface 输出的Surface
             */
            mVirtualDisplay = mMediaProjection.createVirtualDisplay(TAG + "-display",
                    mWidth, mHeight, mDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
//                    mSurface,
                    mMediaRecorder.getSurface(),//开启声音录制2
                    null, null);
            mMediaRecorder.start();//开启声音录制3
            recordVirtualDisplay();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 初始化参数
     *
     * @param intent
     */
    private void initConfig(Intent intent) {
        mWidth = intent.getIntExtra("width", -1);
        mHeight = intent.getIntExtra("height", -1);
//        mSurface = intent.getParcelableExtra("surface");
        mRecordPath = intent.getStringExtra("path");

        mDpi = 1;
        mBufferInfo = new MediaCodec.BufferInfo();
        mQuit = new AtomicBoolean(false);
    }

    /**
     * 编码器准备
     *
     * @throws IOException
     */
    private void initMediaCodec() throws IOException {

        MediaFormat format = MediaFormat.createVideoFormat(ScreenRecordConstant.MIME_TYPE, mWidth, mHeight);
        // 颜色格式
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, ScreenRecordConstant.BIT_RATE);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, ScreenRecordConstant.FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, ScreenRecordConstant.IFRAME_INTERVAL);

        Log.d(TAG, "created video format: " + format);
        mEncoder = MediaCodec.createEncoderByType(ScreenRecordConstant.MIME_TYPE);
        mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mSurface = mEncoder.createInputSurface();
        Log.d(TAG, "created input surface: " + mSurface);
        mEncoder.start();
    }


    /**
     * 录制
     */
    private void recordVirtualDisplay() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mQuit.get()) {
                    try {
                        int index = mEncoder.dequeueOutputBuffer(mBufferInfo, ScreenRecordConstant.TIMEOUT_US);
                        if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                            // 后续输出格式变化
                            resetOutputFormat();
                        } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {
                            // 请求超时
                        } else if (index >= 0) {
                            // 有效输出
                            if (!mMuxerStarted) {
                                throw new IllegalStateException("MediaMuxer没有调用addTrack(format)");
                            }
                            encodeToVideoTrack(index);
                            mEncoder.releaseOutputBuffer(index, false);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        }).start();
    }

    /**
     * 硬解码获取实时帧数据并写入mp4文件
     *
     * @param index
     */
    private void encodeToVideoTrack(int index) {
        // 获取到的实时帧视频数据
        ByteBuffer encodedData = mEncoder.getOutputBuffer(index);

        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            // 当我们获得INFO_OUTPUT_FORMAT_CHANGED状态时，编解码器配置数据被取出并提供给muxer.
            // 忽略它.
            mBufferInfo.size = 0;
        }
        if (mBufferInfo.size == 0) {
            encodedData = null;
        } else {
//      Log.d(TAG, "got buffer, info: size=" + mBufferInfo.size + ", presentationTimeUs="
//          + mBufferInfo.presentationTimeUs + ", offset=" + mBufferInfo.offset);
        }
        if (encodedData != null) {
            mMuxer.writeSampleData(mVideoTrackIndex, encodedData, mBufferInfo);
        }
    }

    private void resetOutputFormat() {
        // 应该在接收缓冲区之前发生，并且应该只发生一次
        if (mMuxerStarted) {
            throw new IllegalStateException("输出格式已经改变!");
        }
        MediaFormat newFormat = mEncoder.getOutputFormat();
        mVideoTrackIndex = mMuxer.addTrack(newFormat);
        mMuxer.start();
        mMuxerStarted = true;
    }

    /**
     * 初始化视频参数
     */
    private void initMediaRecorder() {
        mMediaRecorder = new MediaRecorder();
        // 设置音频来源
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        // 设置视频来源
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        // 设置输出格式
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        // 设置输出文件
        String absolutePath = new File(mRecordPath).getAbsolutePath();
        mMediaRecorder.setOutputFile(absolutePath);
        // 设置视频宽高
        mMediaRecorder.setVideoSize(mWidth, mHeight);
        // 设置视频帧率
        mMediaRecorder.setVideoFrameRate(ScreenRecordConstant.FRAME_RATE);
        // 设置视频编码比特率
        mMediaRecorder.setVideoEncodingBitRate(ScreenRecordConstant.BIT_RATE);
        // 设置音频编码
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        // 设置视频编码
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 资源释放
     */
    private void release() {
        try {
            if (mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
            if (mEncoder != null) {
                mEncoder.stop();
                mEncoder.release();
                mEncoder = null;
            }
            if (mVirtualDisplay != null) {
                mVirtualDisplay.release();
            }
            if (mMediaProjection != null) {
                mMediaProjection.stop();
            }
            if (mMuxer != null) {
                mMuxer.stop();
                mMuxer.release();
                mMuxer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
