package com.melvinhou.kami.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2022/8/11 0011 14:46
 * <p>
 * = 分 类 说 明：用于适配viewbing
 * ================================================
 */
public abstract class BindRecyclerAdapter<T, VB extends ViewBinding> extends RecyclerAdapter<T, BindViewHolder<VB>> {

    @Override
    public void bindData(BindViewHolder<VB> viewHolder, int position, T data) {
        bindData(viewHolder.getBinding(), position, data);
    }

    @Override
    public int getItemLayoutId(int viewType) {
        return 0;
    }

    @Override
    protected BindViewHolder<VB> onCreate(View view, int viewType) {
        return new BindViewHolder(
                getViewBinding(
                        LayoutInflater.from(view.getContext()),
                        (ViewGroup) view.getParent()
                )
        );
    }

    @Override
    public RecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return viewType != TYPE_NORMAL ? onCustomCreate(getCustomView(viewType), viewType)
                : new BindViewHolder(getViewBinding(LayoutInflater.from(parent.getContext()), parent));
    }

    protected abstract VB getViewBinding(@NonNull LayoutInflater inflater, ViewGroup parent);

    protected abstract void bindData(@NonNull VB binding, int position, @NonNull T data);
}
