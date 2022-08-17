package com.melvinhou.dimension2.prespace;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2022/8/17 0017 13:47
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class UnlockView extends View implements View.OnTouchListener {
    //密码记录
    private StringBuffer pass = new StringBuffer();
    //坐标记录
    private int[][] coordinate = new int[9][2];
    //结尾直线点记录
    private float startX = 0;
    private float startY = 0;
    private float endX = 0;
    private float endY = 0;
    //对外接口
    private TrajectoryListener mListener = null;
    //默认颜色
    private static int DEFAULT_COLOR = Color.parseColor("#cccccc");
    //选中颜色
    private static int PITCH_ON_COLOR = Color.parseColor("#0088ff");

    public UnlockView(Context context) {
        super(context);
        setOnTouchListener(this);
    }

    public UnlockView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);
    }

    public UnlockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnTouchListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //初始化所有点的坐标
        getCoordinate();
        //绘出所有点
        for (int[] i : coordinate) {
            Paint p = new Paint();
            //设置画笔宽度
            p.setStrokeWidth(4);
            //抗锯齿
            p.setAntiAlias(true);
            p.setColor(DEFAULT_COLOR);
            //设置画圆圈
            p.setStyle(Paint.Style.STROKE);
            //绘画
            canvas.drawCircle(i[0], i[1], 60, p);
        }
        //创建选中状态的画笔
        Paint p0 = new Paint();
        p0.setColor(PITCH_ON_COLOR);
        p0.setStrokeWidth(10);
        p0.setAntiAlias(true);
        for (int j = 0; j < pass.length(); j++) {
            //画出选中的圆
            canvas.drawCircle(coordinate[pass.charAt(j) - '0'][0], coordinate[pass.charAt(j) - '0'][1], 48, p0);
            //判断是否需要连接线
            if (j > 0) {
                //画出连接线
                canvas.drawLine(coordinate[pass.charAt(j - 1) - '0'][0], coordinate[pass.charAt(j - 1) - '0'][1],
                        coordinate[pass.charAt(j) - '0'][0], coordinate[pass.charAt(j) - '0'][1], p0);
            }
        }
        if (startX != 0 && endX != 0) {
            //画出最后一根线
            canvas.drawLine(startX, startY, endX, endY, p0);
        }
    }

    /**
     * 计算获取所有点的坐标
     */
    private void getCoordinate() {
        int w = getWidth();
        int h = getHeight();
        //得到点之间的距离
        int c = w / 4;
        //得到点与高之间的差（这里只实现了竖屏，高比宽大，所以是以宽为基准，使9个点在布局的中心）
        int x = (h - w) / 2;
        //得到每个点的XY坐标
        for (int i = 0; i < 9; i++) {
            coordinate[i][0] = c + ((i % 3) * c);
            coordinate[i][1] = c + x + ((i / 3) * c);
        }
    }

    //监听滑动操作
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            //手指按下
            case MotionEvent.ACTION_DOWN:
                //清空数据
                cliar();
                //获取按下位置有效性或按下的点
                int p = getPoint(motionEvent.getX(), motionEvent.getY());
                //判断有效性
                if (p != -1) {
                    //保存直线的起点
                    startX = coordinate[p][0];
                    startY = coordinate[p][1];
                    //保存点
                    pass.append(p);
                    //刷新界面
                    invalidate();
                    if (mListener != null) {
                        //通知activity
                        mListener.Start(p);
                    }
                } else {
                    invalidate();
                    return false;
                }
                break;
            case MotionEvent.ACTION_MOVE://手指滑动
                //获取手指当前位置或当前经过的点
                int pm = getPoint(motionEvent.getX(), motionEvent.getY());
                //判断是否进过了点
                if (pm != -1) {
                    //更新直线起点坐标
                    startX = coordinate[pm][0];
                    startY = coordinate[pm][1];
                    //保存点
                    pass.append(pm);
                    if (mListener != null) {
                        //通知activity
                        mListener.Move(pm, pass.toString());
                    }
                } else {
                    //更新直线结束点坐标
                    endX = motionEvent.getX();
                    endY = motionEvent.getY();
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                //重置直线坐标但不取消已绘制的图形
                startX = 0;
                startY = 0;
                endX = 0;
                endY = 0;
                if (mListener != null) {
                    //通知activity最终密码
                    mListener.Stop(pass.toString());
                }
                invalidate();
                break;
        }
        return true;
    }

    /**
     * 将密码归零及重置直线坐标
     */
    public void cliar() {
        pass.setLength(0);
        startX = 0;
        startY = 0;
        endX = 0;
        endY = 0;
        invalidate();
    }

    /**
     * 判断坐标有效性
     *
     * @param x
     * @param y
     * @return为-1时无效，其它为点
     */
    private int getPoint(float x, float y) {
        int back = -1;
        //遍历所有坐标
        for (int i = 0; i < coordinate.length; i++) {
            //判断坐标是否有效，30为有效范围，可更改
            if (Math.abs(x - coordinate[i][0]) < 30 && Math.abs(y - coordinate[i][1]) < 30) {
                back = i;
                break;
            }
        }
        //遍历密码
        for (int k = 0; k < pass.length(); k++) {
            int j = pass.charAt(k) - '0';
            //如果密码已经存在则无效
            back = back == j ? -1 : back;
        }
        return back;
    }

    /**
     * 设置通知接口
     *
     * @param listener
     */
    public void setListener(TrajectoryListener listener) {
        this.mListener = listener;
    }


    public interface TrajectoryListener {
        /**
         * 开始
         *
         * @param p 第一个点
         */
        public void Start(int p);

        /**
         * 移动时经过有效点
         *
         * @param p    经过的点
         * @param pass 已经输入的密码
         */
        public void Move(int p, String pass);

        /**
         * 滑动结束
         *
         * @param pass 滑动密码
         */
        public void Stop(String pass);
    }
}