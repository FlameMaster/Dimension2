package com.melvinhou.dimension2.pager;

import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2019/12/5 17:32
 * <p>
 * = 分 类 说 明：pager页面的横向适配器
 * ============================================================
 */
public class PagerAdapter extends androidx.viewpager.widget.PagerAdapter {

    /*页面*/
    private List<BasePager> mPagers;

    /*初始化页面合集*/
    public PagerAdapter(List<BasePager> pagers) {
        mPagers = pagers;
    }


/////////////////////////////////////////来自继承/////////////////////////////////////////////////////

    @Override
    public int getCount() {
        if (mPagers == null) return 0;
        return mPagers.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }



    /*初始化内容*/
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        //初始化条目
        OnItemCreate(position);
        container.addView(getItemView(position));
        return getItemView(position);
    }

    /*清理缓存之外的*/
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(getItemView(position));
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return getPager(position).getTitle();
    }

    /////////////////////////////////////////核心/////////////////////////////////////////////////////


    /**
     * 获取pager的布局
     * @param position
     * @return
     */
    public View getItemView(int position) {
        return getPager(position).getRootView();
    }

    /**
     * 初始化布局
     * @param position
     */
    public void OnItemCreate(int position) {
        getPager(position).onCreate(position);
    }

    /**
     * 获取对应pager
     * @param position
     * @return
     */
    public BasePager getPager(int position) {
        return mPagers.get(position);
    }

}
