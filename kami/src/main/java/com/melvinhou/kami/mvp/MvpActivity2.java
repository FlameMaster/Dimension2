package com.melvinhou.kami.mvp;

import com.melvinhou.kami.mvp.interfaces.MvpPresenter;
import com.melvinhou.kami.mvp.interfaces.MvpView;
import com.melvinhou.kami.view.activities.BaseActivity2;

import androidx.lifecycle.LifecycleOwner;
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
 * = 时 间：2020/6/30 20:24
 * <p>
 * = 分 类 说 明：加个vb
 * ================================================
 */
public abstract class MvpActivity2<VB extends ViewBinding, P extends MvpPresenter> extends MvpActivity<P>{


    private VB mBinding;

    public VB getBinding() {
        return mBinding;
    }

    @Override
    public ViewModelProvider getViewModelProvider() {
        return new ViewModelProvider(this);
    }

    @Override
    protected void initActivity(int layoutId) {
        mBinding = openViewBinding();
        setContentView(mBinding.getRoot());
        super.initActivity(layoutId);
    }

    protected abstract VB openViewBinding();

    @Override
    protected int getLayoutID() {
        return 0;
    }
}
