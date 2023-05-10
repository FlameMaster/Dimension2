package com.sample.im_sample

import android.graphics.Rect
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.melvinhou.accountlibrary.bean.User
import com.melvinhou.kami.adapter.BindRecyclerAdapter
import com.melvinhou.kami.io.IOUtils
import com.melvinhou.kami.io.SharePrefUtil
import com.melvinhou.kami.tool.ThreadManager
import com.melvinhou.kami.tool.UITools
import com.melvinhou.kami.util.DimenUtils
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.knight.KindFragment
import com.melvinhou.knight.loadImage
import com.sample.im_sample.bean.ImContactEntity
import com.sample.im_sample.databinding.FragmentImContactsBinding
import com.sample.im_sample.databinding.ItemImContactBinding
import com.sample.im_sample.model.ImViewModel
import java.io.DataInputStream
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.util.*


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
 * = 分 类 说 明：即时通讯-联系人列表
 * ================================================
 */
class ImContactsFragment : KindFragment<FragmentImContactsBinding, ImViewModel>() {
    override fun openViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentImContactsBinding =
        FragmentImContactsBinding.inflate(inflater, container, false)

    override val _ModelClazz: Class<ImViewModel>
        get() = ImViewModel::class.java


    private var mServerSocket: ServerSocket? = null
    private var mSocket: Socket? = null
    private var adapter: MyAdapter? = null


    override fun upBarMenuID(): Int = R.menu.bar_im_contacts
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_contact_add -> {
                showAddDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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
            val bundle = bundleOf(User.USER_ID to data.userId, "ip" to data.ip, "port" to data.port)
            mModel.toFragment(R.id.action_im_contacts2chat, bundle)
        }
    }

    override fun initListener() {
        mBinding.quickBar.setOnTouchLetterListener(object : QuickIndexBar.OnTouchLetterListener {
            override fun onTouchLetter(letter: String?) {
                if (mBinding.tvCurrentWord.visibility != View.VISIBLE)
                    mBinding.tvCurrentWord.visibility = View.VISIBLE
                mBinding.tvCurrentWord.text = letter
            }

            override fun onDispose() {
                if (mBinding.tvCurrentWord.visibility != View.GONE)
                    mBinding.tvCurrentWord.visibility = View.GONE
            }

        })
    }


    override fun initData() {
        //获取当前用户的userid
        var userid = SharePrefUtil.getLong(User.USER_ID, -1)
        if (userid < 0) {
            userid = mModel.createUserId()
            Thread {
                val ip = IOUtils.getIPAddress()
                mModel.addContact(userid, "用户$userid", ip, mPort.toString())
                SharePrefUtil.saveLong(User.USER_ID, userid)
                Log.e("获取ip", "当前ip=$ip")
                FcUtils.runOnUiThread {
                    initData()
                }
            }.start()
            return
        }
        //获取当前用户的id
        mModel.currentUserId = userid
        //加载列表数据
        mModel.getAllContacts().observe(this) {
            adapter?.clearData()
            //排序
            Collections.sort(it, object : Comparator<ImContactEntity> {
                override fun compare(p0: ImContactEntity, p1: ImContactEntity): Int {
                    return p0.initial.compareTo(p1.initial)
                }
            })
            var current: Char? = null
            val contacts = it.map { entity ->
                if (entity.initial != current)
                    current = entity.initial
                else
                    entity.initial = Char(0)

                //不返回自己
                if (entity.userId == mModel.currentUserId)
                    null
                else
                    entity
            }.filterNotNull()
            adapter?.addDatas(contacts)
        }


        initSocketServer()
    }


    /**
     * 添加联系人弹窗
     */
    private fun showAddDialog() {
        val dialog = UITools.createDialog(
            activity,
            R.layout.dialog_im_contact_add,
            Gravity.BOTTOM,
            R.style.Animation_Dialog_Bottom
        )
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        val btCancel = dialog.window?.findViewById<View>(R.id.iv_cancel)
        val btSubmit = dialog.window?.findViewById<View>(R.id.bt_submit)
        val etName = dialog.window?.findViewById<EditText>(R.id.et_name)
        val etIp = dialog.window?.findViewById<EditText>(R.id.et_ip)
        val etPort = dialog.window?.findViewById<EditText>(R.id.et_port)
        btSubmit?.setOnClickListener {
            mModel.addContact(
                null,
                etName?.text.toString(),
                etIp?.text.toString(),
                etPort?.text.toString()
            )
            UITools.hideSoftKeyboard(dialog)
            dialog.dismiss()
        }
        btCancel?.setOnClickListener {
            UITools.hideSoftKeyboard(dialog)
            dialog.dismiss()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            mServerSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        //TODO 这是服务器断开连接重新初始化，本来需要一个空闲线程轮询的
        //启动服务线程
        ThreadManager.getThreadPool().execute(mAwaitSocketAcceptRunnadle)
    }

    /**
     * 等待被连接的Runnable
     */
    private val mAwaitSocketAcceptRunnadle = Runnable {
        try {
            val ip = IOUtils.getIPAddress()
            Log.e("获取ip", "当前ip=$ip")
            //等待客户端的连接，Accept会阻塞，直到建立连接
            mSocket = mServerSocket?.accept()
            Log.w("IM服务器", "已经连接成功:${mSocket?.localSocketAddress}/${mSocket?.remoteSocketAddress}");
        } catch (e: IOException) {
            e.printStackTrace()
            return@Runnable
        }
        //启动消息接收线程
        if (mSocket != null)
            startReader()
    }
    private val mPort = 17432

    /**
     * 初始化socket服务器
     */
    private fun initSocketServer() {
        try {
            mServerSocket?.close()
            mServerSocket = ServerSocket(mPort)
        } catch (e: IOException) {
            e.printStackTrace()
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
                reader = DataInputStream(mSocket!!.getInputStream())
                while (mSocket != null) {
                    Log.w("IM服务器", "等待消息中...")
                    // 读取数据
                    val msg = reader.readUTF()
                    Log.w("IM服务器", "获取到客户端的信息1:$msg")
                    //告知客户端消息收到
//                    if (mCurrentSocket != null) {
//                        DataOutputStream writer = new DataOutputStream(mCurrentSocket.getOutputStream());
//                        writer.writeUTF(msg); // 写一个UTF-8的信息
//                    }
                    val ip = mSocket!!.inetAddress.hostAddress
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    /**
     * 列表适配器
     */
    inner class MyAdapter : BindRecyclerAdapter<ImContactEntity, ItemImContactBinding>() {

        override fun getViewBinding(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): ItemImContactBinding {
            val binding = ItemImContactBinding.inflate(inflater, parent, false)
            return binding
        }

        override fun bindData(
            binding: ItemImContactBinding,
            position: Int,
            data: ImContactEntity
        ) {
            binding.tvExplain.text = "IP:\r\t${data.ip}\r\tProt:\r\t${data.port}"
            binding.tvTitle.text = data.initial.toString()
            binding.tvTitle.isVisible = data.initial.code > 0
            mModel.getUserInfo(data.userId) {
                binding.tvNikename.text =
                    if (TextUtils.isEmpty(it.nickName)) it.name else it.nickName
                binding.ivHeadpic.loadImage(it.photo)
            }
        }
    }
}