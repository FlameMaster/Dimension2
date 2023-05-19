package com.melvinhou.dimension2.aaa.function

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.melvinhou.dimension2.aaa.function.desktop.DesktopActivity
import com.melvinhou.dimension2.aaa.function.document.pdf.PdfActivity
import com.melvinhou.dimension2.aaa.function.document.zip.ZipActivity
import com.melvinhou.tiktok_sample.TiktokActivity
import com.melvinhou.dimension2.databinding.FragmentFunctionBinding
import com.melvinhou.dimension2.net.HttpConstant
import com.melvinhou.kami.mvvm.BaseViewModel
import com.melvinhou.knight.FragmentContainActivity
import com.melvinhou.knight.KindFragment
import com.sample.im_sample.ImHomeActivity
import com.sample.im_sample.tcp.ImTcpChatFragment


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/5/5 0005 14:56
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
class FunctionFragment  : KindFragment<FragmentFunctionBinding, BaseViewModel>() {
    override fun openViewBinding(inflater: LayoutInflater, container: ViewGroup): FragmentFunctionBinding =
        FragmentFunctionBinding.inflate(inflater, container, false)
    override val _ModelClazz: Class<BaseViewModel>
        get() = BaseViewModel::class.java

    override fun initView() {

    }

    override fun initListener() {
        mBinding.inFunDocument.root.getChildAt(0).setOnClickListener {
            Bundle().apply {
                val url = HttpConstant.SERVER_RES + "pdf/product.pdf"
                putString("url",url)
                putString("title","测试专用PDF")
                toActivity<PdfActivity>(this)
            }
        }
        mBinding.inFunDocument.root.getChildAt(1).setOnClickListener {
            toActivity<ZipActivity>()
        }
        mBinding.inFunIm.root.getChildAt(0).setOnClickListener {
            toActivity<ImHomeActivity>()
        }
        mBinding.inFunIm.root.getChildAt(1).setOnClickListener {
            Bundle().apply {
                putSerializable("fragment", ImTcpChatFragment::class.java)
                toActivity<FragmentContainActivity>(this)
            }
        }
        mBinding.inFunMedia.root.getChildAt(0).setOnClickListener {
            toActivity<TiktokActivity>()
        }
        mBinding.inFunSys.root.getChildAt(0).setOnClickListener {
            toActivity<DesktopActivity>()
//            Bundle().apply {
//                putSerializable("fragment",ImContactsFragment::class.java)
//                toActivity<FragmentContainActivity>(this)
//            }
        }
    }
}