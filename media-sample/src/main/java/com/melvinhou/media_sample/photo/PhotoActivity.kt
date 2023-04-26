package com.melvinhou.media_sample.photo

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.melvinhou.kami.adapter.RecyclerAdapter
import com.melvinhou.kami.adapter.RecyclerHolder
import com.melvinhou.kami.adapter.ViewPagerLayoutManager
import com.melvinhou.kami.adapter.ViewPagerListener
import com.melvinhou.kami.mvvm.BaseViewModel
import com.melvinhou.kami.mvvm.BindActivity
import com.melvinhou.kami.mvvm.BindFragment
import com.melvinhou.knight.loadImage
import com.melvinhou.media_sample.databinding.FragmentPhotoBinding
import com.melvinhou.media_sample.databinding.FragmentPictureBinding
import com.melvinhou.medialibrary.photoview.view.PhotoView


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
class PhotoActivity : BindActivity<FragmentPhotoBinding, BaseViewModel>() {

    override fun openViewBinding(): FragmentPhotoBinding =
        FragmentPhotoBinding.inflate(layoutInflater)

    override fun openModelClazz(): Class<BaseViewModel> =
        BaseViewModel::class.java

    private val TAG = PhotoActivity::class.java.name

    private var mAdapter: MyAdapter? = null
    private var mLayoutManager :ViewPagerLayoutManager? = null

    override fun initView() {
        mBinding.barRoot.title.text = "图片详情"
        mBinding.barRoot.barRoot.setBackgroundColor(0x80FFFFFF.toInt())
    }

    override fun initListener() {
        mAdapter = MyAdapter()
        mLayoutManager = ViewPagerLayoutManager(baseContext,ViewPagerLayoutManager.HORIZONTAL)
        mBinding.container.adapter = mAdapter
        mBinding.container.layoutManager = mLayoutManager
        mAdapter?.setOnItemClickListener { viewHolder: RecyclerHolder?, position: Int, data: String? ->
            //标题栏
            val isVisible = mBinding.barRoot.barRoot.isVisible
            mBinding.barRoot.barRoot.isVisible = !isVisible
        }
        mLayoutManager?.setViewPagerListener(object :ViewPagerListener{
            override fun onInitComplete() {
                Log.i(TAG,"onInitComplete" )
            }

            override fun onPageRelease(isNext: Boolean, position: Int) {
                Log.i(TAG,"release position :$position next page:$isNext")
                var index = 0
                index = if (isNext) {
                    0
                } else {
                    1
                }
            }

            override fun onPageSelected(position: Int, isBottom: Boolean, isLeftScroll: Boolean) {
                Log.i(TAG,"select:"+position+" isBottom:"+isBottom + "isLeftScroll:"+isBottom);
                mBinding.barRoot.title.text = "${position+1}/${mAdapter?.datasSize}"
            }
        })
    }

    override fun initData() {
        val list = intent.getStringExtra("url")?.split(",")
        mAdapter?.clearData()
        list?.let {
            mAdapter?.addDatas(it)
            mBinding.barRoot.title.text = "1/${it.size}"
        }
    }


    internal class MyAdapter : RecyclerAdapter<String?, RecyclerHolder>() {
        override fun bindData(viewHolder: RecyclerHolder, position: Int, data: String?) {
            val child = viewHolder.itemView as? PhotoView
            child?.let {
                it.loadImage(data,-1,-1)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerHolder {
            val child = PhotoView(parent.context)
            child.layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT
            )
            return RecyclerHolder(child)
        }

        override fun getItemLayoutId(viewType: Int): Int {
            return 0
        }

        override fun onCreate(view: View, viewType: Int): RecyclerHolder? {
            return RecyclerHolder(view)
        }
    }
}