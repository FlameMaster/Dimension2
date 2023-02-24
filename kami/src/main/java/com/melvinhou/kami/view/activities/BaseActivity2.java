package com.melvinhou.kami.view.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.melvinhou.kami.R;
import com.melvinhou.kami.net.RequestState;
import com.melvinhou.kami.net.ResultState;
import com.melvinhou.kami.view.interfaces.BaseView;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.ResourcesUtils;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/6/30 18:04
 * <p>
 * = 分 类 说 明：activity的进阶基础类
 * ================================================
 */
public abstract class BaseActivity2 extends BaseActivity implements BaseView {
    //是否在加载中
    private boolean isShowLoading = false;
    //加载布局
    private View mLoadingView;

    public boolean isShowLoading() {
        return isShowLoading;
    }

    public void setShowLoading(boolean showLoading) {
        isShowLoading = showLoading;
    }

//***********************************生命周期*********************************************//


    @Override
    protected void onStart() {
        super.onStart();
        onLoading();
    }

    /**
     * 开始加载
     */
    protected void onLoading() {
//        showLoadingView(true);
//        changeRequestState(EmptyState.PROGRESS,"加载中...");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideLoadingView();
    }


//**********************************弹窗**********************************************//

    /**
     * 获取加载控件的父控件
     *
     * @return
     */
    protected ViewGroup getLoadingRootLayout() {
        return (ViewGroup) getWindow().getDecorView().getRootView();
    }

    /**
     * 获取加载布局
     *
     * @return
     */
    protected View initLoadingView() {
        View view = View.inflate(this, R.layout.view_loading, null);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        view.setLayoutParams(lp);
        view.setTag("loading");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            view.setElevation(DimenUtils.dp2px(8));
        view.setOnClickListener(v -> emptyClick());
        return view;
    }

    /**
     * 显示加载布局
     *
     * @param isCover
     */
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

    /**
     * 隐藏加载布局
     */
    @Override
    public void hideLoadingView() {
        if (isShowLoading) {
            getLoadingRootLayout().removeView(mLoadingView);
            mLoadingView = null;
            isShowLoading = false;
        }
    }


    /**
     * 改变加载布局状态
     *
     * @param state
     */
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

//******************************操作**********************************************//

    /**
     * 空点击
     */
    @Override
    public void emptyClick() {

    }

    /**
     * 返回
     */
    @Override
    public void backward() {
        onActivityBack(-1);
    }

    /**
     * 关闭页面
     */
    @Override
    public void close() {
        finish();
    }

    /**
     * 提交
     */
    @Override
    public void submit() {
    }

    /**
     * 刷新
     */
    @Override
    public void refresh() {
        onLoading();
    }

    /**
     * 前往新页面
     *
     * @param intent
     */
    @Override
    public void toActivity(Intent intent) {
        startActivity(intent);
    }


    public void toActivity(View view, Intent intent) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Pair<View, String> p = new Pair<>(view, view.getTransitionName());
            ActivityOptions activityOptions =
                    ActivityOptions.makeSceneTransitionAnimation(this, p);
            startActivityForResult(intent, 0, activityOptions.toBundle());
//            ActivityOptions options = ActivityOptions.makeScaleUpAnimation(view, 0,
//                    0, view.getWidth(), view.getHeight());
//            startActivity(intent, options.toBundle());
        } else
            startActivityForResult(intent, 0);
    }
}
