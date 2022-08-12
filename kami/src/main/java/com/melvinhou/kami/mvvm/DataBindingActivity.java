package com.melvinhou.kami.mvvm;

import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import com.melvinhou.kami.R;
import com.melvinhou.kami.databinding.ViewLoadingBD;
import com.melvinhou.kami.model.StateModel;
import com.melvinhou.kami.net.EmptyState;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.view.BaseActivity2;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2017/5/23 19:09
 * <p>
 * = 分 类 说 明：最基础的activity
 * ================================================
 */
public abstract class DataBindingActivity<DB extends ViewDataBinding> extends BaseActivity2 {

    /*视图模型*/
    private DB mBinding;
    private ViewLoadingBD mLoadingBD;

    /*获取绑定器*/
    public DB getViewDataBinding() {
        return mBinding;
    }


    @Override
    protected void initActivity(int layoutId) {
        initWindowUI();
        mBinding = DataBindingUtil.setContentView(this, layoutId);
        mBinding.setLifecycleOwner(this);
        //工具栏
        initToolBar();
        //初始化显示
        initView();
        //初始化监听
        initListener();
        //初始化数据
        initData();
    }

    @Override
    public ViewGroup getLoadingRootLayout() {
        return (ViewGroup) getViewDataBinding().getRoot();
    }

    @Override
    protected View initLoadingView() {

        mLoadingBD = DataBindingUtil.bind(
                View.inflate(DataBindingActivity.this,
                        R.layout.view_loading_forbinding, null));

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        mLoadingBD.getRoot().setLayoutParams(lp);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mLoadingBD.getRoot().setElevation(DimenUtils.dp2px(8));
        return mLoadingBD.getRoot();
    }

    /**
     * 显示加载布局
     *
     * @param isCover
     */
    @Override
    public void showLoadingView(boolean isCover) {
        if (!isShowLoading()) {
            getLoadingRootLayout().addView(initLoadingView());
            setShowLoading(true);
        }

        //判断是否覆盖界面
        if (isCover) {
            mLoadingBD.getRoot().setBackgroundColor(Color.WHITE);
        } else {
            mLoadingBD.getRoot().setBackgroundColor(Color.TRANSPARENT);
        }
    }

    /**
     * 隐藏加载布局
     */
    @Override
    public void hideLoadingView() {
        if (isShowLoading()) {
            getLoadingRootLayout().removeView(mLoadingBD.getRoot());
            mLoadingBD = null;
            setShowLoading(false);
        }
    }


    /**
     * 改变加载布局状态
     *
     * @param code
     * @param message
     */
    @Override
    public void changeLoadingState(@EmptyState int code, String message) {
        if (mLoadingBD != null) {
            mLoadingBD.setState(new StateModel(code).setUserText(message));
        }
    }

    /**
     * mvvm特供版
     *
     * @param view
     */
    public void nullClick(View view) {
        nullClick();
    }


    /**
     * mvvm特供版
     *
     * @param view
     */
    public void back(View view) {
        back();
    }


    /**
     * mvvm特供版
     *
     * @param view
     */
    public void submit(View view) {
        submit();
    }

    /**
     * mvvm特供版
     *
     * @param view
     */
    public void refresh(View view) {
        refresh();
    }
}
