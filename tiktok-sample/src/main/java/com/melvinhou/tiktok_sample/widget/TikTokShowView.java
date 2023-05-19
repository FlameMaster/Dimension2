package com.melvinhou.tiktok_sample.widget;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.melvinhou.kami.util.FcUtils;

import androidx.annotation.Nullable;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/4/25 1:37
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class TikTokShowView extends View {

    //是否禁止触摸
    private boolean isCanTouch = false;

    public void setCanTouch(boolean isCanTouch) {
        this.isCanTouch = isCanTouch;
    }

    public TikTokShowView(Context context) {
        this(context, null);
    }

    public TikTokShowView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TikTokShowView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isCanTouch)
            return false;
        return super.onTouchEvent(event);
    }

    /**
     * 显示中心位置
     *
     * @param x
     * @param y
     */
    public void show(float x, float y) {
        //正确方法是使用SurfaceView完成动画
        //懒得写
        Toast toast = Toast.makeText(FcUtils.getContext(), "+1", Toast.LENGTH_SHORT);
        int left = (int) (x + Math.random() * 200 -200);
        int top = (int) (y + Math.random() * 200 -400);
        toast.setGravity(Gravity.TOP | Gravity.LEFT, left, top);
        new CountDownTimer(300, 300) {
            @Override
            public void onTick(long millisUntilFinished) {
                toast.show();
            }

            @Override
            public void onFinish() {
                toast.cancel();
            }
        }.start();
    }
}
