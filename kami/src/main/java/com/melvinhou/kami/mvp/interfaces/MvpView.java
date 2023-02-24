package com.melvinhou.kami.mvp.interfaces;

import com.melvinhou.kami.view.interfaces.BaseView;

import androidx.lifecycle.LifecycleOwner;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/4/25 19:38
 * <p>
 * = 分 类 说 明：mvp-v
 * ================================================
 */
public interface MvpView<P extends MvpPresenter> extends BaseView {
    P getPresenter();
    LifecycleOwner getLifecycleOwner();


}
