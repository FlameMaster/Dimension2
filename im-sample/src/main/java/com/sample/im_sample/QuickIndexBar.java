package com.sample.im_sample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.melvinhou.kami.util.DimenUtils;


/**
 * 仿微信快速檢索條目
 */
public class QuickIndexBar extends View {

    /**
     * 初始化26個英文字母
     */
    private String[] indexArr = {"A", "B", "C", "D", "E", "F", "G", "H",
            "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
            "V", "W", "X", "Y", "Z"};
    /**
     * 画笔
     */
    private Paint paint;
    /**
     * 快速检索条目的宽度
     */
    private int width;
    /**
     * 一个字母的高度
     */
    private float cellHeight;


    /**
     * 字体颜色
     */
    private int textcolor = Color.GRAY;
    /**
     * 字体大小sp
     */
    private int textsize = 12;


    public QuickIndexBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public QuickIndexBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public QuickIndexBar(Context context) {
        super(context);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);//设置抗锯齿
        paint.setColor(textcolor);
        paint.setTextSize(DimenUtils.dp2px(textsize));
        paint.setTextAlign(Align.CENTER);//设置文本的起点是文字边框底边的中心
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = getMeasuredWidth();
        //得到一个格子的高度
        cellHeight = getMeasuredHeight() * 1f / indexArr.length;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < indexArr.length; i++) {
            float x = width / 2;
            float y = cellHeight / 2 + getTextHeight(indexArr[i]) / 2 + i * cellHeight;

            paint.setColor(lastIndex == i ? Color.GREEN : textcolor);

            canvas.drawText(indexArr[i], x, y, paint);
        }
    }

    private int lastIndex = -1;//记录上次的触摸字母的索引

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setBackgroundColor(0x30000000);
            case MotionEvent.ACTION_MOVE:
                float y = event.getY();
                int index = (int) (y / cellHeight);//得到字母对应的索引
                if (lastIndex != index) {
                    //说明当前触摸字母和上一个不是同一个字母
//				Log.e("tag", indexArr[index]);
                    //对index做安全性的检查
                    if (index >= 0 && index < indexArr.length) {
                        if (listener != null) {
                            listener.onTouchLetter(indexArr[index]);
                        }
                    }
                }
                lastIndex = index;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                setBackgroundColor(Color.TRANSPARENT);
                if (listener != null) {
                    listener.onDispose();
                }
                //重置lastIndex
                lastIndex = -1;
                break;
        }
        //引起重绘
        invalidate();
        return true;
    }

    /**
     * 获取文本的高度
     *
     * @param text
     * @return
     */
    private int getTextHeight(String text) {
        //获取文本的高度
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return bounds.height();
    }

    private OnTouchLetterListener listener;

    public void setOnTouchLetterListener(OnTouchLetterListener listener) {
        this.listener = listener;
    }

    /**
     * 触摸字母的监听器
     *
     * @author Administrator
     */
    public interface OnTouchLetterListener {
        void onTouchLetter(String letter);

        void onDispose();
    }

}
