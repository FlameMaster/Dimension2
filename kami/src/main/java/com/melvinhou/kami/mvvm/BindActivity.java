package com.melvinhou.kami.mvvm;

import com.melvinhou.kami.view.activities.BaseActivity2;

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
public abstract class BindActivity<VB extends ViewBinding, M extends BaseViewModel> extends BaseActivity2 {

    protected VB mBinding;
    protected M mModel;

    @Override
    protected int getLayoutID() {
        return 0;
    }

    @Override
    protected void initActivity(int layoutId) {
        mBinding = openViewBinding();
        setContentView(mBinding.getRoot());
        mModel = new ViewModelProvider(this).get(openModelClazz());
        super.initActivity(layoutId);
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


    protected abstract VB openViewBinding();

    protected abstract Class<M> openModelClazz();
}
