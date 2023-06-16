package com.melvinhou.ar_sample.sample;

import android.Manifest;
import android.app.Dialog;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.melvinhou.ar_sample.DisplayRotationHelper;
import com.melvinhou.ar_sample.R;
import com.melvinhou.ar_sample.TapHelper;
import com.melvinhou.ar_sample.databinding.ActivityArSampleBinding;
import com.melvinhou.kami.mvvm.BindActivity;
import com.melvinhou.kami.tool.UITools;
import com.melvinhou.opengllibrary.d3.D3Config;

import androidx.core.graphics.Insets;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/5/25 0025 9:35
 * <p>
 * = 分 类 说 明：ar演示
 * ================================================
 */
public class ArSampleActivity extends BindActivity<ActivityArSampleBinding, ArSampleModel> {
    @Override
    protected ActivityArSampleBinding openViewBinding() {
        return ActivityArSampleBinding.inflate(getLayoutInflater());
    }

    @Override
    protected Class<ArSampleModel> openModelClazz() {
        return ArSampleModel.class;
    }


    private static final String TAG = ArSampleActivity.class.getSimpleName();
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};

    private boolean installRequested;//判断是否安装ArCore
    private ArSampleRenderer mRenderer;//渲染器
    private Session mSession;
    private DisplayRotationHelper mDisplayRotationHelper;//显示旋转助手
    private TapHelper mTapHelper;//手势助手-将主线程的手势存储
    private D3Config mConfig;//配置
    private boolean rendererSet = false;


    @Override
    protected void initView() {
        mBinding.barRoot.title.setText("AR演示");
    }

    @Override
    protected void onWindowInsetsChange(Insets insets) {
        super.onWindowInsetsChange(insets);
        mBinding.getRoot().setPadding(0,0,0,insets.bottom);
    }

    @Override
    protected int getLoadDialogThemeID() {
        return R.style.NonBackgroundDialog;
    }

    @Override
    protected int upBarMenuID() {
        return R.menu.bar_ar_sample;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_zoom) {
            showProgressDialog(1, "缩放等级");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void initListener() {
        mConfig = D3Config.instance(true);
        mDisplayRotationHelper = new DisplayRotationHelper(this);
        mTapHelper = new TapHelper(this);
        mBinding.container.setOnTouchListener(mTapHelper);
    }

    @Override
    protected void initData() {
        //可能需要查询网络资源来确定设备是否支持 ARCore。 在此期间，它将返回 UNKNOWN_CHECKING
//        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(this);
        try {
            switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                case INSTALL_REQUESTED:
                    installRequested = true;
                    return;
                case INSTALLED:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessage(true, "ArCore加载失败");
            return;
        }
        //ARCore需要相机权限才能运行。这里获取相机权限
        if (!checkPermission(REQUIRED_PERMISSIONS)) {
            requestPermissions(REQUIRED_PERMISSIONS);
            return;
        }

        mConfig.scaleFactor = 0.1f;//模型大小
        //材质
        mConfig.ambient = 0.3f;
        mConfig.diffuse = 1.0f;
        mConfig.specular = 1.0f;
        mConfig.specularPower = 6.0f;
        //加载模型
        String objPath = getIntent().getStringExtra("objPath");
        String objName = getIntent().getStringExtra("objName");
        if (TextUtils.isEmpty(objPath)) objPath = "d3/sample";
        if (TextUtils.isEmpty(objName)) objName = "redcar.obj";
        mRenderer = new ArSampleRenderer(objPath, objName);
        mRenderer.setDisplayRotationHelper(mDisplayRotationHelper);
        mRenderer.setTapHelper(mTapHelper);
        // 设置渲染器
        mBinding.container.setPreserveEGLContextOnPause(true);
        mBinding.container.setEGLContextClientVersion(2);
        // Alpha used for plane blending.
        mBinding.container.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mBinding.container.setRenderer(mRenderer);
        //RENDERMODE_WHEN_DIRTY：被动渲染，设置只在requestRender时重绘，RENDERMODE_CONTINUOUSLY：主动渲染
        mBinding.container.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        mBinding.container.setVisibility(View.VISIBLE);
        installRequested = false;
//        rendererSet = true;

        //优化体验
        showProcess(null);
        mBinding.getRoot().postDelayed(new Runnable() {
            @Override
            public void run() {
                rendererSet = true;
                hideProcess();
                onResume();
            }
        }, 1000);
    }

    @Override
    protected void onPermissionGranted() {
        //权限申请成功
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!rendererSet) return;
        if (mSession == null) initArCoreSession();
        // 调用顺序很重要 - 请参阅onPause（）中的注释，在onResume()中顺序与onPause（）中的相反。
        try {
            mSession.resume();
        } catch (CameraNotAvailableException e) {
            //在某些情况下（例如另一个相机应用程序启动），相机可能会被提供给q其他应用程序。
            // 通过显示错误提示并在下一次迭代中重新创建session 来正确解决此问题。
            showMessage(true, "相机不可用,请重新启动应用程序");
            mSession = null;
            return;
        }
        mBinding.container.onResume();
        mDisplayRotationHelper.onResume();
        showMessage(false, "寻找水平面...");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!rendererSet) return;
        if (mSession != null) {
            //请注意，调用顺序，首先暂停GLSurfaceView，以便它不会尝试查询Session。
            // 如果在GLSurfaceView之前暂停Session，则GLSurfaceView仍可调用session.update（）从而可能导致抛出SessionPausedException。
            mDisplayRotationHelper.onPause();
            mBinding.container.onPause();
            mSession.pause();
        }
    }


    /**
     * 初始化ar的会话
     */
    private void initArCoreSession() {
        Exception exception = null;
        String message = null;
        try {
            switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                case INSTALL_REQUESTED:
                    installRequested = true;
                    return;
                case INSTALLED:
                    break;
            }
            // 创建 session.session类用来管理AR系统状态并处理session自己的生命周期。
            mSession = new Session(this);
            Config config = mSession.getConfig();
            if (mSession.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                config.setDepthMode(Config.DepthMode.AUTOMATIC);
            } else {
                config.setDepthMode(Config.DepthMode.DISABLED);
            }
            mSession.configure(config);
            mRenderer.setSession(mSession);
        } catch (UnavailableArcoreNotInstalledException
                 | UnavailableUserDeclinedInstallationException e) {
            message = "Please install ARCore";
            exception = e;
        } catch (UnavailableApkTooOldException e) {
            message = "Please update ARCore";
            exception = e;
        } catch (UnavailableSdkTooOldException e) {
            message = "Please update this app";
            exception = e;
        } catch (UnavailableDeviceNotCompatibleException e) {
            message = "This device does not support AR";
            exception = e;
        } catch (Exception e) {
            message = "Failed to create AR session";
            exception = e;
        }
        if (message != null) {
            showMessage(true, message);
            Log.e(TAG, "Exception creating session", exception);
            return;
        }
    }

    private void showMessage(boolean isError, String msg) {
//        FcUtils.showToast(msg);
    }


    /**
     * 参数调整进度条
     *
     * @param type
     * @param title
     */
    private void showProgressDialog(int type, String title) {
        float current = 0;
        int min = 0;
        int max = 100;
        int negative = 0;//负数进度弥补
        //判断
        switch (type) {
            case 1:
                current = mConfig.scaleFactor * max;
                break;
        }
        //ui
        Dialog dialog = UITools.createDialog(this,
                R.layout.dialog_d3_progress, getLoadDialogThemeID(), Gravity.BOTTOM, R.style.Animation_Dialog_Bottom);
        //设置整体大小包括外部半透明
        dialog.getWindow().setBackgroundDrawable(null);
        dialog.setCanceledOnTouchOutside(false);//外部点击不消失
        SeekBar progress = dialog.findViewById(com.melvinhou.model3d_sample.R.id.progress);
        TextView tvTitle = dialog.findViewById(com.melvinhou.model3d_sample.R.id.tv_title);
        TextView tvNow = dialog.findViewById(com.melvinhou.model3d_sample.R.id.tv_now);
        TextView tvMin = dialog.findViewById(com.melvinhou.model3d_sample.R.id.tv_min);
        TextView tvMax = dialog.findViewById(com.melvinhou.model3d_sample.R.id.tv_max);
        tvTitle.setText(title);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            progress.setMin(min);
        }
        progress.setMax(max);
        progress.setProgress((int) current + negative);
        tvMin.setText(String.valueOf(min - negative));
        tvMax.setText(String.valueOf(max - negative));
        tvNow.setText("当前：" + current);
        dialog.findViewById(com.melvinhou.model3d_sample.R.id.back).setOnClickListener(v -> {
            dialog.dismiss();
        });
        //滑动监听
        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                progress -= negative;
                switch (type) {
                    case 1:
                        mConfig.scaleFactor = progress / (float) max;
                        break;
                }
                tvNow.setText("当前：" + progress);
                mBinding.container.requestRender();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }
}
