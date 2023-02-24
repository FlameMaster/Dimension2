package com.melvinhou.kami.mvvm;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.melvinhou.kami.R;
import com.melvinhou.kami.databinding.ViewLoadingBD;
import com.melvinhou.kami.net.RequestState;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.view.fragments.BaseFragment2;

import androidx.annotation.Nullable;
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
 * = 时 间：2017/5/24 14:17
 * <p>
 * = 分 类 说 明：最基础的fragment
 * ================================================
 */
public abstract class DataBindingFragment<DB extends ViewDataBinding> extends BaseFragment2 {

    /*视图模型*/
    private DB mBinding;
    private ViewLoadingBD mLoadingBD;

    /*获取绑定器*/
    public DB getViewDataBinding() {
        return mBinding;
    }

    @Override
    public View getRootView() {
        return getViewDataBinding().getRoot();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        //初始化布局模型
        int layoutId = getLayoutID();
//      DataBindingUtil.setContentView(getActivity(), layoutId);//这样会引发异常，只适合在activity中调用
        mBinding = DataBindingUtil.inflate(inflater, layoutId, container, false);
        mBinding.setLifecycleOwner(this);
        //初始化
        initFragment();
        return getViewDataBinding().getRoot();
    }

    @Override
    public ViewGroup getLoadingRootLayout() {
        return (ViewGroup) getViewDataBinding().getRoot();
    }

    /**
     * 显示加载布局
     *
     * @param message
     * @param isCover
     */
    public void showLoadingView(String message, boolean isCover) {
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
     * @param state
     */
    @Override
    public void changeRequestState(@RequestState int state) {
        if (mLoadingBD!=null){
            mLoadingBD.setState(state);
            mLoadingBD.imgResult.setImageDrawable(getRequestStateImage(state));
            mLoadingBD.textState.setText(getRequestStateMessage(state));
        }
    }



    @Override
    protected View initLoadingView() {
        mLoadingBD = DataBindingUtil.bind(
                View.inflate(requireContext(), R.layout.view_loading_forbinding,null));
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        mLoadingBD.getRoot().setLayoutParams(lp);
        mLoadingBD.getRoot().setTag("loading");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mLoadingBD.getRoot().setElevation(DimenUtils.dp2px(8));
        mLoadingBD.refresh.setOnClickListener(view -> refresh());
        return mLoadingBD.getRoot();
    }
}
