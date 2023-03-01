package com.melvinhou.medialibrary.music.ui;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.melvinhou.kami.util.DateUtils;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.view.activities.BaseActivity;
import com.melvinhou.medialibrary.R;
import com.melvinhou.medialibrary.music.component.FcMusicService;
import com.melvinhou.medialibrary.music.proxy.MediaBrowserCallback2;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/3/1 0001 13:30
 * <p>
 * = 分 类 说 明：音乐播放器类
 * ================================================
 */
public class FcMusicActivity extends BaseActivity {

    private ImageView mCoverView, mBackgroundView;
    private View mBarLayout, mPlayButton, mBackwardButton, mForwardButton;
    private SeekBar mProgressBar;
    private TextView mTitleView, mCurrentPositionText, mDurationText, mMessageView;

    private MediaBrowserCompat mediaBrowser;

    //播放器回调
    private MediaBrowserCallback2 mMediaBrowserCallback = new MediaBrowserCallback2() {
        @Override
        public void onMediaListLoaded(List<MediaBrowserCompat.MediaItem> list) {

        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {

        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat mediaMetadata) {

        }

        @Override
        public void onPlayProgressChanged(int progress) {

        }
    };
    //保存指向控制器的链接，以便处理媒体按钮
    private final MediaBrowserCompat.ConnectionCallback connectionCallbacks =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {

                    // 获取MediaSession的令牌
                    MediaSessionCompat.Token token = mediaBrowser.getSessionToken();

                    // 创建MediaControllerCompat
                    MediaControllerCompat mediaController =
                            new MediaControllerCompat(FcMusicActivity.this, // Context
                                    token);

                    // 保存控制器
                    MediaControllerCompat.setMediaController(FcMusicActivity.this, mediaController);

                    // Finish building the UI
                    buildTransportControls();
                }

                @Override
                public void onConnectionSuspended() {
                    // 服务崩溃了。禁用传输控制，直到它自动重新连接
                }

                @Override
                public void onConnectionFailed() {
                    // 服务处拒绝了我们的连接
                }
            };
    //媒体会话的状态或元数据每次发生更改时从媒体会话接收回调
    private final MediaControllerCompat.Callback controllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                }
            };


    @Override
    protected int getLayoutID() {
        return R.layout.activity_music_fc;
    }

    @Override
    protected void initWindowUI() {
        super.initWindowUI();
        //浅色
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().setNavigationBarDividerColor(Color.TRANSPARENT);
        }
    }

    @Override
    protected void initView() {
        mBarLayout = findViewById(R.id.bar_root);
        mTitleView = findViewById(R.id.title);
        mCoverView = findViewById(R.id.iv_cover);
        mBackgroundView = findViewById(R.id.iv_background);
        mPlayButton = findViewById(R.id.player_play);
        mBackwardButton = findViewById(R.id.player_previous);
        mForwardButton = findViewById(R.id.player_next);
        mProgressBar = findViewById(R.id.player_progress);
        mCurrentPositionText = findViewById(R.id.player_progress_text);
        mDurationText = findViewById(R.id.player_progress_max_text);

        //
        mBarLayout.setBackground(null);
        mBarLayout.setPadding(0, DimenUtils.getStatusHeight(), 0, 0);
    }

    @Override
    protected void initListener() {
        mProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCurrentPositionText.setText(
                        DateUtils.formatDuration((int) progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @SuppressLint("CheckResult")
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    protected void initData() {
        // Create MediaBrowserServiceCompat
        mediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, FcMusicService.class),
                connectionCallbacks,
                null); // optional Bundle

    }

    @Override
    public void onStart() {
        super.onStart();
        mediaBrowser.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onStop() {
        super.onStop();
        // (see "stay in sync with the MediaSession")
        if (MediaControllerCompat.getMediaController(FcMusicActivity.this) != null) {
            MediaControllerCompat.getMediaController(FcMusicActivity.this).unregisterCallback(controllerCallback);
        }
        mediaBrowser.disconnect();

    }

    //将您的界面连接到媒体控制器
    void buildTransportControls() {
        // 将侦听器附加到按钮
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 由于这是一个播放/暂停按钮，您需要测试当前状态并相应地选择操作

                int pbState = MediaControllerCompat.getMediaController(FcMusicActivity.this).getPlaybackState().getState();
                if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                    MediaControllerCompat.getMediaController(FcMusicActivity.this).getTransportControls().pause();
                } else {
                    MediaControllerCompat.getMediaController(FcMusicActivity.this).getTransportControls().play();
                }
            }
        });

        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(FcMusicActivity.this);

        // 显示初始状态
        MediaMetadataCompat metadata = mediaController.getMetadata();
        PlaybackStateCompat pbState = mediaController.getPlaybackState();

        // 注册一个Callback以保持同步
        mediaController.registerCallback(controllerCallback);
    }

}
