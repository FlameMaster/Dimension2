package com.melvinhou.kami.mvp;

import com.melvinhou.kami.view.activities.BaseActivity2;
import com.melvinhou.kami.mvp.interfaces.MvpPresenter;
import com.melvinhou.kami.mvp.interfaces.MvpView;

import androidx.lifecycle.LifecycleOwner;

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
 * = 分 类 说 明：mvp页面顶层
 * ================================================
 */
public abstract class MvpActivity<P extends MvpPresenter> extends BaseActivity2 implements MvpView<P> {


    /*mvp-p*/
    private P mPresenter;


    public P getPresenter() {
        return mPresenter;
    }

    @Override
    public LifecycleOwner getLifecycleOwner() {
        return this;
    }

    @Override
    protected void initActivity(int layoutId) {
        mPresenter = upPresenter();
        //使用新组件，监听activity生命周期
        getLifecycle().addObserver(getPresenter().getLifecycleObserver());
        super.initActivity(layoutId);
    }

    /**
     * 初始化p
     * @return
     */
    protected abstract P upPresenter();




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
    public void refresh() {
    }
}
