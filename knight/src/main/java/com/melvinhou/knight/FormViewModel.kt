package com.melvinhou.knight

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.melvinhou.kami.mvvm.BaseViewModel
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.kami.util.StringCompareUtils


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2022/6/13 15:49
 * <p>
 * = 分 类 说 明：表格的基类
 * ================================================
 */
open class FormViewModel(application: Application) : BaseViewModel(application){


    //是否提交成功
    var isSubmit = MutableLiveData(false)

    //修改模式
    var isChanged = false

    //申请状态
    var status = MutableLiveData(-1)






    /**
     * 选择器参数校验
     */
    protected fun checkChooserParameter(
        keyStr: String,
        valueInt: Int?
    ): Boolean {
        val ok = valueInt != null && valueInt != 0
        if (!ok) FcUtils.showToast("请选择$keyStr")
        return ok
    }

    /**
     * 选择器参数校验
     */
    protected fun checkChooserParameter(
        keyStr: String,
        valueStr: String?
    ): Boolean {
        val ok = TextUtils.isEmpty(valueStr)
        if (ok) FcUtils.showToast("请选择$keyStr")
        return !ok
    }

    /**
     * 输入框参数校验
     */
    protected fun checkEditParameter(
        keyStr: String,
        valueStr: String?
    ): Boolean {
        return checkEditParameter(keyStr, valueStr, true)
    }

    /**
     * 输入框参数校验
     */
    protected fun checkEditParameter(
        keyStr: String,
        valueStr: String?,
        isMust: Boolean
    ): Boolean {
        return checkEditParameter(keyStr, valueStr, isMust, true)
    }

    /**
     * 输入框参数校验
     */
    protected fun checkEditParameter(
        keyStr: String,
        valueStr: String?,
        isMust: Boolean,
        isCheckSpecial: Boolean
    ): Boolean {
        //非空判断
        if (TextUtils.isEmpty(valueStr) && isMust) {
            FcUtils.showToast("请填写$keyStr")
            return false
        }
        //校验特殊字符
        if (StringCompareUtils.isConSpeChar(valueStr) && isCheckSpecial) {
            FcUtils.showToast("${keyStr}格式不规范")
            return false
        }
        return true
    }
}