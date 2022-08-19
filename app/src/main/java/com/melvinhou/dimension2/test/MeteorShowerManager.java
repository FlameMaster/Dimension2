package com.melvinhou.dimension2.test;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.melvinhou.kami.util.DimenUtils;

import java.util.Random;

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
 * = 分 类 说 明：给胡老师写的更高级
 * ================================================
 */
public class MeteorShowerManager extends RecyclerView.LayoutManager {

    //最多同屏显示
    public final int MAX_COUNT = 12;
    public final int  ITEM_SCROLL_HEIGHT =1000;

    //总偏移量,负数
    private int verticalScrolloffset = 0;

    private double[] ratioSet;
    private int[][] rightLocation;
    //随机距离
    private float[] randomDistance;

    private int itemSize;


    MeteorShowerManager() {
        itemSize = DimenUtils.dp2px(50);
        //平分角度
        double degreeUnit = 360d / MAX_COUNT;
        ratioSet = new double[MAX_COUNT];
        Random random = new Random();
        //添加随机角度
        for (int i = 0; i < MAX_COUNT; i++) {
            ratioSet[i] = i * degreeUnit;
        }
        for (int i = 0; i < ratioSet.length; i++) {
            int index = random.nextInt(ratioSet.length);
            double temp = ratioSet[index];
            ratioSet[index] = ratioSet[i];
            ratioSet[i] = temp;
        }
        randomDistance =new float[]{1f,0.8f,0.3f,1,0.5f};

    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.getItemCount() == 0 || state.isPreLayout()) return;
        removeAndRecycleAllViews(recycler);
        rightLocation = new int[ratioSet.length][2];
        //半径确认
        int radius = Math.min(getWidth(), getHeight());
        //限制半径
        int endRadius = (radius-itemSize) / 2;
        for (int i = 0; i < ratioSet.length; i++) {
            int[] parameters = fcc(ratioSet[i]);
            //角度
            double degree = parameters[0];
            //斜边计算xy
            double cosX = Math.cos(Math.toRadians(degree));
            double cosY = Math.cos(Math.toRadians(90 - degree));
            int vectorX = parameters[1], vectorY = parameters[2];//修正方向
            int endX = (int) (endRadius * cosX * vectorX);
            int endY = (int) (endRadius * cosY * vectorY);
            rightLocation[i] = new int[]{endX, endY};
            Log.e("滑动" + i, "endX=" + endX + "\r\tendY=" + endY + "\r\tdegree=" + degree);
        }
        layout(recycler);
    }

    protected void layout(RecyclerView.Recycler recycler) {
        detachAndScrapAttachedViews(recycler);
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        for (int i = 0; i < getItemCount(); i++) {
            int childTop = i * ITEM_SCROLL_HEIGHT;
            int currentY = verticalScrolloffset - childTop;
            View child = recycler.getViewForPosition(i);
            if (currentY < 0 || currentY >= ITEM_SCROLL_HEIGHT * MAX_COUNT) {
                removeAndRecycleView(child, recycler);
            } else {
                addView(child);
                measureChildWithMargins(child, 0, 0);
                int width = getDecoratedMeasuredWidth(child);
                int height = getDecoratedMeasuredHeight(child);

                int left = centerX - width / 2;
                int top = centerY - height / 2;
                layoutDecorated(child, left, top, left+width, top+height);

                float offsetRatio = (float) currentY / (float) (ITEM_SCROLL_HEIGHT * MAX_COUNT);
                float tRatio = offsetRatio / 0.6f;
                float aStartRatio = offsetRatio / 0.2f;
                float aEndRatio = 1f - (offsetRatio - 0.8f) / 0.2f;
                if (tRatio > 1f) tRatio = 1f;
                if (aStartRatio > 1f) aStartRatio = 1f;
                if (aEndRatio > 1f) aEndRatio = 1f;


//                int left = (int) (centerX + rightLocation[i % MAX_COUNT][0] * tRatio - width / 2);
//                int top = (int) (centerY + rightLocation[i % MAX_COUNT][1] * tRatio - height / 2);
//                layoutDecorated(child, left, top, left + width, top + height);
                child.setAlpha(offsetRatio > 0.5f ? aEndRatio : aStartRatio);
                int position = i % MAX_COUNT;
                child.setTranslationX(rightLocation[position][0] * tRatio * randomDistance[i%randomDistance.length]);
                child.setTranslationY(rightLocation[position][1] * tRatio * randomDistance[i%randomDistance.length]);
                child.setScaleX(tRatio);
                child.setScaleY(tRatio);
            }
        }
    }

    private int[] fcc(double degree) {
        int[] parameters = new int[3];

        //方向修正
        int vectorX = 0, vectorY = 0;
        if (degree > 0 && degree < 180) vectorY = 1;
        else if (degree > 180 && degree < 360) vectorY = -1;
        int temDegree = (int) ((degree + 90) % 360);
        if (temDegree > 0 && temDegree < 180) vectorX = 1;
        else if (temDegree > 180 && temDegree < 360) vectorX = -1;

        //角度修正，用于直角计算xy
        double rightDegree = degree;
        if (degree > 270) {
            rightDegree = 360 - degree;
        } else if (degree > 180) {
            rightDegree = degree - 180;
        } else if (degree > 90) {
            rightDegree = 180 - degree;
        }
        parameters[0] = (int) rightDegree;
        parameters[1] = vectorX;
        parameters[2] = vectorY;
        return parameters;
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
        }
        verticalScrolloffset += travel;
        layout(recycler);
        return travel;
    }
}
