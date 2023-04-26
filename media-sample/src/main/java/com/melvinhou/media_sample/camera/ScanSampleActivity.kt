package com.melvinhou.media_sample.camera

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview.SurfaceProvider
import androidx.camera.view.PreviewView
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.maxicode.MaxiCodeReader
import com.google.zxing.qrcode.QRCodeReader
import com.melvinhou.cameralibrary.CameraActivity
import com.melvinhou.cameralibrary.CameraXCustomTouchView
import com.melvinhou.kami.io.IOUtils
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.kami.util.ImageUtils
import com.melvinhou.kami.util.ResourcesUtils
import com.melvinhou.media_sample.R
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.ObservableEmitter
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
 * = 分 类 说 明：扫描仪
 * ================================================
 */
class ScanSampleActivity : CameraActivity() {

    private lateinit var touchView: CameraXCustomTouchView
    private lateinit var viewFinder: PreviewView
    private lateinit var focusView: View
    private lateinit var textView: TextView

    //图片分析管理
    private var mDisposable: Disposable? = null

    //焦点消失的管理
    private var mFocusDisposable: Disposable? = null

    //图片处理中
    private var isAnalyzing = false


    override fun getLayoutID(): Int = R.layout.activity_scan_sample

    override fun initView() {
        viewFinder = findViewById(R.id.view_finder)
        touchView = findViewById(R.id.view_touch)
        focusView = findViewById(R.id.focus)
        textView = findViewById(R.id.text)
    }

    override fun initListener() {
        super.initListener()
        //设置手势事件
        setCustomTouchView(touchView)
    }

    @SuppressLint("CheckResult")
    override fun onCameraFocusMove(x: Float, y: Float) {
        mFocusDisposable?.dispose()
        focusView.translationX = x - focusView.width / 2
        focusView.translationY = y - focusView.height / 2
        focusView.visibility = View.VISIBLE
        focusView.postInvalidate()
        mFocusDisposable = Observable.timer(500, TimeUnit.MILLISECONDS)
            .compose(IOUtils.setThread())
            .subscribe { aLong: Long? ->
                focusView.visibility = View.GONE
            }
    }

    override fun onStop() {
        super.onStop()
        mFocusDisposable?.dispose()
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

    //图片分析结果
    private fun onAnalyzeResult(result: Result?) {
        val text = result?.text ?: ""
        textView.text = text

        //判断
        if (TextUtils.isEmpty(text)) {
        } else if (text.contains("http://") || text.contains("https://")) {
//            val intent = Intent(FcUtils.getContext(), WebActivity::class.java)
//            intent.putExtra("title", "扫描结果")
//            intent.putExtra("url", text)
//            startActivity(intent)
//            finish()
        } else {
            val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            //            cm.setText(text);
            val clip = ClipData.newPlainText(text, text)
            cm.setPrimaryClip(clip)
            FcUtils.showToast("已复制扫描结果：$text")
            finish()
        }
    }

    //图片分析回调
    @SuppressLint("CheckResult")
    override fun upImageAnalyzer(): ImageAnalysis.Analyzer =
        ImageAnalysis.Analyzer { image ->
            if (isAnalyzing) {
                image.close()
                return@Analyzer
            }
            isAnalyzing = true
            Observable
                .create { emitter: ObservableEmitter<BinaryBitmap?> ->
                    val buffer = image.planes[0].buffer
                    val data = IOUtils.toByteArray(buffer)
                    val width = image.width
                    val height = image.height
                    image.close()
                    //TODO 调整crop的矩形区域，目前是全屏（全屏有更好的识别体验，但是在部分手机上可能OOM）
                    val source = PlanarYUVLuminanceSource(
                        data, width, height, 0, 0, width, height, false
                    )
                    val bitmap = BinaryBitmap(HybridBinarizer(source))
                    emitter.onNext(bitmap)
                    emitter.onComplete()
                }
                .flatMap {
                    //rxjava并发
//                    Observable
//                        .merge(
//                            getMaxiCode(it),
//                            getQrCode(it)
//                        )
//                        .firstElement()
//                        .toObservable()
                    getZxCode(it)
                }
                .compose(IOUtils.setThread())
                .subscribe(object : Observer<Result?> {
                    override fun onSubscribe(d: Disposable) {
                        mDisposable = d
                    }

                    override fun onError(e: Throwable) {
                        isAnalyzing = false
                    }

                    override fun onComplete() {
                        isAnalyzing = false
                    }

                    override fun onNext(result: Result) {
                        onAnalyzeResult(result)
                    }

                })
        }

    //zxing码识别
    private fun getZxCode(bitmap: BinaryBitmap): Observable<Result?> {
        return Observable
            .create { emitter: ObservableEmitter<Result?> ->
                var result: Result? = null
                try {
                    //条形码识别需要角度矫正,且需要方向竖直
                    if (bitmap.isRotateSupported)
                        result = MultiFormatReader().decode(bitmap.rotateCounterClockwise())
                    else
                        result = MultiFormatReader().decode(bitmap)
                } catch (e: NotFoundException) {
                    e.printStackTrace()
                } catch (e: ChecksumException) {
                    e.printStackTrace()
                } catch (e: FormatException) {
                    e.printStackTrace()
                } finally {
                    if (result != null)
                        emitter.onNext(result)
                    emitter.onComplete()
                }
            }
    }

    //二维码识别
    private fun getQrCode(bitmap: BinaryBitmap): Observable<Result?> {
        return Observable
            .create { emitter: ObservableEmitter<Result?> ->
                var result: Result? = null
                try {
                    result = QRCodeReader().decode(bitmap)
                } catch (e: NotFoundException) {
                    e.printStackTrace()
                } catch (e: ChecksumException) {
                    e.printStackTrace()
                } catch (e: FormatException) {
                    e.printStackTrace()
                } finally {
                    if (result != null)
                        emitter.onNext(result)
                    emitter.onComplete()
                }
            }
    }

    //条形码识别
    private fun getMaxiCode(bitmap: BinaryBitmap): Observable<Result?> {
        return Observable
            .create { emitter: ObservableEmitter<Result?> ->
                var result: Result? = null
                try {
                    result = MaxiCodeReader().decode(bitmap)
                } catch (e: NotFoundException) {
                    e.printStackTrace()
                } catch (e: ChecksumException) {
                    e.printStackTrace()
                } catch (e: FormatException) {
                    e.printStackTrace()
                } finally {
                    if (result != null)
                        emitter.onNext(result)
                    emitter.onComplete()
                }
            }
    }

}
