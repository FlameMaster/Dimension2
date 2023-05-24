package com.melvinhou.test.t05;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;


/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2018/12/4 11:36
 * <p>
 * = 分 类 说 明：推荐列表的帮助类：滚动对齐
 * ============================================================
 */
public class CardSnapHelper extends SnapHelper {

    OrientationHelper mVerticalHelper;
    OrientationHelper mHorizontalHelper;
    int maxCount;


    public CardSnapHelper(int maxCount) {
        this.maxCount = maxCount;
    }

    private OrientationHelper getVerticalHelper(RecyclerView.LayoutManager layoutManager) {
        if (mVerticalHelper == null) {
            mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager);
        }
        return mVerticalHelper;
    }

    private OrientationHelper getHorizontalHelper(RecyclerView.LayoutManager layoutManager) {
        if (mHorizontalHelper == null) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager);
        }
        return mHorizontalHelper;
    }

    //targetView的start坐标与RecyclerView的paddingStart之间的差值
    //就是需要滚动调整的距离
    private int distanceToStart(View targetView, OrientationHelper helper) {
//        Log.e("CardSnapHelper", "targetView:" + targetView.getTag()+"\tview:"+targetView.getId());
        if (targetView.getTag() != null)
            return -(int) targetView.getTag();
        else return 0;
    }

    /*计算的SnapView当前位置与目标位置的距离*/
    @Nullable
    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager, @NonNull View targetView) {
        int[] out = new int[2];
        //竖直判断
        if (layoutManager.canScrollVertically()) {
            out[1] = distanceToStart(targetView, getVerticalHelper(layoutManager));
        } else {
            out[1] = 0;
        }
        //水平判断
        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceToStart(targetView, getHorizontalHelper(layoutManager));
        } else {
            out[0] = 0;
        }
//        Log.e("CardSnapHelper", "out:" + out[0]);
        return out;
    }

    /*找到当前时刻的的SnapView*/
    @Nullable
    @Override
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {

        if (layoutManager.getChildCount() <= 0) return null;
//        Log.e("CardSnapHelper", "count:" + layoutManager.getChildCount());

        if (layoutManager instanceof CardLayoutManager) {
//            View firstChildView = layoutManager.findViewByPosition(0);
            View firstChildView = layoutManager.getChildAt(maxCount / 2);
            return firstChildView;
        } else {
            return null;
        }
    }

    /*在触发fling时找到targetSnapPosition*/
    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
//        Log.e("CardSnapHelper", "velocityX:" + velocityX);
        return -velocityX;
    }
}
