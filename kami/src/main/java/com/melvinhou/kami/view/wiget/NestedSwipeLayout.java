package com.melvinhou.kami.view.wiget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.OverScroller;

import com.melvinhou.kami.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.NestedScrollingChild3;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.NestedScrollingParent3;
import androidx.core.view.NestedScrollingParentHelper;
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
 * = 分 类 说 明：列表加载的适配器
 * ================================================
 */
public class NestedSwipeLayout extends ViewGroup implements NestedScrollingParent3, NestedScrollingChild3 {
    /***
     *  执行顺序
     *  onNestedPreScroll -> dispatchNestedPreScroll -> onNestedScroll -> dispatchNestedScroll
     */

    //关键控件
    private View mTopSwipeView, mBottomSwipeView;
    private View mListView;
    //阻泥控件高度
    private int mTopSwipeHeight = 0, mBottomSwipeHeight = 0;


    //是否开启滑动
    private boolean mSwipeOpen = true;
    //监听
    private SwipeListener mSwipeListener;


    public void setSwipeListener(SwipeListener swipeListener) {
        this.mSwipeListener = swipeListener;
    }

    public NestedSwipeLayout(Context context) {
        super(context);
        init();
    }

    public NestedSwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NestedSwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mParentHelper = new NestedScrollingParentHelper(this);
        mChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);//是否开启布局滚动
        setBackgroundResource(R.drawable.bg_box);
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
        int width = getWidth();
        int height = getHeight();
        mListView.layout(left, top, right, bottom);
        mTopSwipeView.layout(0, -mTopSwipeHeight, width, 0);
        mBottomSwipeView.layout(0, height, width, height + mBottomSwipeHeight);
    }

    /*初始化控件*/
    private void findViews() {
        mTopSwipeView = findViewById(R.id.swipe_top_view);
        mBottomSwipeView = findViewById(R.id.swipe_bottom_view);
        mListView = findViewById(R.id.list_view);
    }

    /*初始化各项参数*/
    private void initParameter() {
        mTopSwipeHeight = mTopSwipeView.getMeasuredHeight();
        mBottomSwipeHeight = mBottomSwipeView.getMeasuredHeight();
    }

    private void moveLayout() {
//        mListView.layout(getLeft(), mSideContainerTop, getRight(), getHeight() + mSideContainerTop);
//        mTopSwipeView.layout(getLeft(), mSideContainerTop - mTopSwipeHeight, getRight(), mSideContainerTop);
//        mBottomSwipeView.layout(getLeft(), getHeight() + mSideContainerTop, getRight(), getHeight() + mSideContainerTop + mBottomSwipeHeight);
    }


//////////////////////————————————————————嵌套滑动Parent——————————————————————//////////////////////

    private NestedScrollingParentHelper mParentHelper;

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        boolean isStart = (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
        return isStart;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
        mParentHelper.onNestedScrollAccepted(child, target, axes, type);
        startNestedScroll(axes, type);
    }

    @Override
    public int getNestedScrollAxes() {
        return mParentHelper.getNestedScrollAxes();
    }

    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {
        Log.w("t_o", new StringBuffer("onStopNestedScroll\t")
                .append("ScrollY=[").append(getScrollY()).append("]\t")
                .append("type=[").append(type).append("]\t")
                .toString());
        mParentHelper.onStopNestedScroll(target, type);
        stopNestedScroll(type);
        final int scrollY = getScrollY();
        //当滑动超过初始位置的时候，滑回去
        if (scrollY < 0) {
            if (mSwipeListener != null
                    && type == ViewCompat.TYPE_TOUCH
                    && -scrollY > mTopSwipeHeight / 2) {
                mSwipeListener.onRefresh();
            } else{
                if (type == ViewCompat.TYPE_NON_TOUCH){
                    startTop2Original();
                }
            }
        } else if (scrollY > 0) {
            if (mSwipeListener != null
                    && type == ViewCompat.TYPE_TOUCH
                    && (scrollY - getHeight()) > mBottomSwipeHeight / 2) {
                mSwipeListener.onContinue();
            } else{
                if (type == ViewCompat.TYPE_NON_TOUCH){
//                startBottom2Original();
                }
            }
        }
    }


    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        int consumedY = 0;//消耗量
        //dy>0,手指轨迹↑，在父控件前判断
        /*第一轮在父前子前*/
        int oldScrollY = getScrollY();
        int myDy = dy - consumedY;
        if (dy > 0 && oldScrollY < 0) {
            int targetY = oldScrollY + myDy;
            if (targetY > 0) targetY = 0;
            scrollTo(0, targetY);
            consumedY = targetY - oldScrollY;
            myDy = dy - consumedY;
        }
        int[] myConsumed = new int[2];//父控件消耗量
        dispatchNestedPreScroll(dx, myDy, myConsumed, null, type);//调用父类处理
        consumedY += myConsumed[1];
        oldScrollY = getScrollY();
        /*第一轮在父后子前*/
        myDy = dy - consumedY;
        if (dy < 0 && oldScrollY > 0) {
            int targetY = oldScrollY + myDy;
            if (targetY < 0) targetY = 0;
            //移动
            scrollTo(0, targetY);
            consumedY = targetY - oldScrollY;
        }
        consumed[1] = consumedY;
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
//        Log.w("t_o", new StringBuffer("onNestedScroll\t")
//                .append("ScrollY=[").append(getScrollY()).append("]\t")
//                .append("dyConsumed=[").append(dyConsumed).append("]\t")
//                .append("dyUnconsumed=[").append(dyUnconsumed).append("]\t")
//                .append("type=[").append(type).append("]\t")
//                .toString());
        int[] consumed = new int[2];//父控件消耗量
        dispatchNestedScroll(0, dyConsumed, 0, dyUnconsumed, null, type, consumed);//交给父控件处理

        /*第二轮在父后子后*/
        int myConsumed = dyUnconsumed - consumed[1];
        final int oldScrollY = getScrollY();
        int targetY = oldScrollY + myConsumed;
        //限制器
        if (targetY < -mTopSwipeHeight) targetY = -mTopSwipeHeight;
        else if (targetY > mBottomSwipeHeight) targetY = mBottomSwipeHeight;
        //移动
        scrollTo(0, targetY);

    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, @NonNull int[] consumed) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean onNestedFling(
            @NonNull View target, float velocityX, float velocityY, boolean consumed) {
        Log.w("t_o", new StringBuffer("onNestedFling\t")
                .append("ScrollY=[").append(getScrollY()).append("]\t")
                .append("velocityY=[").append(velocityY).append("]\t")
                .append("consumed=[").append(consumed).append("]\t")
                .toString());
        if (!consumed) {
            dispatchNestedFling(0, velocityY, true);
            fling((int) velocityY);
            return true;
        }
        return false;
    }



//////////////////////————————————————————嵌套滑动Child——————————————————————//////////////////////

    private NestedScrollingChildHelper mChildHelper;

    @Override
    public boolean startNestedScroll(int axes, int type) {
        return mChildHelper.startNestedScroll(axes, type);
    }

    @Override
    public void stopNestedScroll(int type) {
        mChildHelper.stopNestedScroll(type);
    }

    @Override
    public boolean hasNestedScrollingParent(int type) {
        return mChildHelper.hasNestedScrollingParent(type);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow, int type) {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type);
    }

    @Override
    public void dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow, int type, @NonNull int[] consumed) {
        mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type, consumed);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow, int type) {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type);
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        Log.w("t_o", new StringBuffer("dispatchNestedPreFling\t")
                .append("ScrollY=[").append(getScrollY()).append("]\t")
                .append("velocityY=[").append(velocityY).append("]\t").toString());
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }


//////////////////////————————————————————其它——————————————————————//////////////////////

    private ValueAnimator animator;

    public void finishTop() {
        startTop2Original();
    }

    /*执行动画回到原来的位置*/
    private void startTop2Original() {
        if (animator!=null)animator.cancel();
        final int scrollY = getScrollY();
        float time = Math.abs(scrollY) / (float) mTopSwipeHeight * 500f;
        animator = ValueAnimator.ofInt(scrollY, 0);
        animator.addUpdateListener(animator1 -> {
            scrollTo(0, (Integer) animator1.getAnimatedValue());
            if (scrollY == 0)
                animator1.cancel();
        });
        //弹性的插值器
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(Float.valueOf(time).longValue());
        animator.start();
    }

    /*执行动画回到原来的位置*/
    private void startBottom2Original() {
        if (animator!=null)animator.cancel();
        final int scrollY = getScrollY();
        float time = Math.abs(scrollY - getHeight()) / (float) mTopSwipeHeight * 500f;
        animator = ValueAnimator.ofInt(scrollY, getHeight());
        animator.addUpdateListener(animator1 -> {
            scrollTo(0, (Integer) animator1.getAnimatedValue());
            if (scrollY == 0)
                animator1.cancel();
        });
        //弹性的插值器
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(Float.valueOf(time).longValue());
        animator.start();
    }

    /**
     * Fling the scroll view
     *
     * @param velocityY The initial velocity in the Y direction. Positive
     *                  numbers mean that the finger/cursor is moving down the screen,
     *                  which means we want to scroll towards the top.
     */
    public void fling(int velocityY) {
        if (getChildCount() > 0) {
//            mScroller.fling(getScrollX(), getScrollY(), // start
//                    0, velocityY, // velocities
//                    0, 0, // x
//                    Integer.MIN_VALUE, Integer.MAX_VALUE, // y
//                    0, 0); // overscroll
//            runAnimatedScroll(true);
        }
    }

    public interface SwipeListener {
        void onRefresh();

        void onContinue();
    }


}
