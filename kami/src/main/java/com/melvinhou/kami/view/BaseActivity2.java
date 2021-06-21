package com.melvinhou.kami.view;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.melvinhou.kami.R;
import com.melvinhou.kami.manager.DialogCheckBuilder;
import com.melvinhou.kami.net.EmptyState;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.StringUtils;

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
public abstract class BaseActivity2 extends BaseActivity  implements BaseView{
    //工具栏菜单
    private Menu mMenu;
    //检查弹窗
    private Dialog mCheckDialog;
    //是否在加载中
    private boolean isShowLoading = false;
    //加载布局
    private View mLoadingView;

    public Menu getMenu() {
        return mMenu;
    }

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
//        changeLoadingState(EmptyState.PROGRESS,"加载中...");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideCheckView();
        hideLoadingView();
    }


//**********************************弹窗**********************************************//


    /**
     * 显示校验弹窗
     *
     * @param builder
     */
    @Override
    public void showCheckView(final DialogCheckBuilder builder) {
        if (builder == null) return;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this)
                .setMessage(Html.fromHtml("<font color='#000000'>" + builder.getExplainText() + "</font>"));

        //标题
        if (StringUtils.noNull(builder.getTitleText()))
            dialogBuilder.setTitle(builder.getTitleText());

        //积极按钮
        String confirmText = "确定";
        if (StringUtils.noNull(builder.getConfirmText())) confirmText = builder.getConfirmText();
        dialogBuilder.setPositiveButton(confirmText, (dialog, which) -> builder.confirm());

        //消极按钮
        if (StringUtils.noNull(builder.getCancelText()))
            dialogBuilder.setNegativeButton(builder.getCancelText(), (dialog, which) -> builder.cancel());

        //显示
        mCheckDialog = dialogBuilder.show();
    }

    /**
     * 隐藏校验弹窗
     */
    @Override
    public void hideCheckView() {
        if (mCheckDialog != null) mCheckDialog.hide();
    }

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
        view.setOnClickListener(v -> nullClick());
        return view;
    }

    /**
     * 显示一个透明的空加载布局
     *
     */
    @Override
    public void showLoadingView() {
        showLoadingView(false);
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
     * @param code
     * @param message
     */
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

//***********************************页面菜单*********************************************//

    /**
     * 菜单栏
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        int barMenuID = upBarMenuID();
        if (barMenuID > 0)
            getMenuInflater().inflate(barMenuID, menu);
        initMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * toolbar的菜单
     *
     * @return 菜单栏资源id
     */
    protected int upBarMenuID() {
        return -1;
    }

    /**
     * 初始化菜单栏
     *
     * @param menu
     */
    protected void initMenu(Menu menu) {

    }

    /**
     * 菜单栏按键
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                back();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 系统按键
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            back();
            return true;
        }
        return false;
    }

//******************************操作**********************************************//

    /**
     * 空点击
     */
    @Override
    public void nullClick() {

    }

    /**
     * 返回
     */
    @Override
    public void back() {
        close();
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
            startActivityForResult(intent,0, activityOptions.toBundle());
//            ActivityOptions options = ActivityOptions.makeScaleUpAnimation(view, 0,
//                    0, view.getWidth(), view.getHeight());
//            startActivity(intent, options.toBundle());
        } else
            startActivityForResult(intent, 0);
    }
}
