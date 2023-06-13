package com.melvinhou.kami.view.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.melvinhou.kami.R;
import com.melvinhou.kami.lucas.CallBack;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.StringUtils;
import com.melvinhou.kami.view.activities.BaseActivity;
import com.melvinhou.kami.view.dialog.DialogCheckBuilder;

import java.util.List;
import java.util.Map;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
        mRootView = inflater.inflate(layoutId, container, false);
        //初始化
        initFragment();
        return mRootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //启动器
        startActivity = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), this::onActivityBack);
        //权限申请
        startPermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), this::onPermissionResult);
        startPermissions = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), this::onPermissionResult);
        //文件打开
        openFile = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(), this::onFileResult);
        openFiles = registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(), this::onFileResult);
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
    protected void initFragment() {
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
        if (barLayout instanceof ConstraintLayout) {
            barLayout.setPadding(0, DimenUtils.getStatusBarHeight(), 0, 0);
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


    public void showCheckView(CharSequence title, @NonNull CharSequence message,
                              CharSequence positiveStr, CharSequence negativeStr,
                              CallBack<Boolean> callBack) {
        BaseActivity activity = getAct();
        if (activity != null)
            activity.showCheckView(title, message, positiveStr, negativeStr, callBack);

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


//***********************************权限*********************************************//

    //新版本的权限申请
    private ActivityResultLauncher<String> startPermission;
    private ActivityResultLauncher<String[]> startPermissions;

    /**
     * 权限请求
     *
     * @param permission
     */
    protected void requestPermission(String permission) {
        startPermission.launch(permission);
    }

    /**
     * 多权限请求
     *
     * @param permissions
     */
    protected void requestPermissions(String[] permissions) {
        startPermissions.launch(permissions);
    }

    /**
     * 处理权限请求结果
     */
    protected void onPermissionResult(boolean result) {
        if (result) onPermissionGranted();
        else onPermissionCancel(null);
    }

    /**
     * 处理权限请求结果
     */
    protected void onPermissionResult(Map<String, Boolean> result) {
        //权限判断
        for (String permission : result.keySet()) {
            if (Boolean.FALSE.equals(result.get(permission))) {
                onPermissionCancel(permission);
                return;
            }
        }
        onPermissionGranted();

    }

    /**
     * 获取权限回调
     */
    protected void onPermissionGranted() {
        Log.i(getClass().getName(), "权限授予成功");
    }

    /**
     * 获取权限回调
     *
     * @param permission
     */
    protected void onPermissionCancel(String permission) {
        if (permission != null)
            Log.w(getClass().getName(), "权限[" + permission + "]授予失败");
        else
            Log.w(getClass().getName(), "权限授予失败");
    }


    /**
     * 权限验证判断
     *
     * @param permissions
     * @return
     */
    protected boolean checkPermission(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
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
        toResultActivity(new Intent(requireContext(), clazz));
    }

    public <T extends Activity> void toResultActivity(Class<T> clazz, Bundle bundle) {
        Intent intent = new Intent(requireContext(), clazz);
        if (bundle != null)
            intent.putExtras(bundle);
        toResultActivity(intent);
    }

    public <T extends Activity> void toResultActivity(Class<T> clazz, ActivityResultCallback<ActivityResult> callback) {
        toResultActivity(clazz, null, callback);
    }

    public <T extends Activity> void toResultActivity(Class<T> clazz, Bundle bundle, ActivityResultCallback<ActivityResult> callback) {
        Intent intent = new Intent(requireContext(), clazz);
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
        if (result.getResultCode() == Activity.RESULT_OK) {
        }
    }

    public <T extends Activity> void toActivity(Class<T> clazz) {
        toActivity(clazz, null);
    }

    public <T extends Activity> void toActivity(Class<T> clazz, Bundle bundle) {
        Intent intent = new Intent(requireContext(), clazz);
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
