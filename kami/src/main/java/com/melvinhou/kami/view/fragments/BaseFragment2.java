package com.melvinhou.kami.view.fragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.melvinhou.kami.R;
import com.melvinhou.kami.view.activities.BaseActivity2;
import com.melvinhou.kami.net.RequestState;
import com.melvinhou.kami.net.ResultState;
import com.melvinhou.kami.view.interfaces.BaseView;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.ResourcesUtils;

import androidx.appcompat.widget.Toolbar;
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

    private Toolbar mToolbar;
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
                backward();
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
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
        view.setOnClickListener(v -> emptyClick());
        return view;
    }

    @Override
    public void showLoadingView(boolean isShade) {
        if (!isShowLoading) {
            mLoadingView = initLoadingView();
            getLoadingRootLayout().addView(mLoadingView);
            isShowLoading = true;
        }

        //判断是否覆盖界面
        if (isShade) {
            mLoadingView.setBackgroundColor(Color.WHITE);
            mLoadingView.findViewById(R.id.text_state).setVisibility(View.VISIBLE);
        } else {
            mLoadingView.setBackgroundColor(Color.TRANSPARENT);
            mLoadingView.findViewById(R.id.text_state).setVisibility(View.GONE);
        }
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
    public void changeRequestState(@RequestState int state) {
        //文字图片
        TextView tvMsg = mLoadingView.findViewById(R.id.text_state);
        tvMsg.setText(getRequestStateMessage(state));
        ImageView ivResult = mLoadingView.findViewById(R.id.img_result);
        ivResult.setImageDrawable(getRequestStateImage(state));
        ivResult.setVisibility(View.GONE);
        //进度/刷新
        mLoadingView.findViewById(R.id.progress).setVisibility(
                state == RequestState.RUNNING ? View.VISIBLE : View.GONE);
        View btReload = mLoadingView.findViewById(R.id.refresh);
        boolean isReLoad = state == ResultState.NETWORK_ERROR;
        btReload.setVisibility(isReLoad ? View.VISIBLE : View.GONE);
        if (isReLoad)
            btReload.setOnClickListener(v -> refresh());
    }

    //加载文字
    protected String getRequestStateMessage(@RequestState int state) {
        String text = null;
        switch (state) {
            case RequestState.EMPTY:
                break;
            case RequestState.READY:
                break;
            case RequestState.RUNNING:
                text = ResourcesUtils.getString(R.string.request_runing);
                break;
            case ResultState.SUCCESS:
                text = ResourcesUtils.getString(R.string.request_result_success);
                break;
            case ResultState.FAILED:
            case ResultState.CONVERT_ERROR:
                text = ResourcesUtils.getString(R.string.request_result_failed);
                break;
            case ResultState.RELOGIN:
                text = ResourcesUtils.getString(R.string.request_result_relogin);
                break;
            case ResultState.NETWORK_ERROR:
                text = ResourcesUtils.getString(R.string.request_result_network_error);
                break;
        }
        return text;
    }

    //加载图片
    protected Drawable getRequestStateImage(@RequestState int state) {
        Drawable drawable = null;
        switch (state) {
            case RequestState.EMPTY:
                break;
            case RequestState.READY:
                break;
            case RequestState.RUNNING:
                break;
            case ResultState.SUCCESS:
                drawable = ResourcesUtils.getDrawable(0);
                break;
            case ResultState.FAILED:
            case ResultState.CONVERT_ERROR:
                drawable = ResourcesUtils.getDrawable(0);
                break;
            case ResultState.RELOGIN:
                drawable = ResourcesUtils.getDrawable(0);
                break;
            case ResultState.NETWORK_ERROR:
                drawable = ResourcesUtils.getDrawable(0);
                break;
        }
        return drawable;
    }


//*************************************操作************************************************//

    @Override
    public void emptyClick() {
        BaseActivity2 activity = getAct();
        if (activity != null)
            activity.emptyClick();
    }

    @Override
    public void backward() {

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
            startActivityForResult(intent, 0, activityOptions.toBundle());
        } else
            startActivityForResult(intent, 0);
    }
}
