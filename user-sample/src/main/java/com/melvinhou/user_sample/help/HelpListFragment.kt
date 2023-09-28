package com.melvinhou.user_sample.help

import android.graphics.Rect
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.melvinhou.kami.adapter.BindRecyclerAdapter
import com.melvinhou.kami.adapter.RecyclerHolder
import com.melvinhou.kami.databinding.ActivityListSwipeBinding
import com.melvinhou.kami.util.DimenUtils
import com.melvinhou.kami.view.wiget.NestedSwipeLayout
import com.melvinhou.knight.KindFragment
import com.melvinhou.knight.databinding.ItemTitle01Binding
import com.melvinhou.user_sample.R


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
 * = 分 类 说 明：问题列表
 * ================================================
 */
class HelpListFragment : KindFragment<ActivityListSwipeBinding, HelpModel>() {
    override val _ModelClazz: Class<HelpModel>
        get() = HelpModel::class.java

    override fun openViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ActivityListSwipeBinding = ActivityListSwipeBinding.inflate(inflater, container, false)

    private var adapter: MyAdapter? = null
    private var p = 1


    override fun initView() {
        val title = arguments?.getString("title")
        mBinding.barRoot.title.text = if (TextUtils.isEmpty(title)) "全部问题" else "分类$title"
        initList()
    }

    /**
     * 初始化列表
     */
    private fun initList() {
        val dp8 = DimenUtils.dp2px(8)
        adapter = MyAdapter()
        mBinding.listRoot.listView.adapter = this.adapter
        mBinding.listRoot.listView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL, false
        )
        //设定边距
        mBinding.listRoot.listView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View,
                parent: RecyclerView, state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                outRect[0, 1, 0] = 0
            }
        })
        //点击事件
        adapter?.setOnItemClickListener { _, _, data ->
            val bundle = bundleOf("title" to data)
            mModel.toFragment(R.id.action_list2detail, bundle)
        }
    }

    override fun initListener() {
        mBinding.listRoot.slRoot.setSwipeListener(object : NestedSwipeLayout.SwipeListener {
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
        mModel.loadDelayList(p) {
            if (p == 1) {
                adapter?.clearData()
                mBinding.listRoot.slRoot.finishTop()
            }
            //列表
            adapter?.addDatas(it)
            if (it.isEmpty() || it.size < 10) {
                mBinding.listRoot.swipeBottomView.text = "暂无更多数据~"
            } else {
                mBinding.listRoot.swipeBottomView.text = "加载中..."
            }
        }
    }

    /**
     * 列表适配器
     */
    inner class MyAdapter :
        BindRecyclerAdapter<String, ItemTitle01Binding>() {

        override fun getViewBinding(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): ItemTitle01Binding {
            val binding = ItemTitle01Binding.inflate(inflater, parent, false)
            binding.itemValue.text = ""
            return binding
        }

        override fun bindData(
            binding: ItemTitle01Binding,
            position: Int,
            data: String
        ) {
            binding.itemTitle.text = "分类问题$data"
        }

        override fun bindCustomData(viewHolder: RecyclerHolder?, position: Int, itemViewType: Int) {
//            p++
//            loadData()
        }

    }
}