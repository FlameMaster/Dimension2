package com.melvinhou.user_sample.setting

import android.view.LayoutInflater
import android.view.ViewGroup
import com.melvinhou.knight.KindFragment
import com.melvinhou.user_sample.databinding.FragmentSettingSysBinding


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
class SettingSysFragment : KindFragment<FragmentSettingSysBinding, SettingModel>() {
    override val _ModelClazz: Class<SettingModel>
        get() = SettingModel::class.java

    override fun openViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSettingSysBinding = FragmentSettingSysBinding.inflate(inflater, container, false)

    override fun initView() {
        mBinding.barRoot.title.text = "系统设置"
    }

    override fun initListener() {
    }

    override fun onResume() {
        super.onResume()
    }

    override fun initData() {
    }

    private fun loadData() {
    }
}