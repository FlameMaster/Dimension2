package com.melvinhou.dimension2.ar.d3;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

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
     * 手势检测
     */
    private GestureDetector mGestureDetector;
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
        mGestureDetector = new GestureDetector(getContext(), new GestureListener());
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
            if (mRenderer != null) {
                mGestureDetector.onTouchEvent(event);
                mScaleGestureDetector.onTouchEvent(event);
                requestRender();
            }
            float x = event.getX();
            float y = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    float dx = x - previousX;
                    float dy = y - previousY;
            }

            previousX = x;
            previousY = y;
            return true;
        }
    }


    /**
     * 手势监听，需要用什么继承什么
     */
    class GestureListener extends GestureDetector.SimpleOnGestureListener {
        //按下
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        //单击确认
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return super.onSingleTapConfirmed(e);
        }

        //双击
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return super.onDoubleTap(e);
        }

        //双击按下抬起各触发一次
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return super.onDoubleTapEvent(e);
        }

        //抬起
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return super.onSingleTapUp(e);
        }

        //短按
        @Override
        public void onShowPress(MotionEvent e) {
            super.onShowPress(e);
        }

        //长按
        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);//不精确
        }

        //滑动
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//            if (Math.sqrt(distanceX * distanceX + distanceY * distanceY) > 10f)
            if (mRenderer != null) {
                if (e2.getPointerCount() > 1) {
                    mRenderer.updateTranslationY(distanceY/getHeight());
                } else {
                    mRenderer.updateAngleX(-distanceX);
                    mRenderer.updateAngleY(-distanceY);
                }
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        //滚动
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return super.onFling(e1, e2, velocityX, velocityY);
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
//            Log.d("缩放",
//            "x=" + detector.getFocusX() +
//            "\r\ty=" + detector.getFocusY() +
//            "\r\tscale=" + detector.getScaleFactor());
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
