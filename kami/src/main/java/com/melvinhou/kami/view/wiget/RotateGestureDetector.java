package com.melvinhou.kami.view.wiget;

import android.content.Context;
import android.view.MotionEvent;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/1/7 14:07
 * <p>
 * = 分 类 说 明：处理手指旋转的工具
 * ============================================================
 */
public class RotateGestureDetector {

    /**
     * 上下文
     */
    private Context mContext;
    /**
     * 实现操作的监听
     */
    private SimpleOnRotateGestureListener mRotateGestureListener;

    /**
     * 按下时的xy中心点
     */
    private Float mFocusX, mFocusY;
    /**
     * 当前角度和总的角度
     */
    private float mRotation, mSumRotation;
    /**
     * 上一次的角度
     */
    private Float mPreviousAngle;


    public RotateGestureDetector(Context context, SimpleOnRotateGestureListener listener) {
        mContext = context;
        mRotateGestureListener = listener;
    }


    /**
     * 在touch方法中调用
     *
     * @param event
     * @return
     */
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
//        Log.e("旋转", "action=" + action + "\r\tcount=" + event.getPointerCount());
        if (event.getPointerCount() > 1) {
            if (action == MotionEvent.ACTION_MOVE) {
                onTouch(event);
            } else if (action == MotionEvent.ACTION_DOWN
                    || action == MotionEvent.ACTION_POINTER_DOWN
                    || action == 261) {//坑爹的ACTION_POINTER_1_DOWN=261，但是被废弃了
                onDown(event);
            } else if (action == MotionEvent.ACTION_UP
                    || action == MotionEvent.ACTION_POINTER_UP
                    || action == 262) {
                onUp(event);
            }
        }
        return true;
    }

    /**
     * 手指按下时处理
     *
     * @param event
     */
    private void onDown(MotionEvent event) {
        //初始化中心点
        int size = event.getPointerCount();
        float[][] locationList = new float[size][];
        for (int i = 0; i < size; i++) {
            locationList[i] = new float[]{event.getX(i), event.getY(i)};
        }
        float[] focusLocation = getCenterLocation(locationList);
        mFocusX = focusLocation[0];
        mFocusY = focusLocation[1];

//        Log.d("旋转中心点", "x=" + mFocusX + "\r\ty=" + mFocusY);
        //通知监听
        if (mRotateGestureListener != null)
            mRotateGestureListener.onRotateBegin();
    }

    /**
     * 获取中心点
     *
     * @param locationList
     * @return
     */
    private float[] getCenterLocation(float[][] locationList) {
        float x, y, sumX = 0, sumY = 0;
        for (float[] location : locationList) {
            sumX += location[0];
            sumY += location[1];
        }
        x = sumX / locationList.length;
        y = sumY / locationList.length;
        return new float[]{x, y};
    }


    /**
     * 手指滑动时处理
     *
     * @param event
     */
    private void onTouch(MotionEvent event) {
        //仅计算两个点
        double deltaX = event.getX(0) - event.getX(1);
        double deltaY = event.getY(0) - event.getY(1);
        float currentAngle = (float) Math.atan2(deltaX, deltaY);
        if (mPreviousAngle != null) {
            mRotation = (float) Math.toDegrees(mPreviousAngle - currentAngle);
            mSumRotation += mRotation;
            //通知监听
            if (mRotateGestureListener != null && mFocusX != null && mFocusY != null)
                mRotateGestureListener.onRotate(mRotation, mFocusX, mFocusY);
        }
//        Log.d("角度计算", "mPreviousAngle=" + mPreviousAngle + "\r\tmRotation=" + mRotation + "\r\tcurrentAngle=" + currentAngle);
        mPreviousAngle = currentAngle;

    }

    /**
     * 手指抬起
     *
     * @param event
     */
    private void onUp(MotionEvent event) {
        if (mRotateGestureListener != null && mFocusX != null && mFocusY != null)
            mRotateGestureListener.onRotateEnd(mSumRotation, mFocusX, mFocusY);
        //清除本次数据
        mPreviousAngle = null;
        mFocusX = null;
        mFocusY = null;
        mSumRotation = 0;
    }

    public interface SimpleOnRotateGestureListener {

        /**
         * 开始旋转
         */
        void onRotateBegin();

        /**
         * 旋转结束
         *
         * @param focusX
         * @param focusY
         */
        void onRotateEnd(float sumRotation, float focusX, float focusY);

        /**
         * 旋转时
         *
         * @param rotation
         * @param focusX
         * @param focusY
         */
        void onRotate(float rotation, float focusX, float focusY);
    }
}
