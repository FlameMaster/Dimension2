package com.melvinhou.kami.view;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.melvinhou.kami.BaseApplication;
import com.melvinhou.kami.R;
import com.melvinhou.kami.util.PermissionUtil;
import com.melvinhou.kami.wiget.LoadDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
 * = 分 类 说 明：activity的基础类
 * ================================================
 */
public abstract class BaseActivity extends AppCompatActivity {

    //请求回调
    private PermissionUtil.PermissionGrant permissionGrant;
    //工具栏
    private Toolbar mToolbar;
    //加载弹窗
    private LoadDialog mLoadDialog;


    public Toolbar getToolbar() {
        return mToolbar;
    }


//***********************************生命周期*********************************************//


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseApplication.getInstance().putActivity(this);
        //初始化布局模型
        int layoutId = getLayoutID();
        //初始化
        initActivity(layoutId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BaseApplication.getInstance().removeActivity(this);
        hideProcess();
    }


//**********************************弹窗**********************************************//

    /**
     * 显示进度条
     *
     * @param message
     */
    public void showProcess(String message) {
        if (mLoadDialog == null) {
            int themeId = getLoadDialogThemeID();
            if (themeId > 0) mLoadDialog = new LoadDialog(this, themeId);
            else mLoadDialog = new LoadDialog(this);
        }
        mLoadDialog.show(message);
    }

    /**
     * 隐藏进度条
     */
    public void hideProcess() {
        if (mLoadDialog != null) {
            mLoadDialog.hide();
        }
    }

    /**
     * 进度条弹窗的样式
     *
     * @return
     */
    protected int getLoadDialogThemeID() {
        return -1;
    }

//**********************************初始化相关**********************************************//


    /**
     * 获取布局id
     *
     * @return
     */
    protected abstract int getLayoutID();

    /**
     * 初始化
     */
    protected void initActivity(int layoutId) {
        initWindowUI();
        setContentView(layoutId);
        //工具栏
        initToolBar();
        //初始化显示
        initView();
        //初始化监听
        initListener();
        //初始化数据
        initData();
    }

    /**
     * 初始化状态栏和导航栏
     */
    protected void initWindowUI() {
        //透明工具条
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {// Android 5.0 以上 全透明

//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
//                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            /**
             *控制状态栏和导航栏的显示：
             *SYSTEM_UI_FLAG_VISIBLE：默认状态栏，呼出虚拟导航栏会自动resize
             *INVISIBLE：隐藏状态栏，同时Activity会伸展全屏显示
             *SYSTEM_UI_FLAG_FULLSCREEN：隐藏状态栏，API >= 16不可单独使用，否则部分手机顶部有白条
             *SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN：半透明状态栏，API >= 16
             *SYSTEM_UI_FLAG_HIDE_NAVIGATION：隐藏虚拟导航栏，API >= 16 自动resize,触摸屏幕会自动显示虚拟导航栏
             *SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION：半透明虚拟导航栏，会自动设置半透明状态栏，API >= 16
             *SYSTEM_UI_FLAG_IMMERSIVE：自动隐藏状态栏和虚拟导航栏，并且在bar出现的位置滑动可以呼出bar，API >= 19
             *SYSTEM_UI_FLAG_IMMERSIVE_STIKY：和上面不同的是，呼出的bar会自动再隐藏掉，API >= 19
             * SYSTEM_UI_FLAG_LAYOUT_STABLE：保持整个View稳定，使View不会因为System UI的变化而重新layout，API >= 16没发现作用
             */
            //设置状态栏文字颜色及图标为深色  View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |  View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            //设置状态栏文字颜色及图标为浅色  View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |  View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
                            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            // 状态栏（以上几行代码必须，参考setStatusBarColor|setNavigationBarColor方法源码）
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            // 虚拟导航键
            getWindow().setNavigationBarColor(Color.WHITE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {// Android 4.4 以上 半透明
            // 状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 虚拟导航键
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            //4.4版本颜色无法设置，只能通过view显示
        }

//            tintManager = new SystemBarTintManager(this);
//            //设置图片状态栏
//            tintManager.setStatusBarTintResource(R.drawable.gradient_statusbar_bg);
//            tintManager.setStatusBarTintEnabled(true);
    }

    /**
     * 初始化工具栏
     */
    protected void initToolBar() {
        //工具条
        mToolbar = findViewById(R.id.bar);
        if (mToolbar != null) {
            //初始bar
            setSupportActionBar(mToolbar);

            // 给左上角图标的左边加上一个返回的图标，ActionBar.DISPLAY_HOME_AS_UP
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            //使左上角图标可点击，对应id为android.R.id.home，ActionBar.DISPLAY_SHOW_HOME
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            // 使自定义的普通View能在title栏显示，即actionBar.setCustomView能起作用,ActionBar.DISPLAY_SHOW_CUSTOM
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            // Toolbar自有的Title,ActionBar.DISPLAY_SHOW_TITLE
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    /**
     * 初始化空间
     */
    protected abstract void initView();

    /**
     * 初始化监听
     */
    protected abstract void initListener();

    /**
     * 初始化数据
     */
    protected abstract void initData();


//***********************************回调*********************************************//


    /**
     * 处理权限请求结果
     *
     * @param requestCode  请求权限时传入的请求码，用于区别是哪一次请求的
     * @param permissions  所请求的所有权限的数组
     * @param grantResults 权限授予结果，和 permissions 数组参数中的权限一一对应，元素值为两种情况，如下:
     *                     授予: PackageManager.PERMISSION_GRANTED
     *                     拒绝: PackageManager.PERMISSION_DENIED
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.e(getClass().getName(), "权限授予成功");
            if (permissionGrant != null)
                permissionGrant.onPermissionGranted(requestCode);
        } else {
            if (permissionGrant != null)
                permissionGrant.onPermissionCancel(requestCode);
        }
    }

    /**
     * 权限申请成功的回调
     *
     * @param permissionGrant
     */
    public void setPermissionGrant(PermissionUtil.PermissionGrant permissionGrant) {
        this.permissionGrant = permissionGrant;
    }

}
