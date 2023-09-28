package com.melvinhou.user_sample.account

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import com.melvinhou.kami.util.DateUtils
import com.melvinhou.knight.KfcUtils
import com.melvinhou.knight.KindFragment
import com.melvinhou.user_sample.R
import com.melvinhou.user_sample.databinding.FragmentAccountBillDetailBinding


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
class BillDetailFragment : KindFragment<FragmentAccountBillDetailBinding, AccountModel>() {
    override val _ModelClazz: Class<AccountModel>
        get() = AccountModel::class.java

    override fun openViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAccountBillDetailBinding = FragmentAccountBillDetailBinding.inflate(inflater, container, false)

    //1 收益账单 2 提现账单
    private var type = 0
    private var position = 0


    override fun initView() {
        mBinding.barRoot.title.text = "账单详情"
        mBinding.llContainer.isVisible = false
    }

    override fun initListener() {
    }

    override fun onResume() {
        super.onResume()
    }

    override fun initData() {
        type = arguments?.getInt("type")?:0
        position = arguments?.getInt("position")?:0
        loadData()
    }

    private fun loadData() {
        onSuceess()
    }

    private fun onSuceess() {
        mBinding.llContainer.isVisible = true
        //1充值 2提现 3.商城产品购买 4.套餐购买 5买单6购买商品资金返回7权益类产品8商品退款
        mBinding.tvExplain.text = when (position%8) {
            1 -> "充值"
            2 -> "提现"
            3 -> "商城产品购买"
            4 -> "套餐购买"
            5 -> "购买商品资金返回"
            6 -> "权益类产品"
            7 -> "商品退款"
            else -> ""
        }
        //1进账 2出账
        val unit = when (position%3) {
            1 -> "+"
            2 -> "-"
            else -> ""
        }
        mBinding.tvNumber.text = "${unit}${KfcUtils.getPrice(position.toFloat(),false)}"
        clearItems()
        //判断数据添加
        addItems("账单单号", "——")
        //状态1成功2失败3冻结中
        addItems(
            "当前状态", when (position%4) {
                1->"成功"
                2->"失败"
                3->"冻结中"
                else -> "——"
            }
        )
        addItems("创建时间", DateUtils.formatDuration("yyyy-MM-dd  HH:mm:ss"))
    }

    private fun clearItems() {
        for (i in 3 until mBinding.llContainer.childCount) {
            if (3 < mBinding.llContainer.childCount)
                mBinding.llContainer.removeViewAt(3)
        }
    }

    private fun addItems(title: CharSequence, value: CharSequence?) {
        if (TextUtils.isEmpty(value)) return
        val view = View.inflate(requireContext(), R.layout.item_tabrow, null)
        val titleView = view.findViewById<TextView>(R.id.item_title)
        val valueView = view.findViewById<TextView>(R.id.item_text)
        titleView.text = title
        valueView.text = value
        mBinding.llContainer.addView(view)
    }

}