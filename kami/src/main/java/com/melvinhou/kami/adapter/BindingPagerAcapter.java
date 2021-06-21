package com.melvinhou.kami.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.melvinhou.kami.util.FcUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.core.util.Pools;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.viewpager.widget.PagerAdapter;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2017/9/12 12:38
 * <p>
 * = 分 类 说 明：vierpager的适配器基类
 * ================================================
 */

public abstract class BindingPagerAcapter<VB extends ViewDataBinding, D> extends PagerAdapter {
    private List<D> mList;
    //自己造一个池，可以提高加载效率，与复用率，
    private Pools.Pool<View> pool;
    /**/
    private VB binding;
    /*条目点击事件*/
    private OnPagerClickListener mListener;

    /*设置条目点击事件*/
    public void setOnPagerClickListener(OnPagerClickListener li) {
        mListener = li;
    }

    public VB getBinding() {
        return binding;
    }

    public List<D> getmList() {
        return mList;
    }

    public BindingPagerAcapter(List<D> list, int poolLength) {
        mList = list;
        pool = new Pools.SimplePool<>(poolLength);
    }

    /*更新数据*/
    public void upDataAll(List<D> list) {
        if (list == null) return;
        if (mList == null)
            mList = new ArrayList<>();
        mList.clear();
        mList.addAll(list);
    }

    public boolean upData(int position, D d) {
        if (d == null) return false;
        if (mList == null)
            mList = new ArrayList<>();
        if (position > mList.size()) return false;
        mList.add(position, d);
        return true;
    }

    /*复用条目的view*/
    public View getChildView(ViewGroup container) {
        View view;
        int layout_id = getLayoutId();
        if (layout_id > 0)
            //加载布局文件
            view = DataBindingUtil.inflate(
                    LayoutInflater.from(FcUtils.getContext()),
                    layout_id,
                    container, false).getRoot();
        else view = getChildView();

        return view;
    }

    /*复用条目的id*/
    public abstract int getLayoutId();

    public View getChildView() {
        return null;
    }

    public abstract int getVariableId();

    @Override
    public abstract int getCount();

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        //获取view加入管理
        View view = pool.acquire();
        if (view == null) view = getChildView(container);

        //添加监听，初始化数据，添加到父类中
        if (view != null) {
            binding = DataBindingUtil.bind(view);
            if (getmList() != null) {
                position = position % getmList().size();
                if (getmList().get(position) != null) {
                    binding.setVariable(getVariableId(), getmList().get(position));
                    if (mListener != null) {
                        final int finalPosition = position;
                        view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mListener.onPagerClick(v, finalPosition, mList.get(finalPosition));
                            }
                        });
                    }
                }
                initDataBinding(binding, getmList().get(position));
            }
            container.addView(view);
        }
        return view;
    }

    protected abstract void initDataBinding(VB binding, D data);

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = (View) object;
        container.removeView(view);
        pool.release(view);
    }


    /*条目点击事件接口*/
    public interface OnPagerClickListener<T> {
        void onPagerClick(View view, int position, T data);
    }
}
