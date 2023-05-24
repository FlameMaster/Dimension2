package com.melvinhou.test.t04;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import com.melvinhou.dimension2.R;

import androidx.annotation.NonNull;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.ViewCompat;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2022/10/8 0008 9:48
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class NestedSwipeLayout extends ViewGroup implements NestedScrollingParent3 {

    //关键控件
    private View mTopSwipeView, mBottomSwipeView;
    private View mListView;
    //阻泥控件高度
    private int mTopSwipeHeight = 0, mBottomSwipeHeight = 0;
    private int mSideContainerTop = 0, mSideContainerInitialTop = 0;
    //是否开启滑动
    private boolean mSwipeOpen = true;

    //监听
    private SwipeListener mSwipeListener;

    public void setSwipeListener(SwipeListener swipeListener) {
        this.mSwipeListener = swipeListener;
    }

    public NestedSwipeLayout(Context context) {
        super(context);
    }

    public NestedSwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NestedSwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        moveLayout();
    }

    /*初始化控件*/
    private void findViews() {
        mTopSwipeView = findViewById(R.id.top_swipe_view);
        mBottomSwipeView = findViewById(R.id.bottom_swipe_view);
        mListView = findViewById(R.id.list_view);
    }

    /*初始化各项参数*/
    private void initParameter() {
        mTopSwipeHeight = mTopSwipeView.getMeasuredHeight();
        mBottomSwipeHeight = mBottomSwipeView.getMeasuredHeight();
        mSideContainerInitialTop = 0;
        mSideContainerTop = mSideContainerInitialTop;
    }

    private void moveLayout() {
        mListView.layout(getLeft(), mSideContainerTop, getRight(), getBottom() + mSideContainerTop);
        mTopSwipeView.layout(getLeft(), mSideContainerTop - mTopSwipeHeight, getRight(), mSideContainerTop);
        mBottomSwipeView.layout(getLeft(), getBottom() + mSideContainerTop, getRight(), getBottom() + mSideContainerTop + mBottomSwipeHeight);

//        Log.d("t_o", new StringBuffer("moveLayout\t")
//                .append("mSideContainerTop=[").append(mSideContainerTop).append("]\t")
//                .append("mSideContainerInitialTop=[").append(mSideContainerInitialTop).append("]\t")
//                .toString());
    }


//////////////////////————————————————————嵌套滑动——————————————————————//////////////////////


    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        return mSwipeOpen;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {

    }

    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {

//        Log.d("t_stop", new StringBuffer("onStopNestedScroll===\t")
//                .append("mSideContainerTop=[").append(mSideContainerTop).append("]\t")
//                .append("type=[").append(type).append("]\t")
//                .toString());
        //当滑动超过初始位置的时候，滑回去
        if (mSideContainerTop > mSideContainerInitialTop) {
            if (mSwipeListener != null
                    && type == ViewCompat.TYPE_TOUCH
                    && (mSideContainerTop - mSideContainerInitialTop) > mTopSwipeHeight / 2) {
                mSwipeListener.onRefresh();
            } else {
                startTop2Original();
            }
        } else if (mSideContainerTop < mSideContainerInitialTop) {
            if (mSwipeListener != null
//                    && type == ViewCompat.TYPE_TOUCH
                    && (mSideContainerInitialTop - mSideContainerTop) > mBottomSwipeHeight / 2) {
                mSwipeListener.onContinue();
            } else {
//                startBottom2Original();
            }
        }
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        int targetY = mSideContainerTop - dy;//当前滑动后的目标位置
        int unitY = dy;
        if (dy < 0 && mSideContainerTop < mSideContainerInitialTop) {//下拉状态
            if (targetY > mSideContainerInitialTop)
                unitY += targetY - mSideContainerInitialTop;
        } else if (dy > 0 && mSideContainerTop > mSideContainerInitialTop) {//上拉状态
            if (targetY < mSideContainerInitialTop)
                unitY += targetY - mSideContainerInitialTop;
        } else {
            consumed[1] = 0;
            return;
        }
        mSideContainerTop -= unitY;
        consumed[1] = unitY;
        moveLayout();
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        int unitY = dyUnconsumed;
        int targetY = mSideContainerTop - dyUnconsumed;//目标位置
        if (targetY > mSideContainerInitialTop) {
            if (type == ViewCompat.TYPE_TOUCH) {
                //阻力值
                float proportion = 1f - (float) (mSideContainerTop - mSideContainerInitialTop) / (float) mTopSwipeHeight;
                unitY = (int) ((float) dyUnconsumed * proportion);
            } else {
                unitY = 0;
            }
        }
        mSideContainerTop -= unitY;
        if (mSideContainerTop < mSideContainerInitialTop - mBottomSwipeHeight)
            mSideContainerTop = mSideContainerInitialTop - mBottomSwipeHeight;
        if (mSideContainerTop > mSideContainerInitialTop + mTopSwipeHeight)
            mSideContainerTop = mSideContainerInitialTop + mTopSwipeHeight;
        moveLayout();
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, @NonNull int[] consumed) {
//        Log.w("t_o", new StringBuffer("onNestedScroll\t")
//                .append("mSideContainerTop=[").append(mSideContainerTop).append("]\t")
//                .append("dyConsumed=[").append(dyConsumed).append("]\t")
//                .append("dyUnconsumed=[").append(dyUnconsumed).append("]\t")
//                .append("type=[").append(type).append("]\t")
//                .append("consumed=[").append(consumed[1]).append("]\t")
//                .toString());
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
    }

//////////////////////————————————————————其它——————————————————————//////////////////////

    public void finishTop(){
        startTop2Original();
    }

    /*执行动画回到原来的位置*/
    private void startTop2Original() {
        final float size = Math.abs(mSideContainerTop - mSideContainerInitialTop);
        float time = size / (float) mTopSwipeHeight * 1000f;
        ValueAnimator animator = ValueAnimator.ofInt(0, (int) size);
        animator.addUpdateListener(animator1 -> {
            mSideContainerTop -= (Integer) animator1.getAnimatedValue();
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

    /*执行动画回到原来的位置*/
    private void startBottom2Original() {
        final float size = Math.abs(mSideContainerTop - mSideContainerInitialTop);
        float time = size / (float) mBottomSwipeHeight * 1000f;
        ValueAnimator animator = ValueAnimator.ofInt(0, (int) size);
        animator.addUpdateListener(animator1 -> {
            mSideContainerTop += (Integer) animator1.getAnimatedValue();
            if (mSideContainerTop > mSideContainerInitialTop) {
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


    interface SwipeListener {
        void onRefresh();

        void onContinue();
    }
}
