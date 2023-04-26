package com.melvinhou.cameralibrary;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.melvinhou.kami.io.FileUtils;
import com.melvinhou.kami.io.IOUtils;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.view.activities.BaseActivity;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.FocusMeteringResult;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/7/19 23:18
 * <p>
 * = 分 类 说 明：相机
 * ================================================
 */
public abstract class CameraActivity extends BaseActivity {

    private static final String TAG = CameraActivity.class.getName();
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Preview preview;// 预览画面
    private Camera camera;
    private CameraControl cameraControl;
    private File outputDirectory;
    //图像处理
    private ImageCapture imageCapture;
    //图像分析
    private ImageAnalysis imageAnalyzer;

    private LiveData<ZoomState> zoomState;
    private float maxZoomRatio;
    private float minZoomRatio;

    @Override
    protected void initActivity(int layoutId) {
        super.initActivity(layoutId);
        // 请求权限
        if (checkPermission(REQUIRED_PERMISSIONS)) {
            startCamera();
        } else {
            requestPermissions(REQUIRED_PERMISSIONS);
        }
    }


    @Override
    protected void onPermissionGranted(int requestCode) {
        super.onPermissionGranted(requestCode);
        //权限申请成功
        startCamera();
    }

    @Override
    protected void initListener() {
        //根据方向旋转拍摄
        OrientationEventListener orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (imageCapture == null) return;
                int rotation;
                if (orientation >= 45 && orientation < 135) {
                    rotation = Surface.ROTATION_270;
                } else if (orientation >= 135 && orientation < 225) {
                    rotation = Surface.ROTATION_180;
                } else if (orientation >= 225 && orientation < 315) {
                    rotation = Surface.ROTATION_90;
                } else {
                    rotation = Surface.ROTATION_0;
                }
                imageCapture.setTargetRotation(rotation);
            }
        };
        orientationEventListener.enable();
    }

    @Override
    protected void initData() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        outputDirectory = getOutputDirectory();


        preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)//宽高比
//                .setTargetRotation(viewFinder.getDisplay().getRotation())
//                .setTargetRotation(Surface.ROTATION_90)//低版本的时候用的设置角度
                .build();
        imageCapture = new ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)//宽高比
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)//图像捕获模式/低质量高速
//                .setFlashMode(ImageCapture.FLASH_MODE_ON)//闪光模式
                .build();
        //图片分析
        ImageAnalysis.Analyzer analyzer = upImageAnalyzer();
        if (analyzer != null) {
            imageAnalyzer = new ImageAnalysis.Builder()
                    // 仅将最新图像传送到分析仪，并在到达图像时将其丢弃。
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                        .setTargetResolution(new Size(1440, 1080))//设置分辨率
                    .build();
            ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();
            imageAnalyzer.setAnalyzer(cameraExecutor, upImageAnalyzer());
        }
    }

    /**
     * 准备相机
     */
    private void startCamera() {
        if (cameraProviderFuture == null) return;
        //相机准备好的监听
        cameraProviderFuture.addListener(() -> {
            try {
                // 用于将相机的生命周期绑定到生命周期所有者
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                // 相机选择器
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();
                // 在重新绑定之前解除用例绑定
                cameraProvider.unbindAll();
                // 相机关联生命周期
                if (imageAnalyzer!=null){
                    camera = cameraProvider.bindToLifecycle(CameraActivity.this,
                            cameraSelector, preview, imageCapture, imageAnalyzer);
                }else {
                    camera = cameraProvider.bindToLifecycle(CameraActivity.this,
                            cameraSelector, preview, imageCapture);
                }

                //相机对焦
                cameraControl = camera.getCameraControl();
                //缩放参数
                zoomState = camera.getCameraInfo().getZoomState();
                maxZoomRatio = zoomState.getValue().getMaxZoomRatio();
                minZoomRatio = zoomState.getValue().getMinZoomRatio();
//                preview.setSurfaceProvider(viewFinder.createSurfaceProvider(camera.getCameraInfo()));//低版本
                preview.setSurfaceProvider(upSurfaceProvider());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    //拍照
    protected void takePhoto() {
        // 获得可修改映像捕获用例的稳定引用
        if (imageCapture == null) return;
        // 创建带有时间戳的输出文件来保存图像
        File photoFile = new File(
                outputDirectory, FileUtils.getFileNameForDate() + ".jpg");
        Log.e(TAG, "rotation=" + preview.getTargetRotation());
        // 创建包含文件+元数据的输出选项对象
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions
                .Builder(photoFile)
                .build();
        // 设置图片捕捉监听器，在拍照后触发
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri savedUri = Uri.fromFile(photoFile);
                        String msg = "Photo capture succeeded: " + savedUri.toString();
                        Toast.makeText(FcUtils.getContext(), msg, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, msg);
                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, savedUri);
                        sendBroadcast(mediaScanIntent);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Photo capture failed: ${exc.message}", exception);
                    }
                });
    }

    //设置手势事件
    protected void setCustomTouchView(CameraXCustomTouchView view) {
        if (view != null)
            view.setCustomTouchListener(new CameraXCustomTouchView.CustomTouchListener() {
                @Override
                public void zoom() {
                    if (zoomState == null) return;
                    float zoomRatio = zoomState.getValue().getZoomRatio();
                    if (zoomRatio < maxZoomRatio) {
                        cameraControl.setZoomRatio((float) (zoomRatio + 0.1));
                    }
                }

                @Override
                public void zoomOut() {
                    if (zoomState == null) return;
                    float zoomRatio = zoomState.getValue().getZoomRatio();
                    if (zoomRatio > minZoomRatio) {
                        cameraControl.setZoomRatio((float) (zoomRatio - 0.1));
                    }
                }

                @Override
                public void click(float x, float y) {
                    if (cameraControl == null) return;
                    MeteringPointFactory factory = new SurfaceOrientedMeteringPointFactory(100, 100);
                    MeteringPoint point = factory.createPoint(x, y);
                    FocusMeteringAction action = new FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
//                        .addPoint(point, FocusMeteringAction.FLAG_AE) // 添加第二个点
                            .setAutoCancelDuration(3, TimeUnit.SECONDS)//3秒启动
                            .build();

                    //位移显示
                    onCameraFocusMove(x, y);
                    ListenableFuture<FocusMeteringResult> future = cameraControl.startFocusAndMetering(action);
                    future.addListener(() -> {
                        try {
                            FocusMeteringResult result = future.get();
                            if (result.isFocusSuccessful()) {
                            } else {
                            }
                        } catch (Exception e) {
                        }
                    }, ContextCompat.getMainExecutor(getBaseContext()));
                }

                @Override
                public void doubleClick(float x, float y) {
                    if (zoomState == null) return;
                    // 双击放大缩小
                    float zoomRatio = zoomState.getValue().getZoomRatio();
                    if (zoomRatio > minZoomRatio) {
                        cameraControl.setLinearZoom(0f);
                    } else {
                        cameraControl.setLinearZoom(0.5f);
                    }
                }

                @Override
                public void longClick(float x, float y) {

                }
            });
    }

    //焦点改变
    protected void onCameraFocusMove(float x, float y) {
    }

    //获取照片保存地址
    protected abstract File getOutputDirectory();

    //显示器
    protected abstract Preview.SurfaceProvider upSurfaceProvider();

    //图片分析
    protected ImageAnalysis.Analyzer upImageAnalyzer() {
        return null;
    }
}
