package com.melvinhou.user_sample.account.card

import android.view.LayoutInflater
import android.view.ViewGroup
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.knight.KUITools
import com.melvinhou.knight.KindFragment
import com.melvinhou.user_sample.databinding.FragmentBankcardFormBinding


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/7/18 0018 11:55
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
class BankcardFormFragment : KindFragment<FragmentBankcardFormBinding, BankCardModel>() {
    override val _ModelClazz: Class<BankCardModel>
        get() = BankCardModel::class.java

    override fun openViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBankcardFormBinding = FragmentBankcardFormBinding.inflate(inflater, container, false)

    override fun initView() {
        mBinding.barRoot.title.text = "添加银行卡"
        mBinding.btSubmit.isEnabled = true
    }

    override fun initListener() {
        mBinding.btSubmit.setOnClickListener {
            val name = mBinding.etUserName.text.toString()
            val mobile = mBinding.etUserMobile.text.toString()
            val bankName = mBinding.etBankName.text.toString()
            val cardNumber = mBinding.etBankNumber.text.toString()
            if (mModel.isRequest.value == true) {
                FcUtils.showToast("提交中...")
            } else if (mModel.checkBankCardParameters(name,mobile,bankName,cardNumber)) {
                FcUtils.showToast("添加成功")
                backward()
            }
        }
        mBinding.tvCardType.setOnClickListener {
            val list = arrayOf("储蓄卡", "信用卡").toMutableList()
            KUITools.showListSelectDialog02(requireActivity(), "选择银行卡类型", list) {index->
                mModel.formCardType.postValue(
                    when (index) {
                        0->1
                        1->2
                        else -> 0
                    }
                )
            }
        }
    }

    override fun initData() {
        mModel.formCardType.observe(this) {
            mBinding.tvCardType.text = when (it) {
                1 -> "储蓄卡"
                2 -> "信用卡"
                else -> ""
            }
        }
    }
}