package com.melvinhou.dimension2.media.music;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.databinding.ActMusicPlayerBD;
import com.melvinhou.kami.mvvm.DataBindingActivity;
import com.melvinhou.kami.util.DateUtils;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.StringUtils;

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
 * = 时 间：2021/4/2 17:49
 * <p>
 * = 分 类 说 明：音乐播放器
 * ================================================
 */
public class MusicPlayerActivity extends DataBindingActivity<ActMusicPlayerBD> {

    //帮助类的回调监听
    private MediaBrowserCallback myMusicCallback;
    private boolean mIsPlaying;
    //是否正在拖动进度条
    private boolean mIsTracking = false;

    @Override
    protected void initWindowUI() {
//        super.initWindowUI();
        //浅色
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
    }

    @Override
    protected int getLayoutID() {
        return R.layout.activity_music_player;
    }

    @Override
    public void close() {
        //finish()不会执行动画所以使用finishAfterTransition()
        finishAfterTransition();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.music_player, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.repeat_mode_invalid:
                MediaBrowserHelper.getTransportControls()
                        .setRepeatMode(PlaybackStateCompat.REPEAT_MODE_INVALID);
                return true;
            case R.id.repeat_mode_one:
                MediaBrowserHelper.getTransportControls()
                        .setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE);
                return true;
            case R.id.repeat_mode_none:
                MediaBrowserHelper.getTransportControls()
                        .setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE);
                return true;
            case R.id.repeat_mode_all:
                MediaBrowserHelper.getTransportControls()
                        .setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initListener() {

        findViewById(R.id.player_play).setOnClickListener(this::OnClick);
        findViewById(R.id.player_previous).setOnClickListener(this::OnClick);
        findViewById(R.id.player_next).setOnClickListener(this::OnClick);

        myMusicCallback =new MyMusicCallback();

        getViewDataBinding().playerProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                getViewDataBinding().playerProgressText.setText(
                        DateUtils.formatDuration((int) progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mIsTracking = true;
            }

            @SuppressLint("CheckResult")
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MediaBrowserHelper.getTransportControls().seekTo(
                        getViewDataBinding().playerProgress.getProgress());
                Observable.timer(1, TimeUnit.SECONDS)
                        .subscribe(aLong -> mIsTracking = false);
            }
        });

    }

    @Override
    protected void initData() {
    }

    @Override
    public void onStart() {
        super.onStart();
        MediaBrowserHelper.registerCallback(myMusicCallback);
    }

    @Override
    public void onStop() {
        super.onStop();
        MediaBrowserHelper.unregisterCallback(myMusicCallback);
    }

    /**
     * 点击事件
     *
     * @param v
     */
    public void OnClick(View v) {

        switch (v.getId()) {
            case R.id.player_previous:
                MediaBrowserHelper.getTransportControls().skipToPrevious();
                break;
            case R.id.player_play:
                if (mIsPlaying) {
                    MediaBrowserHelper.getTransportControls().pause();
                } else {
                    MediaBrowserHelper.getTransportControls().play();
                    //指定播放
//                    MediaBrowserHelper.getTransportControls().playFromMediaId(
//                            MusicLibrary.getMediaItems().get(0).getMediaId(),null);
                }
                break;
            case R.id.player_next:
                MediaBrowserHelper.getTransportControls().skipToNext();
                break;
        }
    }

    /**
     * 切换当前音乐
     *
     * @param mediaMetadata
     */
    private void updataPlayerUI(MediaMetadataCompat mediaMetadata) {
        if (mediaMetadata == null) return;

        String title = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
        String artist = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        long duration = mediaMetadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
//        int drationInt = (int)TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS);
        String coverUri = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI);
        String backgroundUri = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);

        getViewDataBinding().playerProgressMaxText.setText(
                DateUtils.formatDuration((int) duration));
        getViewDataBinding().playerProgress.setMax((int) duration);
        getViewDataBinding().bar.setTitle(title);
        //专辑封面
        Glide.with(FcUtils.getContext())
                .asBitmap()
                .load(coverUri)
                .apply(new RequestOptions()
                        .override(480, 480)
                        .placeholder(R.mipmap.fc)
                        .error(R.mipmap.fc)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(getViewDataBinding().musicCover);

        Glide.with(FcUtils.getContext())
                .asBitmap()
                .load(backgroundUri)
                .apply(RequestOptions.bitmapTransform(
                        new BlurTransformation(25, 3))
                        .error(R.mipmap.fc))
                .into(getViewDataBinding().background);
    }

    /**
     * 更新播放状态
     *
     * @param isPlaying
     */
    private void updatePlayingState(boolean isPlaying) {
        mIsPlaying = isPlaying;
        getViewDataBinding().playerPlay.setImageResource(
                isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
        int padding = DimenUtils.dp2px(4);
        getViewDataBinding().playerPlay.setPadding(isPlaying ? padding : DimenUtils.dp2px(10), padding, padding, padding);
    }

    /**
     * 更新进度
     *
     * @param progress
     */
    private void updateProgressUI(int progress) {
        getViewDataBinding().playerProgress.setProgress(progress);
    }

    /**
     * 媒体控制器控制播放过程中的回调接口，可以用来根据播放状态更新UI
     */
    private class MyMusicCallback implements MediaBrowserCallback{

        @Override
        public void onMediaListLoaded(List<MediaBrowserCompat.MediaItem> list) {

        }

        /**
         * 音乐播放状态改变的回调，例如播放模式，播放、暂停，进度条等
         *
         * @param playbackState
         */
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {
            //播放状态
            updatePlayingState(playbackState != null &&
                    playbackState.getState() == PlaybackStateCompat.STATE_PLAYING);
        }

        /**
         * 播放音乐改变的回调
         *
         * @param mediaMetadata
         */
        @Override
        public void onMetadataChanged(MediaMetadataCompat mediaMetadata) {
            if (mediaMetadata == null) return;
            updataPlayerUI(mediaMetadata);
        }

        @Override
        public void onPlayProgressChanged(int progress) {
            if (mIsTracking)return;
            updateProgressUI(progress);
        }
    }
}
