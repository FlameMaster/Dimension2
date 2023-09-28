package com.melvinhou.user_sample.account

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.melvinhou.kami.adapter.BindRecyclerAdapter
import com.melvinhou.kami.databinding.LayoutListSwipeBinding
import com.melvinhou.kami.util.DimenUtils
import com.melvinhou.kami.view.wiget.NestedSwipeLayout
import com.melvinhou.knight.KindFragment
import com.melvinhou.user_sample.R
import com.melvinhou.user_sample.databinding.ItemBillLogBinding


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
class BillListItemFragment : KindFragment<LayoutListSwipeBinding, AccountModel>() {
    override val _ModelClazz: Class<AccountModel>
        get() = AccountModel::class.java

    override fun openViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): LayoutListSwipeBinding = LayoutListSwipeBinding.inflate(inflater, container, false)

    companion object {
        fun instance(position: Int, index: Int): BillListItemFragment {
            val fragment = BillListItemFragment()
            fragment.position = position
            //1 收益账单 2 提现账单
            fragment.type = when (index) {
                0 -> 1
                1 -> 2
                else -> 0
            }
            return fragment
        }
    }

    private var position = 0//0账户中心1账单列表
    private var type = 0//1 收益账单 2 提现账单
    private var adapter: MyAdapter? = null
    private var p = 1


    override fun initView() {
        initList()
    }

    /**
     * 初始化列表
     */
    private fun initList() {
        val line = DimenUtils.dp2px(1)
        adapter = MyAdapter()
        mBinding.listView.adapter = this.adapter
        mBinding.listView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL, false
        )
        //设定边距
        mBinding.listView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View,
                parent: RecyclerView, state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                outRect[0, line, 0] = 0
            }
        })
        //点击事件
        adapter?.setOnItemClickListener { _, index, data ->
            val bundle = bundleOf("type" to type,"position" to index)
            when(position){
                0-> mModel.toFragment(R.id.action_center2detail, bundle)
                1-> mModel.toFragment(R.id.action_list2detail, bundle)
            }
        }
    }

    override fun initListener() {
        mBinding.slRoot.setSwipeListener(object : NestedSwipeLayout.SwipeListener {
            override fun onRefresh() {
                p = 1
                loadData()
            }

            override fun onContinue() {
                p++
                loadData()
            }
        })
    }

    override fun initData() {
        p = 1
        loadData()
    }

    private fun loadData() {
        mBinding.listView.post{
            mModel.loadList(p) {
                if (p == 1) {
                    adapter?.clearData()
                    mBinding.slRoot.finishTop()
                }
                //列表
                adapter?.addDatas(it)
                if (it.isEmpty() || it.size < 10) {
                    mBinding.swipeBottomView.text = "暂无更多数据~"
                } else {
                    mBinding.swipeBottomView.text = "加载中..."
                }
            }
        }
    }

    /**
     * 列表适配器
     */
    inner class MyAdapter :
        BindRecyclerAdapter<String, ItemBillLogBinding>() {

        override fun getViewBinding(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): ItemBillLogBinding {
            val binding = ItemBillLogBinding.inflate(inflater, parent, false)
            return binding
        }

        override fun bindData(
            binding: ItemBillLogBinding,
            position: Int,
            data: String
        ) {
            binding.tvTitle.text = "标题$data"
        }

    }
}