package com.melvinhou.dimension2.launch

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.melvinhou.dimension2.MainActivity
import com.melvinhou.dimension2.R
import com.melvinhou.dimension2.databinding.FragmentGuideBinding
import com.melvinhou.dimension2.utils.KeyConstant
import com.melvinhou.kami.adapter.BindRecyclerAdapter
import com.melvinhou.kami.io.SharePrefUtil
import com.melvinhou.kami.util.DimenUtils
import com.melvinhou.knight.KindFragment
import com.melvinhou.knight.databinding.ItemImgFullBinding
import com.melvinhou.knight.loadImage


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/6/6 0006 17:03
 * <p>
 * = 分 类 说 明：引导页
 * ================================================
 */
class GuideFragment : KindFragment<FragmentGuideBinding, LaunchViewModel>() {
    override val _ModelClazz: Class<LaunchViewModel>
        get() = LaunchViewModel::class.java

    override fun openViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentGuideBinding = FragmentGuideBinding.inflate(inflater, container, false)

    //列表
    private var adapter: MyAdapter? = null
    private var mediator: TabLayoutMediator? = null

    //页面切换监听
    private var pageCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            mBinding.btSkip.visibility =
                if (position == (adapter?.datasSize ?: 1) - 1) View.VISIBLE else View.INVISIBLE
        }
    }


    override fun initView() {
        //间隔
        mModel.WindowInsets.observe(this) { insets ->
            mBinding.root.setPadding(0, insets.top, 0, insets.bottom)
        }
        //设置比例
//        mBinding.container.post {
//            val lp = mBinding.container.layoutParams as? ConstraintLayout.LayoutParams
//            lp?.let {
//                it.dimensionRatio = "${mBinding.root.width}:${mBinding.root.height}"
//                mBinding.container.layoutParams = it
//            }
//        }

        //预加载和用户输入
//        mBinding.container.offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
        mBinding.container.offscreenPageLimit = 2
        mBinding.container.isUserInputEnabled = true
        //切换效果
        val MIN_SCALE = 0.86f//最小缩放
        var MAX_TRANSLATION = 0f //最大中和位移量
        mBinding.container.setPageTransformer { page, position ->
            page.apply {
                if (MAX_TRANSLATION == 0f) MAX_TRANSLATION = width - height / 16f * 9f
                if (position < -1) { //屏幕左边，范围[-Infinity,-1]
//                    alpha = 0f
//                    translationX = pageWidth * 1f
                } else if (position <= 0f) { //左边到中间，范围[-1,0]
//                    alpha = 1 + position
                    // 中和位移
                    translationX = MAX_TRANSLATION * -position
                    // 缩放页面
                    val scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position))
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                } else if (position <= 1) { //中间到右边，范围[0,1]
//                    alpha = 1 - position
                    translationX = MAX_TRANSLATION * -position
                    val scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position))
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                } else { //屏幕右边，范围[1,+Infinity]
//                    alpha = 0f
//                    translationX = pageWidth * -1f
                }
            }
        }
    }

    override fun initListener() {
        //Adapter
        adapter = MyAdapter()
        mBinding.container.adapter = adapter
        val dp2 = DimenUtils.dp2px(2)
        val dp8 = DimenUtils.dp2px(8)
        mediator = TabLayoutMediator(mBinding.indicator, mBinding.container) { tab, position ->
//          tab.text = tabs[position]
            tab.view.setPadding(dp2, 0, dp2, 0)
            val item = View(requireContext())
            item.layoutParams = ViewGroup.LayoutParams(dp8, dp8)
            //切换颜色
            item.setBackgroundResource(R.drawable.round_dp8)
            item.backgroundTintList = resources.getColorStateList(
                R.color.selector_state_text, requireActivity().theme
            )
            tab.setCustomView(item)
        }
        //要执行这一句才是真正将两者绑定起来
        mediator?.attach()

        mBinding.btSkip.setOnClickListener {
            onNext()
//            mBinding.container.beginFakeDrag()
//            val isExecuted = mBinding.container.fakeDragBy(100f)
//            if (isExecuted) mBinding.container.endFakeDrag()
        }
    }

    override fun initData() {
        mModel.loadGuidePage { datas ->
            mBinding.indicator.removeAllTabs()
            datas.forEach {
                val tab = mBinding.indicator.newTab()
                mBinding.indicator.addTab(tab)
            }
            adapter?.clearData()
            adapter?.addDatas(datas)
        }
    }

    override fun onResume() {
        super.onResume()
        mBinding.container.registerOnPageChangeCallback(pageCallback)
    }

    override fun onPause() {
        super.onPause()
        mBinding.container.unregisterOnPageChangeCallback(pageCallback)
    }

    /**
     * 下一步操作
     */
    private fun onNext() {
        SharePrefUtil.saveBoolean(KeyConstant.APP_FIRST_LAUNCH, false)
        toActivity<MainActivity>()
        requireActivity().finish()
    }

    /**
     * 列表
     */
    inner class MyAdapter : BindRecyclerAdapter<String, ItemImgFullBinding>() {

        override fun getViewBinding(
            inflater: LayoutInflater, parent: ViewGroup
        ): ItemImgFullBinding {
            val binding = ItemImgFullBinding.inflate(inflater, parent, false)
            binding.ivImg.round = DimenUtils.dp2px(16).toFloat()
            val lp = binding.ivImg.layoutParams as? ConstraintLayout.LayoutParams
            lp?.let {
                it.dimensionRatio = "9:16"
            }
            return binding
        }

        override fun bindData(
            binding: ItemImgFullBinding, position: Int, data: String
        ) {
            binding.ivImg.loadImage(data)
        }

    }
}