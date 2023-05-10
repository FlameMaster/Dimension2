package com.melvinhou.anim_sample;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;

import com.melvinhou.anim_sample.databinding.FragmentAnimInact02Binding;
import com.melvinhou.kami.bean.PageInfo;
import com.melvinhou.kami.mvvm.BindFragment;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.knight.NavigaionFragmentModel;

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
public class AnimInteractionFragment02 extends BindFragment<FragmentAnimInact02Binding, NavigaionFragmentModel> {
    @Override
    protected FragmentAnimInact02Binding openViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentAnimInact02Binding.inflate(getLayoutInflater());
    }

    @Override
    protected Class<NavigaionFragmentModel> openModelClazz() {
        return NavigaionFragmentModel.class;
    }

    @Override
    protected void initView() {
        mBinding.barRoot.title.setText("交互动画21");
    }

    @Override
    protected void initListener() {
        addButton(mBinding.btInputs, "普通", view -> {
            mModel.toFragment(R.id.action_anim_interaction_02201);
        });
        addButton(mBinding.btInputs, "揭露动画", view -> {
            mModel.toFragment(R.id.nav_anim_interaction01);
        });
    }

    /**
     * 添加一个按钮
     *
     * @param inputs
     * @param text
     * @param clickListener
     */
    private void addButton(GridLayout inputs, String text, View.OnClickListener clickListener) {
        Button button = new Button(FcUtils.getContext());
        button.setText(text);
        button.setTextColor(Color.BLACK);
        button.setClickable(true);
        //触摸涟漪动画
        button.setBackgroundResource(R.drawable.button_down_ripple);
        button.setOnClickListener(clickListener);
        inputs.addView(button);
    }
}
