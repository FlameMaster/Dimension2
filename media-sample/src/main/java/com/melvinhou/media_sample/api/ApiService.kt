package com.melvinhou.media_sample.api

import com.melvinhou.kami.bean.FcEntity
import com.melvinhou.media_sample.bean.BiliEntity
import com.melvinhou.media_sample.bean.BiliUserSpaceEntity
import com.melvinhou.media_sample.bean.BiliLiveEntity
import com.melvinhou.media_sample.bean.BiliMyVideoEntity
import io.reactivex.Observable
import retrofit2.http.*


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/4/13 0013 15:30
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
interface ApiService {



    @Headers("Content-Type:application/x-www-form-urlencoded;charset=UTF-8")
    @FormUrlEncoded
    @POST("test")
    fun test(@FieldMap map: Map<String?, String?>?): Observable<FcEntity<String>>


    //获取直播地址
    @GET("https://api.live.bilibili.com/room/v1/Room/playUrl")
    fun getLivePath(@QueryMap map: HashMap<String, Any>): Observable<BiliEntity<BiliLiveEntity>>


    //获取用户主页
    @GET("/x/space/acc/info")
    fun getUserSpace(@QueryMap map: HashMap<String, Any>): Observable<BiliEntity<BiliUserSpaceEntity>>


    //获取用户视频
    @GET("/x/space/arc/search?mid={uid}&pn=1&ps={ps}&jsonp=jsonp")
    fun getUserVideoList(@QueryMap map: HashMap<String, Any>): Observable<BiliEntity<BiliMyVideoEntity>>


    //获取视频分P
    @GET("/x/player/pagelist?bvid={bvid}")
    fun getVideoPagelist(@QueryMap map: HashMap<String, Any>): Observable<BiliEntity<Any>>


    //获取视频详情
    @GET("/x/player/playurl?cid={cid}&qn=1&type=&otype=json&avid={aid}")
    fun getVideoDetail(@QueryMap map: HashMap<String, Any>): Observable<BiliEntity<Any>>


    //获取视频信息
    @GET("/x/web-interface/view?aid={aid}&cid={cid}")
    fun getVideoInfo(@QueryMap map: HashMap<String, Any>): Observable<BiliEntity<Any>>


    //获取番剧列表
    @GET("/timeline_v2_global")
    fun getSectionList(): Observable<BiliEntity<Any>>
    //获取番剧信息
    @GET("/pgc/web/season/section")
    fun getSectionInfo(@Query("season_id") id: String): Observable<BiliEntity<Any>>
    //获取弹幕
    @GET("/x/v1/dm/list.so")
    fun getVideoDm(@Query("oid") id: String): Observable<BiliEntity<Any>>

}