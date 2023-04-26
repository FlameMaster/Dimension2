package com.melvinhou.media_sample.photo

import android.text.InputType
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.melvinhou.kami.mvvm.BaseViewModel
import com.melvinhou.kami.mvvm.BindFragment
import com.melvinhou.kami.view.wiget.PhotoCutterView
import com.melvinhou.knight.KUITools
import com.melvinhou.knight.loadImage
import com.melvinhou.media_sample.databinding.FragmentPhotoBinding
import com.melvinhou.media_sample.databinding.FragmentPictureBinding


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/4/23 0023 14:21
 * <p>
 * = 分 类 说 明：图片浏览
 * ================================================
 */
class PictureFragment : BindFragment<FragmentPictureBinding, BaseViewModel>() {

    override fun openViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup
    ): FragmentPictureBinding =
        FragmentPictureBinding.inflate(inflater, container, false)

    override fun openModelClazz(): Class<BaseViewModel> =
        BaseViewModel::class.java


    override fun initView() {
        mBinding.barRoot.title.text = "图片详情"
        mBinding.barRoot.barRoot.setBackgroundColor(0x80FFFFFF.toInt())
        mBinding.container.setGestureMode(PhotoCutterView.GESTURE_MODE_INFINITE)
    }

    override fun initListener() {
        mBinding.container.setOnClickListener {
            //标题栏
            val isVisible = mBinding.barRoot.barRoot.isVisible
            mBinding.barRoot.barRoot.isVisible = !isVisible
        }
    }

    override fun initData() {
        val url = requireActivity().intent.getStringExtra("url")
        if (TextUtils.isEmpty(url)){
            KUITools.showInputDialog01(
                requireActivity(),
                "图片地址",
                "输入需要打开的图片url",
                InputType.TYPE_CLASS_TEXT
            ) {
                mBinding.container.loadImage(it,-1,-1)
            }
        }else{
            mBinding.container.loadImage(url,-1,-1)
        }
    }
}