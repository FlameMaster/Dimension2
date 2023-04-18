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
 * = 时 间：2023/4/13 0013 15:39
 * <p>
 * = 分 类 说 明：哔哩哔哩直播地址
 * ================================================
 */
class BiliLiveEntity : Serializable {
    var accept_quality: List<String>? = null
    var current_qn = 0
    var current_quality = 0
    var durl: List<Durl>? = null
    var quality_description: List<QualityDescription>? = null

    class Durl {
        var length = 0
        var order = 0
        var p2p_type = 0
        var stream_type = 0
        var url: String? = null
    }

    class QualityDescription {
        var desc: String? = null
        var qn = 0
    }
}