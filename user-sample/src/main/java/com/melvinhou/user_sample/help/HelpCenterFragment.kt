package com.melvinhou.user_sample.help

import android.graphics.Rect
import android.text.TextUtils
import android.view.*
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.melvinhou.kami.adapter.BindRecyclerAdapter
import com.melvinhou.kami.databinding.ActivityListBinding
import com.melvinhou.kami.util.DimenUtils
import com.melvinhou.knight.KindFragment
import com.melvinhou.knight.databinding.ItemTitle01Binding
import com.melvinhou.user_sample.R
import com.melvinhou.user_sample.databinding.ItemCateImg01Binding
import com.melvinhou.user_sample.databinding.TopHelpCenterBinding


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
 * = 分 类 说 明：帮助中心
 * ================================================
 */
class HelpCenterFragment : KindFragment<ActivityListBinding, HelpModel>() {
    override val _ModelClazz: Class<HelpModel>
        get() = HelpModel::class.java

    override fun openViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ActivityListBinding = ActivityListBinding.inflate(inflater, container, false)

    private var adapter: MyAdapter? = null
    private var cateAdapter: CateAdapter? = null
    private var topBinding: TopHelpCenterBinding? = null


    override fun upBarMenuID(): Int = R.menu.bar_button

    override fun initMenu(menu: Menu?) {
        val item: MenuItem? = menu?.findItem(R.id.menu_txt)
        item?.isVisible = true
        item?.setTitle("客服")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_txt -> {
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun initView() {
        mBinding.barRoot.title.text = "帮助中心"
        mBinding.barRoot.root.background=null
        initList()
    }

    /**
     * 初始化列表
     */
    private fun initList() {
        val line = DimenUtils.dp2px(1)
        val dp8 = DimenUtils.dp2px(8)
        adapter = MyAdapter()
        mBinding.container.adapter = this.adapter
        mBinding.container.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL, false
        )
        //设定边距
        mBinding.container.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View,
                parent: RecyclerView, state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                outRect[0, line, 0] = 0
            }
        })
        //点击事件
        adapter?.setOnItemClickListener { _, _, data ->
            val bundle = bundleOf("title" to data)
            mModel.toFragment(R.id.action_center2detail, bundle)
        }
        //头
        topBinding = TopHelpCenterBinding.inflate(layoutInflater, mBinding.container, false)
        adapter?.removedTopHead()
        adapter?.addHeadView(topBinding!!.root)

        //分类
        cateAdapter = CateAdapter()
        topBinding!!.rvCate.adapter = this.cateAdapter
        topBinding!!.rvCate.layoutManager = GridLayoutManager(requireContext(), 4)
        //设定边距
        topBinding!!.rvCate.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View,
                parent: RecyclerView, state: RecyclerView.State
            ) {
                outRect[0, dp8, 0] = dp8
            }
        })
        cateAdapter?.setOnItemClickListener { tag, position, data ->
            val bundle = bundleOf("title" to data)
            mModel.toFragment(R.id.action_center2list, bundle)
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
        mModel.loadList(1) {
            //分类
            val cate_list = arrayListOf<String>()
            cate_list.addAll(it.subList(0, 7))
            cate_list.add("")
            cateAdapter?.clearData()
            cateAdapter?.addDatas(cate_list)
            //列表
            adapter?.clearData()
            adapter?.addDatas(it)
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
            binding.itemTitle.text = "常见问题$data"
        }

    }

    inner class CateAdapter : BindRecyclerAdapter<String, ItemCateImg01Binding>() {

        override fun getViewBinding(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): ItemCateImg01Binding {
            val binding = ItemCateImg01Binding.inflate(inflater, parent, false)
            return binding
        }

        override fun bindData(
            binding: ItemCateImg01Binding,
            position: Int,
            data: String
        ) {
            if (TextUtils.isEmpty(data)) {//更多
                binding.tvTitle.text = "更多"
            } else {
                binding.tvTitle.text = "分类$data"
            }
        }

    }
}