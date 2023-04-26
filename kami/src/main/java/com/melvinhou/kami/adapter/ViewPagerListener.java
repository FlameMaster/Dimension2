package com.melvinhou.kami.adapter;

public interface ViewPagerListener {
    void onInitComplete();
    void onPageRelease(boolean isNext,int position);
    void onPageSelected(int position,boolean isBottom, boolean isLeftScroll);
}
