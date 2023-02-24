package com.melvinhou.kami.view.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.melvinhou.kami.R;
import com.melvinhou.kami.view.activities.BaseActivity2;
import com.melvinhou.kami.view.dialog.DialogCheckBuilder;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

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
public abstract class BaseFragment extends Fragment {

    //根布局
    private View mRootView;

    /**
     * 获取布局id
     */
    protected abstract int getLayoutID();

    public View getRootView() {
        return mRootView;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        //初始化布局模型
        int layoutId = getLayoutID();
        mRootView =inflater.inflate(layoutId, container, false);
        //初始化
        initFragment();
        return mRootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    /**
     * 初始化
     */
    protected  void initFragment(){
        //初始化主键
        initActionBar();
        initView();
        initListener();
        initData();
    }

    /**
     * 初始化工具栏
     * 一般是act不带工具栏，由fgt携带
     */
    protected void initActionBar() {
        Toolbar toolbar = getRootView().findViewById(R.id.bar);
        if (toolbar != null && getActivity() instanceof AppCompatActivity) {
            setHasOptionsMenu(true);
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            //初始bar
//            activity.setSupportActionBar(toolbar);
            // 给左上角图标的左边加上一个返回的图标，ActionBar.DISPLAY_HOME_AS_UP
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            //使左上角图标可点击，对应id为android.R.id.home，ActionBar.DISPLAY_SHOW_HOME
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
            // 使自定义的普通View能在title栏显示，即actionBar.setCustomView能起作用,ActionBar.DISPLAY_SHOW_CUSTOM
            activity.getSupportActionBar().setDisplayShowCustomEnabled(true);
            // Toolbar自有的Title,ActionBar.DISPLAY_SHOW_TITLE
            activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }


    /**
     * 获取对应类型的act
     *
     * @return
     */
    protected BaseActivity2 getAct() {
        Activity activity = getActivity();
        if (activity instanceof BaseActivity2)
            return (BaseActivity2) activity;
        return null;
    }

    protected abstract void initView();

    protected abstract void initListener();

    protected abstract void initData();


//*******************弹窗*******************************//

    protected void showCheckView(DialogCheckBuilder builder) {
        BaseActivity2 activity = getAct();
        if (activity != null)
            activity.showCheckView(builder);
    }

    protected void hideCheckView() {
        BaseActivity2 activity = getAct();
        if (activity != null)
            activity.hideCheckView();
    }

    public void showProcess(String message) {

        BaseActivity2 activity = getAct();
        if (activity != null)
            activity.showProcess(message);
    }

    public void hideProcess() {
        BaseActivity2 activity = getAct();
        if (activity != null)
            activity.hideProcess();
    }


}
