package com.melvinhou.dimension2.ar.d3;

import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;

import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.ar.d3.model.D3ObjGroup;
import com.melvinhou.kami.model.EventMessage;
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


    }

    @Override
    protected void initData() {
        // 设定好使用的OpenGL版本.
        mSurfaceView.setEGLContextClientVersion(3);
        mConfig = new D3Config(0, 2, 1000,
                0, 15, 30,
                0, 10, -1);
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
}
