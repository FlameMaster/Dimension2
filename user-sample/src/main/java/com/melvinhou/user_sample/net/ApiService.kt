package com.melvinhou.user_sample.net

import com.melvinhou.kami.bean.FcEntity
import com.melvinhou.userlibrary.bean.User
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


    //用户资料
    @GET("api/user/getUserInfo")
    fun getUserInfo(@Query("userId") id: Long): Observable<FcEntity<User>>


    //获取验证码
    @GET(value = "api/user/getAuthCode")
    fun sendCode(@QueryMap map: HashMap<String, Any>): Observable<FcEntity<Any>>

    //账号密码登录
    @POST("api/user/loginPassword")
    fun loginPassword(@QueryMap map: HashMap<String, Any>): Observable<FcEntity<User>>

    //验证码登录
    @POST("api/user/loginAuthCode")
    fun loginAuthCode(@QueryMap map: HashMap<String, Any>): Observable<FcEntity<User>>

    //用户注册
    @POST("api/user/register")
    fun userRegister(@QueryMap map: HashMap<String, Any>): Observable<FcEntity<User>>

    //修改密码
    @POST("api/user/replacePassword")
    fun replacePassword(@QueryMap map: HashMap<String, Any>): Observable<FcEntity<Any>>
}