package com.melvinhou.dimension2.ar.d3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.melvinhou.dimension2.ar.ShaderUtil;
import com.melvinhou.kami.util.FcUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Map;
import java.util.TreeMap;

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
public class D4Triangle {

    private static final String TAG = D4Triangle.class.getSimpleName();
    private static final String OBJ_NAME = "ar/models/girl.obj";
    private static final String TEXTURE_NAME = "ar/models/girl_body.jpg";
    private static final String VERTEX_SHADER_NAME = "ar/dome/dome.vert";
    private static final String FRAGMENT_SHADER_NAME = "ar/dome/dome.frag";
    //OpenGL程序,自定义渲染管线着色器程序id
    private int mProgram;
    private static final int COORDS_PER_VERTEX = 3;


    //纹理贴图数量,用于存储OpenGL生成纹理对象的ID
    private final int[] textures = new int[1];


    public D4Triangle() {
        try {
            compileAndLoadShaderProgram();
            loadTexture2();
            //数据转换
            byteTransition();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Set some default material properties to use for lighting.
    private float ambient = 0.3f;
    private float diffuse = 1.0f;
    private float specular = 1.0f;
    private float specularPower = 6.0f;

    /**
     * Sets the surface characteristics of the rendered model.
     *
     * @param ambient       Intensity of non-directional surface illumination.
     * @param diffuse       Diffuse (matte) surface reflectivity.
     * @param specular      Specular (shiny) surface reflectivity.
     * @param specularPower Surface shininess. Larger values result in a smaller, sharper specular
     *                      highlight.
     */
    public void setMaterialProperties(
            float ambient, float diffuse, float specular, float specularPower) {
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.specularPower = specularPower;
    }

    /**
     * Updates the object model matrix and applies scaling.
     *
     * @param modelMatrix A 4x4 model-to-world transformation matrix, stored in column-major order.
     * @param scaleFactor A separate scaling factor to apply before the {@code modelMatrix}.
     * @see android.opengl.Matrix
     */
    public void updateModelMatrix(float[] modelMatrix, float scaleFactor) {
        float[] scaleMatrix = new float[16];
        Matrix.setIdentityM(scaleMatrix, 0);
        scaleMatrix[0] = scaleFactor;
        scaleMatrix[5] = scaleFactor;
        scaleMatrix[10] = scaleFactor;
        Matrix.multiplyMM(this.modelMatrix, 0, modelMatrix, 0, scaleMatrix, 0);
    }

    // 注意:最后一个分量必须为零，以避免应用矩阵的平移部分.
    private static final float[] LIGHT_DIRECTION = new float[]{0.250f, 0.866f, 0.433f, 0.0f};
    private final float[] viewLightDirection = new float[4];
    private static final float[] DEFAULT_COLOR = {0f, 1.0f, 0f, 1.0f};

    public void draw(
            float[] cameraView,
            float[] cameraPerspective) {

        // 根据图像的平均强度计算光照。
        // 前三个分量是颜色缩放因子。最后一个是伽马空间中的平均像素强度。
        final float[] colorCorrectionRgba = new float[4];

//        ShaderUtil.checkGLError(TAG, "Before draw");

        // Build the ModelView and ModelViewProjection matrices
        // for calculating object position and light.
        Matrix.multiplyMM(modelViewMatrix, 0, cameraView, 0, modelMatrix, 0);
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, cameraPerspective, 0, modelViewMatrix, 0);

        GLES20.glUseProgram(mProgram);

        // Set the lighting environment properties.
        Matrix.multiplyMV(viewLightDirection, 0, modelViewMatrix, 0, LIGHT_DIRECTION, 0);
        normalizeVec3(viewLightDirection);
        GLES20.glUniform4f(
                lightingParametersUniform,
                viewLightDirection[0],
                viewLightDirection[1],
                viewLightDirection[2],
                1.f);
        GLES20.glUniform4fv(colorCorrectionParameterUniform, 1, colorCorrectionRgba, 0);

        // Set the object color property.
        GLES20.glUniform4fv(colorUniform, 1, DEFAULT_COLOR, 0);

        // Set the object material properties.
        GLES20.glUniform4f(materialParametersUniform, ambient, diffuse, specular, specularPower);

        // Attach the object texture.
        //激活纹理单元，GL_TEXTURE0代表纹理单元0，GL_TEXTURE1代表纹理单元1，以此类推。OpenGL使用纹理单元来表示被绘制的纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //绑定纹理到这个纹理单元
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        //把选定的纹理单元传给片段着色器中的u_TextureUnit，
        GLES20.glUniform1i(textureUniform, 0);


        // Set the vertex attributes.
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferId);

        GLES20.glVertexAttribPointer(
                positionAttribute, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, verticesBaseAddress);
        GLES20.glVertexAttribPointer(normalAttribute, 3, GLES20.GL_FLOAT, false, 0, normalsBaseAddress);
        GLES20.glVertexAttribPointer(
                texCoordAttribute, 2, GLES20.GL_FLOAT, false, 0, texCoordsBaseAddress);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        // Set the ModelViewProjection matrix in the shader.
        GLES20.glUniformMatrix4fv(modelViewUniform, 1, false, modelViewMatrix, 0);
        GLES20.glUniformMatrix4fv(modelViewProjectionUniform, 1, false, modelViewProjectionMatrix, 0);

        // Enable vertex arrays
        GLES20.glEnableVertexAttribArray(positionAttribute);
        GLES20.glEnableVertexAttribArray(normalAttribute);
        GLES20.glEnableVertexAttribArray(texCoordAttribute);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBufferId);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(positionAttribute);
        GLES20.glDisableVertexAttribArray(normalAttribute);
        GLES20.glDisableVertexAttribArray(texCoordAttribute);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

//        ShaderUtil.checkGLError(TAG, "After draw");
    }


    private static void normalizeVec3(float[] v) {
        float reciprocalLength = 1.0f / (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        v[0] *= reciprocalLength;
        v[1] *= reciprocalLength;
        v[2] *= reciprocalLength;
    }


    // Shader location: model view projection matrix.
    private int modelViewUniform;
    private int modelViewProjectionUniform;

    // Shader location: object attributes.
    private int positionAttribute;
    private int normalAttribute;
    private int texCoordAttribute;

    // Shader location: texture sampler.
    private int textureUniform;

    // Shader location: environment properties.
    private int lightingParametersUniform;

    // Shader location: material properties.
    private int materialParametersUniform;

    // Shader location: color correction property.
    private int colorCorrectionParameterUniform;

    // Shader location: object color property (to change the primary color of the object).
    private int colorUniform;

    // Shader location: depth texture.
    private int depthTextureUniform;

    // Shader location: transform to depth uvs.
    private int depthUvTransformUniform;

    // Shader location: the aspect ratio of the depth texture.
    private int depthAspectRatioUniform;

    // Depth-for-Occlusion parameters.
    private static final String USE_DEPTH_FOR_OCCLUSION_SHADER_FLAG = "USE_DEPTH_FOR_OCCLUSION";
    private boolean useDepthForOcclusion = false;

    private void compileAndLoadShaderProgram() throws IOException {
        // Compiles and loads the shader program based on the selected mode.
        Map<String, Integer> defineValuesMap = new TreeMap<>();
        defineValuesMap.put(USE_DEPTH_FOR_OCCLUSION_SHADER_FLAG, useDepthForOcclusion ? 1 : 0);

        final int vertexShader =
                ShaderUtil.loadGLShader(TAG, FcUtils.getContext(), GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_NAME);
        final int fragmentShader =
                ShaderUtil.loadGLShader(
                        TAG, FcUtils.getContext(), GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_NAME, defineValuesMap);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
        GLES20.glUseProgram(mProgram);

//        ShaderUtil.checkGLError(TAG, "Program creation");

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

        // Occlusion Uniforms.
        if (useDepthForOcclusion) {
            depthTextureUniform = GLES20.glGetUniformLocation(mProgram, "u_DepthTexture");
            depthUvTransformUniform = GLES20.glGetUniformLocation(mProgram, "u_DepthUvTransform");
            depthAspectRatioUniform = GLES20.glGetUniformLocation(mProgram, "u_DepthAspectRatio");
        }

//        ShaderUtil.checkGLError(TAG, "Program parameters");
    }

    /**
     * 加载纹理
     */
    private void loadTexture() throws IOException {

        //加载纹理资源，解码成bitmap形式
        Bitmap textureBitmap = BitmapFactory.decodeStream(FcUtils.getContext().getAssets().open(TEXTURE_NAME));

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

    /**
     * 加载纹理
     */
    private void loadTexture2() throws IOException {

        //加载纹理资源，解码成bitmap形式
        Bitmap textureBitmap = BitmapFactory.decodeStream(FcUtils.getContext().getAssets().open(TEXTURE_NAME));

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //textures.length代表生成纹理数量
        GLES20.glGenTextures(textures.length, textures, 0);
        for (int i = 0; i < textures.length; i++) {
            if (i > 0)
                textureBitmap = BitmapFactory.decodeStream(FcUtils.getContext().getAssets().open("ar/models/girl_body.jpg"));
            //第一个参数代表这是一个2D纹理，第二个参数就是OpenGL要绑定的纹理对象ID，也就是让OpenGL后面的纹理调用都使用此纹理对象
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[i]);
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
        }

        //bitmap已经被加载到OpenGL了，所以bitmap可释放掉了，防止内存泄露
        textureBitmap.recycle();
    }

    // 对象顶点缓冲变量.
    private int vertexBufferId;
    private int verticesBaseAddress;
    private int texCoordsBaseAddress;
    private int normalsBaseAddress;
    private int indexBufferId;
    private int indexCount;

    //临时的矩阵分配在这里减少分配的数量为每一帧。
    private final float[] modelMatrix = new float[16];
    private final float[] modelViewMatrix = new float[16];
    private final float[] modelViewProjectionMatrix = new float[16];

    /**
     * 位置数据转换
     * 转换数据结构 因为 Java 端使用的是大端字节序，而 OpenGL 使用的小端字节序，所以需要通过 ByteBuffer 去转换
     */
    public void byteTransition() throws IOException {
        // 加载模型文件
        InputStream objInputStream = FcUtils.getContext().getAssets().open(OBJ_NAME);
        Obj obj = ObjReader.read(objInputStream);
        obj = ObjUtils.convertToRenderable(obj);
        IntBuffer wideIndices = ObjData.getFaceVertexIndices(obj, 3);
        FloatBuffer vertices = ObjData.getVertices(obj);
        FloatBuffer texCoords = ObjData.getTexCoords(obj, 2);
        FloatBuffer normals = ObjData.getNormals(obj);


        // Convert int indices to shorts for GL ES 2.0 compatibility
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

        // Load vertex buffer
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

        // Load index buffer
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBufferId);
        indexCount = indices.limit();
        GLES20.glBufferData(
                GLES20.GL_ELEMENT_ARRAY_BUFFER, 2 * indexCount, indices, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

//        ShaderUtil.checkGLError(TAG, "OBJ buffer load");

        Matrix.setIdentityM(modelMatrix, 0);
    }


}
