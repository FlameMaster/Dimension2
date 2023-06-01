package com.melvinhou.model3d_sample;

import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.view.View;

import com.melvinhou.model3d_sample.databinding.ActivityD3SimpleBinding;
import com.melvinhou.kami.mvvm.BindActivity;
import com.melvinhou.model3d_sample.sample.D3SampleModel;
import com.melvinhou.opengllibrary.d3.D3Config;
import com.melvinhou.opengllibrary.d3.D3Renderer;
import com.melvinhou.opengllibrary.d3.entity.D3CustomObj;

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
 * = 分 类 说 明：简易模型演示
 * ================================================
 */
public class D3SimpleActivity extends BindActivity<ActivityD3SimpleBinding, D3SampleModel> {
    @Override
    protected ActivityD3SimpleBinding openViewBinding() {
        return ActivityD3SimpleBinding.inflate(getLayoutInflater());
    }

    @Override
    protected Class<D3SampleModel> openModelClazz() {
        return D3SampleModel.class;
    }


    private static final String TAG = D3SimpleActivity.class.getSimpleName();
    private boolean rendererSet = false;
    private D3Renderer mRenderer;//渲染器
    private D3Config mConfig;//渲染器参数

    //小汽车跑步
    private Runnable run = new Runnable() {
        @Override
        public void run() {
            if (mConfig == null) return;
            for (int i = 0; ; i++) {
                SystemClock.sleep(30);
                float now = (i % 100) * 0.1f;
                mConfig.look_view_y = 5f - now;
                mBinding.container.requestRender();
            }
        }
    };

    @Override
    protected void initListener() {
        mBinding.back.setOnClickListener(v -> {
            backward();
        });
        mBinding.setting.setOnClickListener(v -> {
//            ThreadManager.getThreadPool().execute(run);
        });
    }

    @Override
    protected void initData() {
        mBinding.container.setEGLContextClientVersion(3);
        //初始化配置
        mConfig = D3Config.instance(true);
        mConfig.projection_near = 2;
        mConfig.projection_far = 100;
        //eye到view_center一条直线确定方向,up↑确定视角角度
        mConfig.look_eye_x = -6;
        mConfig.look_eye_y = 6;
        mConfig.look_eye_z = 2;
        mConfig.look_view_x = 0;
        mConfig.look_view_y = 0.2f;
        mConfig.look_view_z = 0;
        mConfig.look_up_x = 0;
        mConfig.look_up_y = 0;
        mConfig.look_up_z = 1;
        //加载
        String objPath = "d3/sample";
        String objName = "redcar.obj";
        mRenderer = new D3Renderer(D3CustomObj.class, objPath, objName);
        mBinding.container.setRenderer(mRenderer);
        //RENDERMODE_WHEN_DIRTY：被动渲染，设置只在requestRender时重绘
        //RENDERMODE_CONTINUOUSLY：主动渲染
        mBinding.container.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        rendererSet = true;
        mBinding.container.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rendererSet) {
            mBinding.container.onPause();
        }
        //停止运行
//        ThreadManager.getThreadPool().cancel(run);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rendererSet) {
            mBinding.container.onResume();
        }
    }
}
