package com.melvinhou.kami.tool;

import android.content.Context;
import android.widget.Scroller;

import java.lang.reflect.Field;

import androidx.viewpager.widget.ViewPager;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2017/8/28 18:24
 * <p>
 * = 分 类 说 明：viewpager的自定义滑动速度类
 * ================================================
 */

public class ViewPagerScroller extends Scroller {
    private int mScrollDuration = 2000; // 滑动速度

    /*设置速度速度*/
    public void setScrollDuration(int duration) {
        this.mScrollDuration = duration;
    }

    public ViewPagerScroller(Context context) {
        super(context);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, mScrollDuration);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        super.startScroll(startX, startY, dx, dy, mScrollDuration);
    }

    /*设置给viewpager*/
    public void initViewPagerScroll(ViewPager viewPager) {
        try {
            Field mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            mScroller.set(viewPager, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}