package com.melvinhou.dimension2.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.recyclerview.widget.*
import com.melvinhou.dimension2.databinding.FragmentHomeBinding
import com.melvinhou.dimension2.web.WebBrowserActivity
import com.melvinhou.game.GameLaunchActivity
import com.melvinhou.kami.adapter.BindRecyclerAdapter
import com.melvinhou.kami.util.DimenUtils
import com.melvinhou.knight.KindFragment
import com.melvinhou.knight.databinding.ItemImgRatioBinding
import com.melvinhou.knight.loadImage
import com.melvinhou.model3d_sample.sample.D3SampleListActivity
import com.melvinhou.test.TestActivity
import io.reactivex.Observable
import java.util.concurrent.TimeUnit


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/4/27 0027 17:02
 * <p>
 * = 分 类 说 明：演示首页
 * ================================================
 */
class HomeFragment() : KindFragment<FragmentHomeBinding, HomeViewModel>() {
    override fun openViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup
    ): FragmentHomeBinding =
        FragmentHomeBinding.inflate(inflater, container, false)

    override val _ModelClazz: Class<HomeViewModel>
        get() = HomeViewModel::class.java

    private var adapter: MyAdapter? = null

    override fun initView() {

        //列表
        initList()
    }

    /**
     * 列表
     */
    private fun initList() {
        adapter = MyAdapter()
        mBinding.rvBanner.adapter = this.adapter
        mBinding.rvBanner.layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.HORIZONTAL, false
        )
//        PagerSnapHelper ().attachToRecyclerView(mBinding.rvBanner)
        mBinding.rvBanner.layoutManager = BannerLayoutManager(true)
        adapter?.setOnItemClickListener { viewHolder, position, data ->
        }
        //轮播
        mModel.startTimer(3) {
            val startChild = mBinding.rvBanner.getChildAt(0)
            val index = mBinding.rvBanner.layoutManager?.getPosition(startChild) ?: 0
            mBinding.rvBanner.smoothScrollToPosition(index + 1)
        }
    }


    override fun initListener() {
        mBinding.tvFunLab.setOnClickListener {
            toActivity<TestActivity>()
        }
        mBinding.tvFunBrowser.setOnClickListener {
            Bundle().apply {
                putString("title", "微软中国")
                putString("url", "https://cn.bing.com/")
                toActivity<WebBrowserActivity>(this)
            }
        }
        mBinding.tvFunGame.setOnClickListener {
            toActivity<GameLaunchActivity>()
        }
        mBinding.tvFunModel.setOnClickListener {
            toActivity<D3SampleListActivity>()
        }
    }


    override fun initData() {

        mModel.loadBanner { datas ->
            adapter?.clearData()
            adapter?.addDatas(datas)
        }
    }


    /**
     * 列表
     */
    inner class MyAdapter : BindRecyclerAdapter<String, ItemImgRatioBinding>() {

        override fun getViewBinding(
            inflater: LayoutInflater, parent: ViewGroup
        ): ItemImgRatioBinding {
            val binding = ItemImgRatioBinding.inflate(inflater, parent, false)
            binding.ivImg.round = DimenUtils.dp2px(8).toFloat()
            val lp = binding.ivImg.layoutParams as? ConstraintLayout.LayoutParams
            lp?.let {
                it.dimensionRatio = "16:9"
                it.marginEnd = DimenUtils.dp2px(16)
                it.marginStart = DimenUtils.dp2px(16)
            }
            return binding
        }

        override fun bindData(
            binding: ItemImgRatioBinding, position: Int, data: String
        ) {
            binding.ivImg.loadImage(data)
        }

    }
}