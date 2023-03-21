package com.melvinhou.media_sample.api

import com.melvinhou.kami.bean.FcEntity
import com.melvinhou.kami.io.AssetsPath
import com.melvinhou.media_sample.MediaItemEntity
import io.reactivex.Observable


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/3/20 0020 15:40
 * <p>
 * = 分 类 说 明：本地文件路径
 * ================================================
 */
interface AssetsApi {

    //媒体列表
    @AssetsPath("test_media_list.json")
    fun getMediaList(): Observable<FcEntity<ArrayList<MediaItemEntity>>>
}