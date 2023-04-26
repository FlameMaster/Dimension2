package com.melvinhou.media_sample.photo

import android.app.ActivityOptions
import android.content.Intent
import android.util.Pair
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.melvinhou.anim_sample.AnimInteractionActivity
import com.melvinhou.kami.adapter.BindRecyclerAdapter
import com.melvinhou.kami.databinding.ActivityListBinding
import com.melvinhou.kami.mvvm.BindFragment
import com.melvinhou.kami.util.DimenUtils
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.knight.FragmentContainActivity
import com.melvinhou.knight.loadImage
import com.melvinhou.media_sample.bean.IllustrationEntity
import com.melvinhou.media_sample.databinding.ItemPhotoIllustrationBinding


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/4/12 0012 17:52
 * <p>
 * = 分 类 说 明：插画
 * ================================================
 */
class IllustrationFragment : BindFragment<ActivityListBinding, PhotoModel>() {

    override fun openViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): ActivityListBinding =
        ActivityListBinding.inflate(inflater, container, false)

    override fun openModelClazz(): Class<PhotoModel> =
        PhotoModel::class.java

    private var adapter: MyAdapter? = null
    override fun initView() {
        mBinding.barRoot.title.text = "插画列表"
        initList()
    }

    /**
     * 初始化列表
     */
    private fun initList() {
        val padding = DimenUtils.dp2px(5)
        mBinding.container.setPadding(padding,0,padding,0)
        adapter = MyAdapter()
        mBinding.container.adapter = this.adapter
        mBinding.container.layoutManager = StaggeredGridLayoutManager(
            2, StaggeredGridLayoutManager.VERTICAL
        )
        //点击事件
        adapter?.setOnItemClickListener { holder, _, data ->
            Intent().apply {
                setClass(requireContext(), PictureActivity::class.java)
                putExtra("url", data.url)
//                startActivity(this, ActivityOptions.makeSceneTransitionAnimation(
//                        requireActivity(),
//                        Pair(holder.binding.ivCover, "photo")
//                    ).toBundle()
//                )
                toActivity(this)
            }
        }
    }

    override fun initListener() {
        mBinding.container.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when(newState){
                    RecyclerView.SCROLL_STATE_IDLE ->{
                        Glide.with(FcUtils.getContext()).resumeRequests()
                    }
                    else->{
                        try {
                            Glide.with(FcUtils.getContext()).pauseRequests()
                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                    }
                }
            }
        })
    }

    override fun initData() {
        //数据加载
        mModel.getIllustrationList {
            adapter?.clearData()
            adapter?.addDatas(it.list)
        }
    }


    /**
     * 列表适配器
     */
    inner class MyAdapter :
        BindRecyclerAdapter<IllustrationEntity, ItemPhotoIllustrationBinding>() {

        override fun getViewBinding(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): ItemPhotoIllustrationBinding {
            val binding = ItemPhotoIllustrationBinding.inflate(inflater, parent, false)
            return binding
        }

        override fun bindData(
            binding: ItemPhotoIllustrationBinding,
            position: Int,
            data: IllustrationEntity
        ) {
            val lp = binding.ivCover.layoutParams as? ConstraintLayout.LayoutParams
            lp?.let {
                it.dimensionRatio = "${data.width}:${data.height}"
                binding.ivCover.layoutParams = it
            }
            binding.ivCover.post {
                binding.ivCover.loadImage(data.url)
            }
        }
    }

}