package com.melvinhou.user_sample

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.melvinhou.kami.io.SharePrefUtil
import com.melvinhou.kami.net.RequestCallback
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
 * = 时 间：2023/6/6 0006 15:21
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
class UserModel(application: Application) : NavigaionFragmentModel(application) {

    private val isNative = true//本地
    private val nativeModel by lazy { UserNativeModel() }



    val info =  MutableLiveData<User>()


    /**
     * 加载用户信息
     */
    fun loadUserInfo() {
        val userId = SharePrefUtil.getLong(User.USER_ID,-1)
        if (isNative){
            nativeModel.loadUserInfo(userId){data->
                data?.let {
                    info.postValue(it)
                }
            }
            return
        }
        val param = ApiModel.instance.Api().getUserInfo(userId)
        requestData(param, object : RequestCallback<User>() {
            override fun onSuceess(data: User) {
                info.postValue(data)
            }
        })
    }

}