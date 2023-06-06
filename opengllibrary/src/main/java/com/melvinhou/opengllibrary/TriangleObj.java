package com.melvinhou.opengllibrary;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * 使用 OpenGL 绘制的图形
 * 原理 http://www.twinklingstar.cn/2015/1532/introduce-to-opengl/
 * https://blog.csdn.net/qq_32175491/article/details/79091647
 * https://blog.csdn.net/ylbs110/article/details/52074826
 * Matrix https://blog.csdn.net/kkae8643150/article/details/52805738
 */

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/5/25 0025 10:52
 * <p>
 * = 分 类 说 明：使用 OpenGL 绘制的三角形
 * ================================================
 */
class TriangleObj {


    // 绘制的数据  3d 坐标点
    private float triangleCoords[] = {
            0.5f, 1.0f, 0.0f,
            -0.5f, 0.0f, 0.0f,
            0.5f, 0.0f, 0.0f,
    };
    // 顶点着色器：把一个单独的顶点作为输入。顶点着色器的目的是把3D点转换成另一种3D点
    // 顶点着色器的 gl 语言代码  我们输入的数据会替代这个 aPosition
    // 顶点着色器是运行在 GPU 上的小代码
    // 这里乘以一个矩阵得到一个等腰三角形
    // https://www.jianshu.com/p/1c23ce604e4e vec4 包含4个浮点数的矢量  mat 代表 Matrix
    // attribute 该值传递给顶点着色器
    // uniform
    private final String vertextShaderCode =
            "attribute vec4 aPosition;" +
            "uniform mat4 uMatrix;\n" +
            "void main(){" +
            "gl_Position=uMatrix*aPosition;" +
            "}";
    // 片段着色器的代码，计算一个像素最终的颜色
    private final String fragmentShaderCode =
            "precision mediump float;\n" +
            "uniform  vec4 uColor;\n" +
            "void main(){\n" +
            "gl_FragColor=uColor;\t\n" +
            "}";

    //object attributes.对象属性
    private int positionAttribute;
    private int matrixUniform;
    private int colorUniform;



    //自定义渲染管线着色器程序id
    int mProgram;
    //顶点坐标缓冲
    private FloatBuffer vertexBuffer;
    //模型颜色，RGB+A
    float objColor[] = {1.0f, 0.0f, 0.0f, 1.0f};

    /**
     * 注意这里着色器的代码执行是很费时的，所以让它只执行一次。
     */
    public void createOnGlThread() {
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


        compileAndLoadShaderProgram();
    }

    /**
     * 编译和加载着色器代码
     */
    private void compileAndLoadShaderProgram(){
        /**
         * 创建定点着色器
         */
        int shader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        // 设置定点着色器的代码
        GLES20.glShaderSource(shader, vertextShaderCode);
        // 编译定点着色器
        GLES20.glCompileShader(shader);
        /**
         * 创建片元着色器
         */
        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShader, fragmentShaderCode);
        GLES20.glCompileShader(fragmentShader);

        /**
         * 将片元着色器和顶点着色器放到统一的OpenGL程序去管理
         */
        mProgram = GLES20.glCreateProgram();
        // 将顶点着色器和片元着色器交给 OpenGL 的程序
        GLES20.glAttachShader(mProgram, shader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        // 创建 OpenGL ES 的可执行程序
        GLES20.glLinkProgram(mProgram);


        //获取参数
        GLES20.glUseProgram(mProgram);
        // 获取顶点着色器的位置的句柄
        positionAttribute = GLES20.glGetAttribLocation(mProgram, "aPosition");
        // 得到形状的变换矩阵的句柄
        matrixUniform = GLES20.glGetUniformLocation(mProgram, "uMatrix");
        // uniform location 代表声明为 uniform 的某个变量
        colorUniform = GLES20.glGetUniformLocation(mProgram, "uColor");
    }

    public void doDraw(float[] mvpMatrix) {
        // 将程序添加到 OpenGL ES 的环境中
        GLES20.glUseProgram(mProgram);
        // 将 mMVPatrix 传递个 GPU 替代 uMatrix
        GLES20.glUniformMatrix4fv(matrixUniform, 1, false, mvpMatrix, 0);
        /**
         * 必须调用这句话 数据才能被 GPU 访问
         * 因为数据虽然传递到了 GPU 但是GPU仍然不能看到，这里是可以让 GPU 正常访问这块数据
         */
        GLES20.glEnableVertexAttribArray(positionAttribute);
        // 3*4 --- 3 表示一个顶点的3个坐标 4代表字节 传递数据
        GLES20.glVertexAttribPointer(positionAttribute, 3, GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer);
        // 将color数据传递给 GPU 替换 uColor字段 指定的是GPU渲染的形状的颜色
        GLES20.glUniform4fv(colorUniform, 1, objColor, 0);
        // 真正的渲染，绘制一个三角形，
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        // 解绑数据，让GPU可以去处理其他工作
        GLES20.glDisableVertexAttribArray(positionAttribute);

    }
}
