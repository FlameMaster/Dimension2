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
import com.melvinhou.dimension2.ui.widget.CameraXCustomPreviewView;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;

/**
 * ===============================================
 * = ??? ????????? ???
 * <p>
 * = ??? ??? ??? ??????melvinhou@163.com
 * <p>
 * = ??? ????????? ??? ??? ??? ??? ??? ??? ???
 * <p>
 * = ??? ??????2020/7/19 23:18
 * <p>
 * = ??? ??? ??? ????????????
 * ================================================
 */
public class CameraActivity extends BaseActivity {

    private static final String TAG = "???????????????";
    //??????????????????
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Preview preview;// ????????????
    private CameraXCustomPreviewView viewFinder;
    private View focusView;
    private TextView textView;
    private Camera camera;
    private CameraControl cameraControl;
    private File outputDirectory;
    private ExecutorService cameraExecutor;
    //????????????
    private ImageCapture imageCapture;
    //????????????
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
        // ?????????????????????
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    /**
     * ??????????????????
     *
     * @return
     */
    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(
                FcUtils.getContext(), REQUIRED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * ??????????????????
     *
     * @param requestCode  ?????????????????????????????????????????????????????????????????????
     * @param permissions  ?????????????????????????????????
     * @param grantResults ???????????????????????? permissions ????????????????????????????????????????????????????????????????????????:
     *                     ??????: PackageManager.PERMISSION_GRANTED
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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
        focusView = findViewById(R.id.focus);
        textView = findViewById(R.id.text);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
    }

    @Override
    protected void initListener() {
        findViewById(R.id.bt_camera_capture).setOnClickListener(this::takePhoto);
        //??????????????????
        viewFinder.setCustomTouchListener(new CameraXCustomPreviewView.CustomTouchListener() {
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
//                        .addPoint(point, FocusMeteringAction.FLAG_AE) // ??????????????????
                        .setAutoCancelDuration(3, TimeUnit.SECONDS)//3?????????
                        .build();

                //????????????
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
                // ??????????????????
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

        //????????????????????????
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
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)//?????????
//                .setTargetRotation(viewFinder.getDisplay().getRotation())
                .setTargetRotation(Surface.ROTATION_90)
                .build();
        imageCapture = new ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)//?????????
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)//??????????????????/???????????????
                .setFlashMode(ImageCapture.FLASH_MODE_ON)//????????????
                .build();
        imageAnalyzer = new ImageAnalysis.Builder()
                // ???????????????????????????????????????????????????????????????????????????
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                        .setTargetResolution(new Size(1440, 1080))//???????????????
                .build();
        imageAnalyzer.setAnalyzer(cameraExecutor, new LuminosityAnalyzer());
    }

    /**
     * ????????????
     */
    private void startCamera() {
        if (cameraProviderFuture == null) return;
        //????????????????????????
        cameraProviderFuture.addListener(() -> {
            try {
                // ????????????????????????????????????????????????????????????
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                // ???????????????
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();
                // ???????????????????????????????????????
                cameraProvider.unbindAll();
                // ????????????????????????
                camera = cameraProvider.bindToLifecycle(CameraActivity.this,
                        cameraSelector, preview, imageCapture, imageAnalyzer);
                //????????????
                cameraControl = camera.getCameraControl();
                //????????????
                zoomState = camera.getCameraInfo().getZoomState();
                maxZoomRatio = zoomState.getValue().getMaxZoomRatio();
                minZoomRatio = zoomState.getValue().getMinZoomRatio();
//                preview.setSurfaceProvider(viewFinder.createSurfaceProvider(camera.getCameraInfo()));//?????????
                preview.setSurfaceProvider(viewFinder.createSurfaceProvider());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * ??????
     *
     * @param view
     */
    private void takePhoto(View view) {
        // ????????????????????????????????????????????????
        if (imageCapture == null) return;
        // ???????????????????????????????????????????????????
        File photoFile = new File(
                outputDirectory, new SimpleDateFormat(FILENAME_FORMAT, Locale.US
        ).format(System.currentTimeMillis()) + ".jpg");
        Log.e(TAG, "rotation=" + preview.getTargetRotation());
        // ??????????????????+??????????????????????????????
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions
                .Builder(photoFile)
                .build();
        // ????????????????????????????????????????????????
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
     * ????????????????????????
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
     * ????????????
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
                //TODO ??????crop???????????????????????????????????????????????????????????????????????????????????????????????????OOM???
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
