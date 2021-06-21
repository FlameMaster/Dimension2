package com.melvinhou.dimension2.ar;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * 使用 OpenGL 绘制的图形
 * 原理 http://www.twinklingstar.cn/2015/1532/introduce-to-opengl/
 * https://blog.csdn.net/qq_32175491/article/details/79091647
 * https://blog.csdn.net/ylbs110/article/details/52074826
 * Matrix https://blog.csdn.net/kkae8643150/article/details/52805738
 * 顶点着色器：把一个单独的顶点作为输入。顶点着色器的目的是把3D点转换成另一种3D点
 * 片段着色器: 计算一个像素最终的颜色
 */
class Ar3Triangle {


    int mProgram;
    // 绘制的数据  3d 坐标点
    static float triangleCoords[] = {
            0.5f, 0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f,
    };
    private FloatBuffer vertexBuffer;
    // 定点着色器的 gl 语言代码  我们输入的数据会替代这个 vPosition
    // 顶点着色器是运行在 GPU 上的小代码
    // 这里乘以一个矩阵得到一个等腰三角形
    // https://www.jianshu.com/p/1c23ce604e4e vec4 包含4个浮点数的矢量  mat 代表 Matrix
    // attribute 该值传递给顶点着色器
    // uniform
    private final String vertextShaderCode = "attribute vec4 vPosition;" +
            "uniform mat4 vMatrix;\n" +
            "void main(){" +
            "gl_Position=vMatrix*vPosition;" +
            "}";
    // 片元着色器的代码
    private final String fragmentShaderCode = "precision mediump float;\n" +
            "uniform  vec4 vColor;\n" +
            "void main(){\n" +
            "gl_FragColor=vColor;\t\n" +
            "}";
    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    // RGB Alpha
    float color[] = {1.0f, 1.0f, 1.0f, 1.0f};

    public void onSurfaceChanged(GL10 gl, int width, int height) {

        //        固定的写法
        //        //计算宽高比
        float ratio = (float) width / height;
        //   在哪个地方 最后将 mProjectMatrix 矩阵的值赋值给 mMVPMatrix
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 3, 120);
        // 设置观察视角
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 7,//摄像机的坐标
                0f, 0f, 0f,//目标物的中心坐标
                0f, 1f, 0f);//相机方向
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
    }

    /**
     * 注意这里着色器的代码执行是很费时的，所以让它只执行一次。
     */
    public Ar3Triangle() {
        // 转换数据结构 因为 Java 端使用的是大端字节序，而 OpenGL 使用的小端字节序，所以需要通过 ByteBuffer 去转换。
        // 公式就是 传入数据的长度*数据的字节数
        // float 是 4 个字节
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(triangleCoords.length * 4);
        // 数据分配的顺序
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        // 把数据交给这个native buffer
        vertexBuffer.put(triangleCoords);
        // 设置这块 Buffer 的位置
        vertexBuffer.position(0);
        // 创建定点着色器
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
    }

    public void onDrawFrame(GL10 gl) {
        // 将程序添加到 OpenGL ES 的环境中
        GLES20.glUseProgram(mProgram);
        // 获取顶点着色器的位置的句柄
        int matrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        // 将 mMVPatrix 传递个 GPU 替代 vMatrix
        GLES20.glUniformMatrix4fv(matrixHandler, 1, false, mMVPMatrix, 0);

        int mPositionsHandler = GLES20.glGetAttribLocation(mProgram, "vPosition");
        /**
         * 必须调用这句话 数据才能被 GPU 访问
         * 因为数据虽然传递到了 GPU 但是GPU仍然不能看到，这里是可以让 GPU 正常访问这块数据
         */
        GLES20.glEnableVertexAttribArray(mPositionsHandler);
        // 3*4 --- 3 表示一个顶点的3个坐标 4代表字节 传递数据
        GLES20.glVertexAttribPointer(mPositionsHandler, 3, GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer);
        // uniform location 代表声明为 uniform 的某个变量
        int mColorHandler = GLES20.glGetUniformLocation(mProgram, "vColor");
        // 将color数据传递给 GPU 替换 vColor字段 指定的是GPU渲染的形状的颜色
        GLES20.glUniform4fv(mColorHandler, 1, color, 0);
        // 真正的渲染，绘制一个三角形，
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
        // 解绑数据，让GPU可以去处理其他工作
        GLES20.glDisableVertexAttribArray(mPositionsHandler);

    }
}
