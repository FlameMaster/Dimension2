package com.melvinhou.dimension2.ar.opengl;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

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
 * = 分 类 说 明：opgl学习
 * ================================================
 */
class OpenglEsRenderer implements GLSurfaceView.Renderer {


    private Triangle mTriangle;
    private Square   mSquare;

    // 模型视图投影矩阵
    private final float[] vPMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];


    public volatile float mAngle;

    public float getAngle() {
        return mAngle;
    }

    public void setAngle(float angle) {
        mAngle = angle;
        Log.e("setAngle","angle: "+angle);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // initialize a triangle
        mTriangle = new Triangle();
        // initialize a square
        mSquare = new Square();
    }


    private float[] rotationMatrix = new float[16];
    @Override
    public void onDrawFrame(GL10 unused) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);


//        mTriangle.draw();



        float[] scratch = new float[16];
        int i =1;

        {
            // Set the camera position (View matrix)
            Matrix.setLookAtM(viewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

            // Calculate the projection and view transformation
            Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

            // Draw shape
            mTriangle.draw(vPMatrix);
        }



         if (i ==3) {
            // Create a rotation transformation for the triangle
            long time = SystemClock.uptimeMillis() % 4000L;
            float angle = 0.090f * ((int) time);
            Matrix.setRotateM(rotationMatrix, 0, angle, 0, 0, -1.0f);

            // Combine the rotation matrix with the projection and camera view
            // Note that the vPMatrix factor *must be first* in order
            // for the matrix multiplication product to be correct.
            Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0);

            // Draw triangle
            mTriangle.draw(scratch);
        }




        else if (i ==4) {
            // Create a rotation for the triangle
//             long time = SystemClock.uptimeMillis() % 4000L;
//             mAngle = 0.090f * ((int) time);
            Matrix.setRotateM(rotationMatrix, 0, -0.25f * mAngle, 0, 0, -1.0f);

            // Combine the rotation matrix with the projection and camera view
            // Note that the vPMatrix factor *must be first* in order
            // for the matrix multiplication product to be correct.
            Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0);

            // Draw triangle
            mTriangle.draw(scratch);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }



    //定义三角形
    public class Triangle {
//        private final String vertexShaderCode =
//                "attribute vec4 vPosition;" +
//                        "void main() {" +
//                        "  gl_Position = vPosition;" +
//                        "}";
    private final String vertexShaderCode =
        // This matrix member variable provides a hook to manipulate
        // the coordinates of the objects that use this vertex shader
        "uniform mat4 uMVPMatrix;" +
                "attribute vec4 vPosition;" +
                "void main() {" +
                // the matrix must be included as a modifier of gl_Position
                // Note that the uMVPMatrix factor *must be first* in order
                // for the matrix multiplication product to be correct.
                "  gl_Position = uMVPMatrix * vPosition;" +
                "}";

        // 用于访问和设置视图转换
        private int vPMatrixHandle;


        private final String fragmentShaderCode =
                "precision mediump float;" +
                        "uniform vec4 vColor;" +
                        "void main() {" +
                        "  gl_FragColor = vColor;" +
                        "}";

        private FloatBuffer vertexBuffer;
        private final int mProgram;

        // 数组中每个顶点的坐标数
         final int COORDS_PER_VERTEX = 3;
         float triangleCoords[] = {   // 以逆时针顺序:
                0.0f,  0.622008459f, 0.0f, // top
                -0.5f, -0.311004243f, 0.0f, // bottom left
                0.5f, -0.311004243f, 0.0f  // bottom right
        };

        // 用红色，绿色，蓝色和alpha(不透明度)值设置颜色
        float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

        public Triangle() {
            // 初始化形状坐标的顶点字节缓冲区
            ByteBuffer bb = ByteBuffer.allocateDirect(
                    //(坐标值的数量*每个浮点数4字节)
                    triangleCoords.length * 4);
            //使用设备硬件的本机字节顺序
            bb.order(ByteOrder.nativeOrder());

            // 从ByteBuffer创建一个浮点缓冲区
            vertexBuffer = bb.asFloatBuffer();
            // 添加坐标到FloatBuffer
            vertexBuffer.put(triangleCoords);
            // 设置缓冲区读取第一个坐标
            vertexBuffer.position(0);




            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                    vertexShaderCode);
            int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                    fragmentShaderCode);

            // 创建空的OpenGL ES程序
            mProgram = GLES20.glCreateProgram();

            // 添加顶点着色器程序
            GLES20.glAttachShader(mProgram, vertexShader);

            // 添加片段着色器程序
            GLES20.glAttachShader(mProgram, fragmentShader);

            // 创建OpenGL ES程序可执行文件
            GLES20.glLinkProgram(mProgram);



        }


        private int positionHandle;
        private int colorHandle;

        private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
        private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

        public void draw(float[] mvpMatrix) {// 传递计算出的变换矩阵
            // 向OpenGL ES环境中添加程序
            GLES20.glUseProgram(mProgram);

            // 获取顶点着色器的vPosition成员的句柄
            positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

            // 启用三角形顶点的句柄
            GLES20.glEnableVertexAttribArray(positionHandle);

            // 准备三角形坐标数据
            GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT, false,
                    vertexStride, vertexBuffer);

            // 获取片段着色器的vColor成员句柄
            colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

            // 设置绘制三角形的颜色
            GLES20.glUniform4fv(colorHandle, 1, color, 0);

            // 画一个三角形
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

            // 禁用顶点数组
            GLES20.glDisableVertexAttribArray(positionHandle);




            // 获得处理形状的变换矩阵
            vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

            // 将投影和视图转换传递给着色器
            GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0);

            // 画一个三角形
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

            // 禁用顶点数组
            GLES20.glDisableVertexAttribArray(positionHandle);
        }
    }


    public class Square {

        private FloatBuffer vertexBuffer;
        private ShortBuffer drawListBuffer;

        // number of coordinates per vertex in this array
         final int COORDS_PER_VERTEX = 3;
         float squareCoords[] = {
                -0.5f,  0.5f, 0.0f,   // top left
                -0.5f, -0.5f, 0.0f,   // bottom left
                0.5f, -0.5f, 0.0f,   // bottom right
                0.5f,  0.5f, 0.0f }; // top right

        private short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

        public Square() {
            // initialize vertex byte buffer for shape coordinates
            ByteBuffer bb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 4 bytes per float)
                    squareCoords.length * 4);
            bb.order(ByteOrder.nativeOrder());
            vertexBuffer = bb.asFloatBuffer();
            vertexBuffer.put(squareCoords);
            vertexBuffer.position(0);

            // initialize byte buffer for the draw list
            ByteBuffer dlb = ByteBuffer.allocateDirect(
                    // (# of coordinate values * 2 bytes per short)
                    drawOrder.length * 2);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(drawOrder);
            drawListBuffer.position(0);
        }
    }
}
