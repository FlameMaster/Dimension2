package com.melvinhou.user_sample.help

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.melvinhou.kami.adapter.BindRecyclerAdapter
import com.melvinhou.kami.databinding.ItemsImgChooseBinding
import com.melvinhou.knight.KindFragment
import com.melvinhou.knight.loadImage
import com.melvinhou.user_sample.databinding.FragmentHelpFeedbackBinding


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
 * = 分 类 说 明：意见反馈
 * ================================================
 */
class FeedbackFragment : KindFragment<FragmentHelpFeedbackBinding, HelpModel>() {
    override val _ModelClazz: Class<HelpModel>
        get() = HelpModel::class.java

    override fun openViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHelpFeedbackBinding = FragmentHelpFeedbackBinding.inflate(inflater, container, false)


    private var fileAdapter: FileAdapter? = null

    override fun initView() {
        mBinding.barRoot.title.text = "用户反馈"
        mBinding.root.postDelayed({
            mBinding.etContent.isFocusable = true
            mBinding.etContent.isFocusableInTouchMode = true
            mBinding.etContact.isFocusable = true
            mBinding.etContact.isFocusableInTouchMode = true
            mBinding.btSubmit.isEnabled = true
        },500)
        initList()
    }

    /**
     * 初始化列表
     */
    private fun initList() {
        fileAdapter = FileAdapter()
        mBinding.rvFiles.adapter = this.fileAdapter
        mBinding.rvFiles.layoutManager = GridLayoutManager(requireContext(), 3)
        //点击事件
        fileAdapter?.setOnItemClickListener { _, _, data ->
            val isFull = !TextUtils.isEmpty(data)
            if (!isFull) {
                openImage(ActivityResultContracts.PickVisualMedia.ImageOnly){uri->
                    val count = fileAdapter?.datasSize ?: 1
                    fileAdapter?.addData(count - 1, uri.toString())
                }
            }
        }
        fileAdapter?.clearData()
        fileAdapter?.addData("")
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



    /**
     * 列表适配器
     */
    inner class FileAdapter :
        BindRecyclerAdapter<String, ItemsImgChooseBinding>() {

        override fun getViewBinding(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): ItemsImgChooseBinding {
            val binding = ItemsImgChooseBinding.inflate(inflater, parent, false)
            return binding
        }

        override fun bindData(
            binding: ItemsImgChooseBinding,
            position: Int,
            data: String
        ) {
            val isFull = !TextUtils.isEmpty(data)
            binding.delete.isVisible = isFull
            binding.add.isVisible = !isFull
            if (isFull) {
                binding.img.loadImage(data)
            } else binding.img.setImageDrawable(null)
            //删除
            binding.delete.setOnClickListener {
                if (isFull) removedData(position)
            }
        }

    }

}