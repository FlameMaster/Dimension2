package com.melvinhou.dimension2.test;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2022/7/20 0020 10:20
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class TestView extends ViewGroup {


    private Disposable mDisposable;

    private List<Animator> animators = new ArrayList<>();


    public TestView(Context context) {
        this(context, null);
    }

    public TestView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TestView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //测量所有子控件的宽和高,只有先测量了所有子控件的尺寸，后面才能使用child.getMeasuredWidth()
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            int x = (getMeasuredWidth() - width) / 2;
            int y = (getMeasuredHeight() - height) / 2;
            child.layout(x, y, x + width, y + height);

            //先隐藏
            ObjectAnimator animator = ObjectAnimator
                    .ofFloat(child, View.ALPHA, 0);
            animator.start();
        }
    }

    private int position = 0;

    /**
     * 启动
     */
    public void start() {
        position =0;
        //准备动画
        fc();
        //随机
        Collections.shuffle(animators);
        //启动
        mDisposable = Observable
                .interval(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(number -> {
                    if (position < animators.size()) {
                        Animator animator = animators.get(position);
                        animator.start();
                        animator.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

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
                    } else if (mDisposable != null) {
                        mDisposable.dispose();
                        mDisposable = null;
                    }
                    position++;
                });
    }


    private void fc() {
        //数量
        int count = getChildCount();
        if (count <= 0) return;

        //平分角度
        double degreeUnit = 360d / count;
        //圆形半径
        int radius = Math.min(getMeasuredWidth(), getMeasuredHeight());
        animators.clear();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            //限制半径
            int endRadius = (radius - child.getMeasuredWidth()) / 2;
            //把半径随机一个区间
            endRadius -= endRadius/2 * Math.random();
            //这一步是通过三角函数计算x,y
            int[] parameters = fcc(i * degreeUnit);
            //角度
            double degree = parameters[0];
            //斜边计算xy
            double cosX = Math.cos(Math.toRadians(degree));
            double cosY = Math.cos(Math.toRadians(90 - degree));
            int vectorX = parameters[1], vectorY = parameters[2];//修正方向
            int endX = (int) (endRadius * cosX * vectorX);
            int endY = (int) (endRadius * cosY * vectorY);


            Log.e("动画参数", "endX=" + endX + "\r\tendY=" + endY);
            //动画
//            child
//                    .animate()
//                    .translationXBy(1)
//                    .translationX(endX)
//                    .translationYBy(1)
//                    .translationY(endY)
//                    .alphaBy(0.2f)
//                    .alpha(1f)
//                    .setDuration(2000)
//                    .start();

            AnimatorSet set = new AnimatorSet();

            ObjectAnimator animator1 = ObjectAnimator
                    .ofFloat(child, View.TRANSLATION_X, 0, endX);
            ObjectAnimator animator2 = ObjectAnimator
                    .ofFloat(child, View.TRANSLATION_Y, 0, endY);
            ObjectAnimator animator3 = ObjectAnimator
                    .ofFloat(child, View.SCALE_X, 0.2f, 1);
            ObjectAnimator animator4 = ObjectAnimator
                    .ofFloat(child, View.SCALE_Y, 0.2f, 1);
            ObjectAnimator animator5 = ObjectAnimator
                    .ofFloat(child, View.ALPHA, 0.3f, 1f, 1f, 1f, 1f, 0f);
            animator1.setDuration(2000);
            animator2.setDuration(2000);
            animator3.setDuration(2000);
            animator4.setDuration(2000);
            animator5.setDuration(4000);
            set.playTogether(animator1, animator2, animator3, animator4, animator5);
            animators.add(set);
        }

    }


    private int[] fcc(double degree) {
        int[] parameters = new int[3];

        //方向修正
        int vectorX = 0, vectorY = 0;
        if (degree > 0 && degree < 180) vectorY = 1;
        else if (degree > 180 && degree < 360) vectorY = -1;
        int temDegree = (int) ((degree + 90) % 360);
        if (temDegree > 0 && temDegree < 180) vectorX = 1;
        else if (temDegree > 180 && temDegree < 360) vectorX = -1;

        //角度修正，用于直角计算xy
        double rightDegree = degree;
        if (degree > 270) {
            rightDegree = 360 - degree;
        } else if (degree > 180) {
            rightDegree = degree - 180;
        } else if (degree > 90) {
            rightDegree = 180 - degree;
        }
        parameters[0] = (int) rightDegree;
        parameters[1] = vectorX;
        parameters[2] = vectorY;
        return parameters;
    }
}
