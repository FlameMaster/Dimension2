package com.melvinhou.dimension2.launch

import android.app.Application
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import androidx.core.graphics.Insets
import androidx.lifecycle.MutableLiveData
import com.melvinhou.kami.io.IOUtils
import com.melvinhou.kami.tool.AssetsUtil
import com.melvinhou.knight.NavigaionFragmentModel
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/6/6 0006 15:21
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
class LaunchViewModel(application: Application) : NavigaionFragmentModel(application) {

    //布局间隔
    val WindowInsets = MutableLiveData<Insets>()

    //广告图
    fun loadAdvertImage(callback: (String) -> Unit) {
        callback("https://i0.hdslb.com/bfs/article/2bb40894777f721b6e4a3a9b044d4020181557fc.jpg")
    }

    //引导页
    fun loadGuidePage(callback: (List<String>) -> Unit) {
        addDisposable(Observable.create(ObservableOnSubscribe { emitter: ObservableEmitter<List<String>> ->
            val text = AssetsUtil.readText("test_media_album.json")
            text?.split(",")
                ?.map {
                    it.replace("\r\n", "")
                }?.let {
                    emitter.onNext(it.subList(it.size-3,it.size))
                }
            emitter.onComplete()
        } as ObservableOnSubscribe<List<String>>)
            .compose(IOUtils.setThread())
            .subscribe { datas: List<String> ->
                callback(datas)
            })
    }
}