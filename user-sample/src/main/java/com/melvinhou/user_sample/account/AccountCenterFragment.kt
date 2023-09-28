package com.melvinhou.user_sample.account

import android.util.SparseArray
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.melvinhou.kami.view.interfaces.BaseView
import com.melvinhou.knight.KindFragment
import com.melvinhou.user_sample.R
import com.melvinhou.user_sample.account.card.BankCardActivity
import com.melvinhou.user_sample.databinding.FragmentAccountCenterBinding


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
class AccountCenterFragment : KindFragment<FragmentAccountCenterBinding, AccountModel>() {
    override val _ModelClazz: Class<AccountModel>
        get() = AccountModel::class.java

    override fun openViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAccountCenterBinding = FragmentAccountCenterBinding.inflate(inflater, container, false)


    private val fragments: SparseArray<BaseView> = SparseArray()
    private var mediator: TabLayoutMediator? = null
    private val tabs = arrayOf("余额账单","提现账单")


    override fun upBarMenuID(): Int = R.menu.bar_button

    override fun initMenu(menu: Menu?) {
        val item: MenuItem? = menu?.findItem(R.id.menu_txt)
        item?.isVisible = true
        item?.setTitle("银行卡")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_txt -> {
                toActivity<BankCardActivity>()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun initView() {
        mBinding.barRoot.title.text = "账户中心"

        //禁用预加载
        mBinding.container.offscreenPageLimit = 1
        mBinding.container.isUserInputEnabled = false
        //Adapter
        mBinding.container.adapter =
            object : FragmentStateAdapter(childFragmentManager, lifecycle) {
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
            tab.text = tabs[position]
        }
        //要执行这一句才是真正将两者绑定起来
        mediator?.attach()
    }

    override fun initListener() {
        mBinding.llLogMore.setOnClickListener {
            mModel.toFragment(R.id.action_center2log)
        }
        mBinding.tvMoneyRecharge.setOnClickListener {
            mModel.toFragment(R.id.action_center2recharge)
        }
        mBinding.tvMoneyWithdrawal.setOnClickListener {
            mModel.toFragment(R.id.action_center2withdraw)
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun initData() {
        //列表
        fragments.clear()
        mBinding.indicator.removeAllTabs()
        for (i in tabs.indices) {
            mBinding.indicator.addTab(
                mBinding.indicator
                    .newTab().setText(tabs[i])
            )
            fragments.put(i, BillListItemFragment.instance(0,i))
//            fragments.put(i, AccountTestFragment())
        }
    }

    private fun loadData() {
    }
}