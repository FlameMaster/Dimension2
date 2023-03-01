package com.melvinhou.dimension2.media.music;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.databinding.ActMusicListBD;
import com.melvinhou.dimension2.media.music.proxy.MediaBrowserCallback;
import com.melvinhou.dimension2.media.music.proxy.MediaBrowserHelper;
import com.melvinhou.dimension2.net.HttpConstant;
import com.melvinhou.kami.adapter.RecyclerAdapter2;
import com.melvinhou.kami.adapter.RecyclerHolder;
import com.melvinhou.kami.mvvm.DataBindingActivity;
import com.melvinhou.kami.tool.GlideBlurTransformation;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.ResourcesUtils;

import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/4/3 21:12
 * <p>
 * = 分 类 说 明：音乐列表
 * ================================================
 */
public class MusicListActivity extends DataBindingActivity<ActMusicListBD> {

    private MusicAdapter mAdapter;
    private static final String URL_BACKGROUND
            = HttpConstant.SERVER_RES +"image/background/music_background.jpg";
    private static final int REQUEST_CODE_PERMISSIONS = 21;
    /**
     * 从Android 10 开始，应用即使申请了权限，也只能读写自己外部存储的私有目录，
     * 就是Android/data/对应应用包名下的相关目目录。
     * 除此之外任何目录的读写都会被拒绝，并提示android Permission denied。
     * 需要在AndroidManifest.xml 文件中，在application标签中添加如下属性android:requestLegacyExternalStorage=“true”
     */
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    //帮助类的回调监听
    private MediaBrowserCallback myMusicCallback;
    private boolean mIsPlaying;


    @Override
    protected void initWindowUI() {
//        super.initWindowUI();
        //浅色
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(ResourcesUtils.getColor(R.color.colorAccent));
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 请求音乐播放权限
        if (checkPermission()) {
            MediaBrowserHelper.registerCallback(myMusicCallback);
        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        MediaBrowserHelper.unregisterCallback(myMusicCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //停止音乐播放服务
        FcUtils.getContext().stopService(
                new Intent(FcUtils.getContext(),MusicService.class));
    }

    /**
     * 音乐权限判断
     *
     * @return
     */
    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(
                FcUtils.getContext(), REQUIRED_PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 权限申请返回
     *
     * @param requestCode  请求权限时传入的请求码，用于区别是哪一次请求的
     * @param permissions  所请求的所有权限的数组
     * @param grantResults 权限授予结果，和 permissions 数组参数中的权限一一对应，元素值为两种情况，如下:
     *                     授予: PackageManager.PERMISSION_GRANTED
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (checkPermission()) {
                MediaBrowserHelper.registerCallback(myMusicCallback);
            } else {
                Toast.makeText(this,
                        "没有权限读取音乐列表",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected int getLayoutID() {
        return R.layout.activity_music_list;
    }

    @Override
    protected void initView() {
        getViewDataBinding().bar.setTitle("音乐列表");
    }

    @Override
    protected void initListener() {
        mAdapter = new MusicAdapter();
        getViewDataBinding().list.setLayoutManager(new GridLayoutManager(FcUtils.getContext(), 3));
        getViewDataBinding().list.setAdapter(mAdapter);
        //设置边距
        DividerItemDecoration itemDecoration = new DividerItemDecoration(
                FcUtils.getContext(), DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(ResourcesUtils.getDrawable(R.drawable.line_s8));
        getViewDataBinding().list.addItemDecoration(itemDecoration);
        DividerItemDecoration itemDecoration2 = new DividerItemDecoration(
                FcUtils.getContext(), DividerItemDecoration.HORIZONTAL);
        itemDecoration2.setDrawable(ResourcesUtils.getDrawable(R.drawable.line_s8));
        getViewDataBinding().list.addItemDecoration(itemDecoration2);
        mAdapter.setOnItemClickListener((viewHolder, position, data) -> playMusic(data));

        getViewDataBinding().musicBottomCover.setOnClickListener(this::toMusicPlayer);
        getViewDataBinding().musicBottomTitle.setOnClickListener(this::toMusicPlayer);
        getViewDataBinding().musicBottomArtist.setOnClickListener(this::toMusicPlayer);
        getViewDataBinding().musicBottomPlay.setOnClickListener(this::playMusic);

        //播放器监听
        myMusicCallback = new MyMusicCallback();

    }

    @Override
    protected void initData() {
//        Glide.with(FcUtils.getContext()).load(URL_BACKGROUND).into(getViewDataBinding().musicTopCover);
    }

    private void updatePlayingState(boolean isPlaying) {
        mIsPlaying = isPlaying;
        getViewDataBinding().musicBottomPlay.setBackgroundResource(
                isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
    }

    private void updataPlayerUI(MediaMetadataCompat mediaMetadata) {
        String title = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
        String artist = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        long duration = mediaMetadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
//        int drationInt = (int)TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS);
        String coverUri = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI);
        String backgroundUri = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);

        getViewDataBinding().musicBottomTitle.setText(title);
        getViewDataBinding().musicBottomArtist.setText(artist);
        getViewDataBinding().musicBottomProgress.setMax((int) duration);

        Glide.with(FcUtils.getContext())
                .asBitmap()
                .load(coverUri)
                .apply(new RequestOptions()
                        .override(480, 480)
                        .placeholder(R.mipmap.fc)
                        .error(R.mipmap.fc)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(getViewDataBinding().musicBottomCover);
        Glide.with(FcUtils.getContext())
                .asBitmap()
                .load(backgroundUri)
                .apply(RequestOptions.bitmapTransform(
                        new GlideBlurTransformation(25, 1)))
                .into(getViewDataBinding().musicTopCover);

    }

    private void updateProgressUI(int progress) {
        getViewDataBinding().musicBottomProgress.setProgress(progress);
    }

    /**
     * 播放选中音乐
     *
     * @param mediaItem
     */
    private void playMusic(MediaBrowserCompat.MediaItem mediaItem) {
        try {
            long id = Long.parseLong(mediaItem.getDescription().getMediaId());
            MediaBrowserHelper.getTransportControls().skipToQueueItem(id);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void playMusic(View view) {
        if (mIsPlaying) {
            MediaBrowserHelper.getTransportControls().pause();
        } else {
            MediaBrowserHelper.getTransportControls().play();
        }
    }

    /**
     * 打开播放器
     *
     * @param view
     */
    private void toMusicPlayer(View view) {
        Intent intent = new Intent(FcUtils.getContext(), MusicPlayerActivity.class);
        toActivity(getViewDataBinding().musicBottomCover, intent);
    }


    class MusicAdapter extends RecyclerAdapter2<MediaBrowserCompat.MediaItem, RecyclerHolder> {

        TextView title, writer;
        RequestOptions options;

        @SuppressLint("CheckResult")
        MusicAdapter() {
            options = new RequestOptions();
            options.centerCrop()
                    .override(480, 480)
                    .placeholder(R.mipmap.fc)
                    .error(R.mipmap.fc)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);
        }

        @Override
        public int getItemLayoutId(int viewType) {
            return R.layout.item_music;
        }

        @Override
        protected RecyclerHolder onCreate(View view, int viewType) {
            return new RecyclerHolder(view);
        }

        @Override
        public RecyclerHolder onCustomCreate(View insertView, int viewType) {
            View view = new View(FcUtils.getContext());
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT, DimenUtils.dp2px(32));
            view.setLayoutParams(lp);
            return new RecyclerHolder(view);
        }

        @Override
        public void bindData(RecyclerHolder viewHolder, int position, MediaBrowserCompat.MediaItem mediaId) {
            title = viewHolder.itemView.findViewById(R.id.title);
            writer = viewHolder.itemView.findViewById(R.id.writer);
            ImageView cover = viewHolder.itemView.findViewById(R.id.cover);

            title.setText(mediaId.getDescription().getTitle());
            writer.setText(mediaId.getDescription().getSubtitle());

            Glide.with(FcUtils.getContext())
                    .asBitmap()
                    .load(mediaId.getDescription().getIconUri())
                    .apply(options)
                    .into(cover);

        }
    }


    /**
     * 媒体控制器控制播放过程中的回调接口，可以用来根据播放状态更新UI
     */
    private class MyMusicCallback implements MediaBrowserCallback {

        @Override
        public void onMediaListLoaded(List<MediaBrowserCompat.MediaItem> list) {
            mAdapter.clearAllData();
            mAdapter.addDatas(list);
            //尾部留白
            mAdapter.addTail("");
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
            updateProgressUI(progress);
        }
    }
}
