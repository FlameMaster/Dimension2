package com.sample.im_sample

import android.graphics.Rect
import android.text.TextUtils
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.melvinhou.accountlibrary.bean.User
import com.melvinhou.kami.adapter.BindRecyclerAdapter
import com.melvinhou.kami.tool.UITools
import com.melvinhou.kami.util.DimenUtils
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.kami.util.StringUtils
import com.melvinhou.knight.KindFragment
import com.melvinhou.knight.loadImage
import com.sample.im_sample.bean.ImChatEntity
import com.sample.im_sample.databinding.FragmentImChatBinding
import com.sample.im_sample.databinding.ItemImChatMessageBinding
import com.sample.im_sample.model.ImViewModel
import com.sample.im_sample.tcp.ImTcpClient
import com.sample.im_sample.udp.ImUdpClient
import java.io.*


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/5/5 0005 16:41
 * <p>
 * = 分 类 说 明：即时通讯-聊天页
 * ================================================
 */
class ImChatFragment : KindFragment<FragmentImChatBinding, ImViewModel>() {
    override fun openViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentImChatBinding =
        FragmentImChatBinding.inflate(inflater, container, false)

    override val _ModelClazz: Class<ImViewModel>
        get() = ImViewModel::class.java

    private var userId: Long? = null
    private var adapter: MyAdapter? = null
    private val mClient: ImUdpClient by lazy { ImUdpClient() }


    override fun upBarMenuID(): Int = R.menu.bar_im_contacts
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_contact_add -> {
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun backward() {
        //隐藏软键盘
        WindowCompat.getInsetsController(requireActivity().window, mBinding.root)
            .hide(WindowInsetsCompat.Type.ime())
        super.backward()
    }


    override fun initView() {
        mBinding.barRoot.title.text = "联系人"
        initList()
    }

    /**
     * 初始化列表
     */
    private fun initList() {
        adapter = MyAdapter()
        mBinding.container.adapter = this.adapter
        mBinding.container.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, true)
        //设定边距
        val decoration = DimenUtils.dp2px(10)
        mBinding.container.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View,
                parent: RecyclerView, state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                outRect[0, 0, 0] = if (position == 0) decoration else 0
            }
        })
        //点击事件
        adapter?.setOnItemClickListener { _, _, data ->

        }
    }

    override fun initListener() {
        mBinding.btSubmit.setOnClickListener {
            sendMessage()
        }
        mBinding.etInput.setOnEditorActionListener { view, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                return@setOnEditorActionListener true
            }
            false
        }
        //监听列表按下关闭软键盘
        mBinding.container.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                if (e.action == MotionEvent.ACTION_DOWN && mModel.isShowKeyboard)
                    UITools.hideSoftKeyboard(mBinding.etInput)//隐藏软键盘
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mClient.close()
    }

    override fun initData() {
        userId = arguments?.getLong(User.USER_ID)
        userId?.let { id ->
            mModel.getUserInfo(id) {
                mBinding.barRoot.title.text =
                    if (TextUtils.isEmpty(it.nickName)) it.name else it.nickName
            }
            //聊天记录
            mModel.getChat(id) {
                adapter?.clearData()
                adapter?.addDatas(it)
                //滚动到底
                mBinding.container.layoutManager?.smoothScrollToPosition(
                    mBinding.container, RecyclerView.State(), 0
                )
            }
        }


        val ip = arguments?.getString("ip") ?: ""
        val port = arguments?.getInt("port") ?: 0
        //连接socket服务器
        mClient.connent(ip, port) { msg ->

        }
    }

    /**
     * 发送消息
     */
    private fun sendMessage() {
        val text = mBinding.etInput.text.toString()
//        mBinding.etInput.clearFocus()
        mBinding.etInput.text.clear()
//        UITools.hideSoftKeyboard(mBinding.etInput)//隐藏软键盘
        if (StringUtils.isEmpty(text))
            return
        //发送
        userId?.let {
            mModel.sendChat(it, text)
        }
        mClient.send(text)
    }

    /**
     * 列表适配器
     */
    inner class MyAdapter : BindRecyclerAdapter<ImChatEntity, ItemImChatMessageBinding>() {

        override fun getViewBinding(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): ItemImChatMessageBinding {
            val binding = ItemImChatMessageBinding.inflate(inflater, parent, false)
            return binding
        }

        override fun bindData(
            binding: ItemImChatMessageBinding,
            position: Int,
            data: ImChatEntity
        ) {
            binding.tvContent.text = data.message
            binding.tvDate.text = data.date
            //判断用户
            val isCurrent = data.userId == mModel.currentUserId
            binding.ivOppo.visibility = if (isCurrent) View.INVISIBLE else View.VISIBLE
            binding.ivMine.visibility = if (isCurrent) View.VISIBLE else View.INVISIBLE
            //头像
            mModel.getUserInfo(data.userId) {
                Glide.with(FcUtils.getContext())
                    .load(it.photo)
                    .apply(
                        RequestOptions()
                            .override(binding.ivMine.width, binding.ivMine.height)
                            .placeholder(R.drawable.ic_head_sample01)
                            .error(R.drawable.ic_head_sample01)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                    )
                    .into(binding.ivMine)
                Glide.with(FcUtils.getContext())
                    .load(it.photo)
                    .apply(
                        RequestOptions()
                            .override(binding.ivOppo.width, binding.ivOppo.height)
                            .placeholder(R.drawable.ic_head_sample02)
                            .error(R.drawable.ic_head_sample02)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                    )
                    .into(binding.ivOppo)
            }
        }
    }
}