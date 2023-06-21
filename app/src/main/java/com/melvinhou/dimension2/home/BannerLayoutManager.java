package com.melvinhou.dimension2.home;

import android.graphics.PointF;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.melvinhou.kami.util.DimenUtils;

import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearSmoothScroller;
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
 * = 分 类 说 明：首页banner
 * ================================================
 */
public class BannerLayoutManager extends RecyclerView.LayoutManager
        implements RecyclerView.SmoothScroller.ScrollVectorProvider {

    public final int MAX_COUNT = 2;//最多同屏显示
    private int offsetX = 0;//总偏移量,负数
    private int itemScrollSize = 0;//单个个条目滑动的距离
    private int totalScrollSize = 0;//总长度，无限循环时为一轮长度
    private boolean isLoop = false;//是否循环

    BannerLayoutManager(boolean isLoop) {
        this.isLoop = isLoop;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onMeasure(@NonNull RecyclerView.Recycler recycler, @NonNull RecyclerView.State state, int widthSpec, int heightSpec) {
        if (getItemCount() > 0) {
            View child = recycler.getViewForPosition(0);
            child.measure(widthSpec, heightSpec);
            //应对RecyclerView的WRAP_CONTENT
            //直接设置容器宽高
//            setMeasuredDimension(child.getMeasuredWidth(), child.getMeasuredHeight());
//            return;
            //两种方式都行↑↓
            widthSpec = View.MeasureSpec.makeMeasureSpec(child.getMeasuredWidth(), View.MeasureSpec.EXACTLY);
            heightSpec = View.MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(), View.MeasureSpec.EXACTLY);
        }
        super.onMeasure(recycler, state, widthSpec, heightSpec);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.getItemCount() == 0 || state.isPreLayout()) return;
        removeAndRecycleAllViews(recycler);
        itemScrollSize = getWidth();
        totalScrollSize = itemScrollSize * getItemCount();
        layout(recycler);
    }

    protected void layout(RecyclerView.Recycler recycler) {
        detachAndScrapAttachedViews(recycler);
        int offsetX2 = totalScrollSize + offsetX % totalScrollSize;//适配负数
        int startIndex = offsetX2 / itemScrollSize;//起始位置
        if (startIndex > 0) {//清除前一个
            int index = startIndex - 1;
            removeAndRecycleView(recycler.getViewForPosition(index % getItemCount()), recycler);
        }
        if (getItemCount() > MAX_COUNT) {//清除后一个
            int index = startIndex + MAX_COUNT;
            removeAndRecycleView(recycler.getViewForPosition(index % getItemCount()), recycler);
        }
        int count = MAX_COUNT;
//        Log.d("自定义list", "startIndex=" + startIndex + "/offsetX=" + offsetX);
        for (int i = 0; i < count; i++) {
            int index = startIndex + i;//当前item的全局位置
            View child = recycler.getViewForPosition(index % getItemCount());
            addView(child);
            //测量子view
            measureChildWithMargins(child, 0, 0);
            int width = getDecoratedMeasuredWidth(child);
            int height = getDecoratedMeasuredHeight(child);
            int left = 0;
            int top = 0;
            layoutDecorated(child, left, top, left + width, top + height);
            int childStart = index * itemScrollSize;//当前item在滑动轴的起始位置
            int currentX = offsetX2 - childStart;//当前item的相对位移
            if (currentX <= 0) {
                child.setTranslationX(-currentX);
                child.setScaleX(1);
                child.setScaleY(1);
            } else {
                child.setTranslationX(0);
                float scale = 1 - 0.3f * currentX / itemScrollSize;
                child.setScaleX(scale);
                child.setScaleY(scale);
            }
        }
    }

    @Override
    public int getPosition(@NonNull View view) {
        int position = super.getPosition(view);
        return offsetX / totalScrollSize * getItemCount() + position;
    }

    @Override
    public void scrollToPosition(int position) {
        int index = offsetX / totalScrollSize * getItemCount() + position;
        offsetX = index * itemScrollSize;
        requestLayout();
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
//        LinearSmoothScroller linearSmoothScroller =
//                new LinearSmoothScroller(recyclerView.getContext());
//        linearSmoothScroller.setTargetPosition(position);
//        startSmoothScroll(linearSmoothScroller);
//        LinearSmoothScroller.TARGET_SEEK_SCROLL_DISTANCE_PX,固定dx为10000，不好修改

        int targetX = position * itemScrollSize;
        recyclerView.smoothScrollBy(targetX - offsetX, 0);
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int travel = dx;
        if (!isLoop) {
            if (offsetX + dx < 0) {
                travel = -offsetX;
            } else if (offsetX + dx > getMaxScroll()) {
                travel = getMaxScroll() - offsetX;
            }
        }
        offsetX += travel;
//        offsetChildrenHorizontal(-travel);//常规用法堆控件长度
        layout(recycler);//非常规
        return travel;
    }

    /**
     * @return 总可滑动距离
     */
    private int getMaxScroll() {
        if (getItemCount() > 0)
            return (getItemCount() - 1) * itemScrollSize;//总距离减剩余一个的距离
        return 0;
    }

    @Nullable
    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        if (getChildCount() == 0) {
            return null;
        }
        final int firstChildPos = getPosition(getChildAt(0));
        final int direction = targetPosition < firstChildPos ? -1 : 1;
        return new PointF(direction, 0);
    }
}
