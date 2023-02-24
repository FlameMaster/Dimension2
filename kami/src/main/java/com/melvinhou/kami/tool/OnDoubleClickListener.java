package com.melvinhou.kami.tool;

import android.view.MotionEvent;
import android.view.View;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2019/3/18 14:47
 * <p>
 * = 分 类 说 明：双击事件监听
 * ============================================================
 */
public abstract class OnDoubleClickListener implements View.OnTouchListener {

    private int count = 0;//点击次数
    private long firstClick = 0;//第一次点击时间
    private long secondClick = 0;//第二次点击时间
    /*两次点击时间间隔，单位毫秒*/
    private final int totalTime = 400;

    /**
     * 触摸事件处理
     *
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()) {//按下
            count++;
            if (1 == count) {
                firstClick = System.currentTimeMillis();//记录第一次点击时间
            } else if (2 == count) {
                secondClick = System.currentTimeMillis();//记录第二次点击时间
                if (secondClick - firstClick < totalTime) {//判断二次点击时间间隔是否在设定的间隔时间之内
                    onDoubleClick(v);
                    count = 0;
                    firstClick = 0;
                    return true;
                } else {
                    firstClick = secondClick;
                    count = 1;
                }
                secondClick = 0;
            }
        }else if (MotionEvent.ACTION_MOVE == event.getAction()){//滑动撤销
            count = 0;
            firstClick = 0;
        }
        return false;
    }

    /*双击事件*/
    public abstract void onDoubleClick(View v);
}
