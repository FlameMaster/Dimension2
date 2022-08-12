package com.melvinhou.dimension2.media.picture;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.melvinhou.dimension2.net.AssetsFileKey;
import com.melvinhou.dimension2.utils.LoadUtils;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.databinding.ActAlbumBD;
import com.melvinhou.kami.adapter.RecyclerAdapter2;
import com.melvinhou.kami.adapter.RecyclerHolder;
import com.melvinhou.kami.mvvm.DataBindingActivity;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.IOUtils;
import com.melvinhou.kami.util.ImageUtils;
import com.melvinhou.kami.util.ResourcesUtils;
import com.melvinhou.kami.wiget.PhotoCutterView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/7/16 19:42
 * <p>
 * = 分 类 说 明：相册
 * ================================================
 */
public class AlbumActivity extends DataBindingActivity<ActAlbumBD> {

    private AlbumAdapter mAdapter;
    private Animator currentAnimator;
    //动画时间
    private int shortAnimationDuration;
    //当前打开的图片
    private View nowThumbView;
    //放大前的倍率和矩阵
    private Rect startBoundsFinal;
    private float startScaleFinal;


    @Override
    protected int getLayoutID() {
        return R.layout.activity_album;
    }

    @Override
    protected void initWindowUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        int statusColor = 0x40000000;
        getWindow().setStatusBarColor(statusColor);
        getWindow().setNavigationBarColor(statusColor);
    }

    @Override
    protected void initToolBar() {
        super.initToolBar();
        //显示自带title
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        getSupportActionBar().setTitle("相册");
    }

    @Override
    protected void initView() {
        getViewDataBinding().expandedImage.setGestureMode(PhotoCutterView.GESTURE_MODE_NORM);

        int padding = DimenUtils.dp2px(4);
        getViewDataBinding().list.setPadding(padding, 0, padding, 0);
        getViewDataBinding().list.setLayoutManager(new GridLayoutManager(FcUtils.getContext(), 3));
        shortAnimationDuration = ResourcesUtils.getResources().getInteger(
                android.R.integer.config_shortAnimTime);
    }

    @Override
    protected void initListener() {
        mAdapter = new AlbumAdapter(3);
        getViewDataBinding().list.setAdapter(mAdapter);
        getViewDataBinding().expandedImage.setOnClickListener(view -> zoomImageFromExpanded());

    }

    @Override
    protected void initData() {
        mAdapter.addHead("");
        mAdapter.addTail("");
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onLoading() {
        Observable.create((ObservableOnSubscribe<String[]>) emitter -> {
            String text = LoadUtils.readAssetsTxt(AssetsFileKey.MEDIAT_ALBUM_LIST);
            text = text
                    .replaceAll(" ", "")
                    .replaceAll("\r|\n", "");
            String[] datas = text.split(",");
            emitter.onNext(datas);
            emitter.onComplete();
        })
                .compose(IOUtils.setThread())
                .subscribe(datas -> {
                    if (datas != null) {
                        mAdapter.clearDatas();
                        for (String data : datas)
                            mAdapter.addData(data);
                    }
                });
    }

    @Override
    public void back() {
        if (getViewDataBinding().expandedImage.getVisibility() == View.VISIBLE) {
            zoomImageFromExpanded();
            return;
        }
        super.back();
    }

    /**
     * 放大照片的过渡动画
     *
     * @param thumbView
     * @param url
     */
    private void zoomImageFromThumb(final View thumbView, String url) {
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }
        Glide.with(FcUtils.getContext())
                .load(url)
                .into(getViewDataBinding().expandedImage);
        Rect startBounds = new Rect();
        Rect finalBounds = new Rect();
        Point globalOffset = new Point();

        thumbView.getGlobalVisibleRect(startBounds);
        getViewDataBinding().getRoot().getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        thumbView.setAlpha(0f);
        getViewDataBinding().expandedImage.setVisibility(View.VISIBLE);
        getViewDataBinding().expandedImage.setPivotX(0f);
        getViewDataBinding().expandedImage.setPivotY(0f);

        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(getViewDataBinding().expandedImage, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(getViewDataBinding().expandedImage, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(getViewDataBinding().expandedImage, View.SCALE_X,
                        startScale, 1f))
                .with(ObjectAnimator.ofFloat(getViewDataBinding().expandedImage,
                        View.SCALE_Y, startScale, 1f))
                .with(ObjectAnimator.ofArgb(getViewDataBinding().expandedBackground,
                        "backgroundColor", Color.TRANSPARENT, Color.BLACK));
        set.setDuration(shortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                currentAnimator = null;
            }
        });
        set.start();
        currentAnimator = set;
        nowThumbView = thumbView;
        startBoundsFinal = startBounds;
        startScaleFinal = startScale;
    }

    //todo 图片放大缩小的倍数和位置位算上

    /**
     * 缩小照片的过渡动画
     */
    private void zoomImageFromExpanded() {
        if (nowThumbView == null) return;

        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator
                .ofFloat(getViewDataBinding().expandedImage, View.X, startBoundsFinal.left))
                .with(ObjectAnimator
                        .ofFloat(getViewDataBinding().expandedImage,
                                View.Y, startBoundsFinal.top))
                .with(ObjectAnimator
                        .ofFloat(getViewDataBinding().expandedImage,
                                View.SCALE_X, startScaleFinal))
                .with(ObjectAnimator
                        .ofFloat(getViewDataBinding().expandedImage,
                                View.SCALE_Y, startScaleFinal));
        set.setDuration(shortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                onRestoreAnimationEnd();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onRestoreAnimationEnd();
            }
        });
        getViewDataBinding().expandedBackground.setBackgroundColor(Color.TRANSPARENT);
        getViewDataBinding().expandedImage.restoreSize();//复位
        set.start();
        currentAnimator = set;
    }

    /**
     * 缩小照片之后
     */
    private void onRestoreAnimationEnd() {
        nowThumbView.setAlpha(1f);
        getViewDataBinding().expandedImage.setVisibility(View.GONE);
        currentAnimator = null;
        try {
            getViewDataBinding().expandedImage.setNullBitmap();
            ImageUtils.releaseResouce(getViewDataBinding().expandedImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    class AlbumAdapter extends RecyclerAdapter2<String, RecyclerHolder> {

        private int viewSize, margin;
        RequestOptions options;

        AlbumAdapter(int spanCount) {
            margin = DimenUtils.dp2px(4);
            viewSize = DimenUtils.getScreenSize()[0] - (spanCount * 2 + 2) * margin;
            viewSize /= spanCount;
            options = new RequestOptions()
                    .override(viewSize / 2, viewSize / 2)
                    .placeholder(R.mipmap.ic_launcher_round)
                    .error(R.mipmap.ic_launcher_round)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);
        }

        @Override
        public int getItemLayoutId(int viewType) {
            return 0;
        }

        @Override
        protected RecyclerHolder onCreate(View View, int viewType) {
            return null;
        }

        @NonNull
        @Override
        public RecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_TAIL || viewType == TYPE_HEAD) {
                View view = new View(FcUtils.getContext());
                RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(margin, viewSize);
                view.setLayoutParams(lp);
                return new RecyclerHolder(view);
            }
            ImageView view = new ImageView(FcUtils.getContext());
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(viewSize, viewSize);
            lp.leftMargin = margin;
            lp.topMargin = margin;
            lp.bottomMargin = margin;
            view.setLayoutParams(lp);
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            view.setClickable(true);
            view.setBackgroundResource(R.drawable.ripple_down);
            return new RecyclerHolder(view);
        }

        @Override
        public void bindData(RecyclerHolder viewHolder, int position, String data) {
            Glide.with(FcUtils.getContext())
                    .load(data)
                    .apply(options)
                    .into((ImageView) viewHolder.itemView);

            viewHolder.itemView.setOnClickListener(view -> zoomImageFromThumb(viewHolder.itemView, data));
        }
    }
}
