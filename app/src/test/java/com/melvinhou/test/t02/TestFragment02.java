package com.melvinhou.test.t02;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.databinding.ActivityListBinding;
import com.melvinhou.kami.adapter.RecyclerAdapter;
import com.melvinhou.kami.adapter.RecyclerHolder;
import com.melvinhou.kami.mvvm.BaseViewModel;
import com.melvinhou.kami.mvvm.BindFragment;
import com.melvinhou.kami.util.FcUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/7/16 2:39
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class TestFragment02 extends BindFragment<ActivityListBinding, BaseViewModel> {

    @Override
    protected ActivityListBinding openViewBinding(LayoutInflater inflater, ViewGroup container) {
        return ActivityListBinding.inflate(getLayoutInflater());
    }

    @Override
    protected Class<BaseViewModel> openModelClazz() {
        return BaseViewModel.class;
    }

    int verticalScrolloffset = 0;

    @Override
    protected void initView() {
        mBinding.barRoot.title.setText("流星RecyclerView");

//        CardLayoutManager mCardLayoutManager = new CardLayoutManager(3, 0.5f);
//        listView.setLayoutManager(mCardLayoutManager);
//        listView.setLayoutManager(new ScrollSpeedLinearLayoutManger(this,
//                LinearLayoutManager.HORIZONTAL, false));
//        new PagerSnapHelper().attachToRecyclerView(listView);
        MeteorShowerManager meteorShowerManager = new MeteorShowerManager();
        mBinding.container.setLayoutManager(meteorShowerManager);
        RecyclerView.Adapter<RecyclerView.ViewHolder> adapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView =
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_test02, parent, false);
                return new RecyclerHolder(itemView) {
                };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                final int index = position;
//                ImageView view = holder.itemView.findViewById(R.id.image);
                TextView textView = holder.itemView.findViewById(R.id.title);
                textView.setText(String.valueOf(position));
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FcUtils.showToast("点击了"+index);
                    }
                });
            }

            @Override
            public int getItemCount() {
                return Integer.MAX_VALUE;
            }
        };
        mBinding.container.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        ValueAnimator animator = ValueAnimator.ofInt(
                meteorShowerManager.ITEM_SCROLL_HEIGHT * meteorShowerManager.MAX_COUNT);
        animator.setDuration(10*1000);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                int offset = value - verticalScrolloffset;
                verticalScrolloffset = value;
//                    listView.smoothScrollBy(0, offset);
                mBinding.container.scrollBy(0, offset);

            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                verticalScrolloffset = 0;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animator.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {

    }


}
