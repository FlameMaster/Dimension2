package com.melvinhou.test.t05;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.Map;

import androidx.collection.ArrayMap;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2019/2/21 09:26
 * <p>
 * = 分 类 说 明：适用于会员购买卡牌
 * ============================================================
 */
public class CardLayoutManager extends RecyclerView.LayoutManager {


    //显示数量
    int mShowCount = 3;
    //缩放比
    float mScale;
    //总的滑动距离
    int mScrolls;
    //最小的位置
    int minPosition;
    //页面切换监听
    ViewPager.OnPageChangeListener mPageChangeListener;


    public CardLayoutManager(int showCount, float scale) {
//        mShowCount = showCount;
        mScale = scale;
        minPosition = 0;
    }

    /*頁面切換監聽*/
    public void setPageChangeListener(ViewPager.OnPageChangeListener pageChangeListener) {
        this.mPageChangeListener = pageChangeListener;
    }

    public void setMinPosition(int minPosition) {
        this.minPosition = minPosition;
        mScrolls = minPosition * getThreshold();
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }


    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
//        Log.e("onLayoutChildren", "onLayoutChildren() called with: recycler = [" + recycler + "], state = [" + state + "]");
        if (state.getItemCount() == 0 || state.isPreLayout()) return;
        removeAndRecycleAllViews(recycler);
//        measure(recycler);
        layout(recycler, state, 0);
    }

    /*佈局子ui*/
    protected void layout(RecyclerView.Recycler recycler, RecyclerView.State state, int dx) {
        int itemCount = getItemCount();
        if (itemCount < 1) {
            return;
        }
        //切换一整个条目需要的距离
        int sp = getThreshold();
        //切換頁面
//        changeMinPage(mScrolls/sp);
        changeMinPage(Math.round((float) mScrolls / (float) sp));
        //回收
        recyclerViews(recycler);

        int width = getScrollWidth();
        //滑动余下的量
//        int surplusX = mScrolls - minPosition * sp;
        int surplusX = mScrolls % sp;

//        Log.w("child", "\tminPosition：" + minPosition + "\tdx：" + dx + "\tsurplusX：" + surplusX + "\tx：" + x);
        //显示：无论传入的是奇数还是偶数，显示时都会是偶数
        int center = mShowCount / 2;
        //是否向左边滑动：用于判断显示顺序
        boolean isToLeft = surplusX >= sp / 2;
        for (int i = 0; i <= center; i++) {
            if (!isToLeft)
                //显示左边的
                layout(recycler, minPosition + i, width, sp, surplusX);

            //显示右边的
            int p = minPosition + center * 2 - i;
            if (p != minPosition + i)
                layout(recycler, p, width, sp, surplusX);

            if (isToLeft)
                //显示左边的
                layout(recycler, minPosition + i, width, sp, surplusX);
        }
    }

    /*根据位置设置大小和位置*/
    protected void layout(RecyclerView.Recycler recycler, int position, int width, int sp, int dx) {
        //显示
        View view = creation(recycler, position);
        //每个item的位置和大小
//        int centerX = (minPosition + mShowCount / 2) * sp;//中心点位置
//        int x = position * sp + dx;//当前移动量
        //相对偏移量：相对于中间位置
//        int itemX = x - centerX;
        //起点（中点点减去显示的点的一半）+位置*条目距离+剩余量-滑动大小-一半宽度
//        int itemX = (width/2-sp*(mShowCount/2))+position * sp + dx - mScrolls-width/2;
        int itemX = sp * (position - mShowCount / 2)
//                + dx
                - mScrolls;
        //相对位置*长度-相对最小位置的位置(-值)-宽度的/2+偏移量
//        int itemX=(position-minPosition)*sp-(sp-width/2)-width/2-dx;
//        int itemX = (position - minPosition - 1) * sp - dx;
//        Log.w("layout", "\titem：" + (position - minPosition) + "\tdx：" + dx + "\titemX：" + itemX + "\tview：" + view.getId());
        //相对位置
        int transX = getTransX(itemX, width);
        //相对大小
        float ratio = getRatio(itemX, width);
        view.setTranslationX(transX);
        view.setScaleY(ratio);
        view.setScaleX(ratio);

        //特殊方法设置回弹值
        if (dx < sp / 2)
            view.setTag(dx);
        else
            view.setTag(-(sp - dx));
    }

    Map<Integer, Integer> mIds = new ArrayMap<>();

    /*初始化對應位置的ui*/
    protected View creation(RecyclerView.Recycler recycler, int position) {
//        if (getChildAt(getRealityPosition(position%mShowCount, mShowCount))!=null)
//            return getChildAt(getRealityPosition(position%mShowCount, mShowCount));
//        Log.d("child", "position：" + position + "\tcount：" + getChildCount());
        View view = recycler.getViewForPosition(position % getItemCount());

        addView(view);
        view.setId(View.NO_ID);
        mIds.put(view.getId(), position);
        measureChildWithMargins(view, 0, 0);
        int widthSpace = getWidth() - getDecoratedMeasuredWidth(view);
        int heightSpace = getHeight() - getDecoratedMeasuredHeight(view);
        //我们在布局时，将childView居中处理，这里也可以改为只水平居中
        layoutDecoratedWithMargins(view, widthSpace / 2, heightSpace / 2,
                widthSpace / 2 + getDecoratedMeasuredWidth(view),
                heightSpace / 2 + getDecoratedMeasuredHeight(view));
        return view;
    }

    /*回收不在屏幕范围的view*/
    private void recyclerViews(RecyclerView.Recycler recycler) {
        detachAndScrapAttachedViews(recycler);
        for (int i = 0; i < getChildCount(); i++) {
            int key = getChildAt(i).getId();
            if (mIds.get(key) != null) {
                int posintion = mIds.get(key);
//            if (getChildAt(i).getTag() instanceof Integer) {
//                int posintion = (int) getChildAt(i).getTag();

//                Log.d("recyclerViews", "minPosition：" + minPosition + "\tposintion：" + posintion);
                if (posintion < minPosition || posintion >= minPosition + mShowCount) {
                    removeAndRecycleView(getChildAt(i), recycler);
                    mIds.remove(key);
                    Log.e("recyclerViews", "posintion=" + posintion);
                }
            }
        }
    }

////////////////////////////—————————————————————滑动相关———————————————————————////////////////////////////

    /*切換頁面*/
    private void changeMinPage(int minPage) {
        if (minPosition != minPage) {
            minPosition = minPage;
            if (minPosition < 0) minPosition = 0;
            if (mPageChangeListener != null) {
                mPageChangeListener.onPageSelected(minPosition + 1);
            }
        }
    }

    /*滑动时的位置*/
    private int getTransX(int transX, int width) {

        if (Math.abs(transX) > width / 2) {
            if (transX > 0) {
                transX = width / 2 - (transX - width / 2);
            } else {
                transX = -width / 2 + (-width / 2 - transX);
            }
        }
//        Log.e("位移", "position=" + position + "\tcenter=" + (minPosition + mShowCount / 2) + "\ttransX=" + transX);
        return transX;
    }

    /*滑动时的大小*/
    private float getRatio(int transX, int width) {
        //相对位置:取绝对值
        float dX = Math.abs(transX);
        //缩放倍数
        float ratio = 1f;
        if (dX > width) {
            dX = width * 2 - dX;
        }
        if (dX < width) {
            ratio = (1f - dX / (float) width) * (1f - mScale) + mScale;
        }
        //特殊处理
        if (dX == width)
            ratio = 0f;

//        Log.e("缩放", "width=" + width + "\ttransX=" + transX + "\tsp=" + getThreshold() + "\tratio=" + ratio);
        return ratio;
    }

    @Override
    public boolean canScrollVertically() {
        return false;
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
//        dx = dx *4/mShowCount;
        if (mScrolls + dx < 0)
            dx = 0 - mScrolls;

//        if (mScrolls+dx<-getThreshold())
//            dx = -getThreshold()-mScrolls;
//
//        if (mScrolls+dx>getThreshold())
//            dx = getThreshold()-mScrolls;

//        offsetChildrenHorizontal(-dx);
//        mHorizontalHelper.offsetChildren(-dx);
        mScrolls += dx;
        layout(recycler, state, dx);

//        Log.e("swipecard", "state = [" + state.getItemCount() + "], dX = [" + dx + "], scrolls = [" + mScrolls + "], xxx = [" + mScrolls%getThreshold() + "]");
        return dx / 3;
    }

    /*水平方向是否可以被回收掉的阈值*/
    public int getThreshold() {
        return getScrollWidth() * 2 / mShowCount;
    }

    /*获取滑动范围值*/
    private int getScrollWidth() {
//        return getWidth();
        return getWidth() * 3 / 5;
    }
}
