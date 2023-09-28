package com.melvinhou.`fun`.device

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.companion.CompanionDeviceManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresPermission
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.melvinhou.dimension2.databinding.FragmentBluetoothBinding
import com.melvinhou.kami.adapter.BindRecyclerAdapter
import com.melvinhou.kami.io.FcLog
import com.melvinhou.kami.mvvm.BaseViewModel
import com.melvinhou.kami.util.DimenUtils
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.kami.util.StringUtils
import com.melvinhou.knight.KindFragment
import com.melvinhou.knight.databinding.ItemTitle01Binding


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/9/14 0014 16:50
 * <p>
 * = 分 类 说 明：蓝牙
 * ================================================
 */
class BluetoothFragment : KindFragment<FragmentBluetoothBinding, BaseViewModel>() {
    override val _ModelClazz: Class<BaseViewModel>
        get() = BaseViewModel::class.java

    override fun openViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBluetoothBinding =
        FragmentBluetoothBinding.inflate(inflater, container, false)

    private val TAG = BluetoothFragment::class.java.name

    //蓝牙开启
    private val REQUEST_ENABLE_BT = 17

    //权限
    val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }

    private val deviceManager: CompanionDeviceManager? by lazy(LazyThreadSafetyMode.NONE) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().getSystemService(CompanionDeviceManager::class.java)
        } else {
            null
        }
    }


    // 获取默认适配器
    var bluetoothAdapter: BluetoothAdapter? = null
    private var adapter: MyAdapter? = null

    /**
     * Member object for the chat services
     */
    private var mBluetoothService: FcBluetoothService? = null
    private val handler = object : Handler() {
        override fun handleMessage(msg: Message) {

        }
    }


    override fun initView() {
        mBinding.barRoot.title.text = "蓝牙"
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
        val dp16 = DimenUtils.dp2px(16)
        mBinding.container.setPadding(dp16, 0, dp16, 0)
        val decoration = DimenUtils.dp2px(1)
        mBinding.container.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View,
                parent: RecyclerView, state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                outRect[0, if (position == 0) decoration else 0, 0] = decoration
            }
        })
        //点击事件
        adapter?.setOnItemClickListener { _, _, device ->
            if (!checkBluetooth(false)) return@setOnItemClickListener
            when (device.bondState) {
                BluetoothDevice.BOND_NONE -> {
                    device.createBond()
                }
                BluetoothDevice.BOND_BONDED -> {
                    connect(device)
                }
            }
        }
    }

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        //更新下边距
        val top = args?.getInt("top")
        val bottom = args?.getInt("bottom")
        mBinding.barRoot.barRoot.setPadding(0, top ?: 0, 0, 0)
        mBinding.root.setPadding(0, 0, 0, bottom ?: 0)
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    override fun initListener() {
        mBinding.open.setOnCheckedChangeListener { _, isChecked ->
            if (!checkBluetooth(true)) return@setOnCheckedChangeListener
            val isOpen = bluetoothAdapter?.isEnabled == true
            if (isChecked && !isOpen) {
                bluetoothAdapter?.enable()//启动蓝牙
                // 您的应用还可选择侦听 ACTION_STATE_CHANGED 广播 Intent，每当蓝牙状态发生变化时，系统都会广播此 Intent。
                // 此广播包含额外字段 EXTRA_STATE 和 EXTRA_PREVIOUS_STATE，二者分别包含新的和旧的蓝牙状态。
                // 这些额外字段可能为以下值：STATE_TURNING_ON、STATE_ON、STATE_TURNING_OFF 和 STATE_OFF。
                // 如果您的应用需检测对蓝牙状态所做的运行时更改，请侦听此广播。
//                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
            if (!isChecked && isOpen) {
                bluetoothAdapter?.disable()//关闭蓝牙
            }
        }
        mBinding.find.setOnCheckedChangeListener { _, isChecked ->
            if (!checkBluetooth(false)) return@setOnCheckedChangeListener
            //设置300秒可被发现
//        val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
//            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
//        }
//        startActivity(discoverableIntent)
        }
        mBinding.btScan.setOnClickListener {
            if (!checkBluetooth(false)) return@setOnClickListener
            scan()
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkBluetooth(true)) {
            mBinding.open.isChecked = bluetoothAdapter?.isEnabled == true
        }
        FcLog.e(TAG,"重新获取焦点")
    }

    override fun onPause() {
        super.onPause()
        // 使用后关闭代理连接.
        bluetoothAdapter?.closeProfileProxy(BluetoothProfile.HEADSET, bluetoothHeadset)
    }

    override fun onDestroy() {
        super.onDestroy()

        // 不要忘记取消注册ACTION_FOUND接收器.
        requireContext().unregisterReceiver(receiver)
        mBluetoothService?.stop()
    }

    override fun initData() {
        // Initialize the BluetoothChatService to perform bluetooth connections
        mBluetoothService = FcBluetoothService(requireContext(), handler)

    }

    /**
     * 判断蓝牙可用情况
     */
    private fun checkBluetooth(isOpen: Boolean): Boolean {
        // 请求权限
        if (!checkPermission(REQUIRED_PERMISSIONS)) {
            requestPermissions(REQUIRED_PERMISSIONS)
            return false
        }
        //蓝牙硬件判断
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            FcUtils.showToast("设备不支持蓝牙")
            return false
        }
        // 建立与代理的连接.
        bluetoothAdapter?.getProfileProxy(context, profileListener, BluetoothProfile.HEADSET)
        //如果是开关就此返回
        if (isOpen) return true
        //是否打开蓝牙
        if (bluetoothAdapter?.isEnabled == false) {
            FcUtils.showToast("请开启蓝牙")
            return false
        }
        return true
    }

    /**
     * 扫描蓝牙
     */
    @RequiresPermission(value = "android.permission.BLUETOOTH_SCAN")
    private fun scan() {
        //如果正在搜索，先取消搜索状态
        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter?.cancelDiscovery();
        }
        adapter?.clearData()
        adapter?.addDatas(bluetoothAdapter?.bondedDevices?.toMutableList())
        // 发现设备时注册广播.
        val filter = IntentFilter()
//        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(BluetoothDevice.ACTION_FOUND)//获得扫描结果
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)//状态变化
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)//开始扫描
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)//扫描结束
        requireContext().registerReceiver(receiver, filter)
        bluetoothAdapter?.startDiscovery()
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    private fun connect(device: BluetoothDevice) {
        val uuid = device.uuids[0].uuid
        mBluetoothService?.connect(device, true, uuid)//是否建立安全连接，安全连接会先建立配队关系
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {

            }
        }
        FcLog.w(TAG, "返回了requestCode=${resultCode}")
        when (requestCode) {
            REQUEST_ENABLE_BT -> when(resultCode) {
                Activity.RESULT_OK -> {
                }
            }
        }
    }


    /**
     * 列表适配器
     */
    inner class MyAdapter : BindRecyclerAdapter<BluetoothDevice, ItemTitle01Binding>() {

        override fun getViewBinding(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): ItemTitle01Binding {
            val binding = ItemTitle01Binding.inflate(inflater, parent, false)
            binding.itemValue.setTextColor(Color.GRAY)
            binding.itemTitle.textSize = 16f
            binding.itemValue.textSize = 14f
            return binding
        }


        @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
        override fun bindData(
            binding: ItemTitle01Binding,
            position: Int,
            device: BluetoothDevice
        ) {
            val type = when (device.type) {
                BluetoothDevice.DEVICE_TYPE_CLASSIC -> "经典蓝牙"
                BluetoothDevice.DEVICE_TYPE_LE -> "低功耗蓝牙"
                BluetoothDevice.DEVICE_TYPE_DUAL -> "双向蓝牙"
                else -> "未知类型"
            }
            val state = when (device.bondState) {
                BluetoothDevice.BOND_NONE -> "未配对"
                BluetoothDevice.BOND_BONDED -> "已配对"
                BluetoothDevice.BOND_BONDING -> "配对中"
                else -> "未知状态"
            }
            val clazz = when (device.bluetoothClass.majorDeviceClass) {
                BluetoothClass.Device.Major.AUDIO_VIDEO -> "影音"
                BluetoothClass.Device.Major.COMPUTER -> "电脑"
                BluetoothClass.Device.Major.HEALTH -> "健康"
                BluetoothClass.Device.Major.IMAGING -> "图像"
                BluetoothClass.Device.Major.MISC -> "麦克风"
                BluetoothClass.Device.Major.NETWORKING -> "网络"
                BluetoothClass.Device.Major.PERIPHERAL -> "周边设备"
                BluetoothClass.Device.Major.PHONE -> "手机"
                BluetoothClass.Device.Major.TOY -> "玩具"
                BluetoothClass.Device.Major.UNCATEGORIZED -> "未分类"
                BluetoothClass.Device.Major.WEARABLE -> "手表"
                else -> "未知类型"
            }
            binding.itemTitle.text = if (StringUtils.isEmpty(device.name)) "未知名称" else device.name
            binding.itemValue.text = "${device.address}\n${clazz}-${state}-${type}"

            val uus = device.uuids?.map {
                it.uuid?.toString()
            }
            FcLog.d(
                TAG, "${device.name}__${device.address}__" +
                        "${device.bluetoothClass.deviceClass}__${device.bluetoothClass.majorDeviceClass}" +
                        "__${device.bluetoothClass.describeContents()}__${uus}"
            )
        }
    }

    /**
     * 发现蓝牙设备的广播
     */
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            FcLog.w(TAG, "收到消息")
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let { adapter?.addData(it) }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val state2 = device?.bondState
                    FcLog.w(TAG, "配队状态变化${device?.name}_${state}_${state2}")
                    val newDevice = bluetoothAdapter?.getRemoteDevice(device?.address)
                    if (state == BluetoothDevice.BOND_BONDED)
                        newDevice?.let { connect(newDevice) }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    FcLog.w(TAG, "开始扫描")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    FcLog.w(TAG, "结束扫描")
                }
            }
        }
    }


    var bluetoothHeadset: BluetoothHeadset? = null


    private val profileListener = object : BluetoothProfile.ServiceListener {

        @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.HEADSET) {
                bluetoothHeadset = proxy as BluetoothHeadset
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HEADSET) {
                bluetoothHeadset = null
            }
        }
    }

}