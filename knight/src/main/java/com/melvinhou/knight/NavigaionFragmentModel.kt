package com.melvinhou.knight

import android.app.Application
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.lifecycle.MutableLiveData
import com.melvinhou.kami.bean.PageInfo
import com.melvinhou.kami.mvvm.BaseViewModel


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/5/8 0008 15:36
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
open class NavigaionFragmentModel(application: Application) : BaseViewModel(application) {


    //页面
    val page = MutableLiveData<PageInfo>()

     fun toFragment(@IdRes resId: Int){
        page.postValue(PageInfo(resId))
    }
    fun toFragment(@IdRes resId: Int, args: Bundle?){
        page.postValue(PageInfo(resId,args))
    }
}