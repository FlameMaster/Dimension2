package com.melvinhou.kami.adapter;

import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p/>
 * = 版 权 所 有：7416064@qq.com
 * <p/>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p/>
 * = 时 间：2016/4/22 15:24
 * <p/>
 * = 分 类 说 明：配合recyclerview使用的viewholder
 * ================================================
 */
public class BindViewHolder<VB extends ViewBinding> extends RecyclerHolder {

    /**/
    private VB binding;

    public BindViewHolder(VB binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    /**获取保存的binding*/
    public VB getBinding() {
        return binding;
    }


    /*获取layoutid*/
    public int getLayoutId(){
        if (binding!=null)
            return binding.getRoot().getId();
        else
            return -1;
    }
}
