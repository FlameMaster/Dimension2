package com.melvinhou.game

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.melvinhou.game.databinding.ItemGameLaunchBinding
import com.melvinhou.game.klotski.GameKlotskiActivity
import com.melvinhou.game.poker.GamePokerActivity
import com.melvinhou.kami.adapter.BindRecyclerAdapter
import com.melvinhou.kami.adapter.BindViewHolder
import com.melvinhou.kami.databinding.ActivityListBinding
import com.melvinhou.kami.mvvm.BaseViewModel
import com.melvinhou.kami.util.DimenUtils
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.knight.KindActivity
import java.util.*


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/4/28 0028 15:02
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
class GameLaunchActivity : KindActivity<ActivityListBinding, BaseViewModel>() {
    override val _ViewBinding: ActivityListBinding
        get() = ActivityListBinding.inflate(layoutInflater)
    override val _ModelClazz: Class<BaseViewModel>
        get() = BaseViewModel::class.java


    private var adapter: MyAdapter? = null

    //触摸帮助类
    private var mItemTouchHelper: ItemTouchHelper? = null

    //拖拽监听
    private val mDragStartListener: SimpleItemTouchHelperCallback.OnStartDragListener =
        object : SimpleItemTouchHelperCallback.OnStartDragListener {
            override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
                mItemTouchHelper?.startDrag(viewHolder)
            }
        }

    override fun initView() {
        mBinding.barRoot.title.text = "风尘的游戏"
        //列表
        initList()
    }

    /**
     * 列表
     */
    private fun initList() {
        adapter = MyAdapter()
        mBinding.container.adapter = this.adapter
        mBinding.container.layoutManager = GridLayoutManager(baseContext, 3)
        //设定边距
        val decoration = DimenUtils.dp2px(5)
        mBinding.container.setPadding(decoration, decoration, decoration, decoration)
        if (decoration < 0)//不建议执行，会扰乱拖拽后样式
            mBinding.container.addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect, view: View,
                    parent: RecyclerView, state: RecyclerView.State
                ) {
                    val position = parent.getChildAdapterPosition(view)
                    val left = decoration
                    val top = if (position % 3 == 0) decoration else 0
                    val right = decoration
                    val bottom = decoration
                    outRect[left, top, right] = bottom
                }
            })
        //点击事件
        adapter?.setOnItemClickListener { tag, position, data ->
            Bundle().apply {

            }
            when (data) {
                "FC斗地主"->toActivity<GamePokerActivity>()
                "华容道FC"->toActivity<GameKlotskiActivity>()
                "风尘的冒险"->toActivity<GamePokerActivity>()
            }
        }
        //初始化拖拽监听
        val callback: ItemTouchHelper.Callback =
            SimpleItemTouchHelperCallback(
                object :
                    SimpleItemTouchHelperCallback.ItemTouchHelperAdapter {
                    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
                        Collections.swap(adapter?.datas, fromPosition, toPosition)
                        adapter?.notifyItemMoved(fromPosition, toPosition)
                        return true
                    }

                    override fun onItemDismiss(position: Int) {
                        adapter?.datas?.removeAt(position)
                        adapter?.notifyItemRemoved(position)
                    }

                })
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper?.attachToRecyclerView(mBinding.container)
    }

    override fun initData() {
        adapter?.clearData()
        adapter?.addData("FC斗地主")
        adapter?.addData("华容道FC")
        adapter?.addData("风尘的冒险")
        adapter?.addData("弹幕游戏")
    }


    /**
     * 列表
     */
    inner class MyAdapter : BindRecyclerAdapter<String, ItemGameLaunchBinding>() {

        override fun getViewBinding(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): ItemGameLaunchBinding {
            val binding = ItemGameLaunchBinding.inflate(inflater, parent, false)
            return binding
        }

        override fun bindData(
            viewHolder: BindViewHolder<ItemGameLaunchBinding>,
            position: Int,
            data: String?
        ) {
            super.bindData(viewHolder, position, data)
            viewHolder.itemView.setOnTouchListener { v, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(viewHolder)
                }
                false
            }
        }

        override fun bindData(
            binding: ItemGameLaunchBinding,
            position: Int,
            data: String
        ) {
            binding.title.text = data
        }

    }
}