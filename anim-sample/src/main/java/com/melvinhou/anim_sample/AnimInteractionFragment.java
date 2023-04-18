package com.melvinhou.anim_sample;

import android.animation.Animator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;

import com.melvinhou.anim_sample.databinding.FragmentAnimInactBinding;
import com.melvinhou.kami.mvvm.BaseViewModel;
import com.melvinhou.kami.mvvm.BindFragment;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/4/14 0014 18:03
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class AnimInteractionFragment extends BindFragment<FragmentAnimInactBinding, BaseViewModel> {
    @Override
    protected FragmentAnimInactBinding openViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentAnimInactBinding.inflate(getLayoutInflater());
    }

    @Override
    protected Class<BaseViewModel> openModelClazz() {
        return BaseViewModel.class;
    }

    @Override
    protected void initView() {
        if (mModel.page.getValue() == R.id.nav_anim_interaction01) {
            mBinding.getRoot().setVisibility(View.INVISIBLE);
            startAnim();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void startAnim() {
        mBinding.getRoot().post(() -> {
            mBinding.getRoot().setVisibility(View.VISIBLE);
            int width = mBinding.getRoot().getWidth();
            int height = mBinding.getRoot().getHeight();
            //斜边
            double viewHypotenuse = Math.hypot(width, height);
            Animator anim = ViewAnimationUtils.createCircularReveal(
                    mBinding.getRoot(), 0, 0, 0f, (float) viewHypotenuse);
            anim.setDuration(1000);
            anim.start();
        });
    }
}
