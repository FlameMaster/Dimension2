package com.melvinhou.kami.view;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.melvinhou.kami.R;
import com.melvinhou.kami.manager.DialogCheckBuilder;
import com.melvinhou.kami.net.EmptyState;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.FcUtils;

import androidx.constraintlayout.widget.ConstraintLayout;

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
 * = 分 类 说 明：进阶的fragment
 * ================================================
 */
public abstract class BaseFragment2 extends BaseFragment implements BaseView {

    /*工具栏菜单*/
    private Menu mMenu;
    /*进度条view*/
    private View mLoadingView;
    /*是否在加载中*/
    private boolean isShowLoading = false;



    public boolean isShowLoading() {
        return isShowLoading;
    }

    public void setShowLoading(boolean showLoading) {
        isShowLoading = showLoading;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideLoadingView();
    }

    /**
     * toolbar的菜单
     *
     * @return 菜单栏资源id
     */
    protected int upBarMenuID() {
        return -1;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mMenu = menu;
        int barMenuID = upBarMenuID();
        if (barMenuID > 0) {
            menu.clear();
            inflater.inflate(barMenuID, menu);
            initMenu(menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    protected void initMenu(Menu menu) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                back();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * 获取对应类型的act
     * @return
     */
    private BaseActivity2 getActivity2() {
        Activity activity = getActivity();
        if (activity instanceof BaseActivity2)
            return (BaseActivity2) activity;
        return null;
    }

//*******************弹窗*******************************//

    public ViewGroup getLoadingRootLayout() {
        return (ViewGroup) getRootView();
    }

    /**
     * 获取加载布局
     *
     * @return
     */
    protected View initLoadingView() {
        View view = View.inflate(FcUtils.getContext(), R.layout.view_loading, null);
        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        view.setLayoutParams(lp);
        view.setTag("loading");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mLoadingView.setElevation(DimenUtils.dp2px(8));
        view.setOnClickListener(v -> nullClick());
        return view;
    }

    @Override
    public void showLoadingView(boolean isCover) {
        if (!isShowLoading) {
            mLoadingView = initLoadingView();
            getLoadingRootLayout().addView(mLoadingView);
            isShowLoading = true;
        }

        //判断是否覆盖界面
        if (isCover) {
            mLoadingView.setBackgroundColor(Color.WHITE);
            mLoadingView.findViewById(R.id.text_state).setVisibility(View.VISIBLE);
        } else {
            mLoadingView.setBackgroundColor(Color.TRANSPARENT);
            mLoadingView.findViewById(R.id.text_state).setVisibility(View.GONE);
        }
    }

    @Override
    public void showLoadingView() {
        showLoadingView(false);
    }

    @Override
    public void hideLoadingView() {
        if (isShowLoading) {
            getLoadingRootLayout().removeView(mLoadingView);
            mLoadingView = null;
            isShowLoading = false;
        }
    }

    @Override
    public void changeLoadingState(@EmptyState int code, String message) {
        ((TextView) mLoadingView.findViewById(R.id.text_state)).setText(message);
        switch (code){
            case EmptyState.NET_ERROR:
                mLoadingView.findViewById(R.id.progress).setVisibility(View.GONE);
                mLoadingView.findViewById(R.id.img_err).setVisibility(View.VISIBLE);
                mLoadingView.findViewById(R.id.refresh).setVisibility(View.VISIBLE);
                mLoadingView.findViewById(R.id.refresh).setOnClickListener(v -> refresh());
                break;
            case EmptyState.PROGRESS:
                mLoadingView.findViewById(R.id.progress).setVisibility(View.VISIBLE);
                mLoadingView.findViewById(R.id.img_err).setVisibility(View.GONE);
                mLoadingView.findViewById(R.id.refresh).setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void showCheckView(DialogCheckBuilder builder) {
        BaseActivity2 activity = getActivity2();
        if (activity!=null)
            activity.showCheckView(builder);
    }

    @Override
    public void hideCheckView() {
        BaseActivity2 activity = getActivity2();
        if (activity!=null)
            activity.hideCheckView();
    }

    public void showProcess(String message) {

        BaseActivity2 activity = getActivity2();
        if (activity!=null)
            activity.showProcess(message);
    }

    public void hideProcess() {
        BaseActivity2 activity = getActivity2();
        if (activity!=null)
            activity.hideProcess();
    }

//*************************************操作************************************************//

    @Override
    public void nullClick() {
        BaseActivity2 activity = getActivity2();
        if (activity!=null)
            activity.nullClick();
    }

    @Override
    public void back() {

    }

    @Override
    public void refresh() {

    }

    @Override
    public void submit() {

    }

    @Override
    public void close() {

    }

    @Override
    public void toActivity(Intent intent) {
        getActivity().startActivity(intent);
    }


    public void toActivity(View view, Intent intent) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Pair<View, String> p = new Pair<>(view, view.getTransitionName());
            ActivityOptions activityOptions =
                    ActivityOptions.makeSceneTransitionAnimation(getActivity(), p);
            startActivityForResult(intent,0, activityOptions.toBundle());
        } else
            startActivityForResult(intent, 0);
    }
}
