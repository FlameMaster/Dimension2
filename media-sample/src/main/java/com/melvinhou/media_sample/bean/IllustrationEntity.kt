package com.melvinhou.media_sample.bean

import java.io.Serializable


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/3/20 0020 13:40
 * <p>
 * = 分 类 说 明：插图
 * ================================================
 */
class IllustrationEntity : Serializable {
    var title: String? = null
    var url: String? = null
    var explain: String? = null
    var date: String? = null
//    var owner: Owner? = null
    var width = 0
    var height = 0

    class Group : Serializable {
        var banners: List<IllustrationEntity>? = null
        var list: List<IllustrationEntity>? = null
    }
}