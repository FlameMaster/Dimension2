package com.melvinhou.media_sample

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.util.Log
import com.melvinhou.anim_sample.AnimInteractionActivity02
import com.melvinhou.anim_sample.AnimSvgActivity
import com.melvinhou.anim_sample.AnimSysActivity
import com.melvinhou.kami.mvvm.BaseViewModel
import com.melvinhou.kami.net.RequestCallback
import com.melvinhou.kami.tool.AssetsUtil
import com.melvinhou.knight.FragmentContainActivity
import com.melvinhou.media_sample.api.AssetsService
import com.melvinhou.media_sample.bean.MediaItemEntity
import com.melvinhou.media_sample.live.LiveFragment
import com.melvinhou.media_sample.record.audio.AudioRecordActivity
import com.melvinhou.media_sample.record.screen.ScreenRecordActivity
import com.melvinhou.media_sample.video.VideoPageActivity
import com.melvinhou.medialibrary.music.ui.FcMusicActivity
import com.melvinhou.medialibrary.music.ui.FcMusicListActivity
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
class MediaViewModel(application: Application) : BaseViewModel(application) {

    @SuppressLint("CheckResult")
    fun getListData() {
        AssetsUtil.loadData<ArrayList<MediaItemEntity>>("sample_media_list.json",
            MediaItemEntity::class.java,ArrayList::class.java)
            .subscribe { data ->
                Log.e("获取数据", "长度=${data?.data?.size ?: -1}")
            }
    }

    /**
     * 列表加载
     */
    fun getListData(callback: (ArrayList<MediaItemEntity>) -> Unit) {
        val observable = AssetsService.instance.Api().getMediaList()
        requestData(observable, object : RequestCallback<ArrayList<MediaItemEntity>?>() {
            override fun onSuceess(data: ArrayList<MediaItemEntity>?) {
                callback(data!!)
            }
        })
    }

    /**
     *
     */
    fun getSubData(id: Long) {

    }


    /**
     * 通过id获取跳转页面
     */
    fun toPage(id:Long,callback: (Intent) -> Unit){
        val intent = Intent()
        when(id){
            211L->{
                intent.setClass(getApplication(), VideoPageActivity::class.java)
            }
            220L->{
                intent.setClass(getApplication(),FcVideoActivity::class.java)
                intent.putExtra("title","崩坏3动画短片")
                intent.putExtra("url","https://webstatic.bh3.com/video/bh3.com/pv/CG_OP_1800.mp4")
            }
            222L->{
                intent.setClass(getApplication(),IjkVideoActivity::class.java)
                intent.putExtra("title","阴阳师动画短片")
                intent.putExtra("url","https://yys.v.netease.com/2018/0725/ebefc466c32aa2c40aede8207956aae8qt.mp4")
            }
            240L->{
                intent.setClass(getApplication(),FragmentContainActivity::class.java)
                intent.putExtra("fragment", LiveFragment::class.java)
            }
            310L->{
                intent.setClass(getApplication(), FcMusicListActivity::class.java)
            }
            320L->{
                intent.setClass(getApplication(), FcMusicActivity::class.java)
            }
            420L->{
                intent.setClass(getApplication(), AnimSysActivity::class.java)
            }
            430L->{
                intent.setClass(getApplication(), AnimInteractionActivity02::class.java)
            }
            440L->{
                intent.setClass(getApplication(), AnimSvgActivity::class.java)
            }
            520L->{
                intent.setClass(getApplication(), AudioRecordActivity::class.java)
            }
            550L->{
                intent.setClass(getApplication(), ScreenRecordActivity::class.java)
            }
        }
        callback(intent)
    }

}