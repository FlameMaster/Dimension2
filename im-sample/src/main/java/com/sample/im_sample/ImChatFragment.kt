package com.sample.im_sample

import android.annotation.SuppressLint
import android.graphics.Rect
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.melvinhou.accountlibrary.bean.User
import com.melvinhou.kami.adapter.BindRecyclerAdapter
import com.melvinhou.kami.io.IOUtils
import com.melvinhou.kami.tool.ThreadManager
import com.melvinhou.kami.tool.UITools
import com.melvinhou.kami.util.DimenUtils
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.kami.util.StringUtils
import com.melvinhou.knight.KindFragment
import com.sample.im_sample.bean.ImChatEntity
import com.sample.im_sample.databinding.FragmentImChatBinding
import com.sample.im_sample.databinding.ItemImChatMessageBinding
import com.sample.im_sample.model.ImViewModel
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import java.io.*
import java.net.Socket


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
        //布局变化
//        ViewCompat.setOnApplyWindowInsetsListener(requireActivity().window.decorView) { view, insets ->
//            val stableInsets =
//                insets.getInsets(
//                    WindowInsetsCompat.Type.systemBars() or//状态栏
//                            WindowInsetsCompat.Type.displayCutout() or//刘海屏
//                            WindowInsetsCompat.Type.ime()//软键盘
//                )
//            Log.e("TTTT", "高度：" + insets.systemWindowInsetTop + "\r\t高度：" + stableInsets.top)
//            FcUtils.showToast("高度：" + insets.systemWindowInsetBottom + "\r\t高度：" + stableInsets.bottom)
////            mBinding.root.setPadding(0,0,0,insets.systemWindowInsetBottom)
//            insets
//        }
    }

    /**
     * 初始化列表
     */
    private fun initList() {
        adapter = MyAdapter()
        mBinding.container.adapter = this.adapter
        mBinding.container.layoutManager = LinearLayoutManager(requireContext())
        //设定边距
        val decoration = DimenUtils.dp2px(1)
        mBinding.container.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View,
                parent: RecyclerView, state: RecyclerView.State
            ) {
                outRect[0, 0, 0] = decoration
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
        mModel.unbindChat()
    }

    override fun initData() {
        userId = arguments?.getLong(User.USER_ID)
        userId?.let { id ->
            mModel.bindChat(id)
            mModel.getUserInfo(id) {
                mBinding.barRoot.title.text =
                    if (TextUtils.isEmpty(it.nickName)) it.name else it.nickName
            }
        }

        //聊天记录
        mModel.getChat {
            adapter?.clearData()
            adapter?.addDatas(it)
        }


        val ip = arguments?.getString("ip") ?: ""
        val port = arguments?.getInt("port") ?: 0
        //连接socket服务器
        connect2ServerSocket("192.168.31.201", port)
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
        mModel.sendChat(text)

        Thread {
            try {
                val writer = DataOutputStream(mOutStream)
                writer.writeUTF(text)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }


    private var mSocket: Socket? = null
    private var mInStream: InputStream? = null
    private var mOutStream: OutputStream? = null

    @SuppressLint("CheckResult")
    private fun connect2ServerSocket(ip: String, port: Int) {
        Observable
            .create(ObservableOnSubscribe { emitter: ObservableEmitter<Socket> ->
                try {
                    val socket = Socket(ip, port)
                    emitter.onNext(socket)
                } catch (e: Exception) {
                    e.printStackTrace()
                    FcUtils.showToast("无法连接对方,请稍后再试")
                } finally {
                    emitter.onComplete()
                }
            } as ObservableOnSubscribe<Socket>)
            .compose(IOUtils.setThread())
            .subscribe { socket: Socket ->
                mSocket = socket
                if (socket != null) {
                    Log.w("IM服务器", "发送地址:${socket.localSocketAddress}/${socket.remoteSocketAddress}");
                    mOutStream = socket.getOutputStream()
                    mInStream = socket.getInputStream()
                    //启动轮询
                    startReader()
                } else {
                    mOutStream = null
                    mInStream = null
                }
            }
    }

    /**
     * 启动消息遍历器
     */
    private fun startReader() {
        ThreadManager.getThreadPool().execute {
            val reader: DataInputStream
            try {
                // 获取读取流
                reader = DataInputStream(mInStream)
                while (mInStream != null) {
                    Log.w("IM服务器", "2等待消息中...");
                    // 读取数据
                    val msg = reader.readUTF()
                    Log.w("IM服务器", "获取到客户端的信息2:" + msg);
                    //告知客户端消息收到
//                    if (mCurrentSocket != null) {
//                        DataOutputStream writer = new DataOutputStream(mOutputStream);
//                        writer.writeUTF(msg); // 写一个UTF-8的信息
//                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
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
        }
    }
}