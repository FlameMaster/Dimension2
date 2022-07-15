package com.melvinhou.dimension2.ar.d3.model;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.melvinhou.dimension2.ar.ShaderUtil;
import com.melvinhou.dimension2.ar.d3.D3Config;
import com.melvinhou.kami.util.FcUtils;

import java.io.IOException;
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
 * = 时 间：2022/7/11 0011 14:06
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class D3Player1Obj implements D3Object{
    private static final String TAG = D3Player1Obj.class.getSimpleName();
    //着色器代码
    private static final String VERTEX_SHADER_NAME = "ar/shaders/player1_vert.sh";
    private static final String FRAGMENT_SHADER_NAME = "ar/shaders/player1_frag.sh";


    //顶点数
    private int vertexCount;

    //顶点坐标缓冲，顶点法向量缓冲，顶点纹理坐标缓冲,光源位置缓冲,相机位置缓冲
    private FloatBuffer vertexBuffer, normalBuffer, texCoordBuffer,lightPositionFB,cameraFB;

    // 纹理id
    protected int textureId;


    //自定义渲染管线着色器程序id
    int mProgram;

    //以下为着色器参数
    //model view projection matrix.模型视图和投影矩阵
    private int modelViewUniform;
    private int modelViewProjectionUniform;
    //object attributes.对象属性
    private int positionAttribute;
    private int normalAttribute;
    private int texCoordAttribute;
    //texture sampler.纹理取样器
    private int textureUniform;
    //environment properties.环境属性
    private int lightingUniform;
    //material properties.材料特性
    private int cameraUniform;
    //color correction property.色彩校正的财产
    private int opacityUniform;
    //object color property (to change the primary color of the object).对象颜色属性(更改对象的原色)
    private int colorUniform;

    public D3Player1Obj(String groupName, float[] vertices, float[] normals, float[] texCoords, Bitmap bitmap, D3Config config) {
        Log.e("模型绘制", "当前：" + groupName);
        initVertexData(vertices, normals, texCoords,config);
        initTexture(bitmap);
        //初始化shader
        try {
            initShader();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载纹理
     */
    private void initTexture(Bitmap bitmap) {
        int[] textures = new int[1];
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //textures.length代表生成纹理数量
        GLES20.glGenTextures(textures.length, textures, 0);
        //第一个参数代表这是一个2D纹理，第二个参数就是OpenGL要绑定的纹理对象ID，也就是让OpenGL后面的纹理调用都使用此纹理对象
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        //设置纹理过滤参数，GL_TEXTURE_MIN_FILTER代表纹理缩写的情况，GL_LINEAR_MIPMAP_LINEAR代表缩小时使用三线性过滤的方式，至于过滤方式以后再详解
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
        //GL_TEXTURE_MAG_FILTER代表纹理放大，GL_LINEAR代表双线性过滤
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        //加载实际纹理图像数据到OpenGL ES的纹理对象中，这个函数是Android封装好的，可以直接加载bitmap格式，
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, // 纹理类型，在OpenGL
                // ES中必须为GL10.GL_TEXTURE_2D
                0, // 纹理的层次，0表示基本图像层，可以理解为直接贴图
                bitmap, // 纹理图像
                0 // 纹理边框尺寸
        );
        //我们为纹理生成MIP贴图，提高渲染性能，但是可占用较多的内存
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        //现在OpenGL已经完成了纹理的加载，不需要再绑定此纹理了，后面使用此纹理时通过纹理对象的ID即可
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        //bitmap已经被加载到OpenGL了，所以bitmap可释放掉了，防止内存泄露
//        bitmap.recycle();
        textureId = textures[0];
    }

    private void initVertexData(float[] vertices, float[] normals, float[] texCoords, D3Config config) {
        vertexCount = vertices.length / 3;

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());//设置字节顺序
        vertexBuffer = vbb.asFloatBuffer();//转换为Float型缓冲
        vertexBuffer.put(vertices);//向缓冲区中放入顶点坐标数据
        vertexBuffer.position(0);//设置缓冲区起始位置

        ByteBuffer nbb = ByteBuffer.allocateDirect(normals.length * 4);
        nbb.order(ByteOrder.nativeOrder());//设置字节顺序
        normalBuffer = nbb.asFloatBuffer();//转换为Float型缓冲
        normalBuffer.put(normals);//向缓冲区中放入顶点坐标数据
        normalBuffer.position(0);//设置缓冲区起始位置

        ByteBuffer tbb = ByteBuffer.allocateDirect(texCoords.length * 4);
        tbb.order(ByteOrder.nativeOrder());//设置字节顺序
        texCoordBuffer = tbb.asFloatBuffer();//转换为Float型缓冲
        texCoordBuffer.put(texCoords);//向缓冲区中放入顶点坐标数据
        texCoordBuffer.position(0);//设置缓冲区起始位置

        //定位光光源位置
        ByteBuffer llbb = ByteBuffer.allocateDirect(3 * 4);
        llbb.order(ByteOrder.nativeOrder());//设置字节顺序
        lightPositionFB = llbb.asFloatBuffer();
        lightPositionFB.put(config.lightLocation);
        lightPositionFB.position(0);

        //摄像机位置
        float[] cameraLocation = new float[3];
        cameraLocation[0] = config.eye_x;
        cameraLocation[1] = config.eye_y;
        cameraLocation[2] = config.eye_z;
        ByteBuffer cbb = ByteBuffer.allocateDirect(3 * 4);
        cbb.order(ByteOrder.nativeOrder());//设置字节顺序
        cameraFB = cbb.asFloatBuffer();
        cameraFB.put(cameraLocation);
        cameraFB.position(0);
    }


    //初始化shader
    public void initShader() throws IOException {

        final int vertexShader =
                ShaderUtil.loadGLShader(TAG, FcUtils.getContext(), GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_NAME);
        final int fragmentShader =
                ShaderUtil.loadGLShader(TAG, FcUtils.getContext(), GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_NAME);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
        GLES20.glUseProgram(mProgram);

        ShaderUtil.checkGLError(TAG, "Program creation");

        modelViewUniform = GLES20.glGetUniformLocation(mProgram, "uMMatrix");
        modelViewProjectionUniform = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        positionAttribute = GLES20.glGetAttribLocation(mProgram, "aPosition");
        normalAttribute = GLES20.glGetAttribLocation(mProgram, "aNormal");
        texCoordAttribute = GLES20.glGetAttribLocation(mProgram, "aTexCoor");

        textureUniform = GLES20.glGetUniformLocation(mProgram, "sTextures");

        lightingUniform = GLES20.glGetUniformLocation(mProgram, "uLightLocation");
        cameraUniform = GLES20.glGetUniformLocation(mProgram, "uCamera");
        opacityUniform =
                GLES20.glGetUniformLocation(mProgram, "uOpacity");
        colorUniform = GLES20.glGetUniformLocation(mProgram, "uColor");
        ShaderUtil.checkGLError(TAG, "Program parameters");
    }



    @Override
    public void onDraw(
            float[] mMatrix,
            float[] mvpMatrix) {

        ShaderUtil.checkGLError(TAG, "Before draw");

        GLES20.glUseProgram(mProgram);


        //制定使用某套着色器程序
        GLES20.glUseProgram(mProgram);
        //将最终变换矩阵传入着色器程序
        GLES20.glUniformMatrix4fv(modelViewProjectionUniform, 1, false, mvpMatrix, 0);
        //将位置、旋转变换矩阵传入着色器程序
        GLES20.glUniformMatrix4fv(modelViewUniform, 1, false, mMatrix, 0);
        //将光源位置传入着色器程序
        GLES20.glUniform3fv(lightingUniform, 1, lightPositionFB);
        //将摄像机位置传入着色器程序
        GLES20.glUniform3fv(cameraUniform, 1, cameraFB);

        // 设置顶点属性.
        GLES20.glVertexAttribPointer(positionAttribute, 3, GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer);
        GLES20.glVertexAttribPointer(normalAttribute, 3, GLES20.GL_FLOAT, false, 3 * 4, normalBuffer);
        GLES20.glVertexAttribPointer(texCoordAttribute, 2, GLES20.GL_FLOAT, false, 2 * 4, texCoordBuffer);
        // 材质alpha
        GLES20.glUniform1f(opacityUniform, 1.0f);
        // 启用顶点数组
        GLES20.glEnableVertexAttribArray(positionAttribute);
        GLES20.glEnableVertexAttribArray(normalAttribute);
        GLES20.glEnableVertexAttribArray(texCoordAttribute);


        // 添加对象纹理.
        //激活纹理单元，GL_TEXTURE0代表纹理单元0，GL_TEXTURE1代表纹理单元1，以此类推。OpenGL使用纹理单元来表示被绘制的纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //绑定纹理到这个纹理单元
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        //把选定的纹理单元传给片段着色器中的u_TextureUnit，
        GLES20.glUniform1i(textureUniform, 0);


        // 渲染绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // 解绑数据，让GPU可以去处理其他工作
//        GLES20.glDisableVertexAttribArray(positionAttribute);
//        GLES20.glDisableVertexAttribArray(normalAttribute);
//        GLES20.glDisableVertexAttribArray(texCoordAttribute);

//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        ShaderUtil.checkGLError(TAG, "After draw");
    }

}
