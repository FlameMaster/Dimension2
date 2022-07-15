package com.melvinhou.dimension2.ar.d3;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.melvinhou.kami.wiget.PhotoCutterView;

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
public class D3SurfaceView extends GLSurfaceView {

    /**
     * 缩放手势检测
     */
    private ScaleGestureDetector mScaleGestureDetector;

    private D3Renderer mRenderer;

    public D3Renderer getD3Renderer() {
        return mRenderer;
    }

    public void setD3Renderer(D3Renderer renderer) {
        this.mRenderer = renderer;
        super.setRenderer(renderer);
    }


    public D3SurfaceView(Context context) {
        this(context, null);
    }

    public D3SurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureListener());
        setOnTouchListener(new MatrixTouchListener());
    }

    private float previousX, previousY;


    /**
     * 触摸监听
     */
    class MatrixTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mRenderer != null)
                mScaleGestureDetector.onTouchEvent(event);
            float x = event.getX();
            float y = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    float dx = x - previousX;
                    float dy = y - previousY;
                    if (mRenderer != null) {
                        mRenderer.updateAngleX(dx);
                        mRenderer.updateAngleY(dy);
                        requestRender();
                    }
            }

            previousX = x;
            previousY = y;
            return true;
        }
    }


    /**
     * 缩放手势监听
     */
    class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        //缩放手势开始
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            //返回false不会缩放
            return true;
        }

        //缩放中
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            Log.d("缩放", "x=" + detector.getFocusX() + "\r\ty=" + detector.getFocusX() + "\r\tscale=" + detector.getScaleFactor());
            mRenderer.updateScale(detector.getScaleFactor());
            return true;
        }

        //缩放手势结束
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
        }
    }
}
