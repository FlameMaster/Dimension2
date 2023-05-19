package com.melvinhou.kami.mvvm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.melvinhou.kami.view.fragments.BaseFragment2;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
 * = 时 间：2022/8/11 0011 15:25
 * <p>
 * = 分 类 说 明：mvvm的基类
 * ================================================
 */
public abstract class BindFragment<VB extends ViewBinding, M extends BaseViewModel> extends BaseFragment2 {

    //新版本的意图打开
    private ActivityResultLauncher<Intent> startActivity;

    protected VB mBinding;
    protected M mModel;

    @Override
    protected int getLayoutID() {
        return 0;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mBinding = openViewBinding(inflater, container);
        mModel = new ViewModelProvider(getActivity()).get(openModelClazz());
        //初始化
        initFragment();
        return mBinding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        startActivity =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        BindFragment.this::onActivityBack);
        super.onAttach(context);
    }

    @Override
    public View getRootView() {
        return mBinding.getRoot();
    }

    @Override
    public ViewGroup getLoadingRootLayout() {
        return (ViewGroup) mBinding.getRoot();
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {

    }



    protected abstract VB openViewBinding(LayoutInflater inflater, ViewGroup container);

    protected abstract Class<M> openModelClazz();

    /**
     * 打开有返回值的intent
     *
     * @param intent
     */
    protected void toResultActivity(Intent intent) {
        startActivity.launch(intent);
    }

    /**
     * 替换早期的返回
     */
    protected void onActivityBack(ActivityResult result) {
        //此处进行数据接收（接收回调）
        if (result.getResultCode() == Activity.RESULT_OK) {
        }
    }
}
