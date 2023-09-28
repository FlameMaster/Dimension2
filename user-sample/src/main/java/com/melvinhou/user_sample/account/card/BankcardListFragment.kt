package com.melvinhou.user_sample.account.card

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.melvinhou.kami.adapter.BindRecyclerAdapter
import com.melvinhou.kami.databinding.ActivityListBinding
import com.melvinhou.kami.util.DimenUtils
import com.melvinhou.knight.KindFragment
import com.melvinhou.user_sample.R
import com.melvinhou.user_sample.databinding.ItemBankcardAddBinding
import com.melvinhou.user_sample.databinding.ItemBankcardBinding


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
 * = 分 类 说 明：账单列表
 * ================================================
 */
class BankcardListFragment : KindFragment<ActivityListBinding, BankCardModel>() {
    override val _ModelClazz: Class<BankCardModel>
        get() = BankCardModel::class.java

    override fun openViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ActivityListBinding = ActivityListBinding.inflate(inflater, container, false)

    private var adapter: MyAdapter? = null
    private var mTailBinding: ItemBankcardAddBinding? = null


    override fun initView() {
        mBinding.barRoot.title.text = "银行卡"
        initList()
    }

    /**
     * 初始化列表
     */
    private fun initList() {
        mBinding.container.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        //设定边距
        val decoration = DimenUtils.dp2px(10)
        mBinding.container.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View,
                parent: RecyclerView, state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                outRect[decoration, if (position == 0) decoration else 0, decoration] = decoration
            }
        })

        //适配器
        if (adapter == null) adapter = MyAdapter()
        mBinding.container.adapter = adapter
        adapter?.setOnItemClickListener { tag, position, data ->

        }
        //tail
        mTailBinding =
            ItemBankcardAddBinding.inflate(layoutInflater, mBinding.container, false)
        adapter?.removedTail(0)
        adapter?.addTailView(mTailBinding?.root)
    }

    override fun initListener() {
        mTailBinding?.itemTitle?.setOnClickListener {
            mModel.toFragment(R.id.action_list2add)
        }
    }

    override fun initData() {
        loadData()
    }

    private fun loadData() {
        mModel.loadList(1) {
            adapter?.clearData()
            adapter?.addDatas(it.subList(0,3))
        }
    }

    /**
     * 列表适配器
     */
    inner class MyAdapter :
        BindRecyclerAdapter<String, ItemBankcardBinding>() {

        override fun getViewBinding(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): ItemBankcardBinding {
            val binding = ItemBankcardBinding.inflate(inflater, parent, false)
            return binding
        }

        override fun bindData(
            binding: ItemBankcardBinding,
            position: Int,
            data: String
        ) {
            binding.tvBankName.text = "银行卡$data"
            binding.tvCardType.text = when (position%3) {
                1 -> "储蓄卡"
                2 -> "信用卡"
                else -> "储蓄卡"
            }
//            val number = data.number.toString()
//            binding.tvCardNumber.text =
//                "**** **** **** ${number.substring(number.length - 4, number.length)}"
        }

    }
}