package com.melvinhou.anim_sample

import android.util.SparseArray
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.melvinhou.kami.databinding.ActivityPageBinding
import com.melvinhou.kami.mvvm.BaseViewModel
import com.melvinhou.kami.mvvm.BindActivity
import com.melvinhou.kami.view.interfaces.BaseView


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2022/10/11 0011 16:59
 * <p>
 * = 分 类 说 明：系统动画
 * ================================================
 */
class AnimSysActivity : BindActivity<ActivityPageBinding, BaseViewModel>() {

    override fun openViewBinding(): ActivityPageBinding =
        ActivityPageBinding.inflate(layoutInflater)

    override fun openModelClazz(): Class<BaseViewModel> =
        BaseViewModel::class.java

    private val fragments: SparseArray<BaseView> = SparseArray()
    private var mediator: TabLayoutMediator? = null
    private val tabs = arrayOf("帧动画", "属性动画-代码", "属性动画-RES", "补间动画-代码", "补间动画-RES")

    override fun initView() {
        super.initView()
        mBinding.barRoot.title.text = "系统动画"

        //禁用预加载
        mBinding.container.offscreenPageLimit = 1
        mBinding.container.isUserInputEnabled = false
        //Adapter
        mBinding.container.adapter =
            object : FragmentStateAdapter(supportFragmentManager, lifecycle) {
                override fun createFragment(position: Int): Fragment {
                    //FragmentStateAdapter内部自己会管理已实例化的fragment对象。
                    // 所以不需要考虑复用的问题
                    return fragments[position] as Fragment
                }

                override fun getItemCount(): Int {
                    return fragments.size()
                }
            }
        mediator = TabLayoutMediator(mBinding.indicator, mBinding.container) { tab, position ->
//          tab.text = tabs[position]
        }
        //要执行这一句才是真正将两者绑定起来
        mediator?.attach()
    }

    override fun initData() {

        mModel.isRequest.observe(this) {
            if (it) {
                showProcess(null)
            } else {
                hideProcess()
            }
        }

        fragments.clear()
        mBinding.indicator.removeAllTabs()
        for (i in tabs.indices) {
            mBinding.indicator.addTab(mBinding.indicator.newTab().setText(tabs[i]))
            fragments.put(i, AnimSysFragment.instance(i))
        }

        //跳转位置
        val index = intent.getIntExtra("index", -1)
        if (index > 0) {
            mBinding.container.setCurrentItem(index, false)
        }
    }

}