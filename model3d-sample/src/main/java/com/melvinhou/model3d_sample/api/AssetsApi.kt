package com.melvinhou.model3d_sample.api

import com.melvinhou.kami.bean.FcEntity
import com.melvinhou.kami.io.AssetsPath
import com.melvinhou.model3d_sample.D3Entity
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

    //模型列表
    @AssetsPath("sample_d3_list.json")
    fun getD3List(): Observable<FcEntity<ArrayList<D3Entity>>>
}