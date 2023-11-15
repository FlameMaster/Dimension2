package com.melvinhou.`fun`.device

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.*
import android.graphics.Color
import android.graphics.Rect
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresPermission
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.melvinhou.dimension2.databinding.FragmentBluetoothScanBinding
import com.melvinhou.kami.adapter.BindRecyclerAdapter
import com.melvinhou.kami.io.FcLog
import com.melvinhou.kami.mvvm.BaseViewModel
import com.melvinhou.kami.util.DimenUtils
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.kami.util.StringUtils
import com.melvinhou.knight.FragmentContainActivity
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
 * = 分 类 说 明：低功耗蓝牙
 * ================================================
 */

private val TAG = BluetoothLeScanFragment::class.java.name
//权限
private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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
class BluetoothLeScanFragment : KindFragment<FragmentBluetoothScanBinding, BaseViewModel>() {
    override val _ModelClazz: Class<BaseViewModel>
        get() = BaseViewModel::class.java

    override fun openViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBluetoothScanBinding =
        FragmentBluetoothScanBinding.inflate(inflater, container, false)


    // 获取默认适配器
    var bluetoothAdapter: BluetoothAdapter? = null
    private var adapter: MyAdapter? = null

    // 10秒后停止扫描.
    private val SCAN_PERIOD: Long = 10000
    private var isScanning = false
    private val handler = Handler()
    // 设备扫描回调.
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            adapter?.addData(result.device)
        }
    }


    override fun initView() {
        mBinding.barRoot.title.text = "蓝牙扫描"
        initList()
    }

    /**
     * 初始化列表
     */
    @SuppressLint("MissingPermission")
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

            //如果正在搜索，先取消搜索状态
            if (isScanning) {
                val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
                isScanning = false
                bluetoothLeScanner?.stopScan(leScanCallback)
            }
            val bundle = bundleOf(
                "fragment" to BluetoothLeFragment::class.java,
                "address" to device.address
            )
            toActivity<FragmentContainActivity>(bundle)
        }
    }

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        //更新下边距
        val top = args?.getInt("top")
        val bottom = args?.getInt("bottom")
        mBinding?.barRoot!!.barRoot.setPadding(0, top ?: 0, 0, 0)
        mBinding?.root!!.setPadding(0, 0, 0, bottom ?: 0)
    }

    @SuppressLint("MissingPermission")
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
    @SuppressLint("MissingPermission")
    private fun scan() {
        adapter?.clearData()
        adapter?.addDatas(bluetoothAdapter?.bondedDevices?.toMutableList())
        val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
        if (!isScanning) { // 在设定的扫描周期后停止扫描.
            handler.postDelayed({
                isScanning = false
                bluetoothLeScanner?.stopScan(leScanCallback)
            }, SCAN_PERIOD)
            isScanning = true
            bluetoothLeScanner?.startScan(leScanCallback)
        } else {
            isScanning = false
            bluetoothLeScanner?.stopScan(leScanCallback)
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
}