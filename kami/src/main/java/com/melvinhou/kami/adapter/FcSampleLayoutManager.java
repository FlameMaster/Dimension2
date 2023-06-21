package com.melvinhou.kami.adapter;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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
 * = 分 类 说 明：模拟LinearLayoutManager
 * ================================================
 */
public class FcSampleLayoutManager extends RecyclerView.LayoutManager {

    //FlexboxLayoutManager流式布局

    private int offsetX = 0, offsetY = 0;//总偏移量,负数


    private int orientation = RecyclerView.VERTICAL;//方向
    private boolean isLoop = false;//是否循环
    private int mTotalWidth = 0, mTotalHeight = 0;//当前总长

    public FcSampleLayoutManager(@RecyclerView.Orientation int orientation, boolean isLoop) {
        this.isLoop = isLoop;
        this.orientation = orientation;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onMeasure(@NonNull RecyclerView.Recycler recycler, @NonNull RecyclerView.State state, int widthSpec, int heightSpec) {
        if (getItemCount() > 0) {
            final int widthMode = View.MeasureSpec.getMode(widthSpec);
            final int heightMode = View.MeasureSpec.getMode(heightSpec);
            int totalWidth = 0;
            int totalHeight = 0;
            for (int i = 0; i < getItemCount(); i++) {
                View child = recycler.getViewForPosition(i);
                child.measure(widthSpec, heightSpec);
                totalWidth += child.getMeasuredWidth();
                totalHeight += child.getMeasuredHeight();
            }
            //适配wrap_content
            if (widthMode == View.MeasureSpec.AT_MOST || widthMode == View.MeasureSpec.UNSPECIFIED) {
                int width = 0;
                if (orientation == RecyclerView.HORIZONTAL)
                    width = totalWidth;
                else
                    width = totalWidth / getItemCount();
                widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
            }
            if (heightMode == View.MeasureSpec.AT_MOST || heightMode == View.MeasureSpec.UNSPECIFIED) {
                int height = 0;
                if (orientation == RecyclerView.VERTICAL)
                    height = totalHeight;
                else
                    height = totalHeight / getItemCount();
                heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
            }
        }
        super.onMeasure(recycler, state, widthSpec, heightSpec);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.getItemCount() == 0 || state.isPreLayout()) return;
        removeAndRecycleAllViews(recycler);
        detachAndScrapAttachedViews(recycler);
        int start = 0;
        int count = getItemCount();
        int curuntX = 0;
        int curuntY = 0;
        for (int i = start; i < count; i++) {
            int index = i;//当前item的全局位置
            View child = recycler.getViewForPosition(index);
            addView(child);
            //测量子view
            measureChildWithMargins(child, 0, 0);
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            int left = curuntX;
            int top = curuntY;
            layoutDecorated(child, left, top, left + width, top + height);
            if (i < count - 1) {
                if (orientation == RecyclerView.VERTICAL)
                    curuntY += height;
                else if (orientation == RecyclerView.HORIZONTAL)
                    curuntX += width;
            }
        }
        mTotalWidth = Math.max(curuntX, getWidth());
        mTotalHeight = Math.max(curuntY, getHeight());
    }

    @Override
    public boolean canScrollHorizontally() {
        return orientation == RecyclerView.HORIZONTAL;
    }

    @Override
    public boolean canScrollVertically() {
        return orientation == RecyclerView.VERTICAL;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int travel = dx;
        if (offsetX + dx < 0) {
            travel = -offsetX;
        } else if (offsetX + dx > getMaxScrollHorizontally()) {
            travel = getMaxScrollHorizontally() - offsetX;
        }
        offsetX += travel;
        offsetChildrenHorizontal(-travel);
        return travel;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int travel = dy;
        if (offsetY + dy < 0) {
            travel = -offsetY;
        } else if (offsetY + dy > getMaxScrollVertically()) {
            travel = getMaxScrollVertically() - offsetY;
        }
        offsetY += travel;
        offsetChildrenVertical(-travel);
        return travel;
    }


    /**
     * @return 总可滑动距离
     */
    private int getMaxScrollHorizontally() {
        return mTotalWidth;
    }


    /**
     * @return 总可滑动距离
     */
    private int getMaxScrollVertically() {
        return mTotalHeight;
    }
}
