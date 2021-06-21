package com.melvinhou.kami.mvp;

import com.melvinhou.kami.net.EmptyState;

import androidx.lifecycle.LifecycleObserver;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2018/12/8 14:37
 * <p>
 * = 分 类 说 明：mvp-p
 * ============================================================
 */
public interface MvpPresenter<V extends MvpView, M extends MvpModel> {
    V getView();
    M getModel();

    /**
     * 用于监听Activity的生命周期
     * @return
     */
    LifecycleObserver getLifecycleObserver();

    /**
     * 开始加载
     * @param message
     */
    void startLoading(String message);

    /**
     * 结束加载
     * @param code
     * @param message
     */
    void endLoading(@EmptyState int code,String message);
}
