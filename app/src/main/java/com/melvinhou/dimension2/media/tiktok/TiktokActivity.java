package com.melvinhou.dimension2.media.tiktok;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.melvinhou.dimension2.Comment;
import com.melvinhou.dimension2.Dimension2Application;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.databinding.ActTikTokBD;
import com.melvinhou.kami.mvp.MvpActivity2;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.ResourcesUtils;

import java.math.BigDecimal;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/4/21 20:19
 * <p>
 * = 分 类 说 明：模仿抖音
 * ================================================
 */
public class TiktokActivity extends MvpActivity2<TiktokCotract.Presenter, ActTikTokBD> implements TiktokCotract.View {

    private TikTokAdapter mAdapter;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_tiktok;
    }

    @Override
    protected void initWindowUI() {
//        getWindow().setBackgroundDrawableResource(R.color.black);
        getWindow().getDecorView().setBackgroundColor(Color.BLACK);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.BLACK);
    }

    @Override
    protected TiktokCotract.Presenter upPresenter() {
        ViewModelProvider.Factory factory = ViewModelProvider.AndroidViewModelFactory
                .getInstance(Dimension2Application.getInstance());
        ViewModelProvider viewModelProvider = new ViewModelProvider(this, factory);
        TiktokModel model = viewModelProvider.get(TiktokModel.class);
        return new TikTokPresenter(this, model);
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initListener() {
        getViewDataBinding().list.setLayoutManager(new LinearLayoutManager(
                FcUtils.getContext(), LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean canScrollVertically() {
                return getPresenter().canScrollVertically(super.canScrollVertically());
            }
        });
        new PagerSnapHelper().attachToRecyclerView(getViewDataBinding().list);
        mAdapter = new TikTokAdapter();
        getViewDataBinding().list.setAdapter(mAdapter);
        getViewDataBinding().list.addOnScrollListener(getPresenter().getListScrollListener());
        getViewDataBinding().comments.setOnClickListener(getPresenter()::openCommentDrawer);
        getViewDataBinding().commentRoot.setStateListener(getPresenter()::onChangeUnfoldState);
        getViewDataBinding().show.setOnClickListener(getPresenter()::closeCommentDrawer);
        mAdapter.setOnItemDoubleClickListener(getPresenter().getItemDoubleClickListener());
    }


    @SuppressLint("CheckResult")
    @Override
    protected void initData() {

    }


    @Override
    public TikTokAdapter getListAdapter() {
        return mAdapter;
    }

    @Override
    public void setCurrentData(TiktokEntity entity) {
        getViewDataBinding().setEntity(entity);
        //手动优化
        getViewDataBinding().star.setText(formatNumber(entity.getStarCount()));
        getViewDataBinding().comments.setText(formatNumber(entity.getCommentCount()));
        getViewDataBinding().share.setText(formatNumber(entity.getShareCount()));
        getViewDataBinding().commentCount.setText(
                new StringBuffer("共")
                        .append(formatNumber(entity.getCommentCount()))
                        .append("条评论"));
    }

    /**
     * 矫正数据显示
     *
     * @param number
     */
    private String formatNumber(int number) {
        String str = String.valueOf(number);
        if (number > 9999) {
            float num1 = ((float) number) / 10000f;
            //保留两位小数
            str = new BigDecimal(num1)
                    .setScale(2, BigDecimal.ROUND_HALF_UP)
                    .floatValue()
                    + "w";
        }
        return str;
    }

    @Override
    public void switchItemCanTouchState(boolean value) {
        getViewDataBinding().show.setCanTouch(!value);
    }

    @Override
    public void closeCommentDrawer() {
        getViewDataBinding().commentRoot.close();
    }

    @Override
    public void openCommentDrawer() {
        getViewDataBinding().commentRoot.open();
    }

    @Override
    public void updateStarCount(int starCount) {
        getViewDataBinding().star.setText(formatNumber(starCount));
    }

    @Override
    public void showStarAnimation(float x, float y) {
        getViewDataBinding().show.show(x, y);
    }

    @Override
    public void clearCommentItems() {
        getViewDataBinding().commentsContainer.removeAllViews();
    }

    @Override
    public void jionHigCommentItem(Comment comment) {
        View view = View.inflate(FcUtils.getContext(), R.layout.item_comment_hig, null);
        getViewDataBinding().commentsContainer.addView(view);
        ImageView photoView = view.findViewById(R.id.user_photo);
        TextView titleView = view.findViewById(R.id.user_name);
        TextView contextView = view.findViewById(R.id.context);
        Glide.with(photoView)
                .load(comment.getUser().getPhoto())
                .apply(new RequestOptions()
                        .override(240, 240)
                        .placeholder(R.mipmap.fc)
                        .error(R.mipmap.fc)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(photoView);
        titleView.setText(comment.getUser().getNickName());
        contextView.setText(comment.getContent());
    }

    @Override
    public void jionSubCommentItem(Comment.SubComment comment) {
        View view = View.inflate(FcUtils.getContext(), R.layout.item_comment_sub, null);
        getViewDataBinding().commentsContainer.addView(view);
        ImageView photoView = view.findViewById(R.id.user_photo);
        TextView titleView = view.findViewById(R.id.user_name);
        TextView contextView = view.findViewById(R.id.context);
        Glide.with(photoView)
                .load(comment.getUser().getPhoto())
                .apply(new RequestOptions()
                        .override(120, 120)
                        .placeholder(R.mipmap.fc)
                        .error(R.mipmap.fc)
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(photoView);
        //用户名

        SpannableStringBuilder spbuilder = new SpannableStringBuilder();
        if (comment.getUser().getNickName().equals(getViewDataBinding().getEntity().getUser().getNickName())){
            spbuilder.append("\r作者\r");
            spbuilder.setSpan(new ForegroundColorSpan(Color.WHITE),
                    0, spbuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spbuilder.setSpan(new BackgroundColorSpan(Color.RED),
                    0, spbuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spbuilder.setSpan(new RelativeSizeSpan(0.8f),
                    0, spbuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spbuilder.append("\r\r");
        }
        titleView.setText(spbuilder.append(comment.getUser().getNickName()));

        //评论内容
        SpannableStringBuilder builder = new SpannableStringBuilder();
        if (!TextUtils.isEmpty(comment.getCorrelativeUserName())) {
            int size = 0;
            builder.append("回复").append("\r");
            builder.setSpan(new ForegroundColorSpan(ResourcesUtils.getColor(R.color.gray)),
                    size, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            size = builder.length();
            builder.append("@").append(comment.getCorrelativeUserName());
            builder.setSpan(new URLSpan("https://www.baidu.com/"),
                    size, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(":\r\r");
        }
        contextView.setText(builder.append(comment.getContent()));
    }

    @Override
    public void back() {
        getPresenter().back();
    }
}
