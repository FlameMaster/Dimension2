package com.melvinhou.dimension2.ar.opengl;

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
 * = 时 间：2022/7/4 0004 10:43
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class OpenglEsView extends GLSurfaceView {


    public OpenglEsView(Context context) {
        this(context, null);
    }

    public OpenglEsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {


    }

    private OpenglEsRenderer renderer;

    @Override
    public void setRenderer(Renderer renderer) {
        super.setRenderer(renderer);
        if (renderer instanceof OpenglEsRenderer)
            this.renderer = (OpenglEsRenderer) renderer;
    }

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float previousX;
    private float previousY;

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
                float dy = y - previousY;

                // reverse direction of rotation above the mid-line
                if (y > getHeight() / 2) {
                    dx = dx * -1;
                }

                // reverse direction of rotation to left of the mid-line
                if (x < getWidth() / 2) {
                    dy = dy * -1;
                }

                if (renderer != null)
                    renderer.setAngle(
                            renderer.getAngle() +
                                    ((dx + dy) * TOUCH_SCALE_FACTOR));

                requestRender();
        }

        previousX = x;
        previousY = y;
        return true;
    }

}
