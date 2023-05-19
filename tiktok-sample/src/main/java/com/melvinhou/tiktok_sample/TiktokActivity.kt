package com.melvinhou.tiktok_sample

import android.graphics.Color
import android.net.Uri
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.URLSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.melvinhou.kami.adapter.BindRecyclerAdapter
import com.melvinhou.kami.mvp.MvpActivity2
import com.melvinhou.knight.loadImage
import com.melvinhou.medialibrary.video.FcVideoView
import com.melvinhou.tiktok_sample.bean.Comment
import com.melvinhou.tiktok_sample.bean.TiktokEntity
import com.melvinhou.tiktok_sample.databinding.ActivityTiktok2Binding
import com.melvinhou.tiktok_sample.databinding.ItemTiktok2Binding


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/5/16 0016 17:29
 * <p>
 * = 分 类 说 明：mvp-v
 * ================================================
 */
class TiktokActivity : MvpActivity2<ActivityTiktok2Binding, TiktokCotract.Presenter>(),
    TiktokCotract.View {

    override fun upPresenter(): TiktokCotract.Presenter = TiktokPresenter(this)

    override fun openViewBinding(): ActivityTiktok2Binding =
        ActivityTiktok2Binding.inflate(layoutInflater)


    private lateinit var adapter: MyAdapter

    //vp的回调
    private val mPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            adapter.getData(position)?.let {
                binding.inInfo.tvTitle.text = "@${it.user.nickName}"
                binding.inInfo.tvExplain.text = it.title
                //评论
                clearCommentItems()
                it.comments.forEach { comment ->
                    jionHigCommentItem(comment)
                    comment.subComments.forEach { subComment ->
                        jionSubCommentItem(subComment)
                    }
                }
            }
        }
    }

    override fun initView() {
        binding.container.isUserInputEnabled = true//用户输入
        binding.container.offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT//预加载
        //适配器
        adapter = MyAdapter()
        binding.container.adapter = adapter
    }

    override fun initListener() {
        adapter.setOnItemClickListener { viewHolder, position, data ->
            if (binding.inComment.root.isOpen)
                binding.inComment.root.close()
            else
                viewHolder.binding.video.apply {
                    if (isPlaying) {
                        pause()
                    } else {
                        start()
                    }
                }
        }
        binding.inComment.root.setStateListener {
            binding.container.isUserInputEnabled = !it//用户输入
        }
        binding.inTools.btComment.setOnClickListener {
            binding.inComment.root.open()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.container.registerOnPageChangeCallback(mPageChangeCallback)
    }

    override fun onPause() {
        super.onPause()
        binding.container.unregisterOnPageChangeCallback(mPageChangeCallback)
    }

    override fun addItems(isRefresh: Boolean, items: ArrayList<TiktokEntity>) {
        if (isRefresh) adapter.clearData()
        adapter.addDatas(items)
    }


    //清空评论
    private fun clearCommentItems() {
        binding.inComment.commentsContainer.removeAllViews()
    }

    //添加评论
    private fun jionHigCommentItem(comment: Comment) {
        val view = View.inflate(this, R.layout.item_comment_hig, null)
        binding.inComment.commentsContainer.addView(view)
        val photoView = view?.findViewById<ImageView>(R.id.user_photo)
        val titleView = view?.findViewById<TextView>(R.id.user_name)
        val contextView = view?.findViewById<TextView>(R.id.context)
        photoView?.loadImage(comment.user.photo)
        titleView?.setText(comment.user.nickName)
        contextView?.setText(comment.content)
    }

    //添加子评论
    private fun jionSubCommentItem(comment: Comment.SubComment) {
        val view = View.inflate(this, R.layout.item_comment_sub, null)
        binding.inComment.commentsContainer.addView(view)
        val photoView = view?.findViewById<ImageView>(R.id.user_photo)
        val titleView = view?.findViewById<TextView>(R.id.user_name)
        val contextView = view?.findViewById<TextView>(R.id.context)
        photoView?.loadImage(comment.user.photo)
        titleView?.setText(comment.user.nickName)
        contextView?.setText(comment.content)
        //用户名
        val currentUser = adapter.getData(binding.container.currentItem)
        val spbuilder = SpannableStringBuilder()
        if (comment.user.nickName == currentUser.user.nickName) {
            spbuilder.append("\r作者\r")
            spbuilder.setSpan(
                ForegroundColorSpan(Color.WHITE),
                0, spbuilder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spbuilder.setSpan(
                BackgroundColorSpan(Color.RED),
                0, spbuilder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spbuilder.setSpan(
                RelativeSizeSpan(0.8f),
                0, spbuilder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spbuilder.append("\r\r")
        }
        titleView?.setText(spbuilder.append(comment.user.nickName))

        //评论内容
        val builder = SpannableStringBuilder()
        if (!TextUtils.isEmpty(comment.getCorrelativeUserName())) {
            var size = 0
            builder.append("回复").append("\r")
            builder.setSpan(
                ForegroundColorSpan(getColor(R.color.gray)),
                size, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            size = builder.length
            builder.append("@").append(comment.getCorrelativeUserName())
            builder.setSpan(
                URLSpan("https://www.baidu.com/"),
                size, builder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            builder.append(":\r\r")
        }
        contextView?.setText(builder.append(comment.getContent()))
    }


    internal class MyAdapter :
        BindRecyclerAdapter<TiktokEntity, ItemTiktok2Binding>() {
        override fun getViewBinding(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): ItemTiktok2Binding {
            return ItemTiktok2Binding.inflate(inflater, parent, false)
        }

        override fun bindData(
            binding: ItemTiktok2Binding,
            position: Int,
            data: TiktokEntity
        ) {
            binding.videoLoading.isVisible = true
            binding.videoErrorMessage.isVisible = false
            binding.videoPlay.isVisible = false
            binding.video.setVideoURI(Uri.parse(data.url))
            //监听
            binding.video.setOnPreparedListener {
                binding.videoLoading.isVisible = false
            }
            binding.video.setOnErrorListener { mp, what, extra ->
                binding.videoErrorMessage.isVisible = true
                binding.videoErrorMessage.text = "视频错误"
                true
            }
            binding.video.setPlayerStateListener {
                when (it) {
                    FcVideoView.STATE_PAUSED,
                    FcVideoView.STATE_STOPPED,
                    FcVideoView.STATE_PLAYBACK_COMPLETED,
                    -> {
                        binding.videoPlay.isVisible = true
                    }
                    else -> {
                        binding.videoPlay.isVisible = false
                    }
                }
            }
        }
    }
}