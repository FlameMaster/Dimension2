package com.melvinhou.dimension2.ar.d3;

import android.opengl.GLSurfaceView;

import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.ar.dome.ArView;
import com.melvinhou.kami.view.BaseActivity;

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

    private GLSurfaceView surfaceView;
    private boolean rendererSet = false;


    @Override
    protected int getLayoutID() {
        return R.layout.activity_ar;
    }

    @Override
    protected void initView() {
        surfaceView = new D3SurfaceView(this);
        setContentView(surfaceView);
        rendererSet = true;
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (rendererSet) {
            surfaceView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (rendererSet) {
            surfaceView.onResume();
        }
    }

}
