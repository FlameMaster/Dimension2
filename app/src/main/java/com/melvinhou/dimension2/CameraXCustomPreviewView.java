package com.melvinhou.dimension2;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.view.PreviewView;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/7/20 18:44
 * <p>
 * = 分 类 说 明：带有手势控制的pv
 * ================================================
 */
public class CameraXCustomPreviewView extends PreviewView
        implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {


    private GestureDetector mGestureDetector;
    private CustomTouchListener mCustomTouchListener;
    /**
     * 缩放相关
     */
    private float currentDistance = 0;
    private float lastDistance = 0;

    public void setCustomTouchListener(CustomTouchListener listener) {
        this.mCustomTouchListener = listener;
    }

    public CameraXCustomPreviewView(@NonNull Context context) {
        this(context, null);
    }

    public CameraXCustomPreviewView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraXCustomPreviewView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }


    public CameraXCustomPreviewView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mGestureDetector = new GestureDetector(context, this);
        mGestureDetector.setOnDoubleTapListener(this);
        // mScaleGestureDetector = new ScaleGestureDetector(context, onScaleGestureListener);
        // 解决长按屏幕无法拖动,但是会造成无法识别长按事件
        // mGestureDetector.setIsLongpressEnabled(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 接管onTouchEvent
        return mGestureDetector.onTouchEvent(event);
    }

/////////////////////////////////////OnGestureListener//////////////////////////////////////////////

    /**
     * 按下
     * @param e
     * @return
     */
    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    /**
     * 刚碰上还没松开
     * @param e
     */
    @Override
    public void onShowPress(MotionEvent e) {
    }

    /**
     * 轻轻一碰后马上松开
     * @param e
     * @return
     */
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

    /**
     * 按下后拖动
     * @param e1
     * @param e2
     * @param distanceX
     * @param distanceY
     * @return
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // 大于两个触摸点
        if (e2.getPointerCount() >= 2) {
            //event中封存了所有屏幕被触摸的点的信息，第一个触摸的位置可以通过event.getX(0)/getY(0)得到
            float offSetX = e2.getX(0) - e2.getX(1);
            float offSetY = e2.getY(0) - e2.getY(1);
            //运用三角函数的公式，通过计算X,Y坐标的差值，计算两点间的距离
            currentDistance = (float) Math.sqrt(offSetX * offSetX + offSetY * offSetY);
            if (lastDistance == 0) {//如果是第一次进行判断
                lastDistance = currentDistance;
            } else {
                if (currentDistance - lastDistance > 10) {
                    // 放大
                    if (mCustomTouchListener != null) {
                        mCustomTouchListener.zoom();
                    }
                } else if (lastDistance - currentDistance > 10) {
                    // 缩小
                    if (mCustomTouchListener != null) {
                        mCustomTouchListener.zoomOut();
                    }
                }
            }
            //在一次缩放操作完成后，将本次的距离赋值给lastDistance，以便下一次判断
            //但这种方法写在move动作中，意味着手指一直没有抬起，监控两手指之间的变化距离超过10
            //就执行缩放操作，不是在两次点击之间的距离变化来判断缩放操作
            //故这种将本次距离留待下一次判断的方法，不能在两次点击之间使用
            lastDistance = currentDistance;
        }
        return true;
    }

    /**
     * 长按屏幕
     * @param e
     */
    @Override
    public void onLongPress(MotionEvent e) {
        if (mCustomTouchListener != null) {
            mCustomTouchListener.longClick(e.getX(), e.getY());
        }
    }

    /**
     * 滑动后松开
     * @param e1
     * @param e2
     * @param velocityX
     * @param velocityY
     * @return
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        currentDistance = 0;
        lastDistance = 0;
        return true;
    }

/////////////////////////////////////OnDoubleTapListener//////////////////////////////////////////////

    /**
     * 严格的单击
     * @param e
     * @return
     */
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (mCustomTouchListener != null) {
            mCustomTouchListener.click(e.getX(), e.getY());
        }
        return true;
    }

    /**
     * 双击
     * @param e
     * @return
     */
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (mCustomTouchListener != null) {
            mCustomTouchListener.doubleClick(e.getX(), e.getY());
        }
        return true;
    }

    /**
     * 表示发生双击行为
     * @param e
     * @return
     */
    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return true;
    }

    /**
     * 手势回调借口
     */
    public interface CustomTouchListener {
        /**
         * 放大
         */
        void zoom();

        /**
         * 缩小
         */
        void zoomOut();

        /**
         * 点击
         */
        void click(float x, float y);

        /**
         * 双击
         */
        void doubleClick(float x, float y);

        /**
         * 长按
         */
        void longClick(float x, float y);
    }
}
