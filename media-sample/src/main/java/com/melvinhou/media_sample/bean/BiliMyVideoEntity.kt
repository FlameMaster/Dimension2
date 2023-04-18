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
 * = 分 类 说 明：哔哩哔哩视频列表
 * ================================================
 */
class BiliMyVideoEntity : Serializable {
    var episodic_button: EpisodicButton? = null
    var gaia_res_type: Int? = null
    var is_risk: Boolean? = null
    var list: InfoData? = null
    var page: Page? = null

    class EpisodicButton {
        var text: String? = null
        var uri: String? = null
    }

    class InfoData {
        var tlist: Tlist? = null
        var vlist: List<VideoBean>? = null
    }

    class Tlist {
        var `160`: X160? = null
        var `188`: X188? = null
        var `234`: X234? = null
        var `36`: X36? = null
        var `4`: X4? = null

        class X160 {
            var count: Int? = null
            var name: String? = null
            var tid: Int? = null
        }

        class X188 {
            var count: Int? = null
            var name: String? = null
            var tid: Int? = null
        }

        class X234 {
            var count: Int? = null
            var name: String? = null
            var tid: Int? = null
        }

        class X36 {
            var count: Int? = null
            var name: String? = null
            var tid: Int? = null
        }

        class X4 {
            var count: Int? = null
            var name: String? = null
            var tid: Int? = null
        }
    }

    class VideoBean {
        var aid: Int? = null
        var attribute: Int? = null
        var author: String? = null
        var bvid: String? = null
        var comment: Int? = null
        var copyright: String? = null
        var created: Int? = null
        var description: String? = null
        var hide_click: Boolean? = null
        var is_avoided: Int? = null
        var is_live_playback: Int? = null
        var is_pay: Int? = null
        var is_steins_gate: Int? = null
        var is_union_video: Int? = null
        var length: String? = null
        var mid: Int? = null
        var pic: String? = null
        var play: Int? = null
        var review: Int? = null
        var subtitle: String? = null
        var title: String? = null
        var typeid: Int? = null
        var video_review: Int? = null
    }

    class Page {
        var count: Int? = null
        var pn: Int? = null
        var ps: Int? = null
    }
}