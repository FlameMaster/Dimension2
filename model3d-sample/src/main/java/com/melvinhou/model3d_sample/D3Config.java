package com.melvinhou.model3d_sample;

/**
 * GL参数保存
 */
public class D3Config {


    //0:dome,1:player1,2:google
    public int texture_type = 0;

    /**
     * near far
     */
    public float projection_near = 2;
    public float projection_far = 1000;

    /**
     * camera position
     */
    public float eye_x = 0f;
    public float eye_y = 15f;
    public float eye_z = 30f;
    public float view_center_x = 0f;
    public float view_center_y = 10f;
    public float view_center_z = -1f;


    //模型默认颜色，用红色，绿色，蓝色和alpha(不透明度)值设置颜色
    public final float[] DEFAULT_COLOR
            = new float[]{0.63671875f, 0.76953125f, 0.22265625f, 1.0f};

    // 注意:最后一个分量必须为零，以避免应用矩阵的平移部分(Dome)
    public final float[] LIGHT_DIRECTION
            = new float[]{0.250f, 0.866f, 0.433f, 0.0f};
    //颜色修正(Dome)
    public final float[] COLOR_CORRECTION_RGBA
            = new float[]{1f, 0.9f, 0.8f, 1f};//rgba

    // 设置一些用于照明的默认材质属性(Dome)
    public float ambient = 0.1f;//环境
    public float diffuse = 0.1f;//扩散
    public float specular = 0.0f;//镜面
    public float specularPower = 6.0f;//高光功率


    //光源位置（Player1）
    public float[] lightLocation = new float[]{0, 0, 100};


    D3Config() {

    }

    D3Config(int rextureType, float near, float far,
             float eyeX, float eyeY, float eyeZ,
             float centerX, float centerY, float centerZ) {
        texture_type = rextureType;
        projection_near = near;
        projection_far = far;
        eye_x = eyeX;
        eye_y = eyeY;
        eye_z = eyeZ;
        view_center_x = centerX;
        view_center_y = centerY;
        view_center_z = centerZ;
    }


}
