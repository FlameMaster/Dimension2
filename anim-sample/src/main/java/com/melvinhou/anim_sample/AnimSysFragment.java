package com.melvinhou.anim_sample;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.melvinhou.anim_sample.databinding.FragmentAnimSysBinding;
import com.melvinhou.kami.mvvm.BaseViewModel;
import com.melvinhou.kami.mvvm.BindFragment;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.ResourcesUtils;

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
public class AnimSysFragment extends BindFragment<FragmentAnimSysBinding, BaseViewModel> {
    @Override
    protected FragmentAnimSysBinding openViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentAnimSysBinding.inflate(getLayoutInflater());
    }

    @Override
    protected Class<BaseViewModel> openModelClazz() {
        return BaseViewModel.class;
    }

    public static AnimSysFragment instance(int index) {
        AnimSysFragment fragment = new AnimSysFragment();
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
                initFrameAnimation(mBinding.llContainer, mBinding.btInputs);
                break;
            case 1:
                initPropertyAnimationJava(mBinding.llContainer, mBinding.btInputs);
                break;
            case 2:
                initPropertyAnimationRes(mBinding.llContainer, mBinding.btInputs);
                break;
            case 3:
                initTweenAnimationJava(mBinding.llContainer, mBinding.btInputs);
                break;
            case 4:
                initTweenAnimationRes(mBinding.llContainer, mBinding.btInputs);
                break;
        }
    }


    //补间动画-资源文件
    private void initTweenAnimationRes(LinearLayout show, GridLayout inputs) {
        show.setBackgroundColor(Color.LTGRAY);
        TextView child = new TextView(FcUtils.getContext());
        child.setTextColor(Color.WHITE);
        child.setTextSize(14);
        child.setGravity(Gravity.CENTER);
        child.setLayoutParams(new ViewGroup.LayoutParams(DimenUtils.dp2px(42), DimenUtils.dp2px(42)));
        child.setBackgroundColor(Color.GREEN);
        show.addView(child);
        addButton(inputs, "位移", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(
                        FcUtils.getContext(), R.anim.translate_system_tween);
                child.startAnimation(animation);
            }
        });
        addButton(inputs, "综合", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(
                        FcUtils.getContext(), R.anim.dome_system_tween);
                child.startAnimation(animation);
            }
        });
    }

    //补间动画-代码
    private void initTweenAnimationJava(LinearLayout show, GridLayout inputs) {
        show.setBackgroundResource(R.mipmap.default_cover);
        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.2f);
        animation.setDuration(500);
        animation.setStartOffset(500);
        animation.setFillAfter(false);
        animation.setRepeatCount(1);
        animation.setRepeatMode(Animation.RESTART);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        addButton(inputs, "开始", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show.startAnimation(animation);
            }
        });
    }

    //属性动画-资源文件
    private void initPropertyAnimationRes(LinearLayout show, GridLayout inputs) {
//        show.setBackgroundColor(Color.LTGRAY);
        show.setForeground(ResourcesUtils.getDrawable(R.mipmap.default_cover));
        addButton(inputs, "开始", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(FcUtils.getContext(),
                        R.animator.dome_system_property);
                set.setTarget(show);
                set.start();
            }
        });
    }

    //属性动画-代码
    private void initPropertyAnimationJava(LinearLayout show, GridLayout inputs) {
//        ObjectAnimator/ValueAnimator/AnimatorSet
        show.setBackgroundColor(Color.LTGRAY);
        TextView child = new TextView(FcUtils.getContext());
        child.setTextColor(Color.WHITE);
        child.setTextSize(14);
        child.setGravity(Gravity.CENTER);
        child.setLayoutParams(new ViewGroup.LayoutParams(DimenUtils.dp2px(42), DimenUtils.dp2px(42)));
        child.setBackgroundColor(Color.GREEN);
        show.addView(child);
        //
        addButton(inputs, "平移", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator animator
//                        = ObjectAnimator.ofFloat(child, View.X, 250,0,1000,0);//child的x坐标（绝对位置
                        = ObjectAnimator.ofFloat(child, View.TRANSLATION_X, 0, 500, -500, 0);//相对位置
                animator.setDuration(2000);
//                animator.setRepeatCount(ValueAnimator.INFINITE);//无限循环,重复次数，播放次数 = 重复次数 + 1
//                animator.setRepeatMode(ValueAnimator.RESTART);//重复模式，RESTART：重新开始； REVERSE：反转回来
//                animator.setStartDelay(1000);//延时执行
//                animator.setInterpolator(new LinearInterpolator());
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }
                });
                animator.setInterpolator(new DecelerateInterpolator());
                animator.start();
            }
        });
        addButton(inputs, "透明度", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator animator
                        = ObjectAnimator.ofFloat(child, View.ALPHA, 1, 0, 1, 0.5f, 1);
                animator.setDuration(2000);
                animator.start();
//                child.animate().alphaBy(1f).alpha(0.5f).setDuration(2000).start();
            }
        });
        addButton(inputs, "缩放", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator animator
                        = ObjectAnimator.ofFloat(child, View.SCALE_X, 1, 2, 3, 0.5f, 1);
                animator.setDuration(2000);
                animator.start();
            }
        });
        addButton(inputs, "旋转", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator animator
                        = ObjectAnimator.ofFloat(child, View.ROTATION_X, 0, 180, 0, 360);
                animator.setDuration(2000);
                animator.start();
            }
        });
        addButton(inputs, "综合1", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                                .ofFloat(child, View.TRANSLATION_X, 0, -300, 300, 300, -300, 0))
                        .with(ObjectAnimator
                                .ofFloat(child,
                                        View.TRANSLATION_Y, 0, 300, 300, -300, -300, 0))
                        .with(ObjectAnimator
                                .ofFloat(child,
                                        View.SCALE_X, 1, 0.5f, 2, 2, 0.5f, 1))
                        .with(ObjectAnimator
                                .ofFloat(child,
                                        View.SCALE_Y, 1, 2, 2, 0.5f, 0.5f, 1));
                set.setDuration(4000);
                set.start();
            }
        });
        addButton(inputs, "综合2", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnimatorSet set = new AnimatorSet();
                ObjectAnimator animator1 = ObjectAnimator
                        .ofFloat(child, View.TRANSLATION_X, 0, -300, 300, 300, -300, 0);
                animator1.setDuration(8000);
                ObjectAnimator animator2 = ObjectAnimator
                        .ofFloat(child, View.TRANSLATION_Y, 0, 300, 300, -300, -300, 0);
                animator2.setDuration(8000);
                ObjectAnimator animator3 = ObjectAnimator
                        .ofFloat(child, View.SCALE_X, 1, 0.5f, 1);
                animator3.setDuration(4000);
                animator3.setRepeatCount(2);
                ObjectAnimator animator4 = ObjectAnimator
                        .ofFloat(child, View.SCALE_Y, 1, 2, 1);
                animator4.setDuration(4000);
                animator4.setRepeatCount(2);
                ObjectAnimator animator5 = ObjectAnimator
                        .ofFloat(child, View.ROTATION, 0, 4320);
                animator5.setDuration(12000);//丝滑
//                        .ofFloat(child,View.ROTATION, 0,360);
//                animator5.setRepeatCount(12);
//                animator5.setDuration(1000);
                ObjectAnimator animator6 = ObjectAnimator
                        .ofFloat(child, View.ALPHA, 1, 0.5f, 1);
                animator6.setDuration(2000);
                animator6.setStartDelay(2000);
                animator6.setRepeatCount(4);
                set.playTogether(animator1, animator2, animator3, animator4, animator5, animator6);
//                set.playSequentially();
                set.start();
            }
        });
        addButton(inputs, "属性", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValueAnimator animator = ValueAnimator.ofInt(1, 5);
                animator.setDuration(5000);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int value = (int) animation.getAnimatedValue();
                        child.setText("[" + value + "]");
                    }
                });
                animator.start();
            }
        });
    }


    //帧动画
    private void initFrameAnimation(LinearLayout show, GridLayout inputs) {
        //设置一个帧动画的list资源
        show.setBackgroundResource(R.drawable.list_animation);
        addButton(inputs, "开始", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AnimationDrawable) show.getBackground()).start();//启动
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
