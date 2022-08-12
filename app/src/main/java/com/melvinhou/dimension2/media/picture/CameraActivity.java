package com.melvinhou.dimension2.media.picture;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.widget.TextView;
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
import com.melvinhou.dimension2.ui.widget.CameraXCustomTouchView;
import com.melvinhou.dimension2.R;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.IOUtils;
import com.melvinhou.kami.util.ResourcesUtils;
import com.melvinhou.kami.view.BaseActivity;

import java.io.File;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Locale;
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
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
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
public class CameraActivity extends BaseActivity {

    private static final String TAG = "二次元相机";
    //照片名称格式
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Preview preview;// 预览画面
    private CameraXCustomTouchView touchView;
    private PreviewView viewFinder;
    private View focusView;
    private TextView textView;
    private Camera camera;
    private CameraControl cameraControl;
    private File outputDirectory;
    private ExecutorService cameraExecutor;
    //图像处理
    private ImageCapture imageCapture;
    //图像分析
    private ImageAnalysis imageAnalyzer;

    LiveData<ZoomState> zoomState;
    float maxZoomRatio;
    float minZoomRatio;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_camera;
    }

    @Override
    protected void initActivity(int layoutId) {
        super.initActivity(layoutId);
        // 请求摄像头权限
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    /**
     * 相机权限判断
     *
     * @return
     */
    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(
                FcUtils.getContext(), REQUIRED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 权限申请返回
     *
     * @param requestCode  请求权限时传入的请求码，用于区别是哪一次请求的
     * @param permissions  所请求的所有权限的数组
     * @param grantResults 权限授予结果，和 permissions 数组参数中的权限一一对应，元素值为两种情况，如下:
     *                     授予: PackageManager.PERMISSION_GRANTED
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void initView() {
        viewFinder = findViewById(R.id.view_finder);
        touchView = findViewById(R.id.view_touch);
        focusView = findViewById(R.id.focus);
        textView = findViewById(R.id.text);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
    }

    @Override
    protected void initListener() {
        findViewById(R.id.bt_camera_capture).setOnClickListener(this::takePhoto);
        //设置手势事件
        touchView.setCustomTouchListener(new CameraXCustomTouchView.CustomTouchListener() {
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

            @SuppressLint("CheckResult")
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
                focusView.setTranslationX(x - focusView.getWidth() / 2);
                focusView.setTranslationY(y - focusView.getHeight() / 2);
                focusView.setVisibility(View.VISIBLE);
                focusView.postInvalidate();
                Observable.timer(500, TimeUnit.MILLISECONDS)
                        .compose(IOUtils.setThread())
                        .subscribe(aLong -> focusView.setVisibility(View.GONE));
                ListenableFuture<FocusMeteringResult> future = cameraControl.startFocusAndMetering(action);
                future.addListener(() -> {
                    try {
                        FocusMeteringResult result = future.get();
                        if (result.isFocusSuccessful()) {
                        } else {
                        }
                    } catch (Exception e) {
                    }
                }, ContextCompat.getMainExecutor(CameraActivity.this));
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
        outputDirectory = getOutputDirectory();
        cameraExecutor = Executors.newSingleThreadExecutor();


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
        imageAnalyzer = new ImageAnalysis.Builder()
                // 仅将最新图像传送到分析仪，并在到达图像时将其丢弃。
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                        .setTargetResolution(new Size(1440, 1080))//设置分辨率
                .build();
        imageAnalyzer.setAnalyzer(cameraExecutor, new LuminosityAnalyzer());
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
                camera = cameraProvider.bindToLifecycle(CameraActivity.this,
                        cameraSelector, preview, imageCapture, imageAnalyzer);
                //相机对焦
                cameraControl = camera.getCameraControl();
                //缩放参数
                zoomState = camera.getCameraInfo().getZoomState();
                maxZoomRatio = zoomState.getValue().getMaxZoomRatio();
                minZoomRatio = zoomState.getValue().getMinZoomRatio();
//                preview.setSurfaceProvider(viewFinder.createSurfaceProvider(camera.getCameraInfo()));//低版本
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * 拍照
     *
     * @param view
     */
    private void takePhoto(View view) {
        // 获得可修改映像捕获用例的稳定引用
        if (imageCapture == null) return;
        // 创建带有时间戳的输出文件来保存图像
        File photoFile = new File(
                outputDirectory, new SimpleDateFormat(FILENAME_FORMAT, Locale.US
        ).format(System.currentTimeMillis()) + ".jpg");
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

    /**
     * 获取照片保存地址
     *
     * @return
     */
    private File getOutputDirectory() {
        File[] mediaDirs = FcUtils.getContext().getExternalMediaDirs();
        File mediaDir = mediaDirs.length > 0 ? mediaDirs[0] : null;
        if (mediaDir != null) {
            mediaDir = new File(mediaDir, ResourcesUtils.getString(R.string.app_name));
            mediaDir.mkdirs();
        }
        return mediaDir != null && mediaDir.exists() ?
                mediaDir : FcUtils.getContext().getFilesDir();
//        return FileUtils.getDiskCacheDir("");
    }

    /**
     * 分析回调
     */
    class LuminosityAnalyzer implements ImageAnalysis.Analyzer {

        private boolean isHeading = false;

        private byte[] toByteArray(ByteBuffer buffer) {
            buffer.rewind();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            return data;
        }

        @SuppressLint("CheckResult")
        @Override
        public void analyze(@NonNull ImageProxy image) {
            if (isHeading) {
                image.close();
                return;
            }
            isHeading = true;
            Observable.create((ObservableOnSubscribe<Result>) emitter -> {
                Result result = null;
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] data = toByteArray(buffer);
                int width = image.getWidth();
                int height = image.getHeight();
                image.close();
                //TODO 调整crop的矩形区域，目前是全屏（全屏有更好的识别体验，但是在部分手机上可能OOM）
                PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
                        data, width, height, 0, 0, width, height, false);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                try {
                    result = new QRCodeReader().decode(bitmap);
                } catch (NotFoundException e) {
                    e.printStackTrace();
                } catch (ChecksumException e) {
                    e.printStackTrace();
                } catch (FormatException e) {
                    e.printStackTrace();
                } finally {
                    if (result!=null)
                    emitter.onNext(result);
                    else  isHeading = false;
                    emitter.onComplete();
                }
            })
                    .compose(IOUtils.setThread())
                    .subscribe(result -> {
                            textView.setText(result.getText());
                    });
        }

    }
}
