package com.melvinhou.dimension2.ar.d3;

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
 * = 时 间：2022/7/4 0004 13:39
 * <p>
 * = 分 类 说 明：2D图形
 * ================================================
 */
public class D2Triangle {

    //GLSurfaceView会在单独一条线程中调用渲染器的方法。
    //需要一个顶点着色器绘制形状和一个片段染色器的颜色
    //顶点着色器（Vertex Shader）顶点着色器是GPU上运行的小程序，
    // 由名字可以知道，通过它来处理顶点，他用于渲染图形顶点的OpenGL ES图形代码。
    // 顶点着色器可用来修改图形的位置，颜色，纹理坐标，不过不能用来创建新的顶点坐标。
    //片段着色器（Fragment Shader ) 用于呈现与颜色或纹理的形状的面的OpenGL ES代码。
    //项目（Program）包含要用于绘制一个或多个形状着色器的OpenGL ES的对象。


    // 定点着色器的 gl 语言代码  我们输入的数据会替代这个 vPosition
    // 顶点着色器是运行在 GPU 上的小代码
    // 这里乘以一个矩阵得到一个等腰三角形
    // vec4 包含4个浮点数的矢量  mat 代表 Matrix
    // attribute 该值传递给顶点着色器
    // uniform表示不可变更，varying表示可以变更
    //顶底着色器代码
    private final String vertexShaderCode =
            //这个矩阵成员变量提供了一个钩子来操作使用这个顶点着色器的对象的坐标
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "varying  vec4 vColor;"+//可变的vColor
                    "attribute vec4 aColor;"+//可变的vColor
                    "void main() {" +
                    //注意:为了使矩阵的乘法乘积正确，uMVPMatrix因子*必须是第一个*。
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  vColor=aColor;"+//可变的vColor
                    "}";
    //片段着色器代码
    private final String fragmentShaderCode =
            "precision mediump float;" +
//                    "uniform vec4 vColor;" +//此时vColor是不改变的
                    "varying vec4 vColor;" +//此时vColor是可变的
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    //三角形坐标
    private float triangleCoords[] = {   // 以逆时针顺序:
            0.0f, 0.622008459f, 0.0f, // top
            -0.5f, -0.311004243f, 0.0f, // bottom left
            0.5f, -0.311004243f, 0.0f  // bottom right
    };
    //顶点缓冲区
    private FloatBuffer vertexBuffer;
    //OpenGL程序
    private final int mProgram;


    //顶点的句柄
    private int positionHandle;
    //数组中每个顶点的坐标数
    private final int COORDS_PER_VERTEX = 3;
    //每个顶点4字节，COORDS_PER_VERTEX表示一个顶点的坐标数量，4代表字节
    private final int vertexStride = COORDS_PER_VERTEX * 4;
    //片段的句柄
    private int colorHandle;
    //顶点数
    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    //颜色，RGB+Alpha
    private float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };
    //顶点颜色缓冲区
    private FloatBuffer colorBuffer;
    //三个顶点不同颜色
    private float color2[] = {
            1.0f, 0f, 0f, 1.0f ,
            0f, 1.0f, 0f, 1.0f ,
            0f, 0f, 1.0f, 1.0f
    };

    //处理形状的变换矩阵
    private int vPMatrixHandle;

    public D2Triangle() {

        //数据转换
        byteTransition();

        // 加载着色器
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);


        // 将片元着色器和顶点着色器放到统一的OpenGL程序去管理
        // 创建空的OpenGL ES程序
        mProgram = GLES20.glCreateProgram();
        // 添加顶点着色器程序
        GLES20.glAttachShader(mProgram, vertexShader);
        // 添加片段着色器程序
        GLES20.glAttachShader(mProgram, fragmentShader);
        // 创建OpenGL ES程序可执行文件
        GLES20.glLinkProgram(mProgram);


        //颜色转换
        colorTransition() ;
    }


    /**
     * 位置数据转换
     * 转换数据结构 因为 Java 端使用的是大端字节序，而 OpenGL 使用的小端字节序，所以需要通过 ByteBuffer 去转换
     */
    public void  byteTransition() {

        // 初始化ByteBuffer，长度为arr数组的长度*4，因为一个float占4个字节
        // 初始化形状坐标的顶点字节缓冲区
        ByteBuffer bb = ByteBuffer.allocateDirect(triangleCoords.length * 4);//坐标值的数量*每个浮点数4字节
        // 使用设备硬件的本机字节顺序
        bb.order(ByteOrder.nativeOrder());
        // 从ByteBuffer创建一个浮点缓冲区
        vertexBuffer = bb.asFloatBuffer();
        // 将坐标添加到FloatBuffer
        vertexBuffer.put(triangleCoords);
        // 设置缓冲区来读取第一个坐标
        vertexBuffer.position(0);
    }

    /**
     * 颜色数据转换
     */
    public void colorTransition() {
        ByteBuffer dd = ByteBuffer.allocateDirect(
                color2.length * 4);
        dd.order(ByteOrder.nativeOrder());
        colorBuffer = dd.asFloatBuffer();
        colorBuffer.put(color2);
        colorBuffer.position(0);
    }

    /**
     * 加载着色器
     * 着色器的代码执行很费时，所以让它只执行一次，一般将执行代码的逻辑写带图形类的构造方法中。
     *
     * @param type       创建一个顶点着色器类型(GLES20.GL_VERTEX_SHADER)或一个片段着色器类型(GLES20.GL_FRAGMENT_SHADER)
     * @param shaderCode 着色器源代码
     * @return
     */
    public int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        //将源代码添加到着色器并编译
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }


    /**
     * @param mvpMatrix 传递来的计算后的变换矩阵
     */
    public void draw(float[] mvpMatrix) {
        // 向OpenGL ES环境中添加程序
        GLES20.glUseProgram(mProgram);


        // 获取顶点着色器的vPosition成员的句柄
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        // 启用顶点的句柄，必须调用这句话 数据才能被GPU访问，因为数据虽然传递到了GPU但是GPU仍然不能看到，这里是可以让GPU正常访问这块数据
        GLES20.glEnableVertexAttribArray(positionHandle);
        // 传递数据
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);


        int colorType = 2;
        if (colorType==1) {
            //单色模式
            // 获取片段着色器的vColor成员句柄，uniformlocation代表声明为uniform的某个变量
            colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
            // 设置绘制颜色，将color数据传递给GPU替换vColor字段，指定的是GPU渲染的形状的颜色
            GLES20.glUniform4fv(colorHandle, 1, color, 0);
        }else {
            //三色模式
            //获取片元着色器的vColor成员的句柄
            colorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
            //设置绘制三角形的颜色
            GLES20.glEnableVertexAttribArray(colorHandle);
            GLES20.glVertexAttribPointer(colorHandle, 4,
                    GLES20.GL_FLOAT, false, 0, colorBuffer);
        }

        // 获得处理形状的变换矩阵
        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        // 将投影和视图转换传递给着色器
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0);



        // 渲染绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        // 解绑数据，让GPU可以去处理其他工作
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(colorHandle);
//        GLES20.glDisableVertexAttribArray(vPMatrixHandle);
    }
}
