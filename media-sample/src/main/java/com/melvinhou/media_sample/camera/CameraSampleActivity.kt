package com.melvinhou.media_sample.camera

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview.SurfaceProvider
import androidx.camera.view.PreviewView
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.melvinhou.cameralibrary.CameraActivity
import com.melvinhou.cameralibrary.CameraXCustomTouchView
import com.melvinhou.kami.io.IOUtils
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.kami.util.ResourcesUtils
import com.melvinhou.media_sample.R
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.disposables.Disposable
import java.io.File
import java.nio.ByteBuffer
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
class CameraSampleActivity : CameraActivity() {

    private lateinit var touchView: CameraXCustomTouchView
    private lateinit var viewFinder: PreviewView
    private lateinit var focusView: View

    //焦点消失的管理
    private var mDisposable: Disposable? = null


    override fun getLayoutID(): Int = R.layout.activity_camera_sample

    override fun initView() {
        viewFinder = findViewById(R.id.view_finder)
        touchView = findViewById(R.id.view_touch)
        focusView = findViewById(R.id.focus)
    }

    override fun initListener() {
        super.initListener()
        //设置手势事件
        setCustomTouchView(touchView)
        //拍照
        findViewById<View>(R.id.bt_camera_capture).setOnClickListener { v: View -> takePhoto() }
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

    override fun upSurfaceProvider(): SurfaceProvider {
        return viewFinder.surfaceProvider
    }


}