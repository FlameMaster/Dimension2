package com.melvinhou.kami.view.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.melvinhou.kami.R;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.view.activities.BaseActivity;
import com.melvinhou.kami.view.dialog.DialogCheckBuilder;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
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

    private Toolbar mToolbar;
    //工具栏菜单
    private Menu mMenu;
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
        mToolbar = getRootView().findViewById(R.id.bar);
        if (mToolbar != null) {
//            mToolbar.setTitle("标题");
//            mToolbar.inflateMenu(R.menu.menu_add);
//            setTitleCenter(mToolbar);
//            mToolbar.setNavigationIcon(R.drawable.ic_bar_back);
            if (upBarMenuID() > 0) {
                mToolbar.inflateMenu(upBarMenuID());
                mMenu = mToolbar.getMenu();
                if (mMenu != null) {
                    initMenu(mMenu);
                    for (int i = 0; i < mMenu.size(); i++) {
                        mMenu.getItem(i).setOnMenuItemClickListener(item -> onOptionsItemSelected(item));
                    }
                }
            }
            mToolbar.setNavigationOnClickListener(v -> backward());
            mToolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);
        }
        //状态栏高度
        View barLayout = getRootView().findViewById(R.id.bar_root);
        if (barLayout instanceof ConstraintLayout){
            barLayout.setPadding(0, DimenUtils.getStatusBarHeight(),0,0);
        }
    }

    /**
     * toolbar的菜单
     *
     * @return 菜单栏资源id
     */
    protected int upBarMenuID() {
        return -1;
    }

    protected void initMenu(Menu menu) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                backward();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * 获取对应类型的act
     *
     * @return
     */
    protected BaseActivity getAct() {
        Activity activity = getActivity();
        if (activity instanceof BaseActivity)
            return (BaseActivity) activity;
        return null;
    }

    protected abstract void initView();

    protected abstract void initListener();

    protected abstract void initData();


//*******************弹窗*******************************//

    protected void showCheckView(DialogCheckBuilder builder) {
        BaseActivity activity = getAct();
        if (activity != null)
            activity.showCheckView(builder);
    }

    protected void hideCheckView() {
        BaseActivity activity = getAct();
        if (activity != null)
            activity.hideCheckView();
    }

    public void showProcess(String message) {

        BaseActivity activity = getAct();
        if (activity != null)
            activity.showProcess(message);
    }

    public void hideProcess() {
        BaseActivity activity = getAct();
        if (activity != null)
            activity.hideProcess();
    }


    public void backward() {
        requireActivity().finish();
    }
}
