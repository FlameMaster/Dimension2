package com.melvinhou.kami.view.wiget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2019/1/4 17:40
 * <p>
 * = 分 类 说 明：禁止滑动的viewpager,添加scrollview嵌套高度适配
 * ============================================================
 */
public class NoSlideViewPager extends ViewPager {
    private int current;
    private int height = 0;
    private int lastX,lastY;

    /*是否可以进行滑动*/
    private boolean isSlide = false;
    /*是否自动测量高度*/
    private boolean isMeasureHeight = false;

    public void setSlide(boolean slide) {
        isSlide = slide;
    }

    public void setMeasureHeight(boolean measureHeight) {
        isMeasureHeight = measureHeight;
    }

    public NoSlideViewPager(@NonNull Context context) {
        this(context, null);
    }

    public NoSlideViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!isSlide) return false;
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isSlide) return false;
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isMeasureHeight) {
            if (getChildCount() > current) {
                View child = getChildAt(current);
                child.measure(widthMeasureSpec,
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                height = child.getMeasuredHeight();
            }

            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);

    }

    public void resetHeight(int current) {
        this.current = current;
        if (getChildCount() > current) {
            ViewGroup.LayoutParams layoutParams =  getLayoutParams();
            if (layoutParams == null) {
                layoutParams = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, height);
            } else {
                layoutParams.height = height;
            }
            setLayoutParams(layoutParams);
        }
    }

}
