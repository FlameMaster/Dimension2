package com.melvinhou.tiktok_sample

import android.app.Application
import com.melvinhou.kami.lucas.CallBack
import com.melvinhou.kami.mvp.BaseModel
import com.melvinhou.kami.net.RequestCallback
import com.melvinhou.tiktok_sample.api.TiktokService
import com.melvinhou.tiktok_sample.bean.TiktokEntity


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/5/17 0017 10:23
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
class TiktokModel(application: Application) : BaseModel<TiktokCotract.Presenter>(application),
    TiktokCotract.Model {

    fun loadList(callback: (ArrayList<TiktokEntity>) -> Unit) {
        val observable = TiktokService.instance.Api().getVideoList()
        requestData(observable, object : RequestCallback<ArrayList<TiktokEntity>?>() {
            override fun onSuceess(data: ArrayList<TiktokEntity>?) {
                callback(data!!)
            }
        })
    }

    override fun loadList(callback: CallBack<ArrayList<TiktokEntity>>?) {
        val observable = TiktokService.instance.Api().getVideoList()
        requestData(observable, object : RequestCallback<ArrayList<TiktokEntity>?>() {
            override fun onSuceess(data: ArrayList<TiktokEntity>?) {
                callback?.callback(data!!)
            }
        })
    }
}