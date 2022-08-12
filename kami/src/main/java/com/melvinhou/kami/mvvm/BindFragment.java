package com.melvinhou.kami.mvvm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.melvinhou.kami.BaseApplication;
import com.melvinhou.kami.view.BaseFragment2;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewbinding.ViewBinding;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2022/8/11 0011 15:25
 * <p>
 * = 分 类 说 明：mvvm的基类
 * ================================================
 */
public abstract class BindFragment<VB extends ViewBinding, M extends BaseModel> extends BaseFragment2 {

    protected VB mBinding;
    protected M mModel;

    @Override
    protected int getLayoutID() {
        return 0;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mBinding = openViewBinding(inflater, container);
        mModel = new ViewModelProvider(this).get(openModelClazz());
        mModel.register();
        //初始化
        initFragment();
        return mBinding.getRoot();
    }

    @Override
    public View getRootView() {
        return mBinding.getRoot();
    }

    @Override
    public ViewGroup getLoadingRootLayout() {
        return (ViewGroup) mBinding.getRoot();
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


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mModel != null)
            mModel.unRegister();
    }



    protected abstract VB openViewBinding(LayoutInflater inflater, ViewGroup container);

    protected abstract Class<M> openModelClazz();
}
