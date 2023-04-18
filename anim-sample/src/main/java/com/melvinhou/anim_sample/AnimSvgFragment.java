package com.melvinhou.anim_sample;

import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;

import com.melvinhou.anim_sample.databinding.FragmentAnimSysBinding;
import com.melvinhou.kami.mvvm.BaseViewModel;
import com.melvinhou.kami.mvvm.BindFragment;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.FcUtils;

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
public class AnimSvgFragment extends BindFragment<FragmentAnimSysBinding, BaseViewModel> {
    @Override
    protected FragmentAnimSysBinding openViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentAnimSysBinding.inflate(getLayoutInflater());
    }

    @Override
    protected Class<BaseViewModel> openModelClazz() {
        return BaseViewModel.class;
    }

    public static AnimSvgFragment instance(int index) {
        AnimSvgFragment fragment = new AnimSvgFragment();
//            fragment.status = when (index) {
//                else -> 0
//            }
        fragment.status = index;
        return fragment;
    }

    private int status;

    @Override
    protected void initView() {
        switch (status) {
            case 0:
                initTextAnimation(mBinding.llContainer, mBinding.btInputs);
                break;
            case 1:
                initColorAnimation(mBinding.llContainer, mBinding.btInputs);
                break;
            case 2:
                initPathAnimation(mBinding.llContainer, mBinding.btInputs);
                break;
        }
    }

    //变化
    private void initTextAnimation(LinearLayout show, GridLayout inputs) {
        show.setBackgroundColor(Color.LTGRAY);
        View child = new View(FcUtils.getContext());
        child.setLayoutParams(new ViewGroup.LayoutParams(DimenUtils.dp2px(240), DimenUtils.dp2px(240)));
        child.setBackgroundResource(R.drawable.av_svg_dome_text);
        show.addView(child);
        addButton(inputs, "开始", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable avDrawable = child.getBackground();
                if (avDrawable instanceof Animatable){
                    ((Animatable) avDrawable).start();
                }
            }
        });

    }

    //路径
    private void initPathAnimation(LinearLayout show, GridLayout inputs) {
        show.setBackgroundColor(Color.LTGRAY);
        View child = new View(FcUtils.getContext());
        child.setLayoutParams(new ViewGroup.LayoutParams(DimenUtils.dp2px(240), DimenUtils.dp2px(240)));
        child.setBackgroundResource(R.drawable.av_svg_dome_path);
        show.addView(child);
        addButton(inputs, "开始", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable avDrawable = child.getBackground();
                if (avDrawable instanceof Animatable){
                    ((Animatable) avDrawable).start();
                }
            }
        });

    }

    //颜色
    private void initColorAnimation(LinearLayout show, GridLayout inputs) {
        show.setBackgroundColor(Color.LTGRAY);
        View child = new View(FcUtils.getContext());
        child.setLayoutParams(new ViewGroup.LayoutParams(DimenUtils.dp2px(240), DimenUtils.dp2px(240)));
        child.setBackgroundResource(R.drawable.av_svg_dome_color);
        show.addView(child);
        addButton(inputs, "开始", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable avDrawable = child.getBackground();
                if (avDrawable instanceof Animatable){
                    ((Animatable) avDrawable).start();
                }
            }
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
        button.setBackgroundResource(R.drawable.button_down_ripple);
        button.setOnClickListener(clickListener);
        inputs.addView(button);
    }

}
