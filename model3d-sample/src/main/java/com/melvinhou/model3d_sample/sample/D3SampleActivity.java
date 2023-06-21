package com.melvinhou.model3d_sample.sample;

import android.app.Dialog;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.melvinhou.kami.io.IOUtils;
import com.melvinhou.kami.lucas.CallBack;
import com.melvinhou.kami.mvvm.BindActivity;
import com.melvinhou.kami.tool.UITools;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.ResourcesUtils;
import com.melvinhou.kami.view.activities.BaseActivity;
import com.melvinhou.knight.KUITools;
import com.melvinhou.model3d_sample.R;
import com.melvinhou.model3d_sample.databinding.ActivityD3SampleBinding;
import com.melvinhou.model3d_sample.databinding.ActivityD3SampleListBinding;
import com.melvinhou.model3d_sample.model.D3DomeObj;
import com.melvinhou.model3d_sample.wiget.D3ShowSurfaceView;
import com.melvinhou.opengllibrary.d3.D3Config;
import com.melvinhou.rxjava.rxbus.RxBus;
import com.melvinhou.rxjava.rxbus.RxBusClient;
import com.melvinhou.rxjava.rxbus.RxBusMessage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.CompositeDisposable;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/7/24 14:27
 * <p>
 * = 分 类 说 明：opengl学习
 * ================================================
 */
public class D3SampleActivity extends BindActivity<ActivityD3SampleBinding, D3SampleModel> {
    @Override
    protected ActivityD3SampleBinding openViewBinding() {
        return ActivityD3SampleBinding.inflate(getLayoutInflater());
    }

    @Override
    protected Class<D3SampleModel> openModelClazz() {
        return D3SampleModel.class;
    }


    private final String TAG = D3SampleActivity.class.getName();

    //渲染器初始化判断
    private boolean rendererSet = false;
    private D3SampleRenderer mRenderer;//渲染器
    private D3Config mConfig;//渲染器参数

    private String mObjPath, mObjName;

    @Override
    protected void initWindowUI() {
//        super.initWindowUI();
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |//粘性沉浸模式
                        View.SYSTEM_UI_FLAG_IMMERSIVE |//沉浸模式
                        //两行全屏
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        //以下防止布局随着系统栏的隐藏和显示调整大小
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        //刘海屏适配
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lp.layoutInDisplayCutoutMode
                    = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().setAttributes(lp);
    }

    @Override
    protected int getLoadDialogThemeID() {
        return R.style.NonBackgroundDialog;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initListener() {
        mBinding.back.setOnClickListener(v -> finish());
        mBinding.setting.setOnClickListener(v -> {
            List<String> list = new ArrayList<>();
            list.add("视线方向X");
            list.add("视线方向Y");
            list.add("视线方向Z");
            list.add("视角坐标X");
            list.add("视角坐标Y");
            list.add("视角坐标Z");
            list.add("目标坐标X");
            list.add("目标坐标Y");
            list.add("目标坐标Z");
            list.add("光源坐标X");
            list.add("光源坐标Y");
            list.add("光源坐标Z");
            list.add("环境光强度");
            list.add("镜面光强度");
            list.add("反射光强度");
            list.add("光源效率");
            KUITools.INSTANCE.showListSelectDialog02(this, "调整参数", list, new Function1<Integer, Unit>() {
                @Override
                public Unit invoke(Integer index) {
                    if (index < list.size()) {
                        showProgressDialog(index + 1, list.get(index));
                    }
                    return null;
                }
            });
        });

    }

    @Override
    protected void initData() {
        mObjPath = getIntent().getStringExtra("objPath");
        mObjName = getIntent().getStringExtra("objName");
        // 设定好使用的OpenGL版本.
        mBinding.surfaceview.setEGLContextClientVersion(3);
        //初始化配置
        mConfig = D3Config.instance(true);
        mConfig.projection_near = 2;
        mConfig.projection_far = 100;
        mConfig.look_eye_x = 0;
        mConfig.look_eye_y = 0;
        mConfig.look_eye_z = 30;
        mConfig.look_view_x = 0;
        mConfig.look_view_y = 0;
        mConfig.look_view_z = 0;
        mConfig.look_up_x = 0;
        mConfig.look_up_y = 1;
        mConfig.look_up_z = 0;
        //材质
        mConfig.ambient = 0.2f;//环境光强度,影响明暗度
        mConfig.diffuse = 0.1f;//散射光强度，影响背光面
        mConfig.specular = 0.0f;//镜面光强度，直射光
        mConfig.specularPower = 10.0f;//高光功率
        //光源位置
        mConfig.LIGHT_DIRECTION[0] = 0f;
        mConfig.LIGHT_DIRECTION[1] = 0f;
        mConfig.LIGHT_DIRECTION[2] = 30f;

        //延迟一点
        mBinding.getRoot().post(new Runnable() {
            @Override
            public void run() {
                mRenderer = new D3SampleRenderer(D3DomeObj.class, mObjPath, mObjName);
                mRenderer.setCreatedCallBack(isSucceed -> {
                    runOnUiThread(() -> hideProcess());
                });
                mBinding.surfaceview.setD3Renderer(mRenderer);//用于支持手势
                //RENDERMODE_WHEN_DIRTY：被动渲染，设置只在requestRender时重绘
                //RENDERMODE_CONTINUOUSLY：主动渲染
                mBinding.surfaceview.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                rendererSet = true;
                mBinding.surfaceview.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rendererSet) {
            mBinding.surfaceview.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showProcess("加载模型中....");
        if (rendererSet) {
            mBinding.surfaceview.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        mRenderer.onDestroy();
        super.onDestroy();
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
        int negative = 50;//负数进度弥补
        //判断
        switch (type) {
            case 1:
                current = mConfig.look_up_x;
                break;
            case 2:
                current = mConfig.look_up_y;
                break;
            case 3:
                current = mConfig.look_up_z;
                break;
            case 4:
                current = mConfig.look_eye_x;
                break;
            case 5:
                current = mConfig.look_eye_y;
                break;
            case 6:
                current = mConfig.look_eye_z;
                break;
            case 7:
                current = mConfig.look_view_x;
                break;
            case 8:
                current = mConfig.look_view_y;
                break;
            case 9:
                current = mConfig.look_view_z;
                break;
            case 10:
                current = mConfig.LIGHT_DIRECTION[0];
                max = 200;
                negative = 100;
                break;
            case 11:
                current = mConfig.LIGHT_DIRECTION[1];
                max = 200;
                negative = 100;
                break;
            case 12:
                current = mConfig.LIGHT_DIRECTION[2];
                max = 200;
                negative = 100;
                break;
            case 13:
                current = mConfig.ambient*100;
                negative = 0;
                break;
            case 14:
                current = mConfig.specular*100;
                negative = 0;
                break;
            case 15:
                current = mConfig.diffuse*100;
                negative = 0;
                break;
            case 16:
                current = mConfig.specularPower;
                negative = 0;
                break;
        }
        //ui
        Dialog dialog = UITools.createDialog(this,
                R.layout.dialog_d3_progress, getLoadDialogThemeID(), Gravity.BOTTOM, R.style.Animation_Dialog_Bottom);
        //设置整体大小包括外部半透明
        dialog.getWindow().setBackgroundDrawable(null);
        dialog.setCanceledOnTouchOutside(false);//外部点击不消失
        SeekBar progress = dialog.findViewById(R.id.progress);
        TextView tvTitle = dialog.findViewById(R.id.tv_title);
        TextView tvNow = dialog.findViewById(R.id.tv_now);
        TextView tvMin = dialog.findViewById(R.id.tv_min);
        TextView tvMax = dialog.findViewById(R.id.tv_max);
        tvTitle.setText(title);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            progress.setMin(min);
        }
        progress.setMax(max);
        progress.setProgress((int) current + negative);
        tvMin.setText(String.valueOf(min - negative));
        tvMax.setText(String.valueOf(max - negative));
        tvNow.setText("当前：" + current);
        dialog.findViewById(R.id.back).setOnClickListener(v -> {
            dialog.dismiss();
        });
        //滑动监听
        int finalNegative = negative;
        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                progress -= finalNegative;
                switch (type) {
                    case 1:
                        mConfig.look_up_x = progress;
                        break;
                    case 2:
                        mConfig.look_up_y = progress;
                        break;
                    case 3:
                        mConfig.look_up_z = progress;
                        break;
                    case 4:
                        mConfig.look_eye_x = progress;
                        break;
                    case 5:
                        mConfig.look_eye_y = progress;
                        break;
                    case 6:
                        mConfig.look_eye_z = progress;
                        break;
                    case 7:
                        mConfig.look_view_x = progress;
                        break;
                    case 8:
                        mConfig.look_view_y = progress;
                        break;
                    case 9:
                        mConfig.look_view_z = progress;
                        break;
                    case 10:
                        mConfig.LIGHT_DIRECTION[0] = progress;
                        break;
                    case 11:
                        mConfig.LIGHT_DIRECTION[1] = progress;
                        break;
                    case 12:
                        mConfig.LIGHT_DIRECTION[2] = progress;
                        break;
                    case 13:
                        mConfig.ambient = progress / 100f;
                        break;
                    case 14:
                        mConfig.specular = progress / 100f;
                        break;
                    case 15:
                        mConfig.diffuse = progress / 100f;
                        break;
                    case 16:
                        mConfig.specularPower = progress;
                        break;
                }
                tvNow.setText("当前：" + progress);
                mBinding.surfaceview.requestRender();
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
