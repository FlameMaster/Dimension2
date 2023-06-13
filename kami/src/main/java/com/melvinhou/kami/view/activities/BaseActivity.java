package com.melvinhou.kami.view.activities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.melvinhou.kami.BaseApplication;
import com.melvinhou.kami.R;
import com.melvinhou.kami.io.FcLog;
import com.melvinhou.kami.lucas.CallBack;
import com.melvinhou.kami.mvvm.BindActivity;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.view.dialog.DialogCheckBuilder;
import com.melvinhou.kami.util.StringUtils;
import com.melvinhou.kami.view.dialog.LoadDialog;

import java.util.List;
import java.util.Map;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import kotlin.Unit;

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

    //当前的屏幕间隔
    private Insets currentStableInsets;
    //工具栏
    private Toolbar mToolbar;
    private View mBarLayout;
    //工具栏菜单
    private Menu mMenu;
    //加载弹窗
    private LoadDialog mLoadDialog;
    //检查弹窗
    private Dialog mCheckDialog;


    public Toolbar getToolbar() {
        return mToolbar;
    }

    public Menu getMenu() {
        return mMenu;
    }


//***********************************生命周期*********************************************//


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseApplication.getInstance().putActivity(this);
        //初始化布局模型
        int layoutId = getLayoutID();
        //启动器
        startActivity = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), this::onActivityBack);
        //文件打开
        openFile = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(), this::onFileResult);
        openFiles = registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(), this::onFileResult);
        //初始化
        initActivity(layoutId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BaseApplication.getInstance().removeActivity(this);
        hideProcess();
        hideCheckView();
    }


//**********************************弹窗**********************************************//

    /**
     * 显示进度条
     *
     * @param message
     */
    public void showProcess(String message) {
        if (mLoadDialog == null) {
            mLoadDialog = new LoadDialog(this, getLoadDialogThemeID());
            //点击返回键的处理
            mLoadDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                        onBackward(1);
                        return true;
                    }
                    return false;
                }
            });
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
        return R.style.KamiDialog;
    }


    /**
     * 显示校验弹窗
     *
     * @param builder
     */
    public void showCheckView(final DialogCheckBuilder builder) {
        if (builder == null) return;

        AlertDialog.Builder dialogBuilder = new AlertDialog
                .Builder(this)
                .setMessage(builder.getExplainText());

        //标题
        if (StringUtils.nonEmpty(builder.getTitleText()))
            dialogBuilder.setTitle(builder.getTitleText());

        //积极按钮
        String confirmText = "确定";
        if (StringUtils.nonEmpty(builder.getConfirmText())) confirmText = builder.getConfirmText();
        dialogBuilder.setPositiveButton(confirmText, (dialog, which) -> builder.confirm());

        //消极按钮
        if (StringUtils.nonEmpty(builder.getCancelText()))
            dialogBuilder.setNegativeButton(builder.getCancelText(), (dialog, which) -> builder.cancel());

        //显示
        mCheckDialog = dialogBuilder.show();
    }

    public void showCheckView(CharSequence title, @NonNull CharSequence message,
                              CharSequence positiveStr, CharSequence negativeStr,
                              CallBack<Boolean> callBack) {

        AlertDialog.Builder dialogBuilder = new AlertDialog
                .Builder(this)
                .setMessage(message);
        if (StringUtils.nonEmpty(title))
            dialogBuilder.setTitle(title);
        if (StringUtils.nonEmpty(positiveStr))
            dialogBuilder.setPositiveButton(positiveStr, (dialog, which) -> {
                if (callBack != null) callBack.callback(true);
            });
        if (StringUtils.nonEmpty(negativeStr))
            dialogBuilder.setNegativeButton(negativeStr, (dialog, which) -> {
                if (callBack != null) callBack.callback(false);
            });
        //显示
        mCheckDialog = dialogBuilder.show();
        mCheckDialog.setCancelable(false);
        mCheckDialog.setCanceledOnTouchOutside(false);
    }

    /**
     * 隐藏校验弹窗
     */
    public void hideCheckView() {
        if (mCheckDialog != null) mCheckDialog.hide();
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
        if (layoutId > 0) setContentView(layoutId);
        initWindowUI();
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
        //向后倾斜
        //向后倾斜模式适用于用户不会与屏幕进行大量互动的全屏体验，例如在观看视频时。
        //当用户希望调出系统栏时，只需点按屏幕上的任意位置即可。
//        decorView.setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        //沉浸模式
        //沉浸模式适用于用户将与屏幕进行大量互动的应用。示例包括游戏、查看图库中的图片或者阅读分页内容，如图书或演示文稿中的幻灯片。
        //当用户需要调出系统栏时，他们可从隐藏系统栏的任一边滑动。
        //要求使用这种这种意图更强的手势是为了确保用户与您应用的互动不会因意外轻触和滑动而中断。
//        decorView.setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_IMMERSIVE
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        //粘性沉浸模式
        //在普通的沉浸模式中，只要用户从边缘滑动，系统就会负责显示系统栏，您的应用甚至不会知道发生了该手势。
        //因此，如果用户实际上可能是出于主要的应用体验而需要从屏幕边缘滑动，例如在玩需要大量滑动的游戏或使用绘图应用时，您应改为启用“粘性”沉浸模式。
        //在粘性沉浸模式下，如果用户从隐藏了系统栏的边缘滑动，系统栏会显示出来，但它们是半透明的，并且轻触手势会传递给应用，因此应用也会响应该手势。
//        decorView.setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION );
        //透明工具条
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {// Android 5.0 以上 全透明
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
//                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            /*
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                getWindow().setNavigationBarDividerColor(Color.TRANSPARENT);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {// Android 4.4 以上 半透明
            // 状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 虚拟导航键
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            //4.4版本颜色无法设置，只能通过view显示
        }

        //监听布局距离变化
        ViewCompat.setOnApplyWindowInsetsListener(getWindow().getDecorView(), (view, insets) -> {
            Insets stableInsets = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() |//状态栏
                            WindowInsetsCompat.Type.displayCutout() |//刘海屏
                            WindowInsetsCompat.Type.ime()//软键盘
            );
            if (!stableInsets.equals(currentStableInsets)) {
                currentStableInsets = stableInsets;
                onWindowInsetsChange(stableInsets);
            }
            return insets;
        });
    }

    /**
     * 界面布局的边距
     *
     * @param insets
     */
    protected void onWindowInsetsChange(Insets insets) {
        if (mBarLayout != null)
            mBarLayout.setPadding(0, insets.top, 0, 0);
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
        //状态栏高度
        View barLayout = findViewById(R.id.bar_root);
        if (barLayout instanceof ConstraintLayout) {
            mBarLayout = barLayout;
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

    /**
     * 返回
     *
     * @param type
     */
    protected void onBackward(int type) {
        finish();
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
        if (item.getItemId() == android.R.id.home) {
            onBackward(2);
            return true;
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
            onBackward(1);
            return true;
        }
        return false;
    }


//***********************************权限*********************************************//

    //权限请求
    public static final int REQUEST_CODE_PERMISSIONS = 14101;

    /**
     * @param permissions
     */
    protected void requestPermissions(String[] permissions) {
        //请求授予此应用程序的权限
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSIONS);
    }

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
        //权限判断
        if (checkPermission(grantResults)) onPermissionGranted();
        else onPermissionCancel();

    }

    /**
     * 获取权限回调
     */
    protected void onPermissionGranted() {
        Log.i(getClass().getName(), "权限授予成功");
    }

    /**
     * 获取权限回调
     */
    protected void onPermissionCancel() {
        Log.w(getClass().getName(), "权限授予失败");
    }


    /**
     * 权限验证判断
     *
     * @param grantResults
     * @return
     */
    private boolean checkPermission(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    /**
     * 权限验证判断
     *
     * @param permissions
     * @return
     */
    protected boolean checkPermission(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


//***********************************启动*********************************************//

    //新版本的意图打开
    private ActivityResultLauncher<Intent> startActivity;
    private ActivityResultCallback<ActivityResult> activityResultCallback;

    /**
     * 打开有返回值的intent
     *
     * @param intent
     */
    protected void toResultActivity(Intent intent, ActivityResultCallback<ActivityResult> callback) {
        activityResultCallback = callback;
        toResultActivity(intent);
    }

    /**
     * 打开有返回值的intent
     *
     * @param intent
     */
    protected void toResultActivity(Intent intent) {
        //判断重复
        long time = System.currentTimeMillis();
        if (time - lastLaunchTime < 500) {
            return;
        }
        startActivity.launch(intent);
    }

    public <T extends Activity> void toResultActivity(Class<T> clazz) {
        toResultActivity(new Intent(this, clazz));
    }

    public <T extends Activity> void toResultActivity(Class<T> clazz, Bundle bundle) {
        Intent intent = new Intent(this, clazz);
        if (bundle != null)
            intent.putExtras(bundle);
        toResultActivity(intent);
    }

    public <T extends Activity> void toResultActivity(Class<T> clazz, ActivityResultCallback<ActivityResult> callback) {
        toResultActivity(clazz, null, callback);
    }

    public <T extends Activity> void toResultActivity(Class<T> clazz, Bundle bundle, ActivityResultCallback<ActivityResult> callback) {
        Intent intent = new Intent(this, clazz);
        if (bundle != null)
            intent.putExtras(bundle);
        toResultActivity(intent, callback);
    }


    /**
     * 替换早期的返回
     */
    protected void onActivityBack(ActivityResult result) {
        //自由处理
        if (activityResultCallback != null) {
            activityResultCallback.onActivityResult(result);
            activityResultCallback = null;
            return;
        }
        //此处进行数据接收（接收回调）
        if (result.getResultCode() == RESULT_OK) {
        }
    }

    public <T extends Activity> void toActivity(Class<T> clazz) {
        toActivity(clazz, null);
    }

    public <T extends Activity> void toActivity(Class<T> clazz, Bundle bundle) {
        Intent intent = new Intent(this, clazz);
        if (bundle != null)
            intent.putExtras(bundle);
        toActivity(intent);
    }

    //上次启动时间
    private long lastLaunchTime = 0;

    public void toActivity(Intent intent) {
        //判断重复
        long time = System.currentTimeMillis();
        if (time - lastLaunchTime < 500) {
            return;
        }
        lastLaunchTime = time;
        startActivity(intent);
    }


//***********************************文件打开*********************************************//

    //新版本的文件选择
    private ActivityResultLauncher<String[]> openFile;
    private ActivityResultLauncher<String[]> openFiles;
    private ActivityResultCallback<Uri> openFileCallback;
    private ActivityResultCallback<List<Uri>> openFilesCallback;


    /**
     * 选择文件
     *
     * @param types
     */
    protected void openFile(String[] types, ActivityResultCallback<Uri> callback) {
        openFileCallback = callback;
        openFile.launch(types);
    }

    /**
     * 选择多个文件
     *
     * @param types
     */
    protected void openFiles(String[] types, ActivityResultCallback<List<Uri>> callback) {
        openFilesCallback = callback;
        openFiles.launch(types);
    }

    /**
     * 文件选择返回
     *
     * @param uri
     */
    protected void onFileResult(Uri uri) {
        if (openFileCallback != null) {
            openFileCallback.onActivityResult(uri);
            openFileCallback = null;
            return;
        }
        Log.w("文件选择", uri.toString());
    }

    /**
     * 文件选择返回
     *
     * @param uris
     */
    protected void onFileResult(List<Uri> uris) {
        if (openFilesCallback != null) {
            openFilesCallback.onActivityResult(uris);
            openFilesCallback = null;
            return;
        }
        StringBuilder buffer = new StringBuilder();
        for (Uri uri : uris) {
            buffer.append(uri.toString()).append(",");
        }
        Log.w("文件选择", buffer.toString());
    }

}
