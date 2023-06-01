package com.melvinhou.ar_sample.sample;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;

import com.google.ar.core.Anchor;
import com.google.ar.core.Camera;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Point;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.melvinhou.ar_sample.BackgroundRenderer;
import com.melvinhou.ar_sample.DisplayRotationHelper;
import com.melvinhou.ar_sample.PlaneRenderer;
import com.melvinhou.ar_sample.PointCloudRenderer;
import com.melvinhou.ar_sample.TapHelper;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.opengllibrary.d3.D3Config;
import com.melvinhou.opengllibrary.d3.entity.D3Group;

import java.io.IOException;
import java.util.ArrayList;

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
 * = 时 间：2023/5/25 0025 10:52
 * <p>
 * = 分 类 说 明：3D模型渲染器
 * ================================================
 */
public class ArSampleRenderer implements GLSurfaceView.Renderer {
    private final String TAG = ArSampleRenderer.class.getName();


    private Session mSession;
    private DisplayRotationHelper mDisplayRotationHelper;
    private TapHelper mTapHelper;
    public void setSession(Session session) {
        mSession = session;
    }
    public void setDisplayRotationHelper(DisplayRotationHelper displayRotationHelper) {
        mDisplayRotationHelper = displayRotationHelper;
    }
    public void setTapHelper(TapHelper tapHelper) {
        mTapHelper = tapHelper;
    }


    //


    private final PlaneRenderer planeRenderer = new PlaneRenderer();//显示对象-水平面
    private final PointCloudRenderer pointCloudRenderer = new PointCloudRenderer();//闪一闪的
    private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();//显示对象-背景,相机内容
    private final ArrayList<Anchor> anchors = new ArrayList<>();//锚点列表


    private D3Group mGroup;//组合模型
    private String mObjPath, mObjName;//文件路径和名称
    // 临时矩阵在这里直接分配空间，而不是对每一帧进行重新分配。这样可以减少每一帧渲染的压力。
    private float[] mMatrix = new float[16];//临时模型视图投影矩阵
    private final float[] projectionMatrix = new float[16];//投影矩阵（应用于onDrawFrame()方法中的对象坐标）
    private final float[] viewMatrix = new float[16];//视图矩阵
    private D3Config mConfig;//配置
    public ArSampleRenderer(String objPath, String objName) {
        mObjPath = objPath;
        mObjName = objName;
        //初始化配置
        mConfig = D3Config.instance(false);
        mGroup = new D3Group(ArSampleObj.class);
        // 初始化变换矩阵
        Matrix.setRotateM(mMatrix, 0, 0, 1, 0, 0);
    }

    /**
     * 渲染前调用
     * 使用着色器和材质属性初始化所有3D模型
     *
     * @param gl
     * @param config
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        // 准备渲染对象（rendering objects）. 这里会涉及到读取着色器（shaders）,所以可能会抛出  IOException.
        try {
            // 创建纹理（texture）并将其传递给ARCore的session 以在update（）期间来进行填充。
            backgroundRenderer.createOnGlThread(FcUtils.getContext());
            planeRenderer.createOnGlThread(FcUtils.getContext(), "d3/models/trigrid.png");
            pointCloudRenderer.createOnGlThread(FcUtils.getContext());
            //模型
            mGroup.load(mObjPath, mObjName);
        } catch (IOException e) {
            Log.e(TAG, "读取资产文件失败", e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 屏幕改变时调用
     *
     * @param gl
     * @param width
     * @param height
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mDisplayRotationHelper.onSurfaceChanged(width, height);
        GLES20.glViewport(0, 0, width, height);
    }

    /**
     * 时刻调用
     * 设置AR框架，让ARCore识别hitTest框架
     *
     * @param gl
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        // 清除屏幕以通知驱动程序它不应加载前一帧的任何像素。
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        if (mSession == null) return;
        // 通知ARCore session 视图大小已更改，以便可以正确调整透视矩阵和视频背景。
        mDisplayRotationHelper.updateSessionIfNeeded(mSession);
        try {
            mSession.setCameraTextureName(backgroundRenderer.getTextureId());
            // 从AR session获取当前帧，可以由此改变AR系统的状态。
            // 当配置设置为UpdateMode.BLOCKING（默认情况下）时，这将限制渲染到相机帧的速率。
            Frame frame = mSession.update();
            Camera camera = frame.getCamera();
            // 每帧处理一个Tap
            handleTap(frame, camera);
            // 对该帧绘制AR 的背景
            backgroundRenderer.draw(frame);
            // 如果相机不是出于跟踪状态下, 不要绘制3D对象。
            if (camera.getTrackingState() == TrackingState.PAUSED) return;
            // 获取投影矩阵。
            float[] projmtx = new float[16];
            camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);
            // 获取相机矩阵并绘制。
            float[] viewmtx = new float[16];
            camera.getViewMatrix(viewmtx, 0);
            // 根据图像的平均强度计算光照。
            // 前三个分量是颜色缩放因子。最后一个是伽马空间中的平均像素强度。
            final float[] colorCorrectionRgba = new float[4];
            frame.getLightEstimate().getColorCorrection(colorCorrectionRgba, 0);
            mConfig.COLOR_CORRECTION_RGBA[0]=colorCorrectionRgba[0];
            mConfig.COLOR_CORRECTION_RGBA[1]=colorCorrectionRgba[1];
            mConfig.COLOR_CORRECTION_RGBA[2]=colorCorrectionRgba[2];
            mConfig.COLOR_CORRECTION_RGBA[3]=colorCorrectionRgba[3];
            // 可视化跟踪点。PointCloud 指 一组观察到的3D点和置信度值。
            PointCloud pointCloud = frame.acquirePointCloud();
            pointCloudRenderer.update(pointCloud);
            pointCloudRenderer.draw(viewmtx, projmtx);
            // 应用程序负责在使用后释放PointCloud 资源。
            pointCloud.release();
            // 可视化平面。
            planeRenderer.drawPlanes(
                    mSession.getAllTrackables(Plane.class), camera.getDisplayOrientedPose(), projmtx);
            // 可视化触摸创建的锚点。
            for (Anchor anchor : anchors) {
                if (anchor.getTrackingState() != TrackingState.TRACKING) {
                    continue;
                }
                // 在世界空间中获取锚点(Anchor)的当前姿势（可以理解为点的位置和方向）。
                // 随着ARCore现实世界的评估，会通过调用session.update（）来更新锚点(Anchor)姿势。
                anchor.getPose().toMatrix(mMatrix, 0);
                // 更新并绘制模型
                Matrix.scaleM(mMatrix, 0, mConfig.scaleFactor, mConfig.scaleFactor, mConfig.scaleFactor);//缩放
                Matrix.multiplyMM(viewMatrix, 0, viewmtx, 0, mMatrix, 0);
                Matrix.multiplyMM(projectionMatrix, 0, projmtx, 0, viewMatrix, 0);
                mGroup.doDraw(viewMatrix, projectionMatrix);
            }
        } catch (Throwable t) {
            // 避免由于未处理的异常而使应用程序崩溃。
            Log.e(TAG, "OpenGL线程上的异常", t);
        }
    }
    /**
     * 每次获取当前帧时处理一次，与帧速率相比，handleTap频率相对较低。
     * 可以理解为其不会对于每一帧都进行获取当前帧操作，因为只有获取当前帧操作后才会进行handleTap 处理。
     * 所以处理handleTap 的频率一般是要低于实际帧率的。
     * @param frame
     * @param camera
     */
    private void handleTap(Frame frame, Camera camera) {
        MotionEvent tap = mTapHelper.poll();
        if (tap != null && camera.getTrackingState() == TrackingState.TRACKING) {
            for (HitResult hit : frame.hitTest(tap)) {
                // 检查是否有任何平面被击中，以及是否在平面多边形内部被击中
                Trackable trackable = hit.getTrackable();
                // 如果击中了平面或定向点，则创建锚点。
                if ((trackable instanceof Plane
                        && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())
                        && (PlaneRenderer.calculateDistanceToPlane(hit.getHitPose(), camera.getPose()) > 0))
                        || (trackable instanceof Point
                        && ((Point) trackable).getOrientationMode()
                        == Point.OrientationMode.ESTIMATED_SURFACE_NORMAL)) {
                    // 命中按深度排序。 考虑仅在平面或定向点上最接近的击中。
                    // 限制创建的对象数量。 这避免了渲染系统和ARCore的重复渲染。
                    if (anchors.size() >= 20) {
                        anchors.get(0).detach();
                        anchors.remove(0);
                    }
                    // 添加锚点告诉ARCore它应该在空间中跟踪这个位置。 在平面上创建此锚点，以将3D模型放置在相对于世界和平面的正确位置。
                    anchors.add(hit.createAnchor());
                    break;
                }
            }
        }
    }
}
