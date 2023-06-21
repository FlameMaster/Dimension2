package com.melvinhou.dimension2.home

import android.app.Application
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.util.Log
import androidx.core.graphics.Insets
import androidx.lifecycle.MutableLiveData
import com.melvinhou.kami.io.IOUtils
import com.melvinhou.kami.tool.AssetsUtil
import com.melvinhou.knight.NavigaionFragmentModel
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit


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
class HomeViewModel(application: Application) : NavigaionFragmentModel(application) {

    var timer: Disposable? = null//计时器


    //横幅
    fun loadBanner(callback: (List<String>) -> Unit) {
        addDisposable(Observable.create(ObservableOnSubscribe { emitter: ObservableEmitter<List<String>> ->
            val text = AssetsUtil.readText("test_media_album.json")
            text?.split(",")
                ?.map {
                    it.replace("\r\n", "")
                }?.let {
                    emitter.onNext(it.subList(0, 5))
                }
            emitter.onComplete()
        } as ObservableOnSubscribe<List<String>>)
            .compose(IOUtils.setThread())
            .subscribe { datas: List<String> ->
                callback(datas)
            })
    }

    /**
     * 开始计时器
     */
    fun startTimer(period: Long,callback: (Long) -> Unit) {
        if (timer!=null){
            addDisposable(timer)
        }
        timer = Observable.interval(period, TimeUnit.SECONDS)
            .subscribe {
                callback(it)
            }
        addDisposable(timer)
    }


}