package com.melvinhou.kami.view;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.melvinhou.kami.R;

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
        //初始化
        initFragment();
        mRootView =inflater.inflate(layoutId, container, false);
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
            activity.setSupportActionBar(toolbar);
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

    public AppCompatActivity getAct(){
        return (AppCompatActivity) getActivity();
    }

    protected abstract void initView();

    protected abstract void initListener();

    protected abstract void initData();

}
