package com.melvinhou.dimension2.test;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.ui.widget.NestedScrollLayout;
import com.melvinhou.kami.util.DimenUtils;

import java.lang.reflect.Field;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.NestedScrollingChild2;
import androidx.core.view.NestedScrollingParent2;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2022/9/29 0029 16:10
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class Test3View extends ViewGroup implements NestedScrollingParent2 {

    public Test3View(@NonNull Context context) {
        super(context);
        init();
    }

    public Test3View(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Test3View(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public Test3View(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    /*最大滑动速度*/
    private final float MAX_VOCLEITY = 6000f;
    private float mvelocityY = 0;

    private NestedScrollingParentHelper mNestedScrollingParentHelper;
    //所需控件:封面，主容器，个人信息，标题栏
    private View mCoverView, mContainerView, mInfoView, mBarView;
    //主容器的位置，初始位置，最高位置，当前,下滑最大值(会回弹),0点位置
    private int mSideContainerInitialTop, mSideContainerMinTop, mSideContainerTop, maxDownSideHeight, mSideContainerZeroTop;
    //手指是否抬起,是否有惯性滑动
    boolean isSideFling;


    private void init() {
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        isSideFling = false;
        maxDownSideHeight = 0;
        mSideContainerInitialTop = 0;
        mSideContainerTop = 0;
        mSideContainerZeroTop = 0;
        mSideContainerMinTop = 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //测量所有子控件的宽和高,只有先测量了所有子控件的尺寸，后面才能使用child.getMeasuredWidth()
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        //调用系统的onMeasure一般是测量自己(当前ViewGroup)的宽和高
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //初始化控件引用
        findViews();
        //初始化自动义参数
        initParameter();

    }

    /*初始化控件*/
    private void findViews() {
        mCoverView = findViewById(R.id.mCover);
        mContainerView = findViewById(R.id.mScroll);
        mInfoView = findViewById(R.id.mInformation);
        mBarView = findViewById(R.id.bar_root);
    }

    /*初始化各项参数*/
    private void initParameter() {
        //主容器
        maxDownSideHeight = DimenUtils.dp2px(200);
        mSideContainerInitialTop = DimenUtils.dp2px(240);
        mSideContainerZeroTop = DimenUtils.getStatusHeight() + DimenUtils.getActionBarSize();
        mSideContainerMinTop = mSideContainerZeroTop;
        mSideContainerTop = mSideContainerInitialTop;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        moveLayout();
    }

    /*改变位置*/
    protected void moveLayout() {
        int coverV = (getBottom() - mSideContainerInitialTop) / 2;
        int coverTop = getTop() - coverV;
        if (mSideContainerTop > mSideContainerInitialTop) {
            coverTop += (mSideContainerTop - mSideContainerInitialTop) / 2;
        }
        mCoverView.layout(getLeft(), coverTop, getRight(), getBottom());
        mContainerView.layout(getLeft(), mSideContainerTop, getRight(), getBottom());
        //个人资料
        mInfoView.layout(getLeft(), mSideContainerTop - mSideContainerInitialTop, getRight(), mSideContainerTop);
        float pre = mSideContainerTop - mSideContainerInitialTop;
        if (pre>0){
            float alpha = 1f - pre /(getBottom()-mSideContainerInitialTop);
            if (alpha>1) alpha=1;
            mInfoView.setAlpha(alpha);
        }
        //标题栏
        mBarView.layout(getLeft(),getTop(),getRight(),mSideContainerZeroTop);
        if (mSideContainerTop <= mSideContainerMinTop)  mBarView.setAlpha(0.9f);
        else mBarView.setAlpha(0);


        Log.d("t_o", new StringBuffer("moveLayout\t")
                .append("mSideContainerTop=[").append(mSideContainerTop).append("]\t")
                .append("mSideContainerMinTop=[").append(mSideContainerMinTop).append("]\t")
                .append("mSideContainerInitialTop=[").append(mSideContainerInitialTop).append("]\t")
                .toString());
    }

//////////////////////————————————————————嵌套滑动——————————————————————//////////////////////

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        return true;
    }

    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {
        mNestedScrollingParentHelper.onStopNestedScroll(target, type);

        Log.d("t_stop", new StringBuffer("===\t")
                .append("是否越界=[").append(mSideContainerTop > mSideContainerInitialTop).append("]\t")
                .append("isSideFling=[").append(isSideFling).append("]\t")
                .append("type=[").append(type).append("]\t")
                .toString());

        //当滑动超过初始位置的时候，滑回去
        if (mSideContainerTop > mSideContainerInitialTop) {
            if (type == ViewCompat.TYPE_TOUCH) {
                if (mvelocityY > 0)
                    start2Original();
                else
                    startOpenCover();
            }
        }

    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return onStartNestedScroll(child, target, nestedScrollAxes, ViewCompat.TYPE_TOUCH);
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes, type);
    }

    //    自己的,主要处理子view未处理的向下
    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        int unDy = dyUnconsumed;
        int targetY = mSideContainerTop - dyUnconsumed;//目标位置
        //阻力判断
        if (targetY > mSideContainerInitialTop) {
            if (targetY<(mSideContainerInitialTop+maxDownSideHeight)){
                if (type == ViewCompat.TYPE_TOUCH) {
                    //阻力值
                    float proportion = 1f - (float) (targetY - mSideContainerInitialTop) / (float) maxDownSideHeight;
                    unDy = (int) ((float) dyUnconsumed * proportion * proportion);
                } else {
                    if (mSideContainerTop > mSideContainerInitialTop) return;
                    unDy += (targetY - mSideContainerInitialTop);
                }
            }
        }
        mSideContainerTop -= unDy;
        if (mSideContainerTop < mSideContainerMinTop) mSideContainerTop = mSideContainerMinTop;
        if (mSideContainerTop>getBottom()) mSideContainerTop = getBottom();
        moveLayout();
    }

    //    接受的,只处理向上滑
    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {

        //不拦截向下滑和滚动
        if (dy <= 0) {
            consumed[1] = 0;
            return;
        }
        int targetY = mSideContainerTop - dy;//当前滑动后的目标位置
        int mSideContainerMaxTop = mSideContainerInitialTop + maxDownSideHeight;
        if (targetY > mSideContainerMaxTop) {
            dy += (targetY - mSideContainerMaxTop);
        }
        //不能超过最小值
        if (targetY < mSideContainerMinTop) {
            dy += (targetY - mSideContainerMinTop);
        }
        mSideContainerTop -= dy;
        consumed[1] = dy;
        moveLayout();
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        mvelocityY = velocityY;
        Log.w("onNestedPreFling", new StringBuffer("===\t")
                .append("velocityY=[").append(velocityY).append("]\t")
                .toString());
        //过界不让滚动
        if (mSideContainerTop > mSideContainerInitialTop) {
            return true;
        }
        //rectckerview无法使用filing，内存泄漏
        if (target instanceof RecyclerView) {
            return false;
        }
//        float sy = Math.abs(velocityY) > MAX_VOCLEITY ? (velocityY / Math.abs(velocityY) * MAX_VOCLEITY) : velocityY;
        if (target instanceof NestedScrollChildView) {
            ((NestedScrollChildView) target).fling((int) velocityY);
            return true;
        }
        return super.onNestedPreFling(target, velocityX, velocityY);
    }

//////////////////////————————————————————其它——————————————————————//////////////////////


    /*打开封面*/
    private void startOpenCover() {
        final int endTop = getBottom();
        final float size = endTop - mSideContainerTop;
        float time = 1000f;
        ValueAnimator animator = ValueAnimator.ofInt(0, (int) size);
        animator.addUpdateListener(animator1 -> {
            mSideContainerTop += (Integer) animator1.getAnimatedValue();
            if (mSideContainerTop > endTop) {
                mSideContainerTop = endTop;
            }
            if (mSideContainerTop < mSideContainerInitialTop) {
                mSideContainerTop = mSideContainerInitialTop;
            }
            moveLayout();
            if (mSideContainerTop == endTop)
                animator1.cancel();
        });
        //弹性的插值器
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(Float.valueOf(time).longValue());
        animator.start();
    }

    /*执行动画回到原来的位置*/
    private void start2Original() {
        final float size = mSideContainerTop - mSideContainerInitialTop;
        float time = size / (float) maxDownSideHeight * 1000f;
        ValueAnimator animator = ValueAnimator.ofInt(0, (int) size);
        animator.addUpdateListener(animator1 -> {
            mSideContainerTop -= (Integer) animator1.getAnimatedValue();
            if (mSideContainerTop > mSideContainerInitialTop + maxDownSideHeight) {
                mSideContainerTop = mSideContainerInitialTop + maxDownSideHeight;
            }
            if (mSideContainerTop < mSideContainerInitialTop) {
                mSideContainerTop = mSideContainerInitialTop;
            }
            moveLayout();
            if (mSideContainerTop == mSideContainerInitialTop)
                animator1.cancel();
        });
        //弹性的插值器
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(Float.valueOf(time).longValue());
        animator.start();
    }

}
