package com.melvinhou.game.klotski;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.customview.widget.ViewDragHelper;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/4/26 23:29
 * <p>
 * = 分 类 说 明：华容道的布局
 * ================================================
 */
public class GameKlotskiLayout extends ConstraintLayout {

    private float mLastX, mLastY;
    private static final int IDLE = 0;
    private static final int HORIZONTAL = 1;
    private static final int VERTICAL = 2;
    private int mOrientation = IDLE;

    //拖拽帮助类，用于拖拽子view
    private ViewDragHelper mDragHelper;
    //拖拽类的实现
    private ViewDragHelper.Callback mCallBack = new ViewDragHelper.Callback() {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            // 1. 决定当前被拖拽的child是否拖的动。(抽象方法，必须重写)
            return true;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            // 2. 决定拖拽的范围
            return getMeasuredWidth() - child.getMeasuredWidth();
        }

        @Override
        public int getViewVerticalDragRange(@NonNull View child) {
            return getMeasuredHeight() - child.getMeasuredHeight();
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            // 3. 决定拖动时的位置，可在这里进行位置修正。（若想在此方向拖动，必须重写，因为默认返回0）
            if (mOrientation != HORIZONTAL) return child.getLeft();
//            Log.d("GameKlotskiLayout", new StringBuffer()
//                    .append("x").append(":\r").append(child.getX()).append("\r\t")
//                    .append("left").append(":\r").append(left).append("\r\t")
//                    .append("dx").append(":\r").append(dx).append("\r\t")
//                    .toString());

            //重合判断
            int right = left + child.getWidth();
            int top = child.getTop();
            int bottom = child.getBottom();
            View view = getCrossChild(child, new Rect(left, top, right, bottom));
            if (view != null) {
                if (dx > 0) {//往右滑
                    left = view.getLeft() - child.getWidth();
                } else {
                    left = view.getRight();
                }
            }

            //边界判断
            if (left < 0) return 0;
            if (left + child.getWidth() > getWidth()) return getWidth() - child.getWidth();

            return left;
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            if (mOrientation != VERTICAL) return child.getTop();

            int bottom = top + child.getHeight();
            int left = child.getLeft();
            int right = child.getRight();
            View view = getCrossChild(child, new Rect(left, top, right, bottom));
            if (view != null) {
                if (dy > 0) {//往下滑
                    top = view.getTop() - child.getHeight();
                } else {
                    top = view.getBottom();
                }
            }


            if (top < 0) return 0;
            if (top + child.getHeight() > getHeight()) return getHeight() - child.getHeight();
            return top;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top,
                                          int dx, int dy) {
            // 4. 决定了当View被拖动时，希望同时引发的其他变化
        }


        /**
         * 手指抬起
         *
         * @param releasedChild 被释放的孩子
         * @param xvel 释放时X方向的速度
         * @param yvel 释放时Y方向的速度
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            // 5. 决定当childView被释放时，希望做的事情——执行打开/关闭动画，更新状态
        }

        //状态改变
        @Override
        public void onViewDragStateChanged(int state) {
            //IDLE,DRAGGING,SETTING[自动滚动时]
        }

        /**
         * 当child被捕获
         * @param capturedChild
         * @param activePointerId
         */
        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
//            Rect rect = new Rect();
//            capturedChild.getHitRect(rect);
//            Log.e("GameKlotskiLayout", new StringBuffer()
//                    .append("rect").append(":\r").append(rect).append("\r\t")
//                    .toString());

        }


    };

    public GameKlotskiLayout(Context context) {
        this(context, null);
    }

    public GameKlotskiLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GameKlotskiLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mDragHelper = ViewDragHelper.create(this, mCallBack);
    }

    boolean isFirst = true;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (isFirst) {
//            isFirst = false;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                child.setScaleX(0.96f);
                child.setScaleY(0.96f);
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mDragHelper.shouldInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            mOrientation
                    = Math.abs(x - mLastX) > Math.abs(y - mLastY) ? HORIZONTAL : VERTICAL;
        } else mOrientation = IDLE;
        mLastX = x;
        mLastY = y;
        mDragHelper.processTouchEvent(event);
        return true;
    }

    /**
     * 获取当前绝对位置的点击者
     *
     * @param rawX
     * @param rawY
     * @return
     */
    private View getCurrentChild(int rawX, int rawY) {
        Rect rect = new Rect();
        //因为华容道不会重合，所以不用考虑重合问题
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.getGlobalVisibleRect(rect);
//            Log.w("GameKlotskiLayout", new StringBuffer()
//                    .append("top").append(":\r").append(rect.top).append("\r\t")
//                    .append("bottom").append(":\r").append(rect.bottom).append("\r\t")
//                    .toString());
            if (rect.contains(rawX, rawY)) return child;
        }
        return null;
    }

    private View getCrossChild(View useView, Rect useRect) {
        Rect rect = new Rect();
        //因为华容道不会重合，所以不用考虑重合问题
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == useView) continue;
            child.getHitRect(rect);
            if (Rect.intersects(useRect, rect)) {
//                Log.w("GameKlotskiLayout", new StringBuffer()
//                        .append("useRect").append(":\r").append(useRect).append("\r\t")
//                        .append("rect").append(":\r").append(rect).append("\r\t")
//                        .toString());
                return child;
            }
        }
        return null;
    }

}
