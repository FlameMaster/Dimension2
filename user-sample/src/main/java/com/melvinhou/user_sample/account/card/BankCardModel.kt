package com.melvinhou.user_sample.account.card

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.kami.util.StringCompareUtils
import com.melvinhou.knight.NavigaionFragmentModel

class BankCardModel(application: Application) : NavigaionFragmentModel(application) {
    //银行卡类型
    val formCardType = MutableLiveData(0)



    /**
     * 格式校验
     */
    fun checkBankCardParameters(
        name: String,
        mobile: String,
        bankName: String,
        cardNumber: String
    ): Boolean {
        if (
            checkEditParameter("姓名", name)
            &&
            checkEditParameter("手机号", mobile)
            &&
            checkChooserParameter("银行卡类型", formCardType.value)
            &&
            checkEditParameter("开户行", bankName)
            &&
            checkEditParameter("银行卡号", cardNumber)
        ) {
            if (!StringCompareUtils.isChinese2(name)) {
                FcUtils.showToast("姓名格式不规范")
                return false
            }
            if (!StringCompareUtils.isPhone(mobile)) {
                FcUtils.showToast("请输入正确的手机号")
                return false
            }
            if (cardNumber.length != 16) {
                FcUtils.showToast("请输入正确的卡号")
                return false
            }
            return true
        }
        return false
    }




}