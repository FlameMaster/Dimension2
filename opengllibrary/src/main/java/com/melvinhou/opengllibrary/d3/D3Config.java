package com.melvinhou.opengllibrary.d3;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/5/25 0025 16:30
 * <p>
 * = 分 类 说 明：模型配置
 * ================================================
 */
public class D3Config {

    private static D3Config mConfig;

    public static D3Config instance(boolean isNew) {
        if (isNew || mConfig == null) mConfig = new D3Config();
        return mConfig;
    }

    private D3Config() {

    }

    /**
     * near far
     */
    public float projection_near = 2;
    public float projection_far = 1000;

    /**
     * camera position
     */
    public float look_eye_x = 0f;
    public float look_eye_y = 15f;
    public float look_eye_z = 30f;
    public float look_view_x = 0f;
    public float look_view_y = 10f;
    public float look_view_z = -1f;
    public float look_up_x = 0f;
    public float look_up_y = 1f;
    public float look_up_z = 0f;


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

    public float scaleFactor = 1.0f;//缩放大小


}
