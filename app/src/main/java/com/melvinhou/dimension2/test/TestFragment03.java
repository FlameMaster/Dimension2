package com.melvinhou.dimension2.test;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.melvinhou.dimension2.databinding.FragmentTest03Binding;
import com.melvinhou.kami.mvvm.BaseModel;
import com.melvinhou.kami.mvvm.BindFragment;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/7/16 2:39
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class TestFragment03 extends BindFragment<FragmentTest03Binding, BaseModel> {

    @Override
    protected FragmentTest03Binding openViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentTest03Binding.inflate(getLayoutInflater());
    }

    @Override
    protected Class<BaseModel> openModelClazz() {
        return BaseModel.class;
    }


    @Override
    protected void initView() {

    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {

    }


}
