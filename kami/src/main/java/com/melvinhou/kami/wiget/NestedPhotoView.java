package com.melvinhou.kami.wiget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;

import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.ResourcesUtils;

import java.io.IOException;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.view.NestedScrollingChild2;
import androidx.core.view.NestedScrollingChild3;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/1/6 10:10
 * <p>
 * = 分 类 说 明：自定义一个photoview，支持图片裁剪功能，注释很多，便于阅读
 * ============================================================
 */
public class NestedPhotoView extends AppCompatImageView implements NestedScrollingChild2 {

    /**
     * 无限制模式（不限制边界，不限制缩放倍数，不限制旋转）
     */
    public static final int GESTURE_MODE_INFINITE = 0;
    /**
     * 标准模式（仅可用位移和缩放，限制缩放倍数及边界）
     */
    public static final int GESTURE_MODE_NORM = 1;
    /**
     * 边界限制模式
     */
    public static final int GESTURE_MODE_BOX = 5;

    /**
     * 手势检测
     */
    private GestureDetector mGestureDetector;
    /**
     * 缩放手势检测
     */
    private ScaleGestureDetector mScaleGestureDetector;
    /**
     * 自定义旋转手势检测
     */
    private RotateGestureDetector mRotateGestureDetector;

    /**
     * 初始预设的图片设置样式
     */
    private ScaleType mIntrinsicScaleType;
    /**
     * MTRANS_X、MTRANS_Y 同时控制着 Translate
     * MSCALE_X、MSCALE_Y 同时控制着 Scale
     * MSCALE_X、MSKEW_X、MSCALE_Y、MSKEW_Y 同时控制着 Rotate
     * MSKEW_X、MSKEW_Y 同时控制着 Skew
     */
    private Matrix mImgMatrix;

    /**
     * 初始化的图片缩放值
     */
    private float mIntrinsicScale;
    /**
     * 最小和最大缩放倍数
     */
    private float mMinScale, mMaxScale;
    /**
     * 缩放时的焦点坐标,写出来的目的是让旋转和缩放保持一个中心点
     */
    private Float mFocusX, mFocusY;
    /**
     * 初始位置和大小
     */
    private float mIntrinsicLeft, mIntrinsicTop, mImageWidth, mImageHeight;
    /**
     * 限制位置
     */
    private Float mCheckLeft, mCheckTop, mCheckRight, mCheckBottom;
    /**
     * 实际显示区域
     */
    private Rect mDrawableRect;
    /**
     * 边框画笔
     */
    private Paint mDrawablePaint;

    /**
     * 当前模式
     */
    private int mGestureMode = GESTURE_MODE_NORM;

    /**
     * 当前图片的key
     */
    private String mImageKey;

//////////////////////////////////////////工具方法///////////////////////////////////////////////////////

    /**
     * 设置最大放大倍数
     *
     * @param maxScale
     */
    public void setMaxScale(float maxScale) {
        this.mMaxScale = maxScale;
    }

    /**
     * 设置一个边界检查框
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void setCheckBox(float left, float top, float right, float bottom) {
        mCheckLeft = left;
        mCheckRight = right;
        mCheckTop = top;
        mCheckBottom = bottom;
    }

    /**
     * 设置一个中间的框
     *
     * @param size
     */
    public void setCenterCheckBox(final float size) {
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                Rect rect = new Rect();
                getDrawingRect(rect);
                mCheckLeft = (rect.right - rect.left - size) / 2;
                mCheckRight = mCheckLeft;
                mCheckTop = (rect.bottom - rect.top - size) / 2;
                mCheckBottom = mCheckTop;

                //赋值结束后，移除该监听函数
                NestedPhotoView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });
    }

    /**
     * 设置图片查看模式
     *
     * @param mode
     */
    public void setGestureMode(int mode) {
        mGestureMode = mode;
    }

    /**
     * 设置校验框颜色
     *
     * @param color
     */
    public void setCheckBoxColor(@ColorInt int color) {
        mDrawablePaint.setColor(color);
    }


//////////////////////////////////////////嵌套滑动///////////////////////////////////////////////////////

    @Override
    public boolean startNestedScroll(int axes, int type) {
        return true;
    }

    @Override
    public void stopNestedScroll(int type) {

    }

    @Override
    public boolean hasNestedScrollingParent(int type) {
        return true;
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow, int type) {
        return true;
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow, int type) {
        consumed[0] = dx;
        consumed[1] = dy;
        return true;
    }


//////////////////////////////////////////初始化///////////////////////////////////////////////////////


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mGestureMode == GESTURE_MODE_BOX) {
            canvas.drawRect(new RectF(0, 0, mCheckLeft,
                    mDrawableRect.bottom - mCheckBottom), mDrawablePaint);
            canvas.drawRect(new RectF(mCheckLeft, 0, mDrawableRect.right, mCheckTop), mDrawablePaint);
            canvas.drawRect(new RectF(mDrawableRect.right - mCheckRight, mCheckTop,
                    mDrawableRect.right, mDrawableRect.bottom), mDrawablePaint);
            canvas.drawRect(new RectF(0, mDrawableRect.bottom - mCheckBottom,
                    mDrawableRect.right - mCheckRight, mDrawableRect.bottom), mDrawablePaint);
        }
    }

    public NestedPhotoView(Context context) {
        this(context, null);
    }

    public NestedPhotoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestedPhotoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

//        setForeground();
        init();
    }

    private void init() {
        //保存原始设置的样式
        mIntrinsicScaleType = getScaleType();
        //默认最大放大4倍
        mMaxScale = 4.0f;
        mIntrinsicScale = 1f;
        //图片显示模式设置为矩阵
        setScaleType(ScaleType.MATRIX);
        mImgMatrix = new Matrix();
        mDrawableRect = new Rect();
        mDrawablePaint = new Paint();
        mDrawablePaint.setColor(Color.parseColor("#80999999"));
//        GestureDetector.OnGestureListener//单击手势
//        GestureDetector.OnDoubleTapListener//双击手势
//        GestureDetector.OnContextClickListener//外部按键
        mGestureDetector = new GestureDetector(getContext(), new GestureListener());
        mScaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureListener());
        mRotateGestureDetector = new RotateGestureDetector(getContext(), new RotateGestureListener());
        setOnTouchListener(new MatrixTouchListener());
    }

    //TODO 仅写了uri的图片加载，其它加载懒得写了

    //每次onStart都会重走一次
    @Override
    public void setImageURI(@Nullable Uri uri) {
        try {
            int imgWidth, imgHeight;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(FcUtils.getContext()
                    .getContentResolver()
                    .openInputStream(uri), null, options);
            imgWidth = options.outWidth;
            imgHeight = options.outHeight;
            //图片太大时使用Bitmap缩放加载
            if (options.outHeight * options.outWidth > 300000) {
                int max = 5200;
                int size;
                int inSampleSize = 1;
                if (options.outWidth > options.outHeight) {
                    size = options.outWidth;
                } else {
                    size = options.outHeight;
                }
                while (size / inSampleSize > max) {
                    inSampleSize++;
                }
                imgWidth /= inSampleSize;
                imgHeight /= inSampleSize;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inSampleSize = inSampleSize;
                options.inJustDecodeBounds = false;
                setImageBitmap(BitmapFactory.decodeStream(FcUtils.getContext()
                        .getContentResolver()
                        .openInputStream(uri), null, options));
            } else super.setImageURI(uri);
            if (uri.toString().equals(mImageKey)) return;
            mImageKey = uri.toString();
//            initImageMatrix(imgWidth, imgHeight);
        } catch (IOException e) {
            Log.e("PhotoCutterView", "图片加载失败");
            e.printStackTrace();
        }
    }

    @Override
    public void setImageResource(int resId) {
        try {
            int imgWidth, imgHeight;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(ResourcesUtils.getResources(), resId, options);
            imgWidth = options.outWidth;
            imgHeight = options.outHeight;
            //图片太大时使用Bitmap缩放加载
            if (options.outHeight * options.outWidth > 300000) {
                int max = 5200;
                int size;
                int inSampleSize = 1;
                if (options.outWidth > options.outHeight) {
                    size = options.outWidth;
                } else {
                    size = options.outHeight;
                }
                while (size / inSampleSize > max) {
                    inSampleSize++;
                }
                imgWidth /= inSampleSize;
                imgHeight /= inSampleSize;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inSampleSize = inSampleSize;
                options.inJustDecodeBounds = false;
                super.setImageBitmap(BitmapFactory.decodeResource(ResourcesUtils.getResources(), resId, options));
            } else
                super.setImageResource(resId);
            if (String.valueOf(resId).equals(mImageKey)) return;
            mImageKey = String.valueOf(resId);
//            initImageMatrix(imgWidth, imgHeight);
        } catch (Exception e) {
            Log.e("PhotoCutterView", "图片加载失败");
            e.printStackTrace();
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if (bm == null) return;
        try {
            super.setImageBitmap(bm);
            int imgWidth = bm.getWidth();
            int imgHeight = bm.getHeight();
            if (bm.toString().equals(mImageKey)) return;
            mImageKey = bm.toString();
            Log.e("setImageBitmap", "imgWidth=" + imgWidth + "\t\timgHeight=" + imgHeight);
//            initImageMatrix(imgWidth, imgHeight);
        } catch (Exception e) {
            Log.e("PhotoCutterView", "图片加载失败");
            e.printStackTrace();
        }
    }

    public void setNullBitmap(){
        super.setImageBitmap(null);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        if (drawable == null) return;
        try {
            super.setImageDrawable(drawable);
            int imgWidth = drawable.getIntrinsicWidth();
            int imgHeight = drawable.getIntrinsicHeight();
            if (drawable.toString().equals(mImageKey)) return;
            mImageKey = drawable.toString();
//            Log.e("setImageDrawable", "imgWidth=" + imgWidth + "\t\timgHeight=" + imgHeight);
            initImageMatrix(imgWidth, imgHeight);
        } catch (Exception e) {
            Log.e("PhotoCutterView", "图片加载失败");
            e.printStackTrace();
        }
    }

    /**
     * 每次设置图片调用初始化,要防止多次调用哦
     *
     * @param imgWidth
     * @param imgHeight
     */
    public void initImageMatrix(final float imgWidth, final float imgHeight) {
        mImgMatrix.set(getImageMatrix());
        mImgMatrix.setScale(1, 1);//这里固定倍数，防止二次执行导致错误，因为setimg都会走到setImageDrawable
        getDrawingRect(mDrawableRect);//这个相当于获取控件绘制区域
        float showWidth = getMeasuredWidth();
        float showHeigh = getMeasuredHeight();
        //保证精确测量
        if (showWidth == 0 || showHeigh == 0) {
            getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    initImageMatrix(imgWidth, imgHeight);
                    //赋值结束后，移除该监听函数
                    NestedPhotoView.this.getViewTreeObserver().removeOnPreDrawListener(this);
                    return true;
                }
            });
            return;
        }
        mImageWidth = imgWidth;
        mImageHeight = imgHeight;
        float[] values = new float[9];
        mImgMatrix.getValues(values);

        //决定宽全展开还是高全展开,优先保障图片全显示
        //TODO box模式时加载超长图和超宽图的适配没做（即宽高有一方会比中间裁剪区小
        if (imgWidth / imgHeight >= showWidth / showHeigh) {//宽度全展开
            mMinScale = showWidth / imgWidth;
            mIntrinsicLeft = 0;
            mIntrinsicTop = (showHeigh - imgHeight * mMinScale) / 2;
        }
        //高度全展开
        else {
            mMinScale = showHeigh / imgHeight;
            mIntrinsicLeft = (showWidth - imgWidth * mMinScale) / 2;
            mIntrinsicTop = 0;
        }
        mImgMatrix.postScale(mMinScale, mMinScale, 0, 0);
        mImgMatrix.postTranslate(mIntrinsicLeft, mIntrinsicTop);
        setImageMatrix(mImgMatrix);
        //边界修正，懒得改直接用现成
        if (mGestureMode==GESTURE_MODE_BOX)restoreSize();
        Log.e("PhotoCutterView", "修改图片初始化参数"
                + "\nscale=" + mMinScale + "\r\tleft=" + mIntrinsicLeft + "\r\ttop=" + mIntrinsicTop
                + "\nimgWidth=" + imgWidth + "\r\timgHeight=" + imgHeight
                + "\nshowWidth=" + showWidth + "\r\tshowHeigh=" + showHeigh);

    }

    /**
     * 大小复位
     */
    public void restoreSize(){

        float scale = mMinScale / mMaxScale;
        float[] values = new float[9];
        mImgMatrix.getValues(values);
        float[] checkScale = checkScale(values, scale, getWidth()/2, getHeight()/2);
        scale = checkScale[0];
        mFocusX = checkScale[1];
        mFocusY = checkScale[2];

        mImgMatrix.postScale(scale, scale, mFocusX, mFocusY);
        setImageMatrix(mImgMatrix);
    }

//////////////////////////////////////////辅助方法///////////////////////////////////////////////////////

    /**
     * 边界校验
     *
     * @param values
     * @param dy
     * @return
     */
    private float checkDyBound(float[] values, float dy) {
        if (mGestureMode != GESTURE_MODE_NORM && mGestureMode != GESTURE_MODE_BOX)
            return dy;
        float nowY = values[Matrix.MTRANS_Y];
        float expectationY = nowY + dy;
        float nowScale = values[Matrix.MSCALE_Y];
//        Rect rect = new Rect();
//        getDrawingRect(rect);//这个相当于获取控件绘制区域
        //获取图片当前高度
        float height = mImageHeight * nowScale;
        if (height < getCheckHeight()) return 0;
        //区域限制上边不能留白
        float checkTop = getCheckTop();
        if (expectationY > checkTop) {
            return checkTop - nowY;
        }
        //区域限制下边不得留白
        float checkBottom = getCheckBottom(height);
        if (expectationY < checkBottom) {
            return checkBottom - nowY;
        }
        return dy;
    }

    private float checkDxBound(float[] values, float dx) {
        if (mGestureMode != GESTURE_MODE_NORM && mGestureMode != GESTURE_MODE_BOX)
            return dx;
        //当前位置
        float nowX = values[Matrix.MTRANS_X];
        //期待值
        float expectationX = nowX + dx;
        //缩放比例
        float nowScale = values[Matrix.MSCALE_X];
        //获取图片当前宽度
        float width = mImageWidth * nowScale;
        //图片小于宽度不予以移动权
        if (width < getCheckWidth()) return 0;
        //区域限制上边不能留白
        float checkLeft = getCheckLeft();
        if (expectationX > checkLeft) {
            return checkLeft - nowX;
        }
        //区域限制下边不得留白
        float checkRight = getCheckRight(width);
        if (expectationX < checkRight) {
            return checkRight - nowX;
        }
        return dx;
    }

    //上边界检查
    private float getCheckTop() {
        if (mGestureMode == GESTURE_MODE_BOX)
            return mCheckTop;
        return 0;
    }

    //左边界检查
    private float getCheckLeft() {
        if (mGestureMode == GESTURE_MODE_BOX)
            return mCheckLeft;
        return 0;
    }

    //下边界检查
    private float getCheckBottom(float nowHeight) {
        if (mGestureMode == GESTURE_MODE_BOX)
            return mDrawableRect.bottom - mCheckBottom - nowHeight;
        return mDrawableRect.bottom - nowHeight;
    }

    //右边界检查
    private float getCheckRight(float nowWidth) {
        if (mGestureMode == GESTURE_MODE_BOX)
            return mDrawableRect.right - mCheckRight - nowWidth;
        return mDrawableRect.right - nowWidth;
    }

    //高度检查
    private float getCheckHeight() {
        if (mGestureMode == GESTURE_MODE_BOX)
            return mDrawableRect.bottom - mDrawableRect.top - mCheckBottom - mCheckTop;
        return mDrawableRect.bottom - mDrawableRect.top;
    }

    //宽度检查
    private float getCheckWidth() {
        if (mGestureMode == GESTURE_MODE_BOX)
            return mDrawableRect.right - mDrawableRect.left - mCheckRight - mCheckLeft;
        return mDrawableRect.right - mDrawableRect.left;
    }


    /**
     * 缩放的校验
     *
     * @param values
     * @param scale
     * @param focusX
     * @param focusY
     * @return
     */
    private float[] checkScale(float[] values, float scale, float focusX, float focusY) {
        if (mGestureMode != GESTURE_MODE_NORM && mGestureMode != GESTURE_MODE_BOX)
            return new float[]{scale, focusX, focusY};

        float checkMinScale = getCheckMinScale();
        float nowX = values[Matrix.MTRANS_X];
        float nowY = values[Matrix.MTRANS_Y];
        float nowScale = values[Matrix.MSCALE_X];
        if (scale * nowScale > mMaxScale) {
            scale = mMaxScale / nowScale;
        } else if (scale * nowScale < checkMinScale) {
            scale = checkMinScale / nowScale;
        }

        float nowWidth = mImageWidth * nowScale;
        float nowHeight = mImageHeight * nowScale;
        float minWidth = mImageWidth * checkMinScale;
        float minHeight = mImageHeight * checkMinScale;
        //缩小时需要校验边界
        if (scale < 1) {
            float checkScaleLeft = getCheckScaleLeft(checkMinScale);
            float checkScaleTop = getCheckScaleTop(checkMinScale);
            //ScaleGestureListener给的焦点坐标是屏幕中的，所以我们要算出缩放到最初时的中心点
            focusX = (checkScaleLeft - nowX) / (nowWidth - minWidth) * minWidth + checkScaleLeft;
            focusY = (checkScaleTop - nowY) / (nowHeight - minHeight) * minHeight + checkScaleTop;
        } else if (scale > 1 && mGestureMode == GESTURE_MODE_NORM) {
            if (nowHeight < getHeight())
                focusY = getHeight() / 2;
            if (nowWidth < getWidth())
                focusX = getWidth() / 2;
        }

        return new float[]{scale, focusX, focusY};
    }

    //最小缩放值
    private float getCheckMinScale() {
        if (mGestureMode == GESTURE_MODE_BOX) {
            if (mImageWidth > mImageHeight) return getCheckHeight() / mImageHeight;
            else return getCheckWidth() / mImageWidth;
        }
        return mMinScale;
    }

    //缩放到最小的x坐标
    private float getCheckScaleLeft(float checkMinScale) {
        if (mGestureMode == GESTURE_MODE_BOX) {
            if (mImageWidth > mImageHeight)
                return (mDrawableRect.right - mDrawableRect.left - checkMinScale * mImageWidth) / 2;
            else return mCheckLeft;
        }
        return mIntrinsicLeft;
    }

    //缩放到最小的y坐标
    private float getCheckScaleTop(float checkMinScale) {
        if (mGestureMode == GESTURE_MODE_BOX) {
            if (mImageWidth > mImageHeight)
                return mCheckTop;
            else
                return (mDrawableRect.bottom - mDrawableRect.top - checkMinScale * mImageHeight) / 2;
        }
        return mIntrinsicTop;
    }

//////////////////////////////////////////各种手势监听///////////////////////////////////////////////////////

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
//            float scale = 0.5f;
//            mImgMatrix.postScale(scale, scale, e.getX(), e.getY());
//            setImageMatrix(mImgMatrix);
            return super.onSingleTapConfirmed(e);
        }

        //双击
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            float scale = 2f;
            float[] values = new float[9];
            mImgMatrix.getValues(values);
//            Log.e("双击", "now=" + values[Matrix.MSCALE_X] + "\r\tmax=" + mMaxScale);
            //判断是否达到最大值
            if (values[Matrix.MSCALE_X] >= mMaxScale)
                scale = mMinScale / mMaxScale;
            float[] checkScale = checkScale(values, scale, e.getX(), e.getY());
            scale = checkScale[0];
            mFocusX = checkScale[1];
            mFocusY = checkScale[2];

            mImgMatrix.postScale(scale, scale, mFocusX, mFocusY);
            setImageMatrix(mImgMatrix);
            return true;
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
            if (Math.sqrt(distanceX * distanceX + distanceY * distanceY) > 10f) {
                float[] values = new float[9];
                mImgMatrix.getValues(values);
                mImgMatrix.postTranslate(checkDxBound(values, -distanceX), checkDyBound(values, -distanceY));
//                mImgMatrix.postTranslate(-distanceX, -distanceY);
                setImageMatrix(mImgMatrix);
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
//            Log.d("缩放", "x=" + detector.getFocusX() + "\r\ty=" + detector.getFocusX());
            float[] values = new float[9];
            mImgMatrix.getValues(values);
            float[] checkScale = checkScale(values, detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
            float scale = checkScale[0];
            mFocusX = checkScale[1];
            mFocusY = checkScale[2];
            mImgMatrix.postScale(scale, scale, mFocusX, mFocusY);
//            mImgMatrix.postRotate(,detector.getFocusX(), detector.getFocusY());
            setImageMatrix(mImgMatrix);
            return true;
        }

        //缩放手势结束
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
        }
    }

    /**
     * 旋转手势监听
     */
    class RotateGestureListener implements RotateGestureDetector.SimpleOnRotateGestureListener {

        @Override
        public void onRotateBegin() {

        }

        @Override
        public void onRotate(float rotation, float focusX, float focusY) {
            if (mFocusX != null && mFocusY != null) {
                focusX = mFocusX;
                focusY = mFocusY;
            }
            mImgMatrix.postRotate(rotation, focusX, focusY);
            setImageMatrix(mImgMatrix);
//            Log.w("旋转", "x=" + focusX + "\r\ty=" + focusY);
        }

        @Override
        public void onRotateEnd(float sumRotation, float focusX, float focusY) {
            if (mFocusX != null && mFocusY != null) {
                focusX = mFocusX;
                focusY = mFocusY;
            }
//            mImgMatrix.postRotate(-sumRotation, focusX, focusY);
//            setImageMatrix(mImgMatrix);
        }
    }

    /**
     * 触摸监听
     */
    class MatrixTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (null == getDrawable()) return false;
            mGestureDetector.onTouchEvent(event);
            //至少两根手指
//            if (event.getPointerCount() > 1) {
            mScaleGestureDetector.onTouchEvent(event);
            if (mGestureMode == GESTURE_MODE_INFINITE)
                mRotateGestureDetector.onTouchEvent(event);
            return true;
        }
    }
}
