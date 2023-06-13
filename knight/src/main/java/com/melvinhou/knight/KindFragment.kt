package com.melvinhou.knight

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.viewbinding.ViewBinding
import com.melvinhou.kami.lucas.CallBack
import com.melvinhou.kami.mvvm.BaseViewModel
import com.melvinhou.kami.mvvm.BindFragment


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/4/27 0027 17:41
 * <p>
 * = 分 类 说 明：进一步简化
 * ================================================
 */
abstract class KindFragment<VB : ViewBinding, M : BaseViewModel>  : BindFragment<VB, M>() {
    override fun openModelClazz(): Class<M> = _ModelClazz
    protected abstract val _ModelClazz: Class<M>


    inline fun <reified Y : Activity> toActivity() {
        toActivity(Y::class.java)
    }
    inline fun <reified Y : Activity> toActivity(bundle: Bundle?) {
        toActivity(Y::class.java, bundle)
    }
    inline fun <reified Y : Activity> toResultActivity() {
        toResultActivity(Y::class.java)
    }
    inline fun <reified Y : Activity> toResultActivity(bundle: Bundle?) {
        toResultActivity(Y::class.java, bundle)
    }
    inline fun <reified Y : Activity> toResultActivity(callback: ActivityResultCallback<ActivityResult>) {
        toResultActivity(Y::class.java,callback)
    }
    inline fun <reified Y : Activity> toResultActivity(bundle: Bundle?, callback: ActivityResultCallback<ActivityResult>) {
        toResultActivity(Y::class.java, bundle,callback)
    }



    fun showCheckDialog(
        title: CharSequence? = "提示", message: CharSequence = "是否确定？",
        positiveStr: CharSequence? = "确定", negativeStr: CharSequence? ="取消",
        callBack: CallBack<Boolean>?
    ) {
        super.showCheckView(title, message, positiveStr, negativeStr, callBack)
    }

}