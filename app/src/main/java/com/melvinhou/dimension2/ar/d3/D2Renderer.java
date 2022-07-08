package com.melvinhou.dimension2.ar.d3;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2022/7/4 0004 13:25
 * <p>
 * = 分 类 说 明：3D渲染器
 * ================================================
 */
public class D2Renderer implements GLSurfaceView.Renderer {

    private D2Triangle mD2Triangle;

    //模型视图投影矩阵
    private final float[] vPMatrix = new float[16];
    //投影矩阵（应用于onDrawFrame()方法中的对象坐标）
    private final float[] projectionMatrix = new float[16];
    //视图矩阵
    private final float[] viewMatrix = new float[16];

    //当Surface被创建的时候，GLSurfaceView会调用这个方法， 这发生在应用程序创建的第一次，
    // 并且当设备被唤醒或者用户从其他activity切换回去时，也会被调用。
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //背景框颜色，清空屏幕用的颜色
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        //
        mD2Triangle = new D2Triangle();
    }

    //在Surface创建以后，每次Surface尺寸变化后，这个方法都会调用
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //设置视窗的大小
        GLES20.glViewport(0, 0, width, height);
        //固定的写法
        //计算宽高比
        float ratio = (float) width / height;
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    //当绘制每一帧的时候会被调用
    @Override
    public void onDrawFrame(GL10 gl) {
        //画的背景色，清空屏幕，会调用glClearColor中定义的颜色来填充整个屏幕
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //设置观察视角
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, -3,//摄像机的坐标
                0f, 0f, 0f,//目标物的中心坐标
                0f, 1.0f, 0.0f);//相机方向
        //计算变换矩阵
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        //渲染绘制
        mD2Triangle.draw(vPMatrix);
    }
}
