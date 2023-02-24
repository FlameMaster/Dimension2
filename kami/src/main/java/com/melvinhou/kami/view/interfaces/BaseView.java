package com.melvinhou.kami.view.interfaces;

import android.content.Intent;

import com.melvinhou.kami.net.RequestState;

import androidx.lifecycle.ViewModelStoreOwner;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2019/11/27 17:28
 * <p>
 * = 分 类 说 明：统一Fragment和Activity的操作,mvp-v/mvvm-v
 * ============================================================
 */
public interface BaseView extends ViewModelStoreOwner {

    /**
     * 关闭页面
     */
    void close();


    /**
     * 跳转页面
     *
     * @param intent
     */
    void toActivity(Intent intent);


////////////////////////////////网络加载相关//////////////////////////////////////////////

    /**
     * 显示加载条
     *
     * @param isShade 是否遮挡页面
     */
    void showLoadingView(boolean isShade);

    /**
     * 隐藏加载条
     */
    void hideLoadingView();

    /**
     * 切换加载状态
     *
     * @param state 状态码
     */
    void changeRequestState(@RequestState  int state);


////////////////////////////////通用点击事件//////////////////////////////////////////////

    void emptyClick();

    /**
     * 返回
     *
     */
    void backward();

    /**
     * 刷新
     *
     */
    void refresh();

    /**
     * 提交
     *
     */
    void submit();

}
