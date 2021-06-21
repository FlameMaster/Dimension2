package com.melvinhou.dimension2;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.NestedScrollingChild3;
import androidx.core.view.NestedScrollingParent3;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/4/23 21:10
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class NestedScrollLayout extends ConstraintLayout
        implements NestedScrollingChild3, NestedScrollingParent3 {



    public NestedScrollLayout(Context context) {
        this(context, null);
    }

    public NestedScrollLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestedScrollLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


////////////////////////————————————————————嵌套滑动(父)——————————————————————////////////////////////


    //3
    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed, int type, @NonNull int[] consumed) {

    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        return false;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {

    }

    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {

    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {

    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {

    }


////////////////////////————————————————————嵌套滑动(子)——————————————————————////////////////////////



    //3
    @Override
    public void dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                     int dyUnconsumed, @Nullable int[] offsetInWindow, int type, @NonNull int[] consumed) {

    }

    @Override
    public boolean startNestedScroll(int axes, int type) {
        return false;
    }

    @Override
    public void stopNestedScroll(int type) {

    }

    @Override
    public boolean hasNestedScrollingParent(int type) {
        return false;
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow, int type) {
        return false;
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow, int type) {
        return false;
    }
}
