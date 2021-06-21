package com.melvinhou.kami.mvp;

import android.content.Intent;
import android.view.View;

import com.melvinhou.kami.net.EmptyState;

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
public interface MvpView<P extends MvpPresenter> {
    P getPresenter();
    LifecycleOwner getLifecycleOwner();

    /**
     *
     * @param intent
     */
    void startActivity(Intent intent);

    /**
     * 关闭
     */
    void close();



    /**
     * 显示加载条
     *
     * @param isBlock 是否遮挡页面
     */
    void showLoadingView(boolean isBlock);

    /**
     * 隐藏加载条
     */
    void hideLoadingView();

    /**
     * 切换加载状态
     *
     * @param code 状态码
     * @param mesage 信息
     */
    void changeLoadingState(@EmptyState int code, String mesage);





////////////////////////////////通用点击事件//////////////////////////////////////////////

    void nullClick(View view);

    /**
     * 返回
     *
     * @param view
     */
    void back(View view);

    /**
     * 刷新
     *
     * @param view
     */
    void refresh(View view);

    /**
     * 提交
     *
     * @param view
     */
    void submit(View view);
}
