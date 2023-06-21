package com.melvinhou.ar_sample.sample;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.opengllibrary.d3.D3Config;
import com.melvinhou.opengllibrary.d3.entity.D3Object;
import com.melvinhou.opengllibrary.utils.ShaderUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Map;
import java.util.TreeMap;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjGroup;

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
 * = 分 类 说 明：ar模型2，这个对于多贴图支持不友好
 * ================================================
 */
public class ArSampleObj2 implements D3Object {
    private static final String TAG = ArSampleObj2.class.getSimpleName();
    //着色器代码
    private static final String DOME_VERTEX_SHADER_NAME = "d3/shaders/dome.vert";
    private static final String DOME_FRAGMENT_SHADER_NAME = "d3/shaders/dome.frag";
    private static final String VERTEX_SHADER_NAME = "ar/shaders/ar_object.vert";
    private static final String FRAGMENT_SHADER_NAME = "ar/shaders/ar_object.frag";
    private String vertex_shader_name = VERTEX_SHADER_NAME;
    private String fragment_shader_name = FRAGMENT_SHADER_NAME;
    //使用深度遮挡的标注
    private static final String USE_DEPTH_FOR_OCCLUSION_SHADER_FLAG = "USE_DEPTH_FOR_OCCLUSION";

    // 对象顶点缓冲变量.
    private int vertexBufferId;
    private int verticesBaseAddress;
    private int texCoordsBaseAddress;
    private int normalsBaseAddress;
    private int indexBufferId;
    private int indexCount;
    private static final int COORDS_PER_VERTEX = 3;

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
    private int lightingParametersUniform;
    //material properties.材料特性
    private int materialParametersUniform;
    //color correction property.色彩校正的财产
    private int colorCorrectionParameterUniform;
    //object color property (to change the primary color of the object).对象颜色属性(更改对象的原色)
    private int colorUniform;


    @Override
    public void loadData(Obj obj, ObjGroup group, Bitmap texture) throws IOException {
        //加载纹理
        initTexture(texture);
        //加载着色器
        initShader();


        // 从OBJ中获取数据，作为直接缓冲区
        IntBuffer wideIndices = ObjData.getFaceVertexIndices(obj, 3);
        FloatBuffer vertices = ObjData.getVertices(obj);
        FloatBuffer texCoords = ObjData.getTexCoords(obj, 2);
        FloatBuffer normals = ObjData.getNormals(obj);
        initVertexData(wideIndices,vertices,texCoords,normals);
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
//        bitmap.recycle();//重复加载时别释放
        textureId = textures[0];
    }

    //初始化shader
    public void initShader() throws IOException {
        // 编译并加载基于所选模式的着色器程序
        Map<String, Integer> defineValuesMap = new TreeMap<>();
        defineValuesMap.put(USE_DEPTH_FOR_OCCLUSION_SHADER_FLAG, 0);//1使用0不使用

        final int vertexShader =
                ShaderUtil.loadGLShader(TAG, FcUtils.getContext(), GLES20.GL_VERTEX_SHADER, vertex_shader_name);
        final int fragmentShader =
                ShaderUtil.loadGLShader(TAG, FcUtils.getContext(), GLES20.GL_FRAGMENT_SHADER, fragment_shader_name,defineValuesMap);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
        GLES20.glUseProgram(mProgram);

        ShaderUtil.checkGLError(TAG, "Program creation");

        modelViewUniform = GLES20.glGetUniformLocation(mProgram, "u_ModelView");
        modelViewProjectionUniform = GLES20.glGetUniformLocation(mProgram, "u_ModelViewProjection");
        positionAttribute = GLES20.glGetAttribLocation(mProgram, "a_Position");
        normalAttribute = GLES20.glGetAttribLocation(mProgram, "a_Normal");
        texCoordAttribute = GLES20.glGetAttribLocation(mProgram, "a_TexCoord");

        textureUniform = GLES20.glGetUniformLocation(mProgram, "u_Texture");

        lightingParametersUniform = GLES20.glGetUniformLocation(mProgram, "u_LightingParameters");
        materialParametersUniform = GLES20.glGetUniformLocation(mProgram, "u_MaterialParameters");
        colorCorrectionParameterUniform =
                GLES20.glGetUniformLocation(mProgram, "u_ColorCorrectionParameters");
        colorUniform = GLES20.glGetUniformLocation(mProgram, "u_ObjColor");
        ShaderUtil.checkGLError(TAG, "Program parameters");
    }

    //顶点参数
    private void initVertexData(IntBuffer wideIndices,FloatBuffer vertices, FloatBuffer texCoords, FloatBuffer normals){

        // 为GL ES 2.0兼容性将int索引转换为short
        ShortBuffer indices =
                ByteBuffer.allocateDirect(2 * wideIndices.limit())
                        .order(ByteOrder.nativeOrder())
                        .asShortBuffer();
        while (wideIndices.hasRemaining()) {
            indices.put((short) wideIndices.get());
        }
        indices.rewind();

        int[] buffers = new int[2];
        GLES20.glGenBuffers(2, buffers, 0);
        vertexBufferId = buffers[0];
        indexBufferId = buffers[1];

        // 加载顶点缓冲器
        verticesBaseAddress = 0;
        texCoordsBaseAddress = verticesBaseAddress + 4 * vertices.limit();
        normalsBaseAddress = texCoordsBaseAddress + 4 * texCoords.limit();
        final int totalBytes = normalsBaseAddress + 4 * normals.limit();

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferId);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, totalBytes, null, GLES20.GL_STATIC_DRAW);
        GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER, verticesBaseAddress, 4 * vertices.limit(), vertices);
        GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER, texCoordsBaseAddress, 4 * texCoords.limit(), texCoords);
        GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER, normalsBaseAddress, 4 * normals.limit(), normals);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        // 加载索引缓冲
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBufferId);
        indexCount = indices.limit();
        GLES20.glBufferData(
                GLES20.GL_ELEMENT_ARRAY_BUFFER, 2 * indexCount, indices, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }


    private final float[] viewLightDirection = new float[4];


    @Override
    public void doDraw(
            float[] mMatrix,
            float[] mvpMatrix) {

        ShaderUtil.checkGLError(TAG, "Before draw");

        //当前配置
        D3Config config = D3Config.instance(false);

        GLES20.glUseProgram(mProgram);

        // 设置照明环境属性.
        Matrix.multiplyMV(viewLightDirection, 0, mMatrix, 0, config.LIGHT_DIRECTION, 0);
        normalizeVec3(viewLightDirection);
        GLES20.glUniform4f(
                lightingParametersUniform,
                viewLightDirection[0],
                viewLightDirection[1],
                viewLightDirection[2],
                1.f);
        GLES20.glUniform4fv(colorCorrectionParameterUniform, 1, config.COLOR_CORRECTION_RGBA, 0);

        // 设置对象材质属性.
        GLES20.glUniform4f(materialParametersUniform,
                config.ambient, config.diffuse, config.specular, config.specularPower);


        // 设置绘制对象颜色，将color数据传递给GPU替换vColor字段，指定的是GPU渲染的形状的颜色
        GLES20.glUniform4fv(colorUniform, 1, config.DEFAULT_COLOR, 0);
        GLES20.glUniformMatrix4fv(modelViewProjectionUniform, 1, false, mvpMatrix, 0);// 将投影和视图转换传递给着色器
        GLES20.glUniformMatrix4fv(modelViewUniform, 1, false, mMatrix, 0);//将位置、旋转变换矩阵传入着色器


        // 添加对象纹理.
        //激活纹理单元，GL_TEXTURE0代表纹理单元0，GL_TEXTURE1代表纹理单元1，以此类推。OpenGL使用纹理单元来表示被绘制的纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //绑定纹理到这个纹理单元
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        //把选定的纹理单元传给片段着色器中的u_TextureUnit，
        GLES20.glUniform1i(textureUniform, 0);


        // 设置顶点属性.
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferId);

        GLES20.glVertexAttribPointer(
                positionAttribute, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, verticesBaseAddress);
        GLES20.glVertexAttribPointer(normalAttribute, 3, GLES20.GL_FLOAT, false, 0, normalsBaseAddress);
        GLES20.glVertexAttribPointer(
                texCoordAttribute, 2, GLES20.GL_FLOAT, false, 0, texCoordsBaseAddress);


        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        // 启用顶点数组
        GLES20.glEnableVertexAttribArray(positionAttribute);
        GLES20.glEnableVertexAttribArray(normalAttribute);
        GLES20.glEnableVertexAttribArray(texCoordAttribute);



        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBufferId);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        // 解绑数据，让GPU可以去处理其他工作
        GLES20.glDisableVertexAttribArray(positionAttribute);
        GLES20.glDisableVertexAttribArray(normalAttribute);
        GLES20.glDisableVertexAttribArray(texCoordAttribute);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        ShaderUtil.checkGLError(TAG, "After draw");
    }


    private static void normalizeVec3(float[] v) {
        float reciprocalLength = 1.0f / (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        v[0] *= reciprocalLength;
        v[1] *= reciprocalLength;
        v[2] *= reciprocalLength;
    }

    @Override
    public void claer() {

    }
}
