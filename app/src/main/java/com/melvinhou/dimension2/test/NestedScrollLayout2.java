package com.melvinhou.dimension2.test;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.NestedScrollingChild2;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.NestedScrollingParent2;
import androidx.core.view.ViewCompat;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2022/9/29 0029 17:59
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class NestedScrollLayout2 extends ConstraintLayout
        implements NestedScrollingChild2 {
    public NestedScrollLayout2(@NonNull Context context) {
        super(context);
        init();
    }

    public NestedScrollLayout2(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NestedScrollLayout2(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public NestedScrollLayout2(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    private void init() {
        mChildHelper = new NestedScrollingChildHelper(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //找到父类
        findNestedScrollingParent(this);
    }

    //找到父类
    private void findNestedScrollingParent(View view) {
        ViewParent parent = view.getParent();
        if (parent instanceof NestedScrollingParent2)
            mNestedScrollingParent = (NestedScrollingParent2) parent;
        else if (parent instanceof View)
            findNestedScrollingParent((View) parent);
    }

    private NestedScrollingChildHelper mChildHelper;
    //当前找到的滑动父类
    private NestedScrollingParent2 mNestedScrollingParent;


    @Override
    public boolean startNestedScroll(int axes, int type) {
        if (mNestedScrollingParent != null) {
            return mNestedScrollingParent.onStartNestedScroll(this, this, axes, type);
        }
        mChildHelper.startNestedScroll(axes, type);
        return true;
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public void stopNestedScroll(int type) {
        mNestedScrollingParent.onStopNestedScroll(this,type);
        mChildHelper.stopNestedScroll(type);
    }

    @Override
    public boolean hasNestedScrollingParent(int type) {
        return mChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow, int type) {
//        Log.w("c_d", new StringBuffer()
//                .append("dyConsumed:[").append(dyConsumed).append("]\t")
//                .append("dyUnconsumed:[").append(dyUnconsumed).append("]\t")
//                .append("offsetInWindowY:[").append(offsetInWindow != null ? offsetInWindow[1] : 233).append("]\t")
//                .toString());
        //询问父类
        if (mNestedScrollingParent != null) {
            return true;
        }
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type);
    }

    //第一个走这,判断方向
    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow, int type) {
//        Log.e("fc...c_dp", new StringBuffer()
//                .append("dy:[").append(dy).append("]\t")
//                .append("consumedY:[").append(consumed[1]).append("]\t")
//                .append("offsetInWindowY:[").append(offsetInWindow != null ? offsetInWindow[1] : 233).append("]\t")
//                .toString());

        //询问父类
        if (mNestedScrollingParent != null) {
            mNestedScrollingParent.onNestedPreScroll(this, dx, dy, consumed, type);
            //判断是否消费
            return consumed[0] != 0 || consumed[1] != 0;
        }
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type);
    }


//////////////////////————————————————————其它——————————————————————//////////////////////


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
            break;
        case MotionEvent.ACTION_MOVE:
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            stopNestedScroll(ViewCompat.TYPE_TOUCH);
            break;
    }
        return super.onTouchEvent(event);
    }
}
