package com.melvinhou.dimension2.ar;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

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
 * = 时 间：2020/7/24 14:34
 * <p>
 * = 分 类 说 明：3D模型加载
 * ================================================
 */
class Ar3Renderer2 implements GLSurfaceView.Renderer {

    //GLSurfaceView会在单独一条线程中调用渲染器的方法。
    //需要一个顶点着色器绘制形状和一个片段染色器的颜色
    //顶点着色器（Vertex Shader）顶点着色器是GPU上运行的小程序，
    // 由名字可以知道，通过它来处理顶点，他用于渲染图形顶点的OpenGL ES图形代码。
    // 顶点着色器可用来修改图形的位置，颜色，纹理坐标，不过不能用来创建新的顶点坐标。
    //片段着色器（Fragment Shader ) 用于呈现与颜色或纹理的形状的面的OpenGL ES代码。
    //项目（Program）包含要用于绘制一个或多个形状着色器的OpenGL ES的对象。







    //当Surface被创建的时候，GLSurfaceView会调用这个方法， 这发生在应用程序创建的第一次，
    // 并且当设备被唤醒或者用户从其他activity切换回去时，也会被调用。
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        //设置清空屏幕用的颜色，这里使用红色
//        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);//黑色

    }

    //在Surface创建以后，每次Surface尺寸变化后，这个方法都会调用
    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        //设置视口的大小
        GLES20.glViewport(0, 0, width, height);
//        mGLObject2.onSurfaceChanged(gl10, width, height);
    }

    //当绘制每一帧的时候会被调用
    @Override
    public void onDrawFrame(GL10 gl10) {
        //清空屏幕，会调用glClearColor中定义的颜色来填充整个屏幕
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);

    }
}
