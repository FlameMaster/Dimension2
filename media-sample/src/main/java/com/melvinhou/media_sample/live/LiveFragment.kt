package com.melvinhou.media_sample.live

import android.content.Intent
import android.graphics.Rect
import android.text.InputType
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.melvinhou.kami.adapter.BindRecyclerAdapter
import com.melvinhou.kami.databinding.ActivityListBinding
import com.melvinhou.kami.mvvm.BindFragment
import com.melvinhou.kami.util.DimenUtils
import com.melvinhou.knight.KUITools
import com.melvinhou.media_sample.databinding.ItemMediaTabBinding
import com.melvinhou.medialibrary.video.FcVideoActivity


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/4/12 0012 17:52
 * <p>
 * = 分 类 说 明：直播
 * ================================================
 */
class LiveFragment : BindFragment<ActivityListBinding, BiliModel>() {

    override fun openViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ActivityListBinding =
        ActivityListBinding.inflate(inflater, container, false)

    override fun openModelClazz(): Class<BiliModel> =
        BiliModel::class.java

    private var adapter: MyAdapter? = null
    override fun initView() {
        initList()
    }

    /**
     * 初始化列表
     */
    private fun initList() {
        adapter = MyAdapter()
        mBinding.container.adapter = this.adapter
        mBinding.container.layoutManager = GridLayoutManager(requireContext(), 3)
        //设定边距
        val decoration = DimenUtils.dp2px(10)
        mBinding.container.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View,
                parent: RecyclerView, state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                val top = if (position / 3 == 0) decoration else 0
                val left = if (position % 3 == 0) decoration else 0
                val right = if (position % 3 == 2) decoration else 0
                val bottom = decoration
                outRect[left, top, right] = bottom
            }
        })
        //点击事件
        adapter?.setOnItemClickListener { _, _, data ->
            KUITools.showInputDialog01(
                requireActivity(),
                "直播间",
                "输入直播间号",
                InputType.TYPE_CLASS_NUMBER
            ) {
                if (!TextUtils.isEmpty(it))
                openBiliLive(it!!)
            }
        }
    }

    override fun initData() {
        adapter?.clearData()
        adapter?.addData("哔哩哔哩")
    }

    private fun openBiliLive(roomId: String) {
        mModel.loadBiliLivePath2(roomId) {
            Intent().apply {
                setClass(requireContext(), FcVideoActivity::class.java)
                putExtra("url", it?.durl!![0].url)
                putExtra("title", "哔哩哔哩直播")
                toActivity(this)
            }
        }
    }


    /**
     * 列表适配器
     */
    inner class MyAdapter : BindRecyclerAdapter<String, ItemMediaTabBinding>() {

        override fun getViewBinding(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): ItemMediaTabBinding {
            val binding = ItemMediaTabBinding.inflate(inflater, parent, false)
            return binding
        }

        override fun bindData(
            binding: ItemMediaTabBinding,
            position: Int,
            data: String
        ) {
            binding.tvTitle.text = data
        }
    }

}