package com.melvinhou.dimension2.media.music;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.melvinhou.dimension2.media.music.proxy.PlaybackInfoListener;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/4/2 21:35
 * <p>
 * = 分 类 说 明：播放器实现，处理播放音乐与正确处理耳机和音频焦点
 * ================================================
 */
public final class  PlayerAdapter {


    private static final float MEDIA_VOLUME_DEFAULT = 1.0f;
    private static final float MEDIA_VOLUME_DUCK = 0.2f;
    private static final IntentFilter AUDIO_NOISY_INTENT_FILTER =
            new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

    private boolean mAudioNoisyReceiverRegistered = false;
    private final BroadcastReceiver mAudioNoisyReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                        if (isPlaying()) {
                            pause();
                        }
                    }
                }
            };



    private final Context mContext;
    //媒体播放器对象
    private MediaPlayer mMediaPlayer;
    //状态更新的监听器
    private PlaybackInfoListener mPlaybackInfoListener;
    //音乐文件路径
    private String mFilename;
    //当前媒体
    private MediaMetadataCompat mCurrentMedia;
    //播放状态
    private int mState;
    //当前媒体是否播放完
    private boolean mCurrentMediaPlayedToCompletion;

    // 解决一个与MediaPlayer. seekto()在不播放时的行为相关的MediaPlayer bug。
    private int mSeekWhileNotPlaying = -1;


    private final AudioManager mAudioManager;
    private final AudioFocusHelper mAudioFocusHelper;
    private boolean mPlayOnAudioFocus = false;



    public PlayerAdapter(Context context, PlaybackInfoListener listener) {
        mContext = context.getApplicationContext();
        mPlaybackInfoListener = listener;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mAudioFocusHelper = new AudioFocusHelper();
    }






    private void initializeMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    setNewState(PlaybackStateCompat.STATE_PAUSED);
                    mPlaybackInfoListener.onPlaybackCompleted();
                }
            });
        }
    }

    // 准备播放是音频
    public void playFromMedia(MediaMetadataCompat metadata) {
        mCurrentMedia = metadata;
        final String mediaId = metadata.getDescription().getMediaId();
        playFile(MusicLibrary.getMusicFilename(mediaId));
    }

    public MediaMetadataCompat getCurrentMedia() {
        return mCurrentMedia;
    }

    private void playFile(String filename) {
        boolean mediaChanged = (mFilename == null || !filename.equals(mFilename));
        if (mCurrentMediaPlayedToCompletion) {
            // 最后一个音频文件被播放到完成，resourceId没有改变，但是播放器被释放了，所以强制重新加载媒体文件来播放。
            mediaChanged = true;
            mCurrentMediaPlayedToCompletion = false;
        }
        if (!mediaChanged) {
            if (!isPlaying()) {
                play();
            }
            return;
        } else {
            release();
        }

        mFilename = filename;

        initializeMediaPlayer();

        try {
//            AssetFileDescriptor assetFileDescriptor = mContext.getAssets().openFd(mFilename);
//            mMediaPlayer.setDataSource(
//                    assetFileDescriptor.getFileDescriptor(),
//                    assetFileDescriptor.getStartOffset(),
//                    assetFileDescriptor.getLength());
            mMediaPlayer.setDataSource(mFilename);
        } catch (Exception e) {
            throw new RuntimeException("Failed to open file: " + mFilename, e);
        }

        try {
            mMediaPlayer.prepare();
        } catch (Exception e) {
            throw new RuntimeException("Failed to open file: " + mFilename, e);
        }

        play();
    }

    /**
     * 播放状态
     * @return
     */
    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    public final void play() {
        if (mAudioFocusHelper.requestAudioFocus()) {
            registerAudioNoisyReceiver();
            onPlay();
        }
    }
    protected void onPlay() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
            setNewState(PlaybackStateCompat.STATE_PLAYING);
        }
    }

    public final void pause() {
        if (!mPlayOnAudioFocus) {
            mAudioFocusHelper.abandonAudioFocus();
        }

        unregisterAudioNoisyReceiver();
        onPause();
    }

    protected void onPause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            setNewState(PlaybackStateCompat.STATE_PAUSED);
        }
    }

    public final void stop() {
        mAudioFocusHelper.abandonAudioFocus();
        unregisterAudioNoisyReceiver();
        onStop();
    }

    public void onStop() {
        // 无论MediaPlayer是否已创建/启动，都必须更新状态，以便MediaNotificationManager可以关闭通知。
        setNewState(PlaybackStateCompat.STATE_STOPPED);
        release();
    }

    public void seekTo(long position) {
        if (mMediaPlayer != null) {
            if (!mMediaPlayer.isPlaying()) {
                mSeekWhileNotPlaying = (int) position;
            }
            mMediaPlayer.seekTo((int) position);

            // 设置状态(为当前状态)，因为位置改变了，应该报告给客户端。
            setNewState(mState);
        }
    }

    public void setVolume(float volume) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(volume, volume);
        }
    }

    private void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }


    private void registerAudioNoisyReceiver() {
        if (!mAudioNoisyReceiverRegistered) {
            mContext.registerReceiver(mAudioNoisyReceiver, AUDIO_NOISY_INTENT_FILTER);
            mAudioNoisyReceiverRegistered = true;
        }
    }

    private void unregisterAudioNoisyReceiver() {
        if (mAudioNoisyReceiverRegistered) {
            mContext.unregisterReceiver(mAudioNoisyReceiver);
            mAudioNoisyReceiverRegistered = false;
        }
    }


    // 这是玩家状态机的主要减速机。
    private void setNewState(@PlaybackStateCompat.State int newPlayerState) {
        mState = newPlayerState;

        // 无论播放转到完成，还是停止，mCurrentMediaPlayedToCompletion被设置为true。
        if (mState == PlaybackStateCompat.STATE_STOPPED) {
            mCurrentMediaPlayedToCompletion = true;
        }

        // 处理MediaPlayer.getCurrentPosition()，当它在不播放时改变。
        final long reportPosition;
        if (mSeekWhileNotPlaying >= 0) {
            reportPosition = mSeekWhileNotPlaying;

            if (mState == PlaybackStateCompat.STATE_PLAYING) {
                mSeekWhileNotPlaying = -1;
            }
        } else {
            reportPosition = mMediaPlayer == null ? 0 : mMediaPlayer.getCurrentPosition();
        }

        final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
        stateBuilder.setActions(getAvailableActions());
        stateBuilder.setState(mState,
                reportPosition,
                1.0f,
                SystemClock.elapsedRealtime());
        mPlaybackInfoListener.onPlaybackStateChange(stateBuilder.build());
    }

    /**
     * 设置此会话上可用的当前功能.
     * 注意:如果功能的位掩码中没有列出某个功能，那么MediaSession将不会处理它.
     * 例如，如果您不希望MediaSession处理ACTION_STOP，那么不要将它包含在返回的位掩码中。
     */
    @PlaybackStateCompat.Actions
    private long getAvailableActions() {
        long actions = PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                | PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
        switch (mState) {
            case PlaybackStateCompat.STATE_STOPPED:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PAUSE;
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                actions |= PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_SEEK_TO;
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_STOP;
                break;
            default:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PLAY_PAUSE
                        | PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_PAUSE;
        }
        return actions;
    }






    /**
     * 管理音频焦点相关任务的助手类。
     */
    private final class AudioFocusHelper
            implements AudioManager.OnAudioFocusChangeListener {

        private boolean requestAudioFocus() {
            final int result = mAudioManager.requestAudioFocus(this,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
            return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }

        private void abandonAudioFocus() {
            mAudioManager.abandonAudioFocus(this);
        }

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (mPlayOnAudioFocus && !isPlaying()) {
                        play();
                    } else if (isPlaying()) {
                        setVolume(MEDIA_VOLUME_DEFAULT);
                    }
                    mPlayOnAudioFocus = false;
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    setVolume(MEDIA_VOLUME_DUCK);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (isPlaying()) {
                        mPlayOnAudioFocus = true;
                        pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    mAudioManager.abandonAudioFocus(this);
                    mPlayOnAudioFocus = false;
                    stop();
                    break;
            }
        }
    }
}
