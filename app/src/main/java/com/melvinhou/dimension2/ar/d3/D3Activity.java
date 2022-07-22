package com.melvinhou.dimension2.ar.d3;

import android.app.Dialog;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.ar.d3.model.D3ObjGroup;
import com.melvinhou.dimension2.function.im.ImHomeActivity;
import com.melvinhou.dimension2.net.HttpConstant;
import com.melvinhou.kami.model.EventMessage;
import com.melvinhou.kami.util.DimenUtils;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.IOUtils;
import com.melvinhou.kami.util.ResourcesUtils;
import com.melvinhou.kami.view.BaseActivity;
import com.melvinhou.rxjava.RxBus;
import com.melvinhou.rxjava.RxBusClient;
import com.melvinhou.rxjava.RxMsgParameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import androidx.lifecycle.ViewModelProvider;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.CompositeDisposable;

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
public class D3Activity extends BaseActivity {

    private boolean rendererSet = false;

    private D3SurfaceView mSurfaceView;//显示器
    private D3Renderer mRenderer;//渲染器
    private D3ObjGroup mObjGroup;//模型对象
    private D3Config mConfig;//渲染器参数
    private View mProcess;
    private TextView mTextProcess;

    //数据存储
    private D3Model mModel;
    //管理进程的
    private final CompositeDisposable mDisposable = new CompositeDisposable();

    //RxBus的接收器
    private RxBusClient mRxBusClient;

    private String mObjPath, mObjName;

    @Override
    protected void initWindowUI() {
        super.initWindowUI();

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    @Override
    protected int getLayoutID() {
        return R.layout.activity_d3;
    }

    @Override
    protected void initView() {
        mModel = new ViewModelProvider(this).get(D3Model.class);

        mSurfaceView = findViewById(R.id.surfaceview);
        mProcess = findViewById(R.id.ll_process);
        mTextProcess = findViewById(R.id.tv_progress);

    }

    @Override
    protected void initListener() {
        findViewById(R.id.back).setOnClickListener(v -> finish());
        findViewById(R.id.setting).setOnClickListener(v -> showDialog());

    }

    @Override
    protected void initData() {
        // 设定好使用的OpenGL版本.
        mSurfaceView.setEGLContextClientVersion(3);
        mConfig = new D3Config(0, 2, 100,
                0, 0, 20,
                0, 0, 0);
        mModel.obj.observe(this, obj -> {
            mRenderer = new D3Renderer(obj, mObjPath, mConfig);
            mSurfaceView.setD3Renderer(mRenderer);
            //RENDERMODE_WHEN_DIRTY：被动渲染，设置只在requestRender时重绘
            //RENDERMODE_CONTINUOUSLY：主动渲染
            mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            rendererSet = true;
            mSurfaceView.setVisibility(View.VISIBLE);
        });

        mObjPath = "ar/models";
        mObjName = "redcar.obj";
//        mObjPath = getModelFilesDir().getAbsolutePath() + File.separator + "keqing";
//        mObjName = "keqing.obj";
        mObjPath = getIntent().getStringExtra("objPath");
        mObjName = getIntent().getStringExtra("objName");
        loadModel(mObjPath, mObjName);
    }

    private void loadModel(String objPath, String objName) {
        mDisposable.add(Observable
                .create((ObservableOnSubscribe<Obj>) emitter -> {
                    // 加载模型文件
                    try {
                        Obj obj = ObjReader.read(getFile(objPath, objName));
                        obj = ObjUtils.convertToRenderable(obj);
                        emitter.onNext(obj);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        emitter.onComplete();
                    }
                })
                .compose(IOUtils.setThread())
                .subscribe(obj -> {
                    mModel.obj.postValue(obj);
                }));
    }


    /**
     * 加载文件
     *
     * @param path
     * @param fileName
     * @return
     * @throws IOException
     */
    private InputStream getFile(String path, String fileName) throws IOException {
        String filePath = path + File.separator + fileName;
        if (!path.contains(ResourcesUtils.getString(R.string.app_name))) {
            return getAssets().open(filePath);
        }
        Uri uri = Uri.fromFile(new File(filePath));
        return FcUtils.getContext().getContentResolver().openInputStream(uri);
    }

    public File getModelFilesDir() {
        File folderFile = new File(Environment.getExternalStorageDirectory().getPath()
                + File.separator + ResourcesUtils.getString(R.string.app_name) + File.separator + "model");
        if (!folderFile.exists()) {
            folderFile.mkdirs();
        }
        return folderFile;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rendererSet) {
            mSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showTextProcess("加载模型中....");
        if (rendererSet) {
            mSurfaceView.onResume();
        }
    }

    private void showTextProcess(String text) {
        if (mProcess.getVisibility() != View.VISIBLE)
            mProcess.setVisibility(View.VISIBLE);
        mTextProcess.setText(text);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindRxBus();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        mDisposable.clear();
        if (mRxBusClient != null) {
            mRxBusClient.unregister();
            mRxBusClient = null;
        }
        super.onDestroy();
    }


    @Override
    protected int getLoadDialogThemeID() {
        return R.style.Dimension2Dialog;
    }

    /**
     * 注册绑定rxbus
     */
    private void bindRxBus() {
        mRxBusClient = new RxBusClient(getClass().getName()) {
            @Override
            protected void onEvent(int type, String message, Object data) {
                D3Activity.this.onEvent(type, message, data);
            }
        };
        //告诉别人我这里初始化了
        RxBus.get().post(new EventMessage(getClass().getName()
                + RxMsgParameters.ACTIVITY_LAUNCHED));
    }


    /**
     * 事件处理
     *
     * @param type    类型
     * @param message 信息
     * @param data    数据
     */
    public void onEvent(@EventMessage.EventType int type, String message, Object data) {
        if (type == EventMessage.EventType.ASSIGN
                && message.contains(getClass().getName())) {
            if (message.contains(RxMsgParameters.DATA_REFRESH)) {
                FcUtils.runOnUIThread(() -> {
                    if (data != null) {
                        showTextProcess(data.toString());
                    } else {
                        mProcess.setVisibility(View.GONE);
                    }
                });
            }
        }
    }


    /**
     * 参数列表
     */
    private void showDialog() {
        Dialog dialog = new Dialog(this, R.style.Dimension2Dialog);
        dialog.setContentView(R.layout.dialog_d3_parameters);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        Window dialogWindow = dialog.getWindow();
        //设置布局大小
        dialogWindow.setLayout(
//                DimenUtils.getScreenSize()[0] - DimenUtils.dp2px(32),
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        //设置整体大小包括外部半透明
//        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
//        params.width = DimenUtils.getScreenSize()[0] - DimenUtils.dp2px(16);
//        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        dialogWindow.setAttributes(params);
        //设置Dialog位置
        dialogWindow.setGravity(Gravity.BOTTOM);

        dialog.findViewById(R.id.tv01).setOnClickListener(view -> {
            dialog.dismiss();
            showProgressDialog(1, ((TextView) view).getText().toString(),
                    (int)mConfig.eye_x, -100, 100);
        });
        dialog.findViewById(R.id.tv02).setOnClickListener(view -> {
            dialog.dismiss();
            showProgressDialog(2, ((TextView) view).getText().toString(),
                    (int)mConfig.eye_y, -100, 100);
        });
        dialog.findViewById(R.id.tv03).setOnClickListener(view -> {
            dialog.dismiss();
            showProgressDialog(3, ((TextView) view).getText().toString(),
                    (int)mConfig.eye_z, 2, 100);
        });
        dialog.findViewById(R.id.tv04).setOnClickListener(view -> {
            dialog.dismiss();
            showProgressDialog(4, ((TextView) view).getText().toString(),
                    (int)mConfig.view_center_x, -100, 100);
        });
        dialog.findViewById(R.id.tv05).setOnClickListener(view -> {
            dialog.dismiss();
            showProgressDialog(5, ((TextView) view).getText().toString(),
                    (int)mConfig.view_center_y, -100, 100);
        });
        dialog.findViewById(R.id.tv06).setOnClickListener(view -> {
            dialog.dismiss();
            showProgressDialog(6, ((TextView) view).getText().toString(),
                    (int)mConfig.view_center_z, -100, 100);
        });

        dialog.findViewById(R.id.tv_submit).setOnClickListener(view -> {
            dialog.dismiss();
                mConfig.eye_x = 0;
                mConfig.eye_y = 0;
                mConfig.eye_z = 10;
                mConfig.view_center_x = 0;
                mConfig.view_center_y = 0;
                mConfig.view_center_z = 0;
            mSurfaceView.requestRender();
        });
        dialog.show();
    }

    //负数进度弥补
    private int negative = 0;

    /**
     * 参数调整
     */
    private void showProgressDialog(int type, String title, int current, int min, int max) {
        //判断是否有负数
        boolean isNegative = min < 0;
        negative = 0;
        if (isNegative) {
            negative = Math.abs(min);
            min = 0;
            max += negative;
            current += negative;
        }
        Dialog dialog = new Dialog(this, R.style.Dimension2Dialog);
        dialog.setContentView(R.layout.dialog_d3_progress);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        Window dialogWindow = dialog.getWindow();
        //设置布局大小
        dialogWindow.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        //设置Dialog位置
        dialogWindow.setGravity(Gravity.BOTTOM);
        SeekBar progress = dialog.findViewById(R.id.progress);
        TextView tvTitle = dialog.findViewById(R.id.tv_title);
        TextView tvNow = dialog.findViewById(R.id.tv_now);
        TextView tvMin = dialog.findViewById(R.id.tv_min);
        TextView tvMax = dialog.findViewById(R.id.tv_max);
        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress -= negative;
                switch (type) {
                    case 1:
                        mConfig.eye_x = progress;
                        break;
                    case 2:
                        mConfig.eye_y = progress;
                        break;
                    case 3:
                        mConfig.eye_z = progress;
                        break;
                    case 4:
                        mConfig.view_center_x = progress;
                        break;
                    case 5:
                        mConfig.view_center_y = progress;
                        break;
                    case 6:
                        mConfig.view_center_z = progress;
                        break;
                }
                tvNow.setText("当前：" + progress);
                mSurfaceView.requestRender();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        tvTitle.setText(title);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            progress.setMin(min);
        }
        progress.setMax(max);
        progress.setProgress(current);
        tvMin.setText(String.valueOf(min - negative));
        tvMax.setText(String.valueOf(max - negative));
        tvNow.setText("当前：" + (current - negative));
        dialog.show();
    }
}
