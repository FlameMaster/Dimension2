package com.melvinhou.`fun`.device

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.*
import android.graphics.Rect
import android.os.Bundle
import android.os.IBinder
import android.text.InputType
import android.view.*
import androidx.annotation.RequiresPermission
import androidx.core.net.toFile
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.melvinhou.dimension2.R
import com.melvinhou.dimension2.databinding.FragmentBluetoothLeBinding
import com.melvinhou.dimension2.databinding.ItemBluetoothServiceBinding
import com.melvinhou.`fun`.device.ota.OTAHelper
import com.melvinhou.`fun`.device.ota.callback.IProgress
import com.melvinhou.kami.adapter.BindRecyclerAdapter
import com.melvinhou.kami.io.FcLog
import com.melvinhou.kami.io.IOUtils
import com.melvinhou.kami.mvvm.BaseViewModel
import com.melvinhou.kami.util.DimenUtils
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.knight.KUITools
import com.melvinhou.knight.KindFragment
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.util.*

private val TAG = BluetoothLeFragment::class.java.name

class BluetoothLeFragment : KindFragment<FragmentBluetoothLeBinding, BaseViewModel>() {
    override val _ModelClazz: Class<BaseViewModel>
        get() = BaseViewModel::class.java

    override fun openViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentBluetoothLeBinding =
        FragmentBluetoothLeBinding.inflate(inflater, container, false)

    //连接状态0未连接1连接2连接中
    private var connectStatus = MutableLiveData(0)
    //选中的
    private var checkedService = hashSetOf<UUID?>()
    private var checkedCharacteristic = MutableLiveData<BluetoothGattCharacteristic?>()

    private var adapter: MyAdapter? = null
    private var deviceAddress: String? = null

    //通过服务控制
    private var bluetoothService: FcBluetoothLeService? = null

    // 管理服务生命周期的代码.
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        @SuppressLint("MissingPermission")
        override fun onServiceConnected(
            componentName: ComponentName,
            service: IBinder
        ) {
            bluetoothService = (service as FcBluetoothLeService.LocalBinder).getService()
            bluetoothService?.let { bluetooth ->
                if (!bluetooth.initialize()) {
                    FcLog.e(TAG, "无法初始化蓝牙")
                    backward()
                }
                // 进行设备连接
                connect()
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bluetoothService = null
        }
    }
    // 蓝牙设备广播
    private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                FcBluetoothLeService.ACTION_GATT_CONNECTED -> {//连接成功
                    connectStatus.postValue(1)
//                    updateConnectionState(R.string.connected)
                    FcUtils.showToast("连接成功")
                    mBinding.tvLog.text = "连接成功"
                }
                FcBluetoothLeService.ACTION_GATT_DISCONNECTED -> {//未连接
                    connectStatus.postValue(0)
//                    updateConnectionState(R.string.disconnected)
                    FcUtils.showToast("连接失败")
                    putLog("连接失败")
                }
                FcBluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED -> {//可用特征
                    // 在用户界面上显示所有支持的服务和特征.
                    displayGattServices(bluetoothService?.getSupportedGattServices())
                }
                FcBluetoothLeService.ACTION_DATA_AVAILABLE -> {//数据监听
                    //数据更新
                    val type = intent.getIntExtra(FcBluetoothLeService.EXTRA_DATA_TYPE,-1)
                    when(type){
                        BluetoothGattCharacteristic.PROPERTY_READ,
                        BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE
                        ->{
                            val value = intent.getStringExtra(FcBluetoothLeService.EXTRA_DATA)
                            putLog("读取特征=>${value}")
                        }
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY ->{
                            val value = intent.getStringExtra(FcBluetoothLeService.EXTRA_DATA)
                            putLog("特征通知=>${value}")
                        }
                        else->{
                            val value = intent.getStringExtra(FcBluetoothLeService.EXTRA_DATA)
                            putLog("其它=>${value}")
                        }
                    }
                }
                else -> {
                    FcLog.e(TAG, "你的：${intent.extras.toString()}")
                }
            }
        }

        // 演示如何遍历支持的GATT服务/特性.
        // 在这个示例中，我们填充绑定到UI上的expandableelistview的数据结构.
        private fun displayGattServices(gattServices: List<BluetoothGattService?>?) {
            if (gattServices == null) return
            val mGattCharacteristics = arrayListOf<BluetoothServiceBean>()
            // 循环浏览可用的关贸总协定服务.
            gattServices.forEach{gattService ->
                // 通过可用特性进行循环.
                gattService?.characteristics?.forEachIndexed { position, gattCharacteristic ->
                    val bean = BluetoothServiceBean()
                    bean.chlid = gattCharacteristic
                    bean.parent = gattService
                    bean.isTop = position == 0
                    bean.parentName = BluetoothServiceBean.getGattServiceName(gattService.uuid.toString())
                    bean.chlidName = BluetoothServiceBean.getGattCharacteristicName(
                        gattService.uuid.toString(),gattService.uuid.toString())
                    mGattCharacteristics.add(bean)
                }
            }
            adapter?.clearData()
            checkedService.clear()
            checkedCharacteristic.postValue(null)
            adapter?.addDatas(mGattCharacteristics)
        }
    }

    override fun upBarMenuID(): Int = R.menu.bluetooth_le
    override fun initMenu(menu: Menu?) {
        menu?.forEach {itemMenu ->
            if (itemMenu.itemId==R.id.menu_connect){
                connectStatus.observe(this){state->
                    itemMenu.title = when(state){
                        1->"断开"
                        else->"连接"
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_connect -> {
                when(connectStatus.value){
                    0->{
                        connect()
                    }
                    1->{
                        deviceAddress?.let {addreess->
                            bluetoothService?.disConnect()
                        }
                    }
                }
                return true
            }
            R.id.menu_update -> {
                var oldTemp = 0
                var newTemp = 0
                openFile(arrayOf("/*")){uri->
                    FcLog.d("选中文件${uri}")
                    val file = uri.toFile()
                    Observable.create(ObservableOnSubscribe { emitter: ObservableEmitter<String?> ->
                        OTAHelper.getCurrentImageInfo()
                        OTAHelper.startOTAUpdate(0,file,object : IProgress {
                            override fun onEraseStart() {
                                emitter.onNext("erase start!" + 8563)
                            }

                            override fun onEraseFinish() {
                                emitter.onNext("erase finish!" + 8563)
                            }

                            override fun onProgramStart() {
                                oldTemp = 0
                                newTemp = 0
                                emitter.onNext("program start!" + 8563)
                            }

                            override fun onProgramProgress(current: Int, total: Int) {
                                newTemp = current
//                                progressBar.setProgress(current * 100 / total)
                            }

                            override fun onProgramFinish() {
                                emitter.onNext("program finish!" + 8563)
                            }

                            override fun onVerifyStart() {
                                oldTemp = 0
                                newTemp = 0
                                emitter.onNext("verify start!" + 8563)
                            }

                            override fun onVerifyProgress(current: Int, total: Int) {
                                newTemp = current
//                                progressBar.setProgress(current * 100 / total)
                            }

                            override fun onVerifyFinish() {
                                emitter.onNext("verify finish!" + 8563)
                            }

                            override fun onEnd() {
                                emitter.onNext("end!" + 8563)
                                emitter.onComplete()
                            }

                            override fun onCancel() {
                                emitter.onNext("cancel!" + 8563)
                                emitter.onError(Throwable("取消升级"))
                            }

                            override fun onError(message: String) {
                                FcLog.d("onError :$message")
                                emitter.onNext(message + 8563)
                                emitter.onError(Throwable(message))
                            }

                            override fun onInformation(message: String) {
                                emitter.onNext(message + 8563)
                            }
                        })
                    })
                        .compose(IOUtils.setThread())
                        .subscribe(object:Observer<String?>{
                            override fun onSubscribe(d: Disposable) {
                                //开始升级
//                                progressBar.setProgress(0)
                            }

                            override fun onError(e: Throwable) {
                                oldTemp = 0
                                newTemp = 0
                                FcLog.d(e.message)
                                FcUtils.showToast(e.message)
                            }

                            override fun onComplete() {
                                oldTemp = 0
                                newTemp = 0
                                onNext("update success!" + 8563)
                                FcLog.d("Complete!")
                            }

                            override fun onNext(t: String) {
                                FcLog.w(TAG,t)
                            }
                        })
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun initView() {
        requireActivity().intent.getStringExtra("address")?.let {
            deviceAddress = it
        }
        mBinding.barRoot.title.text = "蓝牙调试"
        mBinding.btRead.isVisible = false
        mBinding.btWrite.isVisible = false
        mBinding.cbNotify.isVisible = false
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
                val position = parent.getChildAdapterPosition(view)
                outRect[0, if (position == 0) decoration else 0, 0] = decoration
            }
        })
    }

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        //更新下边距
        val top = args?.getInt("top")
        val bottom = args?.getInt("bottom")
        mBinding.barRoot.barRoot.setPadding(0, top ?: 0, 0, 0)
        mBinding.root.setPadding(0, 0, 0, bottom ?: 0)
    }

    @SuppressLint("MissingPermission")
    override fun initListener() {
        //点击事件
        adapter?.setOnItemClickListener { _, _, data ->

        }
        //读取
        mBinding.btRead.setOnClickListener {
            checkedCharacteristic.value?.let {
                bluetoothService?.readCharacteristic(it)
            }
        }
        //写入
        mBinding.btWrite.setOnClickListener {
            KUITools.showInputDialog01(requireActivity(),"请输入","",InputType.TYPE_CLASS_NUMBER){txt->
                checkedCharacteristic.value?.let {
                        bluetoothService?.writeCharacteristic(it,txt?:"")
                }
            }
        }
        //监听
        mBinding.cbNotify.setOnCheckedChangeListener { _, ischecked ->
            checkedCharacteristic.value?.let {
                setCharacteristicNotification(it,ischecked)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireContext().registerReceiver(gattUpdateReceiver, IntentFilter().apply {
            addAction(FcBluetoothLeService.ACTION_GATT_CONNECTED)
            addAction(FcBluetoothLeService.ACTION_GATT_DISCONNECTED)
            addAction(FcBluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
            addAction(FcBluetoothLeService.ACTION_DATA_AVAILABLE)
        })
//        if (bluetoothService != null) {
//            val result = bluetoothService!!.connect(deviceAddress)
//            FcLog.d(TAG, "Connect request result=$result")
//        }
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(gattUpdateReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unbindService(serviceConnection)
    }

    override fun initData() {
        //连接状态
        connectStatus.observe(this){
            when(it){
                0->{
                    hideProcess()
                }
                1->{
                    hideProcess()
                }
                2->{
                    showProcess("连接中...")
                }
            }
        }
        //选中特征
        checkedCharacteristic.observe(this){
            it?.let {characteristic->
                mBinding.btRead.isVisible = when(characteristic.properties){
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE-> true
                    else -> false
                }
                mBinding.btWrite.isVisible = when(characteristic.properties){
                    BluetoothGattCharacteristic.PROPERTY_WRITE,
                    BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE-> true
                    else -> false
                }
                mBinding.cbNotify.isVisible = when(characteristic.properties){
                    BluetoothGattCharacteristic.PROPERTY_NOTIFY -> true
                    else -> false
                }
            }
        }
        val gattServiceIntent = Intent(requireContext(), FcBluetoothLeService::class.java)
        requireActivity().bindService(
            gattServiceIntent,
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    //连接
    @SuppressLint("MissingPermission")
    fun connect() {
        deviceAddress?.let {addreess->
            connectStatus.value = 2
            bluetoothService?.connect(addreess)
        }
    }

    /**
     * 添加log
     */
    fun putLog(txt:String){
        val buffer = StringBuffer(mBinding.tvLog.text)
        buffer.append("\n").append(txt)
        mBinding.tvLog.text = buffer
        mBinding.svLog.post {
            mBinding.svLog.fullScroll(View.FOCUS_DOWN)
        }
    }

    /**
     * 监听
     */
    @SuppressLint("MissingPermission")
    fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic,
        enabled: Boolean
    ){
        bluetoothService?.setCharacteristicNotification(characteristic,enabled)
    }


    /**
     * 列表适配器
     */
    inner class MyAdapter :
        BindRecyclerAdapter<BluetoothServiceBean, ItemBluetoothServiceBinding>() {

        override fun getViewBinding(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): ItemBluetoothServiceBinding {
            val binding = ItemBluetoothServiceBinding.inflate(inflater, parent, false)
            return binding
        }


        @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
        override fun bindData(
            binding: ItemBluetoothServiceBinding,
            position: Int,
            data: BluetoothServiceBean
        ) {
            binding.tvParent.text = data.parentName
            binding.tvChild.text = data.chlidName
            val isShowChild = checkedService.contains(data.parent?.uuid)
            binding.llParent.isVisible = data.isTop
            binding.ivOpen.isVisible = data.isTop
            binding.llChild.isVisible = isShowChild
            binding.ivCheck.isVisible = isShowChild
            data.parent?.let {
                binding.tvParentExplain.text = "UUID:${it.uuid}"
            }
            data.chlid?.let {
                val uuid = it.uuid.toString()
                val propertiesTxt = when (it.properties) {
                    BluetoothGattCharacteristic.PROPERTY_READ -> "可读"
                    BluetoothGattCharacteristic.PROPERTY_WRITE -> "可写"
                    BluetoothGattCharacteristic.PROPERTY_NOTIFY -> "可监听"
                    BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE  -> "可读写"
                    else -> it.properties.toString()
                }
                binding.tvChildExplain.text = "UUID:${uuid}\n特征：${propertiesTxt}"
            }
            binding.llParent.setOnClickListener {
                if (isShowChild){
                    checkedService.remove(data.parent?.uuid)
                }else{
                    checkedService.add(data.parent?.uuid)
                }
                notifyDataSetChanged()
            }
            binding.llChild.setOnClickListener {
                if (mBinding.cbNotify.isChecked){
                    checkedCharacteristic.value?.let {
                        setCharacteristicNotification(it,false)
                    }
                    mBinding.cbNotify.isChecked = false
                }
                checkedCharacteristic.postValue(data.chlid)
            }
            //判断
            checkedCharacteristic.observe(this@BluetoothLeFragment){
                val isChecked = it?.uuid == data.chlid?.uuid
                binding.ivCheck.isSelected = isChecked
            }
        }
    }
}