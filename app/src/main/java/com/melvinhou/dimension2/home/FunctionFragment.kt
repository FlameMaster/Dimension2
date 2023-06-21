package com.melvinhou.dimension2.home

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.melvinhou.accountlibrary.bean.User
import com.melvinhou.ar_sample.sample.ArSampleListActivity
import com.melvinhou.`fun`.desktop.DesktopActivity
import com.melvinhou.`fun`.document.pdf.PdfActivity
import com.melvinhou.`fun`.document.zip.ZipActivity
import com.melvinhou.tiktok_sample.TiktokActivity
import com.melvinhou.dimension2.databinding.FragmentFunctionBinding
import com.melvinhou.dimension2.net.HttpConstant
import com.melvinhou.dimension2.web.WebBrowserActivity
import com.melvinhou.dimension2.web.WebUtils
import com.melvinhou.kami.mvvm.BaseViewModel
import com.melvinhou.knight.FragmentContainActivity
import com.melvinhou.knight.KindFragment
import com.melvinhou.model3d_sample.sample.D3SampleListActivity
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
        mBinding.root.isFocusable =true
        mBinding.root.isFocusableInTouchMode = true
        mBinding.root.requestFocus()
    }

    override fun initListener() {
        //0是标题，序号从1开始
        mBinding.inFunDocument.root.getChildAt(1).setOnClickListener {
            Bundle().apply {
                val url = HttpConstant.SERVER_RES + "pdf/product.pdf"
                putString("url",url)
                putString("title","测试专用PDF")
                toActivity<PdfActivity>(this)
            }
        }
        mBinding.inFunDocument.root.getChildAt(2).setOnClickListener {
            toActivity<ZipActivity>()
        }
        mBinding.inFunDocument.root.getChildAt(3).setOnClickListener {
            val url = "https://otakuboy.oss-cn-beijing.aliyuncs.com/Ciyuan2/app/pdf/TestWord.doc"
//            val url = "https://otakuboy.oss-cn-beijing.aliyuncs.com/Ciyuan2/app/pdf/TestExcel.xlsx"
            WebUtils.toOfficeWeb(context,url,2)
        }
        mBinding.inFunIm.root.getChildAt(1).setOnClickListener {
            toActivity<ImHomeActivity>()
        }
        mBinding.inFunIm.root.getChildAt(2).setOnClickListener {
            Bundle().apply {
                putSerializable("fragment", ImTcpChatFragment::class.java)
                toActivity<FragmentContainActivity>(this)
            }
        }
        mBinding.inFunVr.root.getChildAt(1).setOnClickListener {
            toActivity<D3SampleListActivity>()
        }
        mBinding.inFunVr.root.getChildAt(2).setOnClickListener {
            toActivity<ArSampleListActivity>()
        }
        mBinding.inFunMedia.root.getChildAt(1).setOnClickListener {
            toActivity<TiktokActivity>()
        }
        mBinding.inFunSys.root.getChildAt(2).setOnClickListener {
            toActivity<DesktopActivity>()
//            Bundle().apply {
//                putSerializable("fragment",ImContactsFragment::class.java)
//                toActivity<FragmentContainActivity>(this)
//            }
        }
        mBinding.inFunSys.root.getChildAt(4).setOnClickListener {
            Bundle().apply {
                putString("url","file:///android_asset/javascript.html")
                toActivity<WebBrowserActivity>(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }
}