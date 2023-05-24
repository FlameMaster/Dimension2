package com.melvinhou.test.t03;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.NestedScrollingChild2;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.ViewCompat;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2019/5/24 19:23
 * <p>
 * = 分 类 说 明：支持嵌套滑动
 * ============================================================
 */
public class NestedScrollChildView extends ConstraintLayout implements NestedScrollingChild2 {
    private NestedScrollingChildHelper mChildHelper;
    /*滑动器*/
    private OverScroller mScroller;
    private VelocityTracker mVelocityTracker;
    /*上一次的位置*/
    private int mLastMotionY;
    private int mNestedYOffset;
    private int mLastScrollerY;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private final int[] mScrollConsumed = new int[2];
    private final int[] mScrollOffset = new int[2];


    public NestedScrollChildView(Context context) {
        this(context, null);
    }

    public NestedScrollChildView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestedScrollChildView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
//        NestedScrollView;
//        RecyclerView
    }

    private void init() {
        mChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
        mScroller = new OverScroller(getContext());
        mLastMotionY = 0;
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
//        setLayerType(View.LAYER_TYPE_HARDWARE,null);//开启硬件加速
    }


//////////////////////————————————————————嵌套滑动——————————————————————//////////////////////

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mChildHelper.startNestedScroll(axes);
    }

    @Override
    public boolean startNestedScroll(int axes, int type) {
        return mChildHelper.startNestedScroll(axes, type);
    }

    @Override
    public void stopNestedScroll() {
        mChildHelper.stopNestedScroll();
    }

    @Override
    public void stopNestedScroll(int type) {
        mChildHelper.stopNestedScroll(type);
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean hasNestedScrollingParent(int type) {
        return mChildHelper.hasNestedScrollingParent(type);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                offsetInWindow);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow, int type) {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                offsetInWindow, type);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow,
                                           int type) {
        Log.e("dispatchNestedPreScroll","dy="+dy);
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

//////////////////////————————————————————其它——————————————————————//////////////////////


    /*重写滑动方法*/
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null) mVelocityTracker = VelocityTracker.obtain();
        MotionEvent vtev = MotionEvent.obtain(event);
        final int actionMasked = event.getAction();
        if (actionMasked == 0) mNestedYOffset = 0;
        vtev.offsetLocation(0, mNestedYOffset);
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionY = (int) event.getRawY();
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                mVelocityTracker.addMovement(vtev);
                mScroller.computeScrollOffset();
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                break;
            case MotionEvent.ACTION_UP:
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int initialVelocity = (int) velocityTracker.getYVelocity();
                if (Math.abs(initialVelocity) > mMinimumVelocity) {
//                    fling(-initialVelocity);
                    flingWithNestedDispatch((int) -mVelocityTracker.getYVelocity());
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                stopNestedScroll();
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(vtev);
                //绝对位置
                final int y = (int) event.getRawY();
                //计算位移相对距离
                int deltaY = mLastMotionY - y;
                Log.e("onTouchEvent","deltaY="+deltaY);
                if (dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset, ViewCompat.TYPE_TOUCH)) {
                    deltaY -= mScrollConsumed[1];
                    vtev.offsetLocation(0, mScrollOffset[1]);
                    mNestedYOffset += mScrollOffset[1];
                }
                mLastMotionY = y;
                //判断前面是否已经消耗完
                if (deltaY == 0 && mScrollConsumed[1] != 0) {
                    if (!hasNestedScrollingParent(ViewCompat.TYPE_NON_TOUCH))
                        mScroller.springBack(0, deltaY, 0, 0, 0, 0);
                    onOverScrolled(0, deltaY, false, false);
                }
                //web滑动距离
                int scrollY = getScrollY();
                //剩余未消耗的距离
                int dyUnconsumed = 0;
                if (scrollY == 0) {
                    dyUnconsumed = deltaY;
                } else if (scrollY + deltaY < 0) {
                    dyUnconsumed = deltaY + scrollY;
                    vtev.offsetLocation(0, -dyUnconsumed);
                }
                if (dispatchNestedScroll(0, deltaY - dyUnconsumed, 0, dyUnconsumed, mScrollOffset, ViewCompat.TYPE_TOUCH)) {
                }
            default:

                break;
        }
        return true;
//        return false;
    }


    /*联动滚动判断*/
    private void flingWithNestedDispatch(int velocityY) {
        int scrollY = getScrollY();
        boolean canFling = (scrollY > 0 || velocityY > 0) && (scrollY < 0 || velocityY < 0);
        if (!dispatchNestedPreFling(0, velocityY)) {
            dispatchNestedFling(0, velocityY, canFling);
//            fling(velocityY);
        }
    }

    /*重写滚动*/
    public void fling(int velocityY) {
        Log.d("web滑动", "fling: velocityY : " + velocityY);
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_NON_TOUCH);
        mScroller.fling(getScrollX(), getScrollY(), 0, velocityY, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
        mLastScrollerY = getScrollY();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    /*重写滚动*/
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int y = mScroller.getCurrY();
            int dy = y - mLastScrollerY;
            if (dispatchNestedPreScroll(0, dy, mScrollConsumed, mScrollOffset, ViewCompat.TYPE_NON_TOUCH)) {
                dy -= mScrollConsumed[1];
            }
//            Log.d("web滑动", new StringBuffer("方法:computeScroll\n")
//                    .append("dy:[").append(dy).append("]\n")
//                    .append("mScrollConsumedY:[").append(mScrollConsumed[1]).append("]\n")
//                    .append("mScrollOffsetY:[").append(mScrollOffset[1]).append("]\n")
//                    .append("scrollY:[").append(scrollY).append("]\n")
//                    .toString());
            if (dy != 0) {//还有剩余滚动量
                int scrollY = getScrollY();
                int unconsumedY = 0;
                int consumedY = dy;
                if (scrollY == 0) {
                    unconsumedY = dy;
                    consumedY = 0;
                } else if (scrollY + dy < 0) {
                    unconsumedY = dy + scrollY;
                    consumedY = -scrollY;
                }
                //web滚动
                super.computeScroll();
                if (!dispatchNestedScroll(0, consumedY, 0, unconsumedY, null, ViewCompat.TYPE_NON_TOUCH)) {

                }
            } else if (mScrollConsumed[1] != 0) {//判断是否是由于父类消耗
                if (!hasNestedScrollingParent(ViewCompat.TYPE_NON_TOUCH))
                    mScroller.springBack(0, -dy, 0, 0, 0, 0);
                onOverScrolled(0, -dy, false, false);
            }

            mLastScrollerY = y;
            ViewCompat.postInvalidateOnAnimation(this);
        } else {
            if (hasNestedScrollingParent(ViewCompat.TYPE_NON_TOUCH))
                stopNestedScroll(ViewCompat.TYPE_NON_TOUCH);
            mLastScrollerY = 0;
        }

    }
}
