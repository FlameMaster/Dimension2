package com.melvinhou.test.t05;

import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2022/8/17 0017 16:22
 * <p>
 * = 分 类 说 明：初级的自定义LayoutManager，recyclerview需要match_parent
 * ================================================
 */
public class TestLayoutManager extends RecyclerView.LayoutManager {
    private int totalHeight = 0;
    private int verticalScrolloffset = 0;


    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.getItemCount() == 0 || state.isPreLayout()) return;
        removeAndRecycleAllViews(recycler);
//        measure(recycler);
        layout(recycler, 0);
    }

    /*佈局子ui*/
    protected void layout(RecyclerView.Recycler recycler, int dx) {
        detachAndScrapAttachedViews(recycler);
        int offsetY = 0;
        totalHeight = 0;
        for (int i = 0; i < getItemCount(); i++) {
            View child = recycler.getViewForPosition(i);
            addView(child);
            measureChildWithMargins(child, 0, 0);
            int width = getDecoratedMeasuredWidth(child);
            int height = getDecoratedMeasuredHeight(child);
            int left = (getWidth() - width) / 2;
            layoutDecorated(child, left, offsetY, getWidth() - left, offsetY + height);
            offsetY += height;
            totalHeight += height;
        }
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int travel = dy;
        if (verticalScrolloffset + dy < 0) {
            travel = -verticalScrolloffset;
        } else if (verticalScrolloffset + dy > totalHeight - getVerticalSpace()) {
            travel = totalHeight - getVerticalSpace() - verticalScrolloffset;
        }

        verticalScrolloffset += travel;
        offsetChildrenVertical(-travel);
        return travel;
    }


    private int getVerticalSpace() {
        return getHeight() - getPaddingBottom() - getPaddingTop();
    }
}
