package com.melvinhou.user_sample.login

import android.annotation.SuppressLint
import com.melvinhou.kami.bean.FcEntity
import com.melvinhou.kami.io.SharePrefUtil
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.userlibrary.bean.User
import com.melvinhou.userlibrary.db.SqlManager
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.lang.reflect.Field


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2022/3/15 0015 13:29
 * <p>
 * = 分 类 说 明：用户登录
 * ================================================
 */
class LoginNativeModel {

    //验证码
    fun sendCode(type: Int, areaCode: String, mobile: String) {
        FcUtils.showToast("请使用邀请码")
    }

    //登录-密码
    @SuppressLint("CheckResult")
    fun loginPassword(
        areaCode: String,
        mobile: String,
        password: String,
        callback: (User?, String?) -> Unit
    ) {
        Observable
            .create { emitter: ObservableEmitter<FcEntity<User>> ->
                val entity = FcEntity<User>()
                try {
                    val user = SqlManager.findUserByPhone(FcUtils.getContext(), mobile)
                    //反射设置值
                    val msg: Field = FcEntity::class.java.getDeclaredField("message")
                    val data: Field = FcEntity::class.java.getDeclaredField("data")
                    msg.isAccessible = true
                    data.isAccessible = true
                    if (user != null) {
                        //todo 密码使用的uuid字段，待修改
                        if (user.uuid != password) {
                            msg.set(entity, "密码错误")
                        } else
                            data.set(entity, user)
                    } else {
                        msg.set(entity, "用户不存在")
                    }
                } finally {
                    emitter.onNext(entity)
                    emitter.onComplete()
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { entity: FcEntity<User> ->
                callback(entity.data, entity.message)
            }
    }

    //登录-验证码
    @SuppressLint("CheckResult")
    fun loginAuthCode(
        areaCode: String,
        mobile: String,
        authCode: String,
        callback: (User?) -> Unit
    ) {
        if (authCode!="998877"){
            FcUtils.showToast("验证码错误")
            return
        }
        Observable
            .create { emitter: ObservableEmitter<User> ->
                var user: User? = null
                try {
                    user = SqlManager.findUserByPhone(FcUtils.getContext(), mobile)
                } finally {
                    if (user != null) emitter.onNext(user)
                    else FcUtils.showToast("用户不存在")
                    emitter.onComplete()
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { data: User ->
                callback(data)
            }
    }

    //修改密码
    @SuppressLint("CheckResult")
    fun changePassword(
        areaCode: String,
        mobile: String,
        authCode: String,
        password: String,
        callback: () -> Unit
    ) {
        if (authCode!="998877"){
            FcUtils.showToast("验证码错误")
            return
        }
        Observable
            .create { emitter: ObservableEmitter<User> ->
                var user: User? = null
                try {
                    user = SqlManager.findUserByPhone(FcUtils.getContext(), mobile)
                    if (user != null) {
                        user.setUuid(password)//todo 密码使用的uuid字段，待修改
                        SqlManager.addUser(FcUtils.getContext(), user)
                    }
                } finally {
                    if (user != null) emitter.onNext(user)
                    else FcUtils.showToast("用户不存在")
                    emitter.onComplete()
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { data: User ->
                callback()
            }
    }

    //注册
    @SuppressLint("CheckResult")
    fun register(
        areaCode: String,
        mobile: String,
        authCode: String,
        password: String,
        callback: (User?) -> Unit
    ) {
        if (authCode!="998877"){
            FcUtils.showToast("验证码错误")
            return
        }
        Observable
            .create { emitter: ObservableEmitter<User> ->
                var user: User? = null
                try {
                    user = SqlManager.findUserByPhone(FcUtils.getContext(), mobile)
                    if (user != null) {
                        FcUtils.showToast("用户已存在")
                    } else {
                        user = User()
                        val id = newUserId()
                        user.userId = id
                        user.setName("用户$id")
                        user.setNickName("用户$id")
                        user.setPhone(mobile)
                        user.setUuid(password)//todo 密码使用的uuid字段，待修改
                        SqlManager.addUser(FcUtils.getContext(), user)
                    }
                } finally {
                    if (user != null) emitter.onNext(user)
                    else FcUtils.showToast("注册失败")
                    emitter.onComplete()
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { data: User ->
                callback(data)
            }
    }


    /**
     * 获取新的userId
     */
    private fun newUserId(): Long {
        val key = "user_register_id"
        val id = SharePrefUtil.getLong(key, 20001L)
        SharePrefUtil.saveLong(key, id + 1L)
        return id
    }

}