package com.melvinhou.opengllibrary;

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
 * = 时 间：2020/7/24 14:34
 * <p>
 * = 分 类 说 明：2D图像，新学习对于triang12
 * ================================================
 */
public class GLSampleRenderer implements GLSurfaceView.Renderer {

    //GLSurfaceView会在单独一条线程中调用渲染器的方法。
    //需要一个顶点着色器绘制形状和一个片段染色器的颜色
    //顶点着色器（Vertex Shader）顶点着色器是GPU上运行的小程序，
    //由名字可以知道，通过它来处理顶点，他用于渲染图形顶点的OpenGL ES图形代码。
    //顶点着色器可用来修改图形的位置，颜色，纹理坐标，不过不能用来创建新的顶点坐标。
    //片段着色器（Fragment Shader ) 用于呈现与颜色或纹理的形状的面的OpenGL ES代码。
    //项目（Program）包含要用于绘制一个或多个形状着色器的OpenGL ES的对象。



    TriangleObj mGLObject = new TriangleObj();
    TriangleRainbowObj mGLObject2 = new TriangleRainbowObj();
    TrianglePyramidObj mGLObject3 = new TrianglePyramidObj();





    //当Surface被创建的时候，GLSurfaceView会调用这个方法， 这发生在应用程序创建的第一次，
    // 并且当设备被唤醒或者用户从其他activity切换回去时，也会被调用。
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        // Set the background frame color
        //设置清空屏幕用的颜色，这里使用红色
//        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);//黑色

        //初始化
        mGLObject.createOnGlThread();
        mGLObject2 .createOnGlThread();
        mGLObject3.createOnGlThread();
    }

    //在Surface创建以后，每次Surface尺寸变化后，这个方法都会调用
    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        //设置视口的大小
        GLES20.glViewport(0, 0, width, height);

        //计算宽高比
        float ratio = (float) width / height;
        // 这个投影矩阵被应用于对象坐标在onDrawFrame（）方法中
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 3, 120);
        // 设置观察视角,摄像头位置 (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 7,//摄像机的坐标
                0f, 0f, 0f,//目标物的中心坐标
                0f, 1f, 0f);//相机方向


        // 计算投影和视图转换,将mProjectMatrix矩阵的值赋值给mMVPMatrix
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
    }

    //当绘制每一帧的时候会被调用
    @Override
    public void onDrawFrame(GL10 gl10) {
        //清空屏幕，会调用glClearColor中定义的颜色来填充整个屏幕
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);
        //绘制图像
        mGLObject.doDraw(mMVPMatrix);
        mGLObject2.doDraw(mMVPMatrix);
        mGLObject3.doDraw(mMVPMatrix);

    }





    //通常情况下，OpenGl中展示的视图和在Android上显示的图形会有偏差。
    //可以通过矩阵转换来解决这种问题，让OpenGl上的视图在任何android设备上显示的比例都是一样
    //使用OpenGl绘制的3D图形，需要展示在移动端2D设备上，这就是投影。Android OpenGl ES中有两种投影方式：一种是正交投影，一种是透视投影：
    //正交投影投影物体的带下不会随观察点的远近而发生变化，我们可以使用下面方法来执行正交投影：
    /*
    Matrix.orthoM (float[] m,           //接收正交投影的变换矩阵
                int mOffset,        //变换矩阵的起始位置（偏移量）
                float left,         //相对观察点近面的左边距
                float right,        //相对观察点近面的右边距
                float bottom,       //相对观察点近面的下边距
                float top,          //相对观察点近面的上边距
                float near,         //相对观察点近面距离
                float far)          //相对观察点远面距离
     */
    //透视投影：随观察点的距离变化而变化，观察点越远，视图越小，反之越大，我们可以通过如下方法来设置透视投影：
    /*
    Matrix.frustumM (float[] m,         //接收透视投影的变换矩阵
                    int mOffset,        //变换矩阵的起始位置（偏移量）
                    float left,         //相对观察点近面的左边距
                    float right,        //相对观察点近面的右边距
                    float bottom,       //相对观察点近面的下边距
                    float top,          //相对观察点近面的上边距
                    float near,         //相对观察点近面距离
                    float far)          //相对观察点远面距离
     */
    //相机视图,简单来说生活中拍照，站的高度，拿相机的位置，姿势不同，拍出来的照片也就不一样，
    // 相机视图就是来修改相机位置，观察方式以及相机的倾斜角度等属性。可以通过下面方法来修改相机视图属性：
    /*
    Matrix.setLookAtM (float[] rm,      //接收相机变换矩阵
                        int rmOffset,       //变换矩阵的起始位置（偏移量）
                        float eyeX,float eyeY, float eyeZ,   //相机位置
                        float centerX,float centerY,float centerZ,  //观察点位置
                        float upX,float upY,float upZ)  //up向量在xyz上的分量
    */
    //转换矩阵就是用来将数据转为OpenGl ES可用的数据字节，将相机视图和投影设置的数据相乘，便得到一个转换矩阵，
    // 然后再讲此矩阵传给顶点着色器，具体使用方法及参数说明如下：
    /*
    Matrix.multiplyMM (float[] result, //接收相乘结果
                int resultOffset,  //接收矩阵的起始位置（偏移量）
                float[] lhs,       //左矩阵
                int lhsOffset,     //左矩阵的起始位置（偏移量）
                float[] rhs,       //右矩阵
                int rhsOffset)     //右矩阵的起始位置（偏移量）
     */


    //定义一个投影
    // mMVPMatrix是“模型视图投影矩阵”的缩写。
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];




}
