package com.melvinhou.opengllibrary;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

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
 * = 分 类 说 明：彩三角,渲染一个opengl的图像，简单实践
 * ================================================
 */
class TriangleRainbowObj {

    //三角形绘制的数据  3d 坐标点
    static float triangleCoords[] = {// 以逆时针顺序
//            0.0f, 0.5f, 0.0f, // top
//            -0.5f, -0.5f, 0.0f, // bottom left
//            0.5f, -0.5f, 0.0f  // bottom right
            -0.5f, 1.0f, 0.0f,
            0.5f, 1.0f, 0.0f,
            -0.5f, 0.0f, 0.0f,
    };

    //三个顶点的颜色
    float objectColor[] = {
            1.0f, 0f, 0f, 1.0f,
            0f, 1.0f, 0f, 1.0f,
            0f, 0f, 1.0f, 1.0f
    };

    /*
     *  关于shader的变量类型(uniform，attribute和varying)的区别及使用，下面做下说明：
     *
     *  1. uniform:uniform变量在vertex和fragment两者之间声明方式完全一样，则它可以在vertex和fragment共享使用。
     *  （相当于一个被vertex和fragment shader共享的全局变量）uniform变量一般用来表示：变换矩阵，材质，光照参数和颜色等信息。
     *  在代码中通过GLES20.glGetUniformLocation(int program, String name)来获取属性值，
     *  并通过 GLES20.glUniformMatrix4fv(int location, int count, boolean transpose, float[] value, int offset);方法将数据传递给着色器。
     *
     *  2. attribute:这个变量只能在顶点着色器中使用(vertex Shader),用来表示顶点的数据，比如顶点坐标，顶点颜色，法线，纹理坐标等。
     *  在绘制的时候通过GLES20.glGetAttribLocation（int program, String name）来获取变量值，
     *  通过 GLES20.glEnableVertexAttribArray(int index)来启动句柄，
     *  最后通过 GLES20.glVertexAttribPointer(int indx,int size,int type,boolean normalized,int stride,java.nio.Buffer ptr)来设置图形数据。
     *
     *  3. varying变量：这个变量只能用来在vertex和fragment shader之间传递数据时使用，不可以通过代码获取其变量值。
     */

    /**
     * 修改顶点颜色过程：
     * 1. 修改着色器代码
     * 2. 将颜色值修改为float数组并转为floatBuffer
     * 3. 将获取的floatBuffer传递给顶点着色器。
     */
    //多彩三角形，顶点着色器
    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "uniform mat4 uMVPMatrix;" +
                    "varying  vec4 vColor;" +
                    "attribute vec4 aColor;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix*vPosition;" +
                    "  vColor=aColor;" +
                    "}";

    //多彩三角形，片段着色器
    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 vColor;" +//此时vColor是可变的
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";






    private final int COORDS_PER_VERTEX = 3;//此数组中每个顶点的坐标数
    private final int vertexStride = COORDS_PER_VERTEX * 4; //每个顶点4字节
    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;//顶点的数量

    private FloatBuffer vertexBuffer;//顶点坐标
    private FloatBuffer colorBuffer;//顶点颜色

    private int mProgram;//自定义渲染管线着色器程序id
    private int mPositionHandle;//位置的对象属性
    private int mColorHandle;//颜色的对象属性
    private int mMVPMatrixHandle;//视图转换的对象属性




    public void createOnGlThread() {
        /**
         * 位置数据转换
         */
        // 转换数据结构 因为 Java 端使用的是大端字节序，而 OpenGL 使用的小端字节序，所以需要通过 ByteBuffer 去转换。
        // 初始化ByteBuffer，长度为arr数组的长度*4，因为一个float占4个字节
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(triangleCoords.length * 4);
        // 数据分配的顺序
        byteBuffer.order(ByteOrder.nativeOrder());
        // 从ByteBuffer创建一个浮点缓冲区
        vertexBuffer = byteBuffer.asFloatBuffer();
        // 将坐标添加到native buffer
        vertexBuffer.put(triangleCoords);
        // 设置这块 Buffer 的位置,0第一个坐标
        vertexBuffer.position(0);
        /**
         * 颜色数据转换
         */
        ByteBuffer bb = ByteBuffer.allocateDirect(
                objectColor.length * 4);
        bb.order(ByteOrder.nativeOrder());
        colorBuffer = bb.asFloatBuffer();
        colorBuffer.put(objectColor);
        colorBuffer.position(0);


        compileAndLoadShaderProgram();
    }


    /**
     * 编译和加载着色器代码
     */
    private void compileAndLoadShaderProgram() {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // 创建空的OpenGL ES程序,将片元着色器和顶点着色器放到统一的OpenGL程序去管理
        mProgram = GLES20.glCreateProgram();
        // 添加顶点着色器到程序中
        GLES20.glAttachShader(mProgram, vertexShader);
        // 添加片段着色器到程序中
        GLES20.glAttachShader(mProgram, fragmentShader);
        // 创建OpenGL ES程序可执行文件
        GLES20.glLinkProgram(mProgram);


        //获取参数
        GLES20.glUseProgram(mProgram);
        // 获取顶点着色器的位置的句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        //三色模式
        //获取片元着色器的vColor成员的句柄
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
        // 得到形状的变换矩阵的句柄
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
    }

    /**
     * 执行着色器代码
     * 着色器的代码执行耗时久，一般放在构造方法中
     *
     * @param type
     * @param shaderCode
     * @return
     */
    private int loadShader(int type, String shaderCode) {
        // 创造顶点着色器类型(GLES20.GL_VERTEX_SHADER)
        // 或者是片段着色器类型 (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);
        // 添加上面编写的着色器代码并编译它
        GLES20.glShaderSource(shader, shaderCode);
        // 编译着色器
        GLES20.glCompileShader(shader);
        return shader;
    }

    public void doDraw(float[] mvpMatrix) {
        // 将程序添加到OpenGL ES环境
        GLES20.glUseProgram(mProgram);
        // 启用三角形顶点位置的句柄,必须调用这句话数据才能被GPU访问，
        // 因为数据虽然传递到了GPU，但是GPU仍然不能看到，这里是可以让GPU 正常访问这块数据
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        //准备三角形坐标数据， 3*4 --- 3 表示一个顶点的3个坐标 4代表字节 传递数据
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        //设置绘制三角形的颜色
        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glVertexAttribPointer(mColorHandle, 4,
                GLES20.GL_FLOAT, false,
                0, colorBuffer);
        // 将投影和视图转换传递给着色器
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        // 真正的渲染，绘制一个三角形，
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        // 解绑数据，让GPU可以去处理其他工作
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mColorHandle);
    }


}
