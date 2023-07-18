package com.melvinhou.user_sample.login

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.melvinhou.kami.net.RequestCallback
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.kami.util.StringCompareUtils
import com.melvinhou.kami.util.StringUtils
import com.melvinhou.knight.NavigaionFragmentModel
import com.melvinhou.user_sample.net.ApiModel
import com.melvinhou.userlibrary.bean.User


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
class LoginModel(application: Application) : NavigaionFragmentModel(application) {

    private val isNative = true//本地
    private val nativeModel by lazy { LoginNativeModel() }


    //登录模式
    val loginModel = MutableLiveData(0)//0验证码1密码

    //验证码
    fun sendCode(type: Int, areaCode: String, mobile: String) {
        if (isNative){
            nativeModel.sendCode(type, areaCode, mobile)
            return
        }
        //参数
        val map = hashMapOf<String, Any>()
        map["type"] = type//类型1有这用户2没这用户3不验证
        map["areaCode"] = areaCode//区号
        map["mobile"] = mobile//手机号
        //请求
        val param = ApiModel.instance.Api().sendCode(map)
        requestData(param, object : RequestCallback<Any>() {
            override fun onSuceess(data: Any) {
                FcUtils.showToast("验证码已发送")
            }
        })
    }

    //登录-密码
    fun loginPassword(
        areaCode: String,
        mobile: String,
        password: String,
        callback: (User?, String?) -> Unit
    ) {
        if (isNative){
            nativeModel.loginPassword(areaCode, mobile, password, callback)
            return
        }
        val map = hashMapOf<String, Any>()
        map["areaCode"] = areaCode
        map["mobile"] = mobile
        map["password"] = password

        val param = ApiModel.instance.Api().loginPassword(map)
        requestData(param, object : RequestCallback<User>() {
            override fun onSuceess(data: User) {
                callback(data, null)
            }

            override fun onFailure(code: Int, message: String?) {
                super.onFailure(code, message)

            }
        })
    }

    //登录-验证码
    fun loginAuthCode(
        areaCode: String,
        mobile: String,
        authCode: String,
        callback: (User?) -> Unit
    ) {
        if (isNative){
            nativeModel.loginAuthCode(areaCode, mobile, authCode, callback)
            return
        }
        val map = hashMapOf<String, Any>()
        map["areaCode"] = areaCode//区号
        map["mobile"] = mobile//手机号
        map["authCode"] = authCode//验证码

        val param = ApiModel.instance.Api().loginAuthCode(map)
        requestData(param, object : RequestCallback<User>() {
            override fun onSuceess(data: User) {
                callback(data)
            }
        })
    }

    //修改密码
    fun changePassword(
        areaCode: String,
        mobile: String,
        authCode: String,
        password: String,
        callback: () -> Unit
    ) {
        if (isNative){
            nativeModel.changePassword(areaCode, mobile, authCode, password, callback)
            return
        }
        val map = hashMapOf<String, Any>()
        map["areaCode"] = areaCode
        map["mobile"] = mobile
        map["authCode"] = authCode
        map["password"] = password

        val param = ApiModel.instance.Api().replacePassword(map)
        requestData(param, object : RequestCallback<Any>() {
            override fun onSuceess(data: Any) {
                callback()
            }
        })
    }

    //注册
    fun register(
        areaCode: String,
        mobile: String,
        authCode: String,
        password: String,
        callback: (User?) -> Unit
    ) {
        if (isNative){
            nativeModel.register(areaCode,mobile, authCode, password, callback)
            return
        }
        val map = hashMapOf<String, Any>()
        map["areaCode"] = areaCode
        map["mobile"] = mobile
        map["authCode"] = authCode
        map["password"] = password

        val param = ApiModel.instance.Api().userRegister(map)
        requestData(param, object : RequestCallback<User>() {
            override fun onSuceess(data: User) {
                callback(data)
            }
        })
    }


    /**
     * 格式校验
     */
    fun checkParameter(
        mobile: String? = null,
        authCode: String? = null,
        password: String? = null
    ): Boolean {
        if (
            checkEditParameter("手机号", mobile)
            &&
            checkEditParameter("验证码", authCode, false)
            &&
            checkEditParameter("密码", password, false)
        ) {
            if (!StringCompareUtils.isPhone(mobile)) {
                FcUtils.showToast("手机号格式不规范")
                return false
            }
            if (StringUtils.nonEmpty(authCode) && authCode?.length != 6) {
                FcUtils.showToast("请输入正确验证码")
                return false
            }
            return true
        }
        return false
    }


}