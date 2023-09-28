package com.melvinhou.knight

import android.app.Application
import android.os.Bundle
import android.os.SystemClock
import androidx.annotation.IdRes
import androidx.lifecycle.MutableLiveData
import com.melvinhou.kami.bean.PageInfo
import com.melvinhou.kami.io.IOUtils
import com.melvinhou.kami.util.FcUtils
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.disposables.Disposable


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/5/8 0008 15:36
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
open class NavigaionFragmentModel(application: Application) : FormViewModel(application) {


    //页面
    val page = MutableLiveData<PageInfo>()


    //加载列表数据
    fun loadList(page: Int, callback: (List<String>) -> Unit) {
        val list = arrayListOf<String>()
        for (i in 0 until 10) {
            list.add(((page-1)*10+i).toString())
        }
        if (page < 4) callback(list)
        else callback(arrayListOf())
    }

    fun loadDelayList(page: Int, callback: (List<String>) -> Unit) {
        val observer = object : Observer<List<String>>{
            override fun onSubscribe(d: Disposable) {
                addDisposable(d)
            }
            override fun onError(e: Throwable) {
            }
            override fun onComplete() {
            }
            override fun onNext(list: List<String>) {
                if (page < 4) callback(list)
                else callback(arrayListOf())
            }

        }
        Observable.create(ObservableOnSubscribe
        { emitter: ObservableEmitter<List<String>> ->
            val list = arrayListOf<String>()
            for (i in 0 until 10) {
                list.add(((page-1)*10+i).toString())
            }
            SystemClock.sleep(1000)
            emitter.onNext(list)
            emitter.onComplete()
        } as ObservableOnSubscribe<List<String>>)
            .compose(IOUtils.setThread())
            .subscribe(observer)
    }

    fun toFragment(@IdRes resId: Int) {
        page.postValue(PageInfo(resId))
    }

    fun toFragment(@IdRes resId: Int, args: Bundle?) {
        page.postValue(PageInfo(resId, args))
    }
}