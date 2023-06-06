package com.melvinhou.ar_sample.dome;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import com.google.ar.core.Anchor;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Point;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.melvinhou.ar_sample.BackgroundRenderer;
import com.melvinhou.ar_sample.DisplayRotationHelper;
import com.melvinhou.ar_sample.ObjectRenderer;
import com.melvinhou.ar_sample.PlaneRenderer;
import com.melvinhou.ar_sample.PointCloudRenderer;
import com.melvinhou.ar_sample.TapHelper;
import com.melvinhou.kami.util.FcUtils;

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
public class ArSampleRenderer2 implements GLSurfaceView.Renderer {
    private final String TAG = ArSampleRenderer2.class.getName();
    private DisplayRotationHelper displayRotationHelper;//显示旋转助手
    private TapHelper tapHelper;//手势助手-将主线程的手势存储
    //显示对象-水平面
    private final PlaneRenderer planeRenderer = new PlaneRenderer();
    private final PointCloudRenderer pointCloudRenderer = new PointCloudRenderer();
    //显示对象-背景,相机内容
    private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
    //显示对象-模型
    private final ObjectRenderer virtualObject = new ObjectRenderer();

    public ArSampleRenderer2() {
        displayRotationHelper = new DisplayRotationHelper(FcUtils.getContext());

        Exception exception = null;
        String message = null;
        try {
            // 创建 session.session类用来管理AR系统状态并处理session自己的生命周期。
            session = new Session(FcUtils.getContext());
            Config config = session.getConfig();
            if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                config.setDepthMode(Config.DepthMode.AUTOMATIC);
            } else {
                config.setDepthMode(Config.DepthMode.DISABLED);
            }
            session.configure(config);
        } catch (UnavailableArcoreNotInstalledException e) {
            message = "Please install ARCore";
            exception = e;
        } catch (UnavailableApkTooOldException e) {
            message = "Please update ARCore";
            exception = e;
        } catch (UnavailableSdkTooOldException e) {
            message = "Please update this app";
            exception = e;
        } catch (UnavailableDeviceNotCompatibleException e) {
            message = "This device does not support AR";
            exception = e;
        } catch (Exception e) {
            message = "Failed to create AR session";
            exception = e;
        }
        if (message != null) {
//            messageSnackbarHelper.showError(this, message);
            Log.e(TAG, "Exception creating session", exception);
            return;
        }
    }
    private Session session;//相机控制
    // 临时矩阵在这里直接分配空间，而不是对每一帧进行重新分配。这样可以减少每一帧渲染的压力。
    private final float[] anchorMatrix = new float[16];
    private static final float[] DEFAULT_COLOR = new float[] {0f, 0f, 0f, 0f};

    public void setTapHelper(TapHelper tapHelper) {
        this.tapHelper = tapHelper;
    }

    public void onResume() {
        // 调用顺序很重要 - 请参阅onPause（）中的注释，在onResume()中顺序与onPause（）中的相反。
        try {
            session.resume();
        } catch (CameraNotAvailableException e) {
            //在某些情况下（例如另一个相机应用程序启动），相机可能会被提供给q其他应用程序。
            // 通过显示错误提示并在下一次迭代中重新创建session 来正确解决此问题。
//            messageSnackbarHelper.showError(this, "Camera not available. Please restart the app.");
            session = null;
            return;
        }
//        surfaceView.onResume();
        displayRotationHelper.onResume();
//        messageSnackbarHelper.showMessage(this, "Searching for surfaces...");
    }

    public void onPause() {
        if (session != null) {
            //请注意，调用顺序 - 首先暂停GLSurfaceView，以便它不会尝试查询Session。
            // 如果在GLSurfaceView之前暂停Session，则GLSurfaceView仍可调用session.update（）从而可能导致抛出SessionPausedException。
            displayRotationHelper.onPause();
//            surfaceView.onPause();
            session.pause();
        }
    }


    /**
     * 渲染前调用
     * 使用着色器和材质属性初始化所有3D模型
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
            planeRenderer.createOnGlThread(FcUtils.getContext(), "ar/models/trigrid.png");
            pointCloudRenderer.createOnGlThread(FcUtils.getContext());
//            //模型对象,可以设置多个纹理，懒得写
            virtualObject.createOnGlThread(FcUtils.getContext(),
                    "d3/sample/redcar.obj", "d3/sample/redcar.jpg");
            virtualObject.setMaterialProperties(0.0f, 2.0f, 0.5f, 6.0f);
//            //阴影
//            virtualObjectShadow.createOnGlThread(
//                    this, "ar/models/andy_shadow.obj", "ar/models/andy_shadow.png");
//            virtualObjectShadow.setBlendMode(ObjectRenderer.BlendMode.Shadow);
//            virtualObjectShadow.setMaterialProperties(1.0f, 0.0f, 0.0f, 1.0f);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read an asset file", e);
        }
    }

    /**
     * 屏幕改变时调用
     * @param gl
     * @param width
     * @param height
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        GLES20.glViewport(0, 0, width, height);
    }

    /**
     * 时刻调用
     * 设置AR框架，让ARCore识别hitTest框架
     * @param gl
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        // 清除屏幕以通知驱动程序它不应加载前一帧的任何像素。
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        if (session == null)return;
        // 通知ARCore session 视图大小已更改，以便可以正确调整透视矩阵和视频背景。
        displayRotationHelper.updateSessionIfNeeded(session);
        try {
            session.setCameraTextureName(backgroundRenderer.getTextureId());
            // 从AR session获取当前帧，可以由此改变AR系统的状态。 当配置设置为UpdateMode.BLOCKING（默认情况下）时，这将限制渲染到相机帧的速率。
            Frame frame = session.update();
            Camera camera = frame.getCamera();
            // 每帧处理一个Tap 。该函数在下面有具体实现。
            // 每次获取当前帧时处理一次，与帧速率相比，handleTap频率相对较低。
            // 可以理解为其不会对于每一帧都进行获取当前帧操作，因为只有获取当前帧操作后才会进行handleTap 处理。
            // 所以处理handleTap 的频率一般是要低于实际帧率的。
            handleTap(frame, camera);
            // 对该帧绘制AR 的背景
            backgroundRenderer.draw(frame);
            // 如果相机不是出于跟踪状态下, 不要绘制3D对象。
            if (camera.getTrackingState() == TrackingState.PAUSED)return;
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
            // 可视化跟踪点。PointCloud 指 一组观察到的3D点和置信度值。
            PointCloud pointCloud = frame.acquirePointCloud();
            pointCloudRenderer.update(pointCloud);
            pointCloudRenderer.draw(viewmtx, projmtx);
            // 应用程序负责在使用后释放PointCloud 资源。
            pointCloud.release();
            // 检查我们是否检测到至少一个平面。 如果是，请隐藏加载消息。
//            if (messageSnackbarHelper.isShowing()) {
//                for (Plane plane : session.getAllTrackables(Plane.class)) {
//                    if (plane.getTrackingState() == TrackingState.TRACKING) {
//                        messageSnackbarHelper.hide(this);
//                        break;
//                    }
//                }
//            }
            // 可视化平面。
            planeRenderer.drawPlanes(
                    session.getAllTrackables(Plane.class), camera.getDisplayOrientedPose(), projmtx);
            // 可视化触摸创建的锚点。
            float scaleFactor = 0.2f;
            for (ColoredAnchor coloredAnchor : anchors) {
                if (coloredAnchor.anchor.getTrackingState() != TrackingState.TRACKING) {
                    continue;
                }
                // 在世界空间中获取锚点(Anchor)的当前姿势（可以理解为点的位置和方向）。
                // 随着ARCore现实世界的评估，会通过调用session.update（）来更新锚点(Anchor)姿势。
                coloredAnchor.anchor.getPose().toMatrix(anchorMatrix, 0);
                // 更新并绘制模型（在这个程序里就是Android小人）及其阴影。
                virtualObject.updateModelMatrix(anchorMatrix, scaleFactor);
//                virtualObjectShadow.updateModelMatrix(anchorMatrix, scaleFactor);
                virtualObject.draw(viewmtx, projmtx, colorCorrectionRgba, coloredAnchor.color);
//                virtualObjectShadow.draw(viewmtx, projmtx, colorCorrectionRgba, coloredAnchor.color);
            }
        } catch (Throwable t) {
            // Avoid crashing the application due to unhandled exceptions.
            Log.e(TAG, "Exception on the OpenGL thread", t);
        }
    }

    // 每次获取当前帧时处理一次，与帧速率相比，handleTap频率相对较低。
    // 可以理解为其不会对于每一帧都进行获取当前帧操作，因为只有获取当前帧操作后才会进行handleTap 处理。
    // 所以处理handleTap 的频率一般是要低于实际帧率的。
    private void handleTap(Frame frame, Camera camera) {
        MotionEvent tap = tapHelper.poll();
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
                        anchors.get(0).anchor.detach();
                        anchors.remove(0);
                    }
                    // 根据此锚点附加到的可跟踪类型，为对象指定颜色以进行渲染。 对于AR_TRACKABLE_POINT，它是蓝色，对于AR_TRACKABLE_PLANE，它是绿色。
                    float[] objColor;
                    if (trackable instanceof Point) {
//                        objColor = new float[] {66.0f, 133.0f, 244.0f, 255.0f};
                        objColor = new float[] {66f, 255f, 0f, 255.0f};
                    } else if (trackable instanceof Plane) {
                        objColor = new float[] {139.0f, 195.0f, 74.0f, 255.0f};
                    } else {
                        objColor = DEFAULT_COLOR;
                    }
                    // 添加锚点告诉ARCore它应该在空间中跟踪这个位置。 在平面上创建此锚点，以将3D模型放置在相对于世界和平面的正确位置。
                    anchors.add(new ColoredAnchor(hit.createAnchor(), objColor));
                    break;
                }
            }
        }
    }


    private final ArrayList<ColoredAnchor> anchors = new ArrayList<>();
    // 这个数据结构用来描述Anchor及及该锚点颜色。Anchor指用来描述现实世界中的固定位置和方向的点。
    private static class ColoredAnchor {
        public final Anchor anchor;
        public final float[] color;
        public ColoredAnchor(Anchor a, float[] color4f) {
            this.anchor = a;
            this.color = color4f;
        }
    }
}
