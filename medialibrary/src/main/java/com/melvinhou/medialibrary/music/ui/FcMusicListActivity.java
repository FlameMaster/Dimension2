package com.melvinhou.medialibrary.music.ui;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.melvinhou.kami.adapter.RecyclerAdapter;
import com.melvinhou.kami.adapter.RecyclerHolder;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.ImageUtils;
import com.melvinhou.kami.view.activities.BaseActivity;
import com.melvinhou.medialibrary.R;
import com.melvinhou.medialibrary.music.component.FcMusicConnection;

import java.util.List;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ItemDecoration;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/3/13 0013 17:09
 * <p>
 * = 分 类 说 明：音乐列表
 * ================================================
 */
public class FcMusicListActivity extends BaseActivity {

    private static final String TAG = FcMusicListActivity.class.getSimpleName();
    /**
     * 从Android 10 开始，应用即使申请了权限，也只能读写自己外部存储的私有目录，
     * 就是Android/data/对应应用包名下的相关目目录。
     * 除此之外任何目录的读写都会被拒绝，并提示android Permission denied。
     * 需要在AndroidManifest.xml 文件中，在application标签中添加如下属性 android:requestLegacyExternalStorage="true"
     */
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final String[] REQUIRED_PERMISSIONS_33 = {Manifest.permission.READ_MEDIA_AUDIO};

    private ImageView mCoverView, mPlayerCoverView;
    private TextView mPlayerName, mPlayerSub;
    private View mPlayButton;
    private CollapsingToolbarLayout mBarLayout;
    private RecyclerView mContainer;
    private MyAdapter mAdapter;
    private boolean isPlaying = false;
    //播放动画
    private ObjectAnimator mPlayAnimator;
    //状态栏背景颜色，true为白（设置图标为黑
    private MutableLiveData<Boolean> isDarkStatusBar = new MutableLiveData<>(true);

    private FcMusicConnection provideMusicServiceConnection() {
        return FcMusicConnection.getInstance(this);
    }

    @Override
    protected int getLayoutID() {
        return R.layout.activity_music_fc_list;
    }

    @Override
    protected void initWindowUI() {
        super.initWindowUI();
        WindowInsetsControllerCompat controllerCompat =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView().getRootView());
        isDarkStatusBar.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isDark) {
                controllerCompat.setAppearanceLightStatusBars(isDark);
            }
        });
    }

    @Override
    protected void initView() {
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        mBarLayout = findViewById(R.id.bar_layout);
        mCoverView = findViewById(R.id.iv_cover);
        mPlayerCoverView = findViewById(R.id.player_cover);
        mPlayerName = findViewById(R.id.player_title);
        mPlayerSub = findViewById(R.id.player_artist);
        mPlayButton = findViewById(R.id.player_play);

        //
        mContainer = findViewById(R.id.container);
        mContainer.setLayoutManager(new GridLayoutManager(getBaseContext(), 3));
        int decoration = DimenUtils.dp2px(10);
        mContainer.addItemDecoration(new ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int position = parent.getChildAdapterPosition(view);
                outRect.set(position % 3 == 0 ? decoration : 0, position < 3 ? decoration : 0, decoration, decoration);
            }
        });
        mAdapter = new MyAdapter();
        mContainer.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isPlaying) provideMusicServiceConnection().transportControls.pause();
        String mediaId = provideMusicServiceConnection().rootMediaId;
        if (!TextUtils.isEmpty(mediaId))
            provideMusicServiceConnection().unsubscribe(mediaId, null);
        FcMusicConnection.disconnect();
    }

    @Override
    protected void initListener() {
        mAdapter.setOnItemClickListener(new RecyclerAdapter.OnItemClickListener<MediaSessionCompat.QueueItem, MyHolder>() {
            @Override
            public void onItemClick(MyHolder viewHolder, int position, MediaSessionCompat.QueueItem data) {
                provideMusicServiceConnection().transportControls
                        .skipToQueueItem(data.getQueueId());
//                        .playFromMediaId(data.getDescription().getMediaId(),null);
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

        findViewById(R.id.ll_tool_bottom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDetail();
            }
        });
        mPlayerCoverView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDetail();
            }
        });
    }

    @Override
    protected void onPermissionGranted(int requestCode) {
        super.onPermissionGranted(requestCode);
        //权限申请成功
        initData();
    }

    @Override
    protected void initData() {
        String[] permissions = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? REQUIRED_PERMISSIONS_33 : REQUIRED_PERMISSIONS;
        // 请求音乐播放权限
        if (!checkPermission(permissions)) {
            requestPermissions(permissions);
            return;
        }
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
                mPlayerName.setText(title);
                mPlayerSub.setText(artist);
                mBarLayout.setTitle(title);
                if (displayIcon != null)
                    mPlayerCoverView.setImageBitmap(displayIcon);
                if (albumArt != null) {
//                    mBackgroundView.setImageURI(Uri.parse(backgroundUri));
                    mCoverView.setImageBitmap(ImageUtils.doBlur(albumArt, 25, false));
                    //通过封面颜色判断状态栏
//                    ImageUtils.getBitmapColor(albumArt, new Palette.PaletteAsyncListener() {
//                        @Override
//                        public void onGenerated(@Nullable Palette palette) {
//                            int color = palette.getDominantColor(Color.WHITE);
//                            isDarkStatusBar.postValue(ImageUtils.isLightColor(color));
//                        }
//                    });
                }
                //动画
                //10s一圈
                float num = duration / 10000f;
                if (mPlayAnimator != null) {
                    mPlayAnimator.cancel();
                    mPlayerCoverView.setRotation(0);
                }
                mPlayAnimator = ObjectAnimator
                        .ofFloat(mPlayerCoverView, View.ROTATION, 0, num * 360)
                        .setDuration(duration);
                mPlayAnimator.setInterpolator(new LinearInterpolator());
            }
        });
        provideMusicServiceConnection().playbackState.observe(this, new Observer<PlaybackStateCompat>() {
            @Override
            public void onChanged(PlaybackStateCompat playbackStateCompat) {
                int pbState = playbackStateCompat != null ? playbackStateCompat.getState() : PlaybackStateCompat.STATE_NONE;
                isPlaying = pbState == PlaybackStateCompat.STATE_PLAYING;
                mPlayButton.setSelected(isPlaying);
                //动画
                if (mPlayAnimator != null) {
                    if (isPlaying) {
                        if (mPlayAnimator.isPaused())
                            mPlayAnimator.resume();
                        else
                            mPlayAnimator.start();
                    } else mPlayAnimator.pause();
                }
            }
        });
        provideMusicServiceConnection().isConnected.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isConnected) {
                if (isConnected) buildTransportControls();
            }
        });
        provideMusicServiceConnection().nowPlayQueue.observe(this, new Observer<List<MediaSessionCompat.QueueItem>>() {
            @Override
            public void onChanged(List<MediaSessionCompat.QueueItem> queueItems) {
                mAdapter.clearData();
                mAdapter.addDatas(queueItems);
            }
        });
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
                //设置队列类型
                provideMusicServiceConnection().transportControls.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL);

            }
        });
    }

    //打开详情
    private void openDetail() {
        MediaMetadataCompat compat = provideMusicServiceConnection().nowPlaying.getValue();
        if (compat != null) {
            Intent intent = new Intent(this, FcMusicActivity.class);
            intent.putExtra("id", compat.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
            toActivity(mPlayerCoverView, intent);
        }
    }

    //懒得继承
    public void toActivity(View view, Intent intent) {
        Pair<View, String> p = new Pair<>(view, view.getTransitionName());
        ActivityOptions activityOptions =
                ActivityOptions.makeSceneTransitionAnimation(this, p);
        startActivity(intent, activityOptions.toBundle());
    }


    class MyAdapter extends RecyclerAdapter<MediaSessionCompat.QueueItem, MyHolder> {

        @Override
        public void bindData(MyHolder viewHolder, int position, MediaSessionCompat.QueueItem data) {
            MediaDescriptionCompat item = data.getDescription();
            viewHolder.update(item);
        }

        @Override
        public int getItemLayoutId(int viewType) {
            return R.layout.item_music_fc;
        }

        @Override
        protected MyHolder onCreate(View view, int viewType) {
            return new MyHolder(view);
        }
    }

    class MyHolder extends RecyclerHolder {
        private ImageView iCoverView;
        private TextView iPlayerName, iPlayerSub;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            iCoverView = itemView.findViewById(R.id.iv_cover);
            iPlayerName = itemView.findViewById(R.id.tv_title);
            iPlayerSub = itemView.findViewById(R.id.tv_artist);
        }

        private void update(MediaDescriptionCompat data) {
            iCoverView.setImageBitmap(data.getIconBitmap());
            iPlayerName.setText(data.getTitle());
            iPlayerSub.setText(data.getSubtitle());
        }


    }
}
