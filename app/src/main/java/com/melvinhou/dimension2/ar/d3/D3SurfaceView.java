package com.melvinhou.dimension2.ar.d3;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2022/7/4 0004 13:20
 * <p>
 * = 分 类 说 明：3d模型的显示view
 * ================================================
 */
public class D3SurfaceView  extends GLSurfaceView {

    private D3Renderer mRenderer;

    public D3SurfaceView(Context context) {
        this(context,null);
    }

    public D3SurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mRenderer = new D3Renderer();
        // 设定好使用的OpenGL版本.
        setEGLContextClientVersion(3);
        setRenderer(mRenderer);
        //RENDERMODE_WHEN_DIRTY：被动渲染，设置只在requestRender时重绘
        //RENDERMODE_CONTINUOUSLY：主动渲染
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }


    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float previousX;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - previousX;

                // reverse direction of rotation above the mid-line
                if (y > getHeight() / 2) {
                    dx = dx * -1;
                }

                if (mRenderer != null)
                    mRenderer.setAngle(
                            mRenderer.getAngle() + dx * TOUCH_SCALE_FACTOR);

                requestRender();
        }

        previousX = x;
        return true;
    }
}
