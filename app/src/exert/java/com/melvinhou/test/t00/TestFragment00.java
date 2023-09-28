package com.melvinhou.test.t00;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.melvinhou.dimension2.databinding.FragmentTest00Binding;
import com.melvinhou.kami.mvvm.BaseViewModel;
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
 * = 分 类 说 明：一些小控件
 * ================================================
 */
public class TestFragment00 extends BindFragment<FragmentTest00Binding, BaseViewModel> {

    @Override
    protected FragmentTest00Binding openViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentTest00Binding.inflate(getLayoutInflater());
    }

    @Override
    protected Class<BaseViewModel> openModelClazz() {
        return BaseViewModel.class;
    }

    @Override
    protected void initView() {
        mBinding.barRoot.title.setText("实验室");
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {
    }


}
