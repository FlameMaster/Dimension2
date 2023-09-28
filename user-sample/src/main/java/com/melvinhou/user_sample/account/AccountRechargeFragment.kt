package com.melvinhou.user_sample.account

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import com.melvinhou.kami.adapter.BindRecyclerAdapter
import com.melvinhou.knight.KindFragment
import com.melvinhou.knight.showPrice
import com.melvinhou.user_sample.databinding.FragmentAccountRechargeBinding
import com.melvinhou.user_sample.databinding.ItemAccountRechargeBinding


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
class AccountRechargeFragment : KindFragment<FragmentAccountRechargeBinding, AccountModel>() {
    override val _ModelClazz: Class<AccountModel>
        get() = AccountModel::class.java

    override fun openViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAccountRechargeBinding = FragmentAccountRechargeBinding.inflate(inflater, container, false)


    private var adapter: MyAdapter? = null
    val payId = MutableLiveData<String>()//支付对应的id

    override fun backward() {
        super.backward()
    }

    override fun initView() {
        mBinding.barRoot.title.text = "余额充值"
        mBinding.btSubmit.isEnabled = true
        initList()
    }

    private fun initList() {
        adapter = MyAdapter()
        mBinding.container.adapter = adapter
        mBinding.container.layoutManager = GridLayoutManager(context, 3)
        adapter?.setOnItemClickListener { _, _, data ->
            payId.postValue(data)
        }
    }

    override fun initListener() {
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun initData() {
    }

    private fun loadData() {
        mModel.loadList(1){
            adapter?.clearData()
            adapter?.addDatas(it.subList(0,6))
        }
    }



    inner class MyAdapter :
        BindRecyclerAdapter<String, ItemAccountRechargeBinding>() {

        override fun getViewBinding(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): ItemAccountRechargeBinding {
            val binding = ItemAccountRechargeBinding.inflate(inflater, parent, false)
            return binding
        }

        override fun bindData(
            binding: ItemAccountRechargeBinding,
            position: Int,
            data: String
        ) {
            binding.itemNumber.text = data
            binding.itemPrice.showPrice(data, true)
            payId.observe(this@AccountRechargeFragment) {
                binding.cb.isSelected = data == it
            }
        }

    }
}