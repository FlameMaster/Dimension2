package com.melvinhou.model3d_sample.sample;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.melvinhou.kami.lucas.CallBack;
import com.melvinhou.opengllibrary.d3.D3Config;
import com.melvinhou.opengllibrary.d3.D3Renderer;
import com.melvinhou.opengllibrary.d3.entity.D3Group;
import com.melvinhou.opengllibrary.d3.entity.D3Object;
import com.melvinhou.opengllibrary.utils.ObjHelper;

import java.util.Stack;

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
 * = 时 间：2022/7/4 0004 13:25
 * <p>
 * = 分 类 说 明：3D渲染器
 * ================================================
 */
public class D3SampleRenderer extends D3Renderer {


    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;//角度缩放比例


    //组合模型
    private D3Group mGroup;
    //文件路径和名称
    private String mObjPath, mObjName;
    //加载完成回调
    private CallBack<Boolean> mCallBack;

    public void setCreatedCallBack(CallBack<Boolean> callback) {
        this.mCallBack = callback;
    }

    //保护变换矩阵的栈
    public Stack<float[]> mStack = new Stack<>();
    //模型视图投影矩阵
    private float[] mMatrix = new float[16];
    //投影矩阵（应用于onDrawFrame()方法中的对象坐标）
    private final float[] projectionMatrix = new float[16];
    //视图矩阵
    private final float[] viewMatrix = new float[16];

    //旋转角度
    private volatile float mAngleX = 0, mAngleY = 0;
    //模型缩放大小
    private volatile float mScale = 1;
    //位移
    private volatile float mTranslationX = 0, mTranslationY = 0;

    public <T extends D3Object> D3SampleRenderer(Class<T> tClass, String objPath, String objName) {
        super(tClass, objPath, objName);
        mGroup = new D3Group(tClass);
        mObjPath = objPath;
        mObjName = objName;
    }

    public void updateAngleX(float angleX) {
        this.mAngleX += angleX * TOUCH_SCALE_FACTOR;
    }

    public void updateAngleY(float angleY) {
        this.mAngleY += angleY * TOUCH_SCALE_FACTOR;

        //角度限制
        if (mAngleY > 45) mAngleY = 45;
        if (mAngleY < -45) mAngleY = -45;
    }

    public void updateScale(float scale) {
        this.mScale *= scale;
    }

    public void updateTranslationX(float mTranslationX) {
        this.mTranslationX += mTranslationX;
    }

    public void updateTranslationY(float mTranslationY) {
        this.mTranslationY += mTranslationY * 20;
    }


    //当Surface被创建的时候，GLSurfaceView会调用这个方法， 这发生在应用程序创建的第一次，
    // 并且当设备被唤醒或者用户从其他activity切换回去时，也会被调用。
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //背景框颜色，清空屏幕用的颜色rgba
        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 0.0f);
        //打开深度检测
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        //打开背面剪裁
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        // 初始化变换矩阵
        Matrix.setRotateM(mMatrix, 0, 0, 1, 0, 0);

        //加载模型
        try {
            mGroup.load(mObjPath, mObjName);
            if (mCallBack!=null) mCallBack.callback(true);
        } catch (Exception e) {
            if (mCallBack!=null) mCallBack.callback(false);
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        mGroup.clear();
        ObjHelper.instance().clear();
    }

    //在Surface创建以后，每次Surface尺寸变化后，这个方法都会调用
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //当前配置
        D3Config config = D3Config.instance(false);
        //设置视窗的大小
        GLES20.glViewport(0, 0, width, height);
        //固定的写法
        //计算宽高比
        float ratio = (float) width / height;
        //计算产生透视投影矩阵
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1,
                //  near和far是一个立方体的前面和后面，需要结合相机的位置来设置
                //  near需要小于setLookAtM中的eyeZ，大于的话会导致目标在观察者后面,绘制的图像就会消失在镜头前
                //  far一般设置的比较大，太小会导致3D图形的投影矩阵没法容纳图形全部的背面,背面部分被隐藏
                config.projection_near, config.projection_far);
    }

    //当绘制每一帧的时候会被调用
    @Override
    public void onDrawFrame(GL10 gl) {
        //画的背景色，清空屏幕，会调用glClearColor中定义的颜色来填充整个屏幕
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        // 清除深度缓冲与颜色缓冲
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        //当前配置
        D3Config config = D3Config.instance(false);

        //设置观察视角,坐标系同屏幕坐标方向，z为深度
        Matrix.setLookAtM(viewMatrix, 0, config.look_eye_x, config.look_eye_y, config.look_eye_z,//摄像机在世界坐标系的位置
                config.look_view_x, config.look_view_y, config.look_view_z,//目标物中心在世界坐标系中的位置
                config.look_up_x, config.look_up_y, config.look_up_z);//相机方向，相机与目标物的连线的垂直线（摄像机为眼睛，头顶指向的方向）

        mStack.push(mMatrix.clone());
        objectTranslation(0, mTranslationY, 0);//位移
        objectScale(mScale, mScale, mScale);//缩放
        objectRotate(mAngleY, mAngleX, 0);//旋转
        float[] mMVPMatrix = new float[16];
        Matrix.multiplyMM(mMVPMatrix, 0, viewMatrix, 0, mMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, projectionMatrix, 0, mMVPMatrix, 0);
        mGroup.doDraw(mMatrix, mMVPMatrix);
        mMatrix = mStack.pop();
    }

    /**
     * 缩放模型
     *
     * @param x
     * @param y
     * @param z
     */
    private void objectScale(float x, float y, float z) {
        Matrix.scaleM(mMatrix, 0, x, y, z);
    }

    /**
     * 旋转模型
     *
     * @param x
     * @param y
     * @param z
     */
    private void objectRotate(float x, float y, float z) {
        Matrix.rotateM(mMatrix, 0, x, 1, 0, 0);
        Matrix.rotateM(mMatrix, 0, y, 0, 1, 0);
        Matrix.rotateM(mMatrix, 0, z, 0, 0, 1);
    }

    /**
     * 移动模型
     *
     * @param x
     * @param y
     * @param z
     */
    private void objectTranslation(float x, float y, float z) {
        Matrix.translateM(mMatrix, 0, x, y, z);
    }
}
