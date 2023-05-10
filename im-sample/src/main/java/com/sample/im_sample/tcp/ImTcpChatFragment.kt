package com.sample.im_sample.tcp

import android.graphics.Color
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.melvinhou.kami.adapter.BindRecyclerAdapter
import com.melvinhou.kami.io.IOUtils
import com.melvinhou.kami.tool.ThreadManager
import com.melvinhou.kami.tool.UITools
import com.melvinhou.kami.util.DateUtils
import com.melvinhou.kami.util.DimenUtils
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.kami.util.StringCompareUtils
import com.melvinhou.knight.KindFragment
import com.melvinhou.knight.databinding.ItemTitle01Binding
import com.sample.im_sample.bean.ImChatEntity
import com.sample.im_sample.databinding.FragmentImChatTcpBinding
import com.sample.im_sample.model.ImViewModel


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/5/10 0010 13:35
 * <p>
 * = 分 类 说 明：使用tcp聊天
 * ================================================
 */
class ImTcpChatFragment : KindFragment<FragmentImChatTcpBinding, ImViewModel>() {
    override fun openViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentImChatTcpBinding = FragmentImChatTcpBinding.inflate(inflater, container, false)

    override val _ModelClazz: Class<ImViewModel>
        get() = ImViewModel::class.java

    //连接模式，1服务器2客户端
    private var pattern = 0
    private var serverStatus = -1//连接状态
    private var clientStatus = -1//连接状态
    private val mServerPort = 17433//默认服务器端口
    private val mServer: ImTcpServer by lazy { ImTcpServer() }
    private val mClient: ImTcpClient by lazy { ImTcpClient() }

    //列表
    private var adapter: MyAdapter? = null

    //底部距离
    private var initialBottom = -1


    override fun initView() {
        mBinding.barRoot.title.text = "TcpChat"
        initList()
        //布局变化
        ViewCompat.setOnApplyWindowInsetsListener(requireActivity().window.decorView) { view, insets ->
            val stableInsets = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or//状态栏
                        WindowInsetsCompat.Type.displayCutout() or//刘海屏
                        WindowInsetsCompat.Type.ime()//软键盘
            )
            val bottom = stableInsets.bottom
            if (initialBottom < 0) initialBottom = bottom//初始化
            mModel.isShowKeyboard = bottom > initialBottom
            //软键盘
            mBinding.root.setPadding(0, 0, 0, bottom)
            insets
        }
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
        val decoration = DimenUtils.dp2px(1)
        mBinding.container.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
            ) {
                outRect[0, 0, 0] = decoration
            }
        })
        //点击事件
        adapter?.setOnItemClickListener { _, _, data ->

        }
    }

    override fun initListener() {
        mBinding.rgPattern.setOnCheckedChangeListener { group, id ->
            hideSoftKeyboard()
            //清空数据
            adapter?.clearData()
            when (id) {
                mBinding.rb01.id -> {
                    pattern = 1
                    mClient.close()//关闭
                    ThreadManager.getThreadPool().execute {
                        val ip = IOUtils.getIPAddress()
                        group.post {
                            mBinding.tvIp.text = "IP地址：$ip"
                        }
                    }
                }
                mBinding.rb02.id -> {
                    pattern = 2
                    mServer.stop()//停止
                }
            }
            //更新
            mBinding.tvIp.isVisible = pattern == 1
            mBinding.egIp.isVisible = pattern == 2
            mBinding.btConnect.isVisible = true
            mBinding.btConnect.text = when (pattern) {
                1 -> "开启服务"
                2 -> "连接服务"
                else -> ""
            }
        }
        //按钮判断
        mBinding.btConnect.setOnClickListener {
            hideSoftKeyboard()
            mBinding.etIp.clearFocus()
            when {
                pattern == 1 && serverStatus != 0 -> {
                    mServer.stop()
                }
                pattern == 1 && serverStatus != 1 -> {
                    mServer.start(mServerPort) { type, msg ->
                        addChat(type, msg)
                    }
                }
                pattern == 2 && clientStatus != 0 -> {
                    mClient.close()
                }
                pattern == 2 && clientStatus != 1 -> {
                    val host = mBinding.etIp.text.toString()
                    if (host.isEmpty() || !StringCompareUtils.isIp(host)) {
                        FcUtils.showToast("请输入正确的ip地址")
                        return@setOnClickListener
                    }
                    mClient.connent(host, mServerPort) { type, msg ->
                        addChat(type, msg)
                    }
                }
            }
        }
        //
        mBinding.btSend.setOnClickListener {
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
                if (e.action == MotionEvent.ACTION_DOWN) hideSoftKeyboard()
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

        })
    }

    override fun initData() {
        //通讯的状态
        mServer.status.observe(this) {
            serverStatus = it
            when (it) {
                1 -> {
                    mBinding.btConnect.text = "停止服务"
                    mBinding.etInput.isEnabled = true
                }
                2 -> {
                    mBinding.btConnect.text = "停止服务"
                    mBinding.etInput.isEnabled = false
                }
                else -> {
                    if (pattern == 1) mBinding.btConnect.text = "开启服务"
                    mBinding.etInput.isEnabled = false
                    addChat(0, "服务端已关闭")
                }
            }
        }
        mClient.status.observe(this) {
            clientStatus = it
            when (it) {
                1 -> {
                    mBinding.btConnect.text = "关闭连接"
                    mBinding.etInput.isEnabled = true
                }
                else -> {
                    if (pattern == 2) mBinding.btConnect.text = "连接服务"
                    mBinding.etInput.isEnabled = false
                    addChat(0, "客户端已关闭")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mClient.close()
        mServer.stop()
    }

    /**
     * 发送消息
     */
    private fun sendMessage() {
        val text = mBinding.etInput.text.toString()
//        mBinding.etInput.clearFocus()
        mBinding.etInput.text.clear()
        when {
            serverStatus == 1 -> {
                mServer.send(text)
                addChat(1, text)
            }
            clientStatus == 1 -> {
                mClient.send(text)
                addChat(1, text)
            }
        }
    }

    /**
     * 隐藏键盘
     */
    private fun hideSoftKeyboard() {
        if (mModel.isShowKeyboard) UITools.hideSoftKeyboard(mBinding.etInput)//隐藏软键盘
    }

    /**
     * 添加聊天
     */
    private fun addChat(type: Int, text: String) {
        val entity = ImChatEntity()
        entity.message = text
        entity.date = DateUtils.formatDuration("yyyy-MM-dd  HH:mm:ss")
        entity.userId = type.toLong()
        requireActivity().runOnUiThread {
            adapter?.addData(0, entity)
            //滚动到底
            mBinding.container.layoutManager?.smoothScrollToPosition(
                mBinding.container, RecyclerView.State(), 0
            )
        }
    }


    /**
     * 列表适配器
     */
    inner class MyAdapter : BindRecyclerAdapter<ImChatEntity, ItemTitle01Binding>() {

        override fun getViewBinding(
            inflater: LayoutInflater, parent: ViewGroup
        ): ItemTitle01Binding {
            val binding = ItemTitle01Binding.inflate(inflater, parent, false)
            binding.itemValue.visibility = View.GONE
            return binding
        }

        override fun bindData(
            binding: ItemTitle01Binding, position: Int, data: ImChatEntity
        ) {
            val user = when (data.userId) {
                1L -> "我的发言"
                2L -> "对方回复"
                else -> "系统消息"
            }
            binding.itemTitle.text = StringBuffer(user).append(":\r\t").append(data.message)
            //添加颜色，易于识别
            binding.itemTitle.setTextColor(
                when (data.userId) {
                    1L -> Color.GREEN
                    2L -> Color.BLUE
                    else -> Color.GRAY
                }
            )
        }
    }
}