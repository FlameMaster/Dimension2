package com.melvinhou.dimension2.media;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.databinding.ItemMediaBD;
import com.melvinhou.kami.adapter.DataBindingHolder;
import com.melvinhou.kami.adapter.DataBindingRecyclerAdapter;
import com.melvinhou.kami.adapter.RecyclerAdapter2;
import com.melvinhou.kami.adapter.RecyclerHolder;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.ResourcesUtils;
import com.melvinhou.kami.util.StringCompareUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/7/2 20:23
 * <p>
 * = 分 类 说 明：多媒体相关
 * ================================================
 */
class MediaListAdapter extends DataBindingRecyclerAdapter<MediaItemEntity, MediaListAdapter.MediaListHolder> {

    /*条目点击事件*/
    private OnTabClickListener mListener;

    /*设置条目点击事件*/
    public void setOnTabClickListener(OnTabClickListener li) {
        mListener = li;
    }

    @Override
    public void bindData(MediaListHolder viewHolder, int position, MediaItemEntity data) {
        viewHolder.updata(data);
    }

    @Override
    public int getItemLayoutId(int viewType) {
        return R.layout.item_media;
    }

    @Override
    protected MediaListHolder onCreate(ViewDataBinding binding, int viewType) {
        return new MediaListHolder((ItemMediaBD) binding);
    }

    /**
     * 一级条目
     */
    class MediaListHolder extends DataBindingHolder<ItemMediaBD> {

        private MediaTabAdapter adapter;

        public MediaListHolder(ItemMediaBD binding) {
            super(binding);
            adapter = new MediaTabAdapter();
            binding.tabs.setLayoutManager(new LinearLayoutManager(FcUtils.getContext(), RecyclerView.HORIZONTAL, false));
            binding.tabs.setAdapter(adapter);
            adapter.addTail("");
        }

        public void updata(MediaItemEntity data) {
            //更新一级类型
            adapter.setLevel1Typ(data.getType());
            //数据更新
            adapter.clearDatas();
            adapter.addDatas(data.getTabs());
            //封面
            getBinding().setCoverUrl(data.getCoverUrl());
            //标题
            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(data.getTitle()).append("\n");
            int length = builder.length();
            builder.append(data.getSubTitle());
            builder.setSpan(new AbsoluteSizeSpan(DimenUtils.sp2px(12)), length, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new ForegroundColorSpan(Color.parseColor("#dddddd")),
                    length, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            getBinding().title.setText(builder);
        }
    }

    /**
     * 二级列表
     */
    class MediaTabAdapter extends RecyclerAdapter2<MediaItemEntity.MediaTabEntity, RecyclerHolder> {

        private int level1Typ;
        private int viewSize, margin, drawablePadding;
        RequestOptions options;

        MediaTabAdapter() {
            level1Typ = 0;
            viewSize = DimenUtils.dp2px(104);
            margin = DimenUtils.dp2px(8);
            drawablePadding = DimenUtils.dp2px(8);
            options = new RequestOptions()
                    .override(viewSize / 2, viewSize / 2)
                    .placeholder(R.mipmap.ic_launcher_round)
                    .error(R.mipmap.ic_launcher_round)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);
        }

        public void setLevel1Typ(int level1Typ) {
            this.level1Typ = level1Typ;
        }

        @NonNull
        @Override
        public RecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_TAIL) {
                View view = new View(FcUtils.getContext());
                RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(margin, viewSize);
                view.setLayoutParams(lp);
                return new RecyclerHolder(view);
            }
            TextView view = new TextView(FcUtils.getContext());
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(viewSize, viewSize);
            lp.leftMargin = margin;
            lp.topMargin = margin;
            lp.bottomMargin = margin;
            view.setLayoutParams(lp);
            view.setBackgroundColor(0x20000000);
            view.setTextSize(12);
            view.setTextColor(Color.BLACK);
            view.setGravity(Gravity.CENTER_HORIZONTAL);
            view.setIncludeFontPadding(false);
            view.setCompoundDrawablePadding(drawablePadding);
            return new RecyclerHolder(view);
        }

        @Override
        public void bindData(RecyclerHolder viewHolder, int position, MediaItemEntity.MediaTabEntity data) {
            int level2Typ = data.getType();
            //点击
            viewHolder.itemView.setOnClickListener(view1 -> {
                if (mListener != null) mListener.onTabItemClick(level1Typ, level2Typ);
            });
            ((TextView) viewHolder.itemView).setText(data.getTitle());
            //加载图片
            if (StringCompareUtils.noNull(data.getIconUrl()) && StringCompareUtils.isImageUrl(data.getIconUrl()))
                Glide.with(FcUtils.getContext())
                        .load(data.getIconUrl())
                        .apply(options)
                        .into(new ViewTarget<View, Drawable>(viewHolder.itemView) {//new SimpleTarget<Drawable>()
                            @Override
                            public void onResourceReady(@NonNull Drawable drawable, @Nullable Transition transition) {
                                upIcon((TextView) viewHolder.itemView, drawable);
                            }
                        });
            else
                upIcon((TextView) viewHolder.itemView, ResourcesUtils.getDrawable(R.mipmap.fc));
        }

        /**
         * 更新icon
         *
         * @param view
         * @param drawable
         */
        private void upIcon(TextView view, Drawable drawable) {
            int size = viewSize - getTextHeight(DimenUtils.sp2px(12)) - drawablePadding;
            int padding = size - viewSize / 2;
            padding /= 2;
            size -= padding;
            drawable.setBounds(0, 0, size, size);//第一次设置大小（用于控制文字位置
            view.setCompoundDrawables(null, drawable, null, null);
            size -= padding;
            //第二次是改变图片位置
            drawable.setBounds(
                    padding / 2,
                    padding,
                    padding / 2 + size,
                    padding + size);
        }

        /**
         * 精确获取控件的高度
         *
         * @param fontSize 传入画笔大小（px
         * @return
         */
        public int getTextHeight(float fontSize) {
            Paint paint = new Paint();
            paint.setTextSize(fontSize);
            Paint.FontMetrics fm = paint.getFontMetrics();
            return (int) Math.ceil(fm.bottom - fm.top);
        }

        @Override
        public int getItemLayoutId(int viewType) {
            return 0;
        }

        @Override
        protected RecyclerHolder onCreate(View View, int viewType) {
            return null;
        }
    }


    /*条目点击事件接口*/
    public interface OnTabClickListener {
        void onTabItemClick(int level1Typ, int level2Typ);
    }
}
