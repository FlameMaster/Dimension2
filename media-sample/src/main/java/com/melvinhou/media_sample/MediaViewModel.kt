package com.melvinhou.media_sample

import android.app.Application
import android.util.Log
import com.google.gson.reflect.TypeToken
import com.melvinhou.kami.bean.FcEntity
import com.melvinhou.kami.mvvm.BaseViewModel
import com.melvinhou.kami.net.RequestCallback
import com.melvinhou.kami.tool.AssetsUtil
import com.melvinhou.media_sample.api.AssetsService
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
 * = 时 间：2023/3/20 0020 13:57
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
class MediaViewModel(application: Application) : BaseViewModel(application) {
    fun fc() {
        val observable = AssetsService.instance.Api().getMediaList()
        requestData(observable,object :RequestCallback<ArrayList<MediaItemEntity>?>(){
            override fun onSuceess(data: ArrayList<MediaItemEntity>?) {
                Log.e("获取数据","长度=${data?.size?:-1}")
            }
        })
    }

}