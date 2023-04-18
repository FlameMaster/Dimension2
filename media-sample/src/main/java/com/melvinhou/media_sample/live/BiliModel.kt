package com.melvinhou.media_sample.live

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.melvinhou.kami.io.IOUtils
import com.melvinhou.kami.mvvm.BaseViewModel
import com.melvinhou.kami.net.RequestCallback
import com.melvinhou.kami.util.StringUtils
import com.melvinhou.media_sample.api.ApiModel
import com.melvinhou.media_sample.bean.BiliEntity
import com.melvinhou.media_sample.bean.BiliLiveEntity
import com.melvinhou.media_sample.bean.BiliMyVideoEntity
import com.melvinhou.media_sample.bean.BiliUserSpaceEntity
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers


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
class BiliModel(application: Application) : BaseViewModel(application) {


    /**
     * 获取哔哩哔哩个人空间
     */
    fun loadBiliUserSpace(userId: String, callBack: (BiliUserSpaceEntity?) -> Unit) {
        val map = hashMapOf<String, Any>()
        map["mid"] = userId
        map["jsonp"] = "jsonp"
        val param = ApiModel.instance.Api().getUserSpace(map)
        requestData(param, object : RequestCallback<BiliUserSpaceEntity>() {
            override fun onSuceess(data: BiliUserSpaceEntity?) {
                callBack(data)
            }
        })
    }


    /**
     * 获取哔哩哔哩个人视频
     */
    fun loadBiliUserVideoList(
        userId: String,
        page: Int,
        callBack: (List<BiliMyVideoEntity.VideoBean>?) -> Unit
    ) {
        val map = hashMapOf<String, Any>()
        map["mid"] = userId
        map["pn"] = page
        map["ps"] = 10
        map["jsonp"] = "jsonp"
        val param = ApiModel.instance.Api().getUserVideoList(map)
        requestData(param, object : RequestCallback<BiliMyVideoEntity>() {
            override fun onSuceess(data: BiliMyVideoEntity?) {
                callBack(data?.list?.vlist)
            }
        })
    }


    /**
     * 获取哔哩哔哩房间的直播地址
     */
    fun loadBiliLivePath(roomId: String, callBack: (BiliLiveEntity?) -> Unit) {
        val map = hashMapOf<String, Any>()
        map["cid"] = roomId
        map["platform"] = "h5"
        map["otype"] = "json"
        map["quality"] = 0
        val param = ApiModel.instance.Api().getLivePath(map)
        requestData(param, object : RequestCallback<BiliLiveEntity>() {
            override fun onSuceess(data: BiliLiveEntity?) {
                callBack(data)
            }
        })
    }


    /**
     * 获取哔哩哔哩房间的直播地址
     */
    @SuppressLint("CheckResult")
    fun loadBiliLivePath2(roomId: String, callBack: (BiliLiveEntity?) -> Unit) {
        val path = ("https://api.live.bilibili.com/room/v1/Room/playUrl?cid="
                + roomId
                + "&platform=h5&otype=json&quality=0")
        Observable
            .create<BiliLiveEntity?>(ObservableOnSubscribe<BiliLiveEntity?> { emitter: ObservableEmitter<BiliLiveEntity?> ->
                val json: String? = IOUtils.requestGet(path)
                val type = object : TypeToken<BiliEntity<BiliLiveEntity>>() {}.type
                val entity: BiliEntity<BiliLiveEntity> = Gson().fromJson(json, type)
                emitter.onNext(entity.getData())
                emitter.onComplete()
            } as ObservableOnSubscribe<BiliLiveEntity?>?)
            .compose<BiliLiveEntity>(IOUtils.setThread<BiliLiveEntity>())
            .subscribe(Consumer<BiliLiveEntity> { entity: BiliLiveEntity? ->
                callBack(entity)
            })
    }


}