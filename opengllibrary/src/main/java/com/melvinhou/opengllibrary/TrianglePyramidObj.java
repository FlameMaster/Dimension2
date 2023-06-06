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
 * = 分 类 说 明：金字塔
 * ================================================
 */
class TrianglePyramidObj {


    // 绘制的数据  3d 坐标点
    private float triangleCoords[] = {
            0.0f, 0.0f, 0.0f,
            0.5f, -1.0f, 0.0f,
            -0.5f, -1.0f, 0.0f,
            -0.5f, -0.5f, -1.0f,
    };
    float objColor[] = {0.7f, 0.7f, 0.7f, 1.0f};
    private final String vertextShaderCode =
            "attribute vec4 aPosition;" +
            "uniform mat4 uMatrix;\n" +
            "void main(){" +
            "gl_Position=uMatrix*aPosition;" +
            "}";
    private final String fragmentShaderCode =
            "precision mediump float;\n" +
            "uniform  vec4 uColor;\n" +
            "void main(){\n" +
            "gl_FragColor=uColor;\t\n" +
            "}";


    private final int COORDS_PER_VERTEX = 3;
    private final int vertexStride = COORDS_PER_VERTEX * 4;
    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private int positionAttribute;
    private int matrixUniform;
    private int colorUniform;

    int mProgram;
    private FloatBuffer vertexBuffer;



    public void createOnGlThread() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(triangleCoords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(triangleCoords);
        vertexBuffer.position(0);


        compileAndLoadShaderProgram();
    }
    private void compileAndLoadShaderProgram(){
        int shader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(shader, vertextShaderCode);
        GLES20.glCompileShader(shader);
        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShader, fragmentShaderCode);
        GLES20.glCompileShader(fragmentShader);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, shader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);


        GLES20.glUseProgram(mProgram);
        positionAttribute = GLES20.glGetAttribLocation(mProgram, "aPosition");
        matrixUniform = GLES20.glGetUniformLocation(mProgram, "uMatrix");
        colorUniform = GLES20.glGetUniformLocation(mProgram, "uColor");
    }

    public void doDraw(float[] mvpMatrix) {
        GLES20.glUseProgram(mProgram);
        GLES20.glUniformMatrix4fv(matrixUniform, 1, false, mvpMatrix, 0);
        GLES20.glEnableVertexAttribArray(positionAttribute);
        GLES20.glVertexAttribPointer(positionAttribute, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
        GLES20.glUniform4fv(colorUniform, 1, objColor, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        GLES20.glDisableVertexAttribArray(positionAttribute);

    }
}
