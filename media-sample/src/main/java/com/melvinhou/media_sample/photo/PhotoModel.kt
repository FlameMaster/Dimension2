package com.melvinhou.media_sample.photo

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.reflect.TypeToken
import com.melvinhou.kami.bean.FcEntity
import com.melvinhou.kami.mvvm.BaseViewModel
import com.melvinhou.kami.net.RequestCallback
import com.melvinhou.kami.tool.AssetsUtil
import com.melvinhou.media_sample.api.AssetsService
import com.melvinhou.media_sample.bean.IllustrationEntity
import com.melvinhou.media_sample.bean.MediaEntity
import com.melvinhou.media_sample.bean.MediaItemEntity
import com.melvinhou.media_sample.bean.VideoPageEntity
import com.melvinhou.medialibrary.video.FcVideoActivity
import com.melvinhou.medialibrary.video.ijk.IjkVideoActivity


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
class PhotoModel(application: Application) : BaseViewModel(application) {

    /**
     * 插画列表
     */
    fun getIllustrationList(callback: (IllustrationEntity.Group) -> Unit) {
        val observable = AssetsService.instance.Api().getIllustrationList()
        requestData(observable, object : RequestCallback<IllustrationEntity.Group?>() {
            override fun onSuceess(data: IllustrationEntity.Group?) {
                callback(data!!)
            }
        })
    }
}