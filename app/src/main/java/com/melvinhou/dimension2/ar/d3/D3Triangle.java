package com.melvinhou.dimension2.ar.d3;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.ar.ShaderUtil;
import com.melvinhou.dimension2.ar.dome.MatrixState;
import com.melvinhou.kami.util.FcUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjFace;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;

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
public class D3Triangle {

    private static final String TAG = D3Triangle.class.getSimpleName();

    //    private static final String OBJ_NAME = "ar/dome/ch_t.obj";
    private static final String OBJ_NAME = "ar/models/andy.obj";
    private static final String TEXTURE_NAME = "ar/dome/dome.png";
    private static final String VERTEX_SHADER_NAME = "ar/dome/dome.vert";
    private static final String FRAGMENT_SHADER_NAME = "ar/dome/dome.frag";
//    private static final String VERTEX_SHADER_NAME = "ar/dome/vertex.sh";
//    private static final String FRAGMENT_SHADER_NAME = "ar/dome/frag.sh";


    //顶点坐标缓冲，顶点法向量缓冲，顶点纹理坐标缓冲,光源位置缓冲,相机位置缓冲
    private FloatBuffer vertexBuffer, normalBuffer, texCoordBuffer, lightBuffer, cameraBuffer;
    //OpenGL程序,自定义渲染管线着色器程序id
    private final int mProgram;


    //顶点位置属性引用,颜色属性引用,总变换矩阵引用,位置、旋转变换矩阵引用,光源位置引用,顶点纹理坐标属性引用,摄像机位置引用
    private int positionHandle, normalHandle, mvpMatrixHandle, mMatrixHandle, lightLocationHandle, texCoordHandle, cameraHandle;
    //用于存储OpenGL生成纹理对象的ID
    private final int[] textures = new int[1];
    //数组中每个顶点的坐标数
    private final int COORDS_PER_VERTEX = 3;
    //每个顶点4字节，COORDS_PER_VERTEX表示一个顶点的坐标数量，4代表字节
    private int vertexStride = COORDS_PER_VERTEX * 4;
    //片段的句柄
    private int colorHandle;
    //顶点数
    private int vertexCount;
    //渲染灰模颜色，RGB+Alpha
    private float color[] = {0f, 1.0f, 0f, 1.0f};


    public D3Triangle() {
        int vertexShader = 0;
        int fragmentShader = 0;
        try {
            //数据转换
            byteTransition();
            // 加载着色器
            vertexShader = ShaderUtil.loadGLShader(TAG, FcUtils.getContext(), GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_NAME);
            fragmentShader = ShaderUtil.loadGLShader(TAG, FcUtils.getContext(), GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }


        // 将片元着色器和顶点着色器放到统一的OpenGL程序去管理
        // 创建空的OpenGL ES程序
        mProgram = GLES20.glCreateProgram();
        // 添加顶点着色器程序
        GLES20.glAttachShader(mProgram, vertexShader);
        // 添加片段着色器程序
        GLES20.glAttachShader(mProgram, fragmentShader);
        // 创建OpenGL ES程序可执行文件
        GLES20.glLinkProgram(mProgram);


        //获取程序代码中的属性引用
        //uniform和attribute
        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");//顶点颜色属性引用
        positionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");//顶点位置属性引用
        normalHandle = GLES20.glGetAttribLocation(mProgram, "a_Normal");//顶点法向量属性引用
        texCoordHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoord");//程序中顶点纹理坐标属性引用
        mvpMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_ModelViewProjection");//处理形状的变换矩阵
        mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_ModelView");//位置、旋转变换矩阵


        //获取程序中顶点位置属性引用
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        //获取程序中顶点颜色属性引用
        maNormalHandle = GLES20.glGetAttribLocation(mProgram, "aNormal");
        //获取程序中总变换矩阵引用
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        //获取位置、旋转变换矩阵引用
        muMMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMMatrix");
        //获取程序中光源位置引用
        maLightLocationHandle = GLES20.glGetUniformLocation(mProgram, "uLightLocation");
        //获取程序中顶点纹理坐标属性引用
        maTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoor");
        //获取程序中摄像机位置引用
        maCameraHandle = GLES20.glGetUniformLocation(mProgram, "uCamera");
    }

    //    int mProgram;//自定义渲染管线着色器程序id
    int muMVPMatrixHandle;//总变换矩阵引用
    int muMMatrixHandle;//位置、旋转变换矩阵
    int maPositionHandle; //顶点位置属性引用
    int maNormalHandle; //顶点法向量属性引用
    int maLightLocationHandle;//光源位置属性引用
    int maCameraHandle; //摄像机位置属性引用
    int maTexCoordHandle; //顶点纹理坐标属性引用


    /**
     * 位置数据转换
     * 转换数据结构 因为 Java 端使用的是大端字节序，而 OpenGL 使用的小端字节序，所以需要通过 ByteBuffer 去转换
     */
    public void byteTransition() throws IOException {
        // 加载模型文件
        InputStream objInputStream = FcUtils.getContext().getAssets().open(OBJ_NAME);
        Obj obj = ObjReader.read(objInputStream);
        obj = ObjUtils.convertToRenderable(obj);
        IntBuffer wideIndices = ObjData.getFaceVertexIndices(obj, COORDS_PER_VERTEX);
        vertexBuffer = ObjData.getVertices(obj);
        texCoordBuffer = ObjData.getTexCoords(obj, 2);
        normalBuffer = ObjData.getNormals(obj);
        vertexCount = wideIndices.limit();


        float[] vertices = new float[vertexCount * 3];
        float[] normals = new float[vertexCount * 3];
        float[] texCoords = new float[vertexCount * 2];
        int vNum = 0, nNum = 0, tNum = 0;
        for (int i = 0; i < obj.getNumFaces(); i++) {
            ObjFace face = obj.getFace(i);
            for (int j = 0; j < face.getNumVertices(); j++) {
                vertices[vNum++] =
                        obj.getVertex(face.getVertexIndex(j)).get(0);
                vertices[vNum++] =
                        obj.getVertex(face.getVertexIndex(j)).get(1);
                vertices[vNum++] =
                        obj.getVertex(face.getVertexIndex(j)).get(2);

                if (obj.getNumNormals() > 0) {
                    normals[nNum++] =
                            obj.getNormal(face.getNormalIndex(j)).get(0);
                    normals[nNum++] =
                            obj.getNormal(face.getNormalIndex(j)).get(1);
                    normals[nNum++] =
                            obj.getNormal(face.getNormalIndex(j)).get(2);
                }
                texCoords[tNum++] =
                        obj.getTexCoord(face.getTexCoordIndex(j)).get(0);
                texCoords[tNum++] =
                        obj.getTexCoord(face.getTexCoordIndex(j)).get(1);
            }
        }
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


        ByteBuffer llbb = ByteBuffer.allocateDirect(3 * 4);
        llbb.order(ByteOrder.nativeOrder());//设置字节顺序
        lightPositionFB = llbb.asFloatBuffer();
        lightPositionFB.put(lightLocation);
        lightPositionFB.position(0);
    }


    public static float[] lightLocation = new float[]{40, 10, 20};//定位光光源位置
    public static FloatBuffer lightPositionFB;

    /**
     * @param mvpMatrix 传递来的计算后的变换矩阵
     */
    public void draw(float[] mvpMatrix, float[] mMatrix) {

        // 向OpenGL ES环境中添加程序
        GLES20.glUseProgram(mProgram);
        //将光源位置传入着色器程序
        GLES20.glUniform3fv(maLightLocationHandle, 1, lightPositionFB);

        // 传递静态数据uniform
        {
//        GLES20.glUniform4fv(colorHandle, 1, color, 0);// 设置绘制时的颜色
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);// 将投影和视图转换传递给着色器
            GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMatrix, 0);//将位置、旋转变换矩阵传入着色器
        }
        // 传递动态数据attribute
        {
            // 将顶点位置数据传入渲染管线
            GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
            GLES20.glVertexAttribPointer(normalHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, normalBuffer);
            //为画笔指定顶点纹理坐标数据
//            GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 2 * 4, texCoordBuffer);
            //启用顶点位置数据
            // 启用顶点的句柄，必须调用这句话 数据才能被GPU访问，因为数据虽然传递到了GPU但是GPU仍然不能看到，这里是可以让GPU正常访问这块数据
            GLES20.glEnableVertexAttribArray(positionHandle);
            GLES20.glEnableVertexAttribArray(normalHandle);
//            GLES20.glEnableVertexAttribArray(texCoordHandle);
        }

        // 渲染绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        // 解绑数据，让GPU可以去处理其他工作
//        GLES20.glDisableVertexAttribArray(positionHandle);
//        GLES20.glDisableVertexAttribArray(colorHandle);
//        GLES20.glDisableVertexAttribArray(vPMatrixHandle);
    }

    public void draw3D(float[] mvpMatrix) {
    }

    /**
     * 加载纹理
     */
    private void loadTexture() {

        //加载纹理资源，解码成bitmap形式
        Bitmap textureBitmap = null;
        try {
            textureBitmap = BitmapFactory.decodeStream(FcUtils.getContext().getAssets().open(TEXTURE_NAME));
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, textureBitmap, 0);
        //我们为纹理生成MIP贴图，提高渲染性能，但是可占用较多的内存
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        //现在OpenGL已经完成了纹理的加载，不需要再绑定此纹理了，后面使用此纹理时通过纹理对象的ID即可
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        //bitmap已经被加载到OpenGL了，所以bitmap可释放掉了，防止内存泄露
        textureBitmap.recycle();
    }
}
