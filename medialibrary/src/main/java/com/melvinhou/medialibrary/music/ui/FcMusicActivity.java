package com.melvinhou.medialibrary.music.ui;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.melvinhou.kami.util.DateUtils;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.ImageUtils;
import com.melvinhou.kami.view.activities.BaseActivity;
import com.melvinhou.medialibrary.R;
import com.melvinhou.medialibrary.music.component.FcMusicConnection;
import com.melvinhou.medialibrary.music.component.FcMusicService;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;

import static androidx.media.MediaBrowserServiceCompat.BrowserRoot.EXTRA_RECENT;

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
    private boolean isPlaying = false;
    //进度条动画
    private ValueAnimator mProgressAnimator;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_music_fc;
    }

    private FcMusicConnection provideMusicServiceConnection() {
        return FcMusicConnection.getInstance(this);
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
                provideMusicServiceConnection().transportControls.seekTo(seekBar.getProgress());
            }
        });
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 由于这是一个播放/暂停按钮，您需要测试当前状态并相应地选择操作
                if (isPlaying) {
                    provideMusicServiceConnection().transportControls.pause();
                } else {
                    provideMusicServiceConnection().transportControls.play();
                }
            }
        });
        mForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                provideMusicServiceConnection().transportControls.skipToNext();
            }
        });
        mBackwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                provideMusicServiceConnection().transportControls.skipToPrevious();
            }
        });
    }

    @Override
    protected void initData() {

        provideMusicServiceConnection().nowPlaying.observe(this, new Observer<MediaMetadataCompat>() {
            @Override
            public void onChanged(MediaMetadataCompat mediaMetadataCompat) {
                String title = mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
                String artist = mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
                long duration = mediaMetadataCompat.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
//                int drationInt = (int)TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS);
//                String coverUri = mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI);
//                String backgroundUri = mediaMetadataCompat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);
                Bitmap albumArt = mediaMetadataCompat.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART);
                Bitmap displayIcon = mediaMetadataCompat.getBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON);
                //ui更新
                mTitleView.setText(title);
                if (displayIcon!=null)
                    mCoverView.setImageBitmap(displayIcon);
                if (albumArt!=null) {
//                    mBackgroundView.setImageURI(Uri.parse(backgroundUri));
                    mBackgroundView.setImageBitmap(ImageUtils.doBlur(albumArt, 25, false));
                }
                //进度条
                mProgressBar.setMax((int) duration);
                mDurationText.setText(DateUtils.formatDuration((int) duration));
            }
        });
        provideMusicServiceConnection().playbackState.observe(this, new Observer<PlaybackStateCompat>() {
            @Override
            public void onChanged(PlaybackStateCompat playbackStateCompat) {
                int pbState = playbackStateCompat != null ? playbackStateCompat.getState() : PlaybackStateCompat.STATE_NONE;
                isPlaying = pbState == PlaybackStateCompat.STATE_PLAYING;
                mPlayButton.setSelected(isPlaying);
                final int max = mProgressBar.getMax();
                final int progress = playbackStateCompat != null
                        ? (int) playbackStateCompat.getPosition()
                        : 0;
                mProgressBar.setProgress(progress);
                //清除进度
                if (mProgressAnimator != null) {
                    mProgressAnimator.cancel();
                    mProgressAnimator = null;
                }
                if (isPlaying) {
                    //根据播放倍数控制动画
                    final int timeToEnd = (int) ((max - progress) / playbackStateCompat.getPlaybackSpeed());
                    mProgressAnimator = ValueAnimator
                            .ofInt(progress, max)
                            .setDuration(timeToEnd);
                    mProgressAnimator.setInterpolator(new LinearInterpolator());
                    mProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
                            //用动画数值来更新进度
                            final int animatedIntValue = (int) valueAnimator.getAnimatedValue();
                            if (!mProgressBar.isFocusableInTouchMode())
                                mProgressBar.setProgress(animatedIntValue);
                        }
                    });
                    mProgressAnimator.start();
                }
            }
        });
        provideMusicServiceConnection().isConnected.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isConnected) {
                if (isConnected) buildTransportControls();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
//        mediaBrowser.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onStop() {
        super.onStop();
        // (参见“与MediaSession保持同步”)
//        if (MediaControllerCompat.getMediaController(FcMusicActivity.this) != null) {
//            MediaControllerCompat.getMediaController(FcMusicActivity.this).unregisterCallback(controllerCallback);
//        }
//        mediaBrowser.disconnect();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        provideMusicServiceConnection().transportControls.pause();
        String mediaId = provideMusicServiceConnection().rootMediaId;
        provideMusicServiceConnection().unsubscribe(mediaId, null);
        FcMusicConnection.disconnect();
    }

    //将您的界面连接到媒体控制器
    private void buildTransportControls() {

        String mediaId = provideMusicServiceConnection().rootMediaId;
        provideMusicServiceConnection().subscribe(mediaId, new MediaBrowserCompat.SubscriptionCallback() {
            //children 即为Service发送回来的媒体数据集合,在onChildrenLoaded可以执行刷新列表UI的操作
            @Override
            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
                // 将这个简单示例的所有媒体项排队。使用队列需要添加FLAG_HANDLES_QUEUE_COMMANDS
                provideMusicServiceConnection().clearPlayQueue();
                provideMusicServiceConnection().addPlayQueue(children);
                // 现在准备好了，按下播放键就可以播放了。
                provideMusicServiceConnection().transportControls.prepare();

            }
        });
    }

}
