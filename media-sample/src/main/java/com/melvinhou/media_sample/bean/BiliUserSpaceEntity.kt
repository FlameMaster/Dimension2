package com.melvinhou.media_sample.bean


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/4/13 0013 17:33
 * <p>
 * = 分 类 说 明：哔哩哔哩个人空间
 * ================================================
 */
class BiliUserSpaceEntity {
    var birthday: String? = null
    var coins: Int? = null
    var elec: Elec? = null
    var face: String? = null
    var face_nft: Int? = null
    var face_nft_type: Int? = null
    var fans_badge: Boolean? = null
    var fans_medal: FansMedal? = null
    var gaia_res_type: Int? = null
    var is_followed: Boolean? = null
    var is_risk: Boolean? = null
    var is_senior_member: Int? = null
    var jointime: Int? = null
    var level: Int? = null
    var live_room: LiveRoom? = null
    var mid: Int? = null
    var moral: Int? = null
    var name: String? = null
    var nameplate: Nameplate? = null
    var official: Official? = null
    var pendant: Pendant? = null
    var profession: Profession? = null
    var rank: Int? = null
    var school: School? = null
    var series: Series? = null
    var sex: String? = null
    var sign: String? = null
    var silence: Int? = null
    var sys_notice: SysNotice? = null
    var theme: Theme? = null
    var top_photo: String? = null
    var user_honour_info: UserHonourInfo? = null
    var vip: Vip? = null
    class Elec {
        var show_info: ShowInfo? = null
        class ShowInfo {
            var icon: String? = null
            var jump_url: String? = null
            var show: Boolean? = null
            var state: Int? = null
            var title: String? = null
        }
    }

    class FansMedal {
        var show: Boolean? = null
        var wear: Boolean? = null
    }

    class LiveRoom {
        var broadcast_type: Int? = null
        var cover: String? = null
        var liveStatus: Int? = null
        var roomStatus: Int? = null
        var roomid: Int? = null
        var roundStatus: Int? = null
        var title: String? = null
        var url: String? = null
        var watched_show: WatchedShow? = null
        class WatchedShow {
            var icon: String? = null
            var icon_location: String? = null
            var icon_web: String? = null
            var num: Int? = null
            var switch: Boolean? = null
            var text_large: String? = null
            var text_small: String? = null
        }
    }

    class Nameplate {
        var condition: String? = null
        var image: String? = null
        var image_small: String? = null
        var level: String? = null
        var name: String? = null
        var nid: Int? = null
    }

    class Official {
        var desc: String? = null
        var role: Int? = null
        var title: String? = null
        var type: Int? = null
    }

    class Pendant {
        var expire: Int? = null
        var image: String? = null
        var image_enhance: String? = null
        var image_enhance_frame: String? = null
        var name: String? = null
        var pid: Int? = null
    }

    class Profession {
        var department: String? = null
        var is_show: Int? = null
        var name: String? = null
        var title: String? = null
    }

    class School {
        var name: String? = null
    }

    class Series {
        var show_upgrade_window: Boolean? = null
        var user_upgrade_status: Int? = null
    }

    class SysNotice

    class Theme

    class UserHonourInfo {
        var mid: Int? = null
        var tags: List<Any?>? = null
    }

    class Vip {
        var avatar_subscript: Int? = null
        var avatar_subscript_url: String? = null
        var due_date: Long? = null
        var label: Label? = null
        var nickname_color: String? = null
        var role: Int? = null
        var status: Int? = null
        var theme_type: Int? = null
        var tv_vip_pay_type: Int? = null
        var tv_vip_status: Int? = null
        var type: Int? = null
        var vip_pay_type: Int? = null
        class Label {
            var bg_color: String? = null
            var bg_style: Int? = null
            var border_color: String? = null
            var img_label_uri_hans: String? = null
            var img_label_uri_hans_static: String? = null
            var img_label_uri_hant: String? = null
            var img_label_uri_hant_static: String? = null
            var label_theme: String? = null
            var path: String? = null
            var text: String? = null
            var text_color: String? = null
            var use_img_label: Boolean? = null
        }
    }
}