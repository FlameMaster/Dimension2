package com.melvinhou.dimension2.test;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.databinding.FragmentTest01Binding;
import com.melvinhou.dimension2.media.video.ijk.IjkVideoView;
import com.melvinhou.kami.adapter.RecyclerAdapter;
import com.melvinhou.kami.adapter.RecyclerHolder;
import com.melvinhou.kami.mvvm.BaseModel;
import com.melvinhou.kami.mvvm.BindFragment;
import com.melvinhou.kami.util.FcUtils;

import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import io.reactivex.disposables.Disposable;

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
public class TestFragment01 extends BindFragment<FragmentTest01Binding, BaseModel> {

    @Override
    protected FragmentTest01Binding openViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentTest01Binding.inflate(getLayoutInflater());
    }

    @Override
    protected Class<BaseModel> openModelClazz() {
        return BaseModel.class;
    }


    @Override
    protected void initView() {

        RecyclerAdapter adapter = new RecyclerAdapter<String, RecyclerHolder>() {
            @Override
            public void bindData(RecyclerHolder viewHolder, int position, String data) {
                NestedPhotoView view = viewHolder.itemView.findViewById(R.id.item_photo);
                String url = "https://i0.hdslb.com/bfs/album/d7cfc41b79f63852b48b5072e4ccb6fc65a81038.jpg";
                Glide.with(FcUtils.getContext()).load(url).into(view);
                view.viewPager2 = mBinding.viewpager;
            }

            @Override
            public int getItemLayoutId(int viewType) {
                return R.layout.item_photo;
            }

            @Override
            protected RecyclerHolder onCreate(View view, int viewType) {
                return new RecyclerHolder(view);
            }
        };
        mBinding.viewpager.setAdapter(adapter);
        for (int i = 0; i < 10; i++)
            adapter.addData("");
        adapter.notifyDataSetChanged();
        mBinding.viewpager.setUserInputEnabled(false);


//        test1();
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {

    }


}
