package com.melvinhou.user_sample.account

import android.os.CountDownTimer
import android.provider.SyncStateContract
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import com.melvinhou.kami.adapter.BindRecyclerAdapter
import com.melvinhou.knight.KUITools
import com.melvinhou.knight.KfcUtils
import com.melvinhou.knight.KindFragment
import com.melvinhou.user_sample.R
import com.melvinhou.user_sample.databinding.FragmentAccountWithdrawBinding
import com.melvinhou.user_sample.databinding.ItemBankcardSelectBinding


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
class AccountWithdrawFragment : KindFragment<FragmentAccountWithdrawBinding, AccountModel>() {
    override val _ModelClazz: Class<AccountModel>
        get() = AccountModel::class.java

    override fun openViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAccountWithdrawBinding = FragmentAccountWithdrawBinding.inflate(inflater, container, false)

    //倒计时
    private var timer: CountDownTimer? = null
    private var bankCardAdapter: BankCardAdapter? = null

    //选中银行卡
    val currentBankCard = MutableLiveData<String>()

    //银行卡返回回调函数
    var cancelBack: (() -> Unit)? = null

    override fun initView() {
        mBinding.barRoot.title.text = "余额提现"

        mBinding.root.postDelayed({
            mBinding.etMoney.isFocusable = true
            mBinding.etMoney.isFocusableInTouchMode = true
            mBinding.etCode.isFocusable = true
            mBinding.etCode.isFocusableInTouchMode = true
            mBinding.btSubmit.isEnabled = true
        },500)
        initList()
    }

    private fun initList() {
        bankCardAdapter = BankCardAdapter()
        bankCardAdapter?.setOnItemClickListener { _, _, data ->
            cancelBack?.let { it() }
            currentBankCard.postValue(data)
        }
    }

    override fun initListener() {
        mBinding.btSubmit.setOnClickListener {
        }
        mBinding.tvBankcard.setOnClickListener {
            cancelBack = KUITools.showListDialog(requireActivity(), "选择银行卡", bankCardAdapter)
        }
        mBinding.tvCode.setOnClickListener {
            time()
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun onPause() {
        super.onPause()
        timer?.cancel()
    }

    override fun initData() {
    }

    private fun loadData() {
        mModel.loadList(1){
            bankCardAdapter?.clearData()
            bankCardAdapter?.addDatas(it.subList(0,3))
        }
    }



    //倒计时
    fun time() {
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                mBinding.tvCode.text =
                    getString(R.string.txt_send_code, (millisUntilFinished / 1000).toString())
                mBinding.tvCode.isEnabled = false
            }

            override fun onFinish() {
                mBinding.tvCode.text = "点击重新发送"
                mBinding.tvCode.isEnabled = true
            }
        }.start()
    }


    /**
     * 列表适配器
     */
    inner class BankCardAdapter : BindRecyclerAdapter<String, ItemBankcardSelectBinding>() {

        override fun getViewBinding(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): ItemBankcardSelectBinding {
            val binding = ItemBankcardSelectBinding.inflate(inflater, parent, false)
            return binding
        }

        override fun bindData(
            binding: ItemBankcardSelectBinding,
            position: Int,
            data: String
        ) {
//            binding.tvTitle.text = data.bankName
//            var number = data.number
//            number = number?.substring(number.length - 4, number.length)
//            binding.tvNumber.text = "（尾号${number}）"
        }

    }
}