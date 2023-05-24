package com.melvinhou.dimension2.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.melvinhou.dimension2.R;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2019/2/19 17:56
 * <p>
 * = 分 类 说 明：
 * ============================================================
 */
public class RoundGifImageView extends AppCompatImageView {
    private float roundLayoutRadius = 0;
    private Path roundPath;
    private RectF rectF;


    public RoundGifImageView(Context context) {
        super(context);
    }

    public RoundGifImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundView);
        roundLayoutRadius = typedArray
                .getDimensionPixelSize(R.styleable.RoundView_corner_radius,
                        (int) roundLayoutRadius);
        typedArray.recycle();

        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }



    private void init() {
        setWillNotDraw(false);//如果你继承的是ViewGroup,注意此行,否则draw方法是不会回调的;
        roundPath = new Path();
        rectF = new RectF();
    }

    private void setRoundPath() {
        //添加一个圆角矩形到path中, 如果要实现任意形状的View, 只需要手动添加path就行
        roundPath.addRoundRect(rectF, roundLayoutRadius, roundLayoutRadius, Path.Direction.CW);
//        roundPath.addRoundRect(new RectF(0, 0, getWidth(), getHeight()),
//                new float[]{roundLayoutRadius, roundLayoutRadius,
//                        roundLayoutRadius, roundLayoutRadius,
//                        0.0f, 0.0f,
//                        0.0f, 0.0f}, Path.Direction.CW);
    }


    public void setRoundLayoutRadius(float roundLayoutRadius) {
        this.roundLayoutRadius = roundLayoutRadius;
        setRoundPath();
        postInvalidate();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        rectF.set(0f, 0f, getMeasuredWidth(), getMeasuredHeight());
        setRoundPath();
    }

    @Override
    public void draw(Canvas canvas) {
        if (roundLayoutRadius > 0f) {
            canvas.clipPath(roundPath);
        }
        super.draw(canvas);
    }
}
