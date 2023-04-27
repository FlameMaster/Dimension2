package com.melvinhou.media_sample.camera

import android.annotation.SuppressLint
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.camera.core.Preview.SurfaceProvider
import androidx.camera.view.PreviewView
import com.google.zxing.*
import com.melvinhou.cameralibrary.FcCameraActivity
import com.melvinhou.cameralibrary.CameraXCustomTouchView
import com.melvinhou.kami.io.IOUtils
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.kami.util.ResourcesUtils
import com.melvinhou.media_sample.R
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.io.File
import java.util.concurrent.TimeUnit


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/4/26 0026 15:06
 * <p>
 * = 分 类 说 明：相机实现
 * ================================================
 */
class CameraSampleActivity : FcCameraActivity() {

    private lateinit var touchView: CameraXCustomTouchView
    private lateinit var viewFinder: PreviewView
    private lateinit var focusView: View
    private lateinit var takeView: Button
    private lateinit var modeChangeView: ImageView
    private lateinit var lensChangeView: ImageView

    //焦点消失的管理
    private var mDisposable: Disposable? = null


    override fun getLayoutID(): Int = R.layout.activity_camera_sample

    override fun initView() {
        viewFinder = findViewById(R.id.view_finder)
        touchView = findViewById(R.id.view_touch)
        focusView = findViewById(R.id.focus)
        takeView = findViewById(R.id.bt_camera_capture)
        modeChangeView = findViewById(R.id.bt_camera_mode_change)
        lensChangeView = findViewById(R.id.bt_lens_facing_change)
    }

    override fun initListener() {
        super.initListener()
        //设置手势事件
        setCustomTouchView(touchView)
        //切换模式
        modeChangeView.setOnClickListener {
            if (cameraMode==CAMERA_MODE_PHOTO){
                changeCameraMode(CAMERA_MODE_VIDEO)
            }else{
                changeCameraMode(CAMERA_MODE_PHOTO)
            }
        }
        //切换摄像头
        lensChangeView.setOnClickListener {
            changeLensFacing()
        }
        //拍照
//        findViewById<View>(R.id.bt_camera_capture).setOnClickListener { v: View -> takePhoto() }
        //录像
        takeView.setOnClickListener { v: View ->
            when (cameraMode) {
                CAMERA_MODE_PHOTO -> takePhoto()
                CAMERA_MODE_VIDEO -> {
                    if (isRecording) {
                        stopVideoRecord()
                    } else {
                        takeView.text = "停止录制"
                        startVideoRecord()
                    }
                }
            }
        }
    }

    /**
     * 切换模式
     */
    override fun changeCameraMode(mode: Int) {
        super.changeCameraMode(mode)
        when (mode) {
            CAMERA_MODE_PHOTO ->{
                modeChangeView.setImageResource(R.drawable.ic_camera_photo)
                takeView.text = "点击拍照"
            }
            CAMERA_MODE_VIDEO ->{
                modeChangeView.setImageResource(R.drawable.ic_camera_video)
                takeView.text = "开始录制"
            }
        }
    }

    override fun stopVideoRecord() {
        super.stopVideoRecord()
        takeView.text = "开始录制"
    }

    @SuppressLint("CheckResult")
    override fun onCameraFocusMove(x: Float, y: Float) {
        mDisposable?.dispose()
        focusView.translationX = x - focusView.width / 2
        focusView.translationY = y - focusView.height / 2
        focusView.visibility = View.VISIBLE
        focusView.postInvalidate()
        mDisposable = Observable.timer(500, TimeUnit.MILLISECONDS)
            .compose(IOUtils.setThread())
            .subscribe { aLong: Long? ->
                focusView.visibility = View.GONE
            }
    }

    override fun onStop() {
        super.onStop()
        mDisposable?.dispose()
    }

    override fun getOutputDirectory(): File {
        val mediaDirs = FcUtils.getContext().externalMediaDirs
        var mediaDir = if (mediaDirs.size > 0) mediaDirs[0] else null
        if (mediaDir != null) {
            mediaDir = File(mediaDir, ResourcesUtils.getString(R.string.app_name))
            mediaDir.mkdirs()
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else FcUtils.getContext().filesDir
//        return FileUtils.getDiskCacheDir("");
    }

    override fun bindSurfaceProvider(): SurfaceProvider {
        return viewFinder.surfaceProvider
    }


}