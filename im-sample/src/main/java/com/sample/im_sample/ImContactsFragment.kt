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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.melvinhou.userlibrary.bean.User
import com.melvinhou.kami.adapter.BindRecyclerAdapter
import com.melvinhou.kami.io.IOUtils
import com.melvinhou.kami.io.SharePrefUtil
import com.melvinhou.kami.tool.UITools
import com.melvinhou.kami.util.DimenUtils
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.knight.KindFragment
import com.sample.im_sample.bean.ImContactEntity
import com.sample.im_sample.databinding.FragmentImContactsBinding
import com.sample.im_sample.databinding.ItemImContactBinding
import com.sample.im_sample.model.ImViewModel
import com.sample.im_sample.udp.ImUdpServer
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


    private var adapter: MyAdapter? = null
    private val mServerPort = 17432//默认服务器端口
    private val mServer: ImUdpServer by lazy { ImUdpServer() }


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
        Thread{
            val ip = IOUtils.getIPAddress()
            Log.e("获取ip", "当前ip=$ip")
            mBinding.tvIpMine.post {
                mBinding.tvIpMine.text = "我的IP:${ip}"
            }
        }.start()
        //获取当前用户的id
        mModel.currentUserId = SharePrefUtil.getLong(User.USER_ID, -1)
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

        //开启服务
        mServer.start(mServerPort) { ip, msg ->
            ip?.let {
                mModel.receiveChat(it,msg)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        mModel.clearChatDbs()
        mServer.stop()
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
                Glide.with(FcUtils.getContext())
                    .load(it.photo)
                    .apply(
                        RequestOptions()
                            .override(binding.ivHeadpic.width, binding.ivHeadpic.height)
                            .placeholder(R.drawable.ic_head_sample02)
                            .error(R.drawable.ic_head_sample02)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                    )
                    .into(binding.ivHeadpic)
            }
        }
    }
}