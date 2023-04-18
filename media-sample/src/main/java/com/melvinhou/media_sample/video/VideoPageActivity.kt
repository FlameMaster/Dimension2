package com.melvinhou.media_sample.video

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Rect
import android.net.Uri
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams.*
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.melvinhou.kami.adapter.BindRecyclerAdapter
import com.melvinhou.kami.mvvm.BindActivity
import com.melvinhou.kami.util.DimenUtils
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.kami.util.ResourcesUtils
import com.melvinhou.media_sample.R
import com.melvinhou.media_sample.bean.MediaEntity
import com.melvinhou.media_sample.databinding.ActivityVideoPageBinding
import com.melvinhou.media_sample.databinding.ItemVideoCatalogBinding
import com.melvinhou.medialibrary.video.FcVideoActivity
import com.melvinhou.medialibrary.video.FcVideoLayout
import com.melvinhou.medialibrary.video.FcVideoView


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/4/11 0011 16:20
 * <p>
 * = 分 类 说 明：播放视频的页面
 * ================================================
 */
class VideoPageActivity : BindActivity<ActivityVideoPageBinding, VideoPageModel>() {
    override fun openViewBinding(): ActivityVideoPageBinding =
        ActivityVideoPageBinding.inflate(layoutInflater)

    override fun openModelClazz(): Class<VideoPageModel> = VideoPageModel::class.java

    private lateinit var windowInsetsController: WindowInsetsControllerCompat
    private var adapter: MyAdapter? = null
    private var mVidoeLayout: FcVideoLayout? = null


    private var mStatusHeight = 0//状态栏高度
    private var mScreenDirection = 0//屏幕方向
    private var isLandscape = false//是否横屏显示

    override fun initWindowUI() {
        super.initWindowUI()
        //设置沉浸后专栏栏和导航字体的颜色，
        windowInsetsController = WindowCompat.getInsetsController(window, mBinding.root);
        windowInsetsController.let {
            it.isAppearanceLightStatusBars = false
            it.isAppearanceLightNavigationBars = true
        }
    }

    override fun onActivityBack(type: Int) {
        if (isLandscape) fullScreen(false)
        else super.onActivityBack(type)
    }

    override fun initView() {
        super.initView()
        //
        mVidoeLayout = findViewById(R.id.video_layout)
        //
        mStatusHeight = DimenUtils.getStatusBarHeight()
        (mBinding.bar.layoutParams as? CollapsingToolbarLayout.LayoutParams)?.let {
            it.topMargin = mStatusHeight
            mBinding.bar.layoutParams = it
        }
        (mVidoeLayout?.layoutParams as? ConstraintLayout.LayoutParams)?.let {
            it.topMargin = mStatusHeight
            mVidoeLayout?.layoutParams = it
        }
        initList()
    }


    /**
     * 初始化列表
     */
    private fun initList() {
        adapter = MyAdapter()
        mBinding.container.adapter = adapter
        mBinding.container.layoutManager = LinearLayoutManager(
            baseContext, LinearLayoutManager.VERTICAL, false
        )
        //设定边距
        val decoration = DimenUtils.dp2px(10)
        mBinding.container.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View,
                parent: RecyclerView, state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                outRect[0, if (position == 0) decoration else 0, 0] = 0
            }
        })
        //点击事件
        adapter?.setOnItemClickListener { _, _, data ->
            if (data.type != 1 || data.status == 1) {
                mVidoeLayout?.setVideoURI(Uri.parse(data.url))
            } else FcUtils.showToast("无法观看")
        }

    }

    override fun initListener() {
        //设置背景
        mVidoeLayout?.setVideoControllerUIListener(object :
            FcVideoLayout.VideoControllerUIListener {
            override fun onShowControllerUI() {
                mBinding.bar.setNavigationIcon(R.drawable.ic_back_white)
                windowInsetsController.show(WindowInsetsCompat.Type.statusBars())
            }

            override fun onHideControllerUI() {
                mBinding.bar.navigationIcon = null
                if (isLandscape) windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
            }

            override fun onFullScreen(isFull: Boolean) {
                fullScreen(isFull)
            }

        })
        mVidoeLayout?.setOnPreparedListener {
            //设置屏幕常亮
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        mVidoeLayout?.setOnCompletionListener {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        mVidoeLayout?.setPlayerStateListener { state ->
            if (!isLandscape)
                (mBinding.barRoot.layoutParams as? AppBarLayout.LayoutParams)?.let {
                    if (state == FcVideoView.STATE_PLAYING) {
                        @SuppressLint("WrongConstant")
                        it.scrollFlags = SCROLL_FLAG_NO_SCROLL
                    } else {
                        it.setScrollFlags(SCROLL_FLAG_SCROLL or SCROLL_FLAG_EXIT_UNTIL_COLLAPSED)
                    }
                    mBinding.barRoot.layoutParams = it
                }
        }
    }

    override fun initData() {
        //屏幕方向
        mScreenDirection = ResourcesUtils.getResources()
            .configuration.orientation
        isLandscape = ResourcesUtils.getResources()
            .configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        //
        mModel.getVideoPage { data ->
            adapter?.clearData()
            data.list?.forEachIndexed { index, group ->
                adapter?.addDatas(group.list?.apply {
                    if (index == 0) {
                        //初始化一下数据
                        mVidoeLayout?.setVideoURI(Uri.parse(get(0).url))
                        mModel.openDrop(group.id)
                    }
                    get(0).parent_title = group.title
                    forEach {
                        it.parent_id = group.id
                    }
                })
            }
        }
    }

    override fun onDestroy() {
        mVidoeLayout?.cancel()
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        if (mVidoeLayout?.isPrepared == true) {
            mVidoeLayout?.start()
            //开启屏幕常亮
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onStop() {
        super.onStop()
        if (mVidoeLayout?.isPrepared == true) {
            mVidoeLayout?.pause()
            //关闭屏幕常亮
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    /**
     * 旋转方向
     */
    fun fullScreen(isFull: Boolean) {
        if (isFull) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            val outMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getRealMetrics(outMetrics)
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }
    }


    /**
     * 屏幕方向切换
     *
     * @param requestedOrientation
     */
    override fun setRequestedOrientation(requestedOrientation: Int) {
        if ((requestedOrientation == FcVideoActivity.SCREEN_DIRECTION_UNDEFINED) or (requestedOrientation == mScreenDirection)) return
        super.setRequestedOrientation(requestedOrientation)
        mScreenDirection = requestedOrientation
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
        super.onConfigurationChanged(newConfig)
        //自动横竖屏的时候设置状态
        mVidoeLayout?.setFullScreen(isLandscape)
        //ui变化
        (mVidoeLayout?.layoutParams as? ConstraintLayout.LayoutParams)?.let {
            if (isLandscape) {
                it.topMargin = 0
                val size = DimenUtils.getScreenSize()
                it.dimensionRatio = "${size[0]}:${size[1]}"
            } else {
                it.topMargin = mStatusHeight
                it.dimensionRatio = "16:9"
            }
            mVidoeLayout?.layoutParams = it
        }
        (mBinding.barRoot.layoutParams as? AppBarLayout.LayoutParams)?.let {
            if (isLandscape) {
                @SuppressLint("WrongConstant")
                it.scrollFlags = SCROLL_FLAG_NO_SCROLL
            } else if (mVidoeLayout?.isPlaying != true){
                it.setScrollFlags(SCROLL_FLAG_SCROLL or SCROLL_FLAG_EXIT_UNTIL_COLLAPSED)
            }
            mBinding.barRoot.layoutParams = it
        }
        //状态栏
        if (!isLandscape) windowInsetsController.show(WindowInsetsCompat.Type.statusBars())
    }

    inner class MyAdapter : BindRecyclerAdapter<MediaEntity, ItemVideoCatalogBinding>() {

        override fun getViewBinding(
            inflater: LayoutInflater, parent: ViewGroup
        ): ItemVideoCatalogBinding {
            val binding = ItemVideoCatalogBinding.inflate(inflater, parent, false)
            return binding
        }

        override fun bindData(
            binding: ItemVideoCatalogBinding, position: Int, data: MediaEntity
        ) {
            //章节标题
            binding.tvTitleBig.visibility =
                if (TextUtils.isEmpty(data.parent_title)) View.GONE else View.VISIBLE
            binding.tvTitleBig.text = data.parent_title
            //小节
            binding.tvTitle.text = data.title
            binding.tvDuration.text = data.duration.toString()
            //0免费1收费
            binding.tvTag.visibility = if (data.type == 0) View.VISIBLE else View.GONE
            //0默认1解锁
            binding.ivLock.visibility =
                if (data.type == 1 && data.status != 1) View.VISIBLE else View.GONE

            //展开和关闭
            mModel.showItem.observe(this@VideoPageActivity) {
                val isCheck = it?.contains(data.parent_id) ?: false
                binding.tvTitleBig.isSelected = isCheck
                binding.clSection.visibility = if (isCheck) View.VISIBLE else View.GONE
            }
            val isCheck = mModel.showItem.value?.contains(data.parent_id) ?: false
            binding.tvTitleBig.isSelected = isCheck
            binding.clSection.visibility = if (isCheck) View.VISIBLE else View.GONE
            binding.tvTitleBig.setOnClickListener {
                mModel.openDrop(data.parent_id)
            }
        }

    }

}