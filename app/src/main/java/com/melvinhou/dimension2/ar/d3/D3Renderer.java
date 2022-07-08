package com.melvinhou.dimension2.ar.d3;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.melvinhou.dimension2.ar.ObjectRenderer;
import com.melvinhou.kami.util.FcUtils;

import java.io.IOException;

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
public class D3Renderer  implements GLSurfaceView.Renderer {

    private D4Triangle mD4Triangle;
    private D3Triangle mD3Triangle;
    private D2Triangle mD2Triangle;

    //模型视图投影矩阵
    private final float[] vPMatrix = new float[16];
    //投影矩阵（应用于onDrawFrame()方法中的对象坐标）
    private final float[] projectionMatrix = new float[16];
    //视图矩阵
    private final float[] viewMatrix = new float[16];


    public volatile float mAngle;

    public float getAngle() {
        return mAngle;
    }

    public void setAngle(float angle) {
        mAngle = angle;
        Log.e("setAngle","angle: "+angle);
    }

    //当Surface被创建的时候，GLSurfaceView会调用这个方法， 这发生在应用程序创建的第一次，
    // 并且当设备被唤醒或者用户从其他activity切换回去时，也会被调用。
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //背景框颜色，清空屏幕用的颜色
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        //打开深度检测
//        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
//        //打开背面剪裁
//        GLES20.glEnable(GLES20.GL_CULL_FACE);

        //
//        mD3Triangle = new D3Triangle();
//        mD2Triangle = new D2Triangle();
        mD4Triangle = new D4Triangle();
    }

    //在Surface创建以后，每次Surface尺寸变化后，这个方法都会调用
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //设置视窗的大小
        GLES20.glViewport(0, 0, width, height);
        //固定的写法
        //计算宽高比
        float ratio = (float) width / height;
        //计算产生透视投影矩阵
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1,
                //  near和far是一个立方体的前面和后面，需要结合相机的位置来设置
                //  near需要小于setLookAtM中的eyeZ，大于的话会导致目标在观察者后面,绘制的图像就会消失在镜头前
                //  far一般设置的比较大，太小会导致3D图形的投影矩阵没法容纳图形全部的背面,背面部分被隐藏
                2, 10);
    }

    private float[] rotationMatrix = new float[16];
    private final float[] mMatrix = new float[16];
    //当绘制每一帧的时候会被调用
    @Override
    public void onDrawFrame(GL10 gl) {
        //画的背景色，清空屏幕，会调用glClearColor中定义的颜色来填充整个屏幕
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        //设置观察视角,坐标系同屏幕坐标方向，z为深度
        Matrix.setLookAtM(viewMatrix, 0, 0, 2, 4f,//摄像机在世界坐标系的位置
                0f, 1.5f, 0f,//目标物中心在世界坐标系中的位置
                0f, 1f, 0f);//相机方向，相机与目标物的连线的垂直线（摄像机为眼睛，头顶指向的方向）
        //计算变换矩阵
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        //渲染绘制
//        mD2Triangle.draw(vPMatrix);
//        mD3Triangle.draw(vPMatrix);



        Matrix.setRotateM(rotationMatrix, 0, -0.25f * mAngle, 0, 1, 0);
        float[] scratch = new float[16];
        Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0);
        Matrix.setRotateM(mMatrix, 0, 0, 1, 0, 0);
        Matrix.scaleM(mMatrix, 0, 4, 4, 4);
//        mD3Triangle.draw(scratch,mMatrix);
        mD4Triangle.draw(mMatrix,scratch);


    }
}
