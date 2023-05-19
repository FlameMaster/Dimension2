package com.melvinhou.kami.mvp;

import android.app.Application;

import com.melvinhou.kami.mvp.interfaces.MvpModel;
import com.melvinhou.kami.mvp.interfaces.MvpPresenter;
import com.melvinhou.kami.mvvm.BaseViewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2019/11/28 10:32
 * <p>
 * = 分 类 说 明：实现mvp-m中需要实现的方法
 * ============================================================
 */
public abstract class BaseModel<P extends MvpPresenter> extends BaseViewModel implements MvpModel<P> {

    /*mvp-p*/
    private P mPresenter;

    public BaseModel(@NonNull Application application) {
        super(application);
    }

    public P getPresenter() {
        return mPresenter;
    }

    public void setPresenter(P presenter) {
        mPresenter = presenter;
        bindData();
    }


    /**
     * 数据绑定
     */
    protected void bindData() {
    }

    /**
     * 加载网络数据数据
     */
    public void loadNetWorkData(){

    }
}
