package com.melvinhou.dimension2.function.pdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/6/1 20:16
 * <p>
 * = 分 类 说 明：签字版
 * ================================================
 */
public class SignatureImageView extends View {


    private float mRadio;
    //画笔
    private Paint mPaint;
    //路径
    private Path mPath;


    public SignatureImageView(Context context) {
        this(context, null);
    }

    public SignatureImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SignatureImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(mPath,mPaint);
    }

    private void init() {
        mRadio = 5;
        // 初始化一个画笔，笔触宽度为5，颜色为红色
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mRadio);
        mPaint.setAntiAlias(true);
        //
        mPath = new Path();
    }

    /**
     * 开始签字
     */
    public void startSignature() {
        //
        setOnTouchListener(mSignatureTouch);
        mPath.reset();
    }

    public void stopSignature(){
        setOnTouchListener(null);
    }


    /**
     * 签字手势
     */
    private View.OnTouchListener mSignatureTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mPath.moveTo(x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
//                     mPath.quadTo(x, y);
                    mPath.lineTo(x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            postInvalidate();
            return true;
        }
    };

    /**
     * 从view获取bitmap
     * setDrawingCacheEnabled(true)  设置能否缓存图片信息
     * buildDrawingCache()  如果能够缓存图片，则创建图片缓存
     * getDrawingCache()  如果图片已经缓存，返回一个bitmap
     * destroyDrawingCache() 释放缓存占用的资源
     */
    public Bitmap getPanelBitmap() {
        return getDrawingCache();
    }
}
