package com.melvinhou.kami.view;

import android.content.Intent;

import com.melvinhou.kami.manager.DialogCheckBuilder;
import com.melvinhou.kami.net.EmptyState;

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
 * = 分 类 说 明：mvp-v
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
     * @param isCover 是否覆盖页面
     */
    void showLoadingView(boolean isCover);
    void showLoadingView();

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


    /**
     * 显示一个检查弹窗
     *
     * @param builder 显示的参数
     */
    void showCheckView(DialogCheckBuilder builder);

    /**
     * 隐藏检查弹窗
     */
    void hideCheckView();

////////////////////////////////通用点击事件//////////////////////////////////////////////

    void nullClick();

    /**
     * 返回
     *
     */
    void back();

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
