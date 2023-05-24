package com.melvinhou.dimension2.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.melvinhou.dimension2.databinding.FragmentHomeBinding
import com.melvinhou.test.TestActivity
import com.melvinhou.dimension2.web.WebActivity
import com.melvinhou.game.GameLaunchActivity
import com.melvinhou.kami.mvvm.BaseViewModel
import com.melvinhou.knight.KindFragment
import com.melvinhou.model3d_sample.D3ListActivity


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
class HomeFragment() : KindFragment<FragmentHomeBinding, BaseViewModel>() {
    override fun openViewBinding(inflater: LayoutInflater, container: ViewGroup): FragmentHomeBinding =
        FragmentHomeBinding.inflate(inflater, container, false)
    override val _ModelClazz: Class<BaseViewModel>
        get() = BaseViewModel::class.java


    override fun initView() {

    }

    override fun initListener() {
        mBinding.tvFunLab.setOnClickListener {
            toActivity<TestActivity>()
        }
        mBinding.tvFunBrowser.setOnClickListener {
            Bundle().apply {
                putString("title","微软中国")
                putString("url", "https://cn.bing.com/")
                toActivity<WebActivity>(this)
            }
        }
        mBinding.tvFunGame.setOnClickListener {
            toActivity<GameLaunchActivity>()
        }
        mBinding.tvFunModel.setOnClickListener {
            toActivity<D3ListActivity>()
        }
    }
}