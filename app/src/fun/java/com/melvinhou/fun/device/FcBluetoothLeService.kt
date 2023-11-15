package com.melvinhou.`fun`.device

import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.SystemClock
import androidx.annotation.RequiresPermission
import com.melvinhou.kami.io.FcLog
import java.util.*


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/9/19 0019 11:41
 * <p>
 * = 分 类 说 明：低功耗蓝牙服务
 * ================================================
 */
private const val TAG = "FcBluetoothLeService"

class FcBluetoothLeService : Service() {


    private val binder = LocalBinder()
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    override fun onUnbind(intent: Intent?): Boolean {
        close()
        return super.onUnbind(intent)
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    private fun close() {
        bluetoothGatt?.let { gatt ->
            gatt.close()
            bluetoothGatt = null
        }
    }

    inner class LocalBinder : Binder() {
        fun getService(): FcBluetoothLeService {
            return this@FcBluetoothLeService
        }
    }

    fun initialize(): Boolean {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            FcLog.e(TAG, "无法获得蓝牙适配器.")
            return false
        }
        return true
    }


    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    fun connect(address: String): Boolean {
        bluetoothAdapter?.let { adapter ->
            try {
                val device = adapter.getRemoteDevice(address)
                // 连接设备上的GATT服务器
                bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback)
                return true
            } catch (exception: IllegalArgumentException) {
                FcLog.w(TAG, "没有找到具有提供地址的设备.  无法连接.")
                return false
            }
        } ?: run {
            FcLog.w(TAG, "BluetoothAdapter未初始化")
            return false
        }
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    fun disConnect() {
        bluetoothGatt?.disconnect()
//        bluetoothGatt?.close()
//        bluetoothAdapter?.disable()
    }


    private var connectionState = STATE_DISCONNECTED

    companion object {
        const val ACTION_GATT_CONNECTED = "com.melvinhou.bluetooth.le.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED =
            "com.melvinhou.bluetooth.le.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED =
            "com.melvinhou.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
        const val ACTION_DATA_AVAILABLE =
            "com.melvinhou.bluetooth.le.ACTION_DATA_AVAILABLE"

        const val EXTRA_DATA_TYPE = "com.melvinhou.bluetooth.le.EXTRA_DATA_TYPE"
        const val EXTRA_DATA = "com.melvinhou.bluetooth.le.EXTRA_DATA"

        private const val STATE_DISCONNECTED = 0
        private const val STATE_CONNECTED = 2

        //        private val UUID_HEART_RATE_MEASUREMENT =
//                    UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
        private val UUID_BLOOD_GLUCOSE_MEASUREMENT =
            UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
        private val UUID_BLOOD_GLUCOSE_CHARACTERISTIC =
            UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb")
        private val UUID_WRITE_BLOOD_GLUCOSE_CHARACTERISTIC =
            UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
        private val CLIENT_CHARACTERISTIC_CONFIG =
            UUID.fromString("00002901-0000-1000-8000-00805f9b34fb")

    }

    /**
     * 蓝牙回调
     */
    private val bluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {

        //连接状态改变时回调，连接成功后使用gatt.discoverService()发现连接设备的服务，当断开连接是应使用gatt.close（）释放连接
        @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            FcLog.d(TAG, "蓝牙连接状态变化status=${status}newState=${newState}")
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // 成功连接到GATT服务器
                broadcastUpdate(ACTION_GATT_CONNECTED, null)
                connectionState = STATE_CONNECTED
                // 连接成功后尝试发现服务.开始扫描目标设备的GattService
                bluetoothGatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // 与GATT服务器断开连接
                broadcastUpdate(ACTION_GATT_DISCONNECTED, null)
                connectionState = STATE_DISCONNECTED
                gatt?.close()
            } else if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, null)
            }
        }

        //获取设备的服务，如果服务获取失败，可认为连接是失败
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, null)
            } else {
                FcLog.d(TAG, "onServicesDiscovered 收到: $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
            }
            //同步回调
            if (characteristic.value == null || characteristic.value.size == 0) {
                mRcvLength = 0
                rwBusy = false
                return
            }
            mRcvLength = characteristic.value.size
            System.arraycopy( characteristic.value, 0, mRcvBuffer, 0, characteristic.value.size )
            rwBusy = false
        }

        //写操作回调
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            rwBusy = false
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
        }

        //通知数据更新时回调
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
        }

        //蓝牙信号强度改变回调
        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
        }

        //蓝牙发送、接收的长度改变回调
        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
            max_packet = mtu - mtuOffset
            max_packet = getSpecificMaxPacketForOTAAlign(mtu)
            mRcvBuffer = ByteArray(max_packet)
        }

    }

    private val connectTimeout: Long = 1000 * 15
    private var readTimeout: Long = 1000 * 10
    private var writeTimeout: Long = 1000 * 10

    private var max_packet = 20
    private val mtuOffset = 3
    private var rwBusy = false//写入和读取成功回调
    private var mRcvBuffer = ByteArray(20);//读取的数据
    private var mRcvLength = 0//读取的长度


    //OTA字节对齐问题
    fun getSpecificMaxPacketForOTAAlign(mtu: Int): Int {
        val frameMaxLen: Int = mtu - mtuOffset
        val dataMaxLen = frameMaxLen - 4
        return dataMaxLen / 16 * 16 + 4
    }

    /**
     * 16进制字符串转byte
     */
    fun hexTxt2Int(str: String): ByteArray {
        val byteTarget = ByteArray(str.length / 2)
        for (i in 0 until str.length / 2) byteTarget[i] =
            (str.substring(i * 2, i * 2 + 2).toInt(16) and 0xff).toByte()
        return byteTarget
    }

    private fun changeAsciiTo16(a: Char): String {
//            FcLog.d(TAG, "change from a =$a")
        var value = ""
        val code = a.code
        FcLog.d(TAG, "change to 10进制ASCII值 val =$code")
        //ascii值到
        value = Integer.toHexString(code).uppercase(Locale.getDefault())
        FcLog.d(TAG, "change to 16进制字符串 value =$value")
        return value
    }

    /**
     * 更新广播
     */
    private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic?) {
        val intent = Intent(action)
        // 这是对心率测量配置文件的特殊处理. 根据概要文件规范执行数据解析.
        if (characteristic != null) {
            when (characteristic.properties) {
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE
                -> {
                    //读取出characteristic的value值
                    val value = String(characteristic.getValue()).trim();
                    val hexTxt: String = characteristic.value.joinToString(separator = " ") {
                        String.format("%02X", it)
                    }
                    FcLog.d(TAG, "=====>读取到 value = ${value} hex = ${hexTxt}");
                    intent.putExtra(EXTRA_DATA, "value=${value}\nhex=${hexTxt}")
                }
                BluetoothGattCharacteristic.PROPERTY_WRITE
                -> {
                    //读取出characteristic的value值
                    val value = String(characteristic.getValue()).trim();
                    val hexTxt: String = characteristic.value.joinToString(separator = " ") {
                        String.format("%02X", it)
                    }
                    FcLog.d(TAG, "=====>写入到 value = ${value} hex = ${hexTxt}");
                    intent.putExtra(EXTRA_DATA, "value=${value}\nhex=${hexTxt}")
                }
                BluetoothGattCharacteristic.PROPERTY_NOTIFY -> {
                    //读取出characteristic的value值
                    val value = String(characteristic.getValue()).trim().replace(" ", "");
                    //此处为ascii表字符，需转换为十进制ascii值
                    //再将十进制ascii值，转换为十六进制
                    val stringBuffer = StringBuffer("[")
                    value.forEach {
                        stringBuffer.append(changeAsciiTo16(it)).append(",")
                    }
                    stringBuffer.append("]")
                    val hexTxt: String = characteristic.value.joinToString(separator = " ") {
                        String.format("%02X", it)
                    }
                    stringBuffer.append("\n").append("hex=${hexTxt}")
                    FcLog.d(TAG, "=====>收到 value = ${value} hex = ${hexTxt}");
                    FcLog.d(TAG, "设备参数=${stringBuffer}")
                    intent.putExtra(EXTRA_DATA, stringBuffer.toString())
                }
                else -> {
                    // 对于所有其他配置文件，写入以HEX格式格式化的数据.
                    val data: ByteArray? = characteristic.value
                    if (data?.isNotEmpty() == true) {
                        val hexString: String = data.joinToString(separator = " ") {
                            String.format("%02X", it)
                        }
                        intent.putExtra(EXTRA_DATA, "${data.toString()}\n$hexString")
                    }

                }
            }
            /*
            when (characteristic.uuid) {
                UUID_BLOOD_GLUCOSE_CHARACTERISTIC -> {
                    val flag = characteristic.properties
                    val format = when (flag and 0x01) {
                        0x01 -> {
                            FcLog.d(TAG,"心率格式UINT16.")
                            BluetoothGattCharacteristic.FORMAT_UINT16
                        }
                        else -> {
                            FcLog.d(TAG,"数据格式UINT8.")
                            BluetoothGattCharacteristic.FORMAT_UINT8
                        }
                    }
                    val t1 = characteristic.getIntValue(format, 1)
                    val t2 = characteristic.getIntValue(format, 2)
                    val t3 = characteristic.getIntValue(format, 5)
                    val t4 = characteristic.getIntValue(format, 6)
                    val p1 = characteristic.getIntValue(format, 7)
                    val p2 = characteristic.getIntValue(format, 8)
                    val v1 = characteristic.getIntValue(format, 9)//固件版本号
//                    FcLog.d(TAG,"测量温度：${t1}.${t2},环境温度：${t3}.${t4},电量：${p1}/${p2}")
                    intent.putExtra(EXTRA_DATA, arrayOf(t1, t2, t3, t4, p1, p2,v1))
//                val heartRate = characteristic.getIntValue(format, 1)
//                FcLog.d(TAG, String.format("接收心率: %d", heartRate))
//                intent.putExtra(EXTRA_DATA, (heartRate).toString())
                }
                else -> {
                    // 对于所有其他配置文件，写入以HEX格式格式化的数据.
                    val data: ByteArray? = characteristic.value
                    if (data?.isNotEmpty() == true) {
                        val hexString: String = data.joinToString(separator = " ") {
                            String.format("%02X", it)
                        }
                        intent.putExtra(EXTRA_DATA, "$data\n$hexString")
                    }
                }
            }
            */
            intent.putExtra(EXTRA_DATA_TYPE, characteristic.properties)
        }
        sendBroadcast(intent)
    }

    /**
     * 当前连接设备的服务列表
     */
    fun getSupportedGattServices(): List<BluetoothGattService?>? {
        return bluetoothGatt?.services
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        bluetoothGatt?.let { gatt ->
            gatt.readCharacteristic(characteristic)
            FcLog.e(TAG, "读取特征：${characteristic.uuid.toString()}")
        } ?: run {
            FcLog.e(TAG, "BluetoothGatt未初始化1")
            return
        }
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    fun readCharacteristicSync(characteristic: BluetoothGattCharacteristic): ByteArray? {
        bluetoothGatt?.let { gatt ->
            rwBusy = true
            val isOk = gatt.readCharacteristic(characteristic)
            FcLog.e(TAG, "读取特征：${characteristic.uuid.toString()}")
            if (isOk == true)
                for (i in 0..20) {
                    SystemClock.sleep(500)
                    if (!rwBusy) {
                        val p1 = ByteArray(mRcvLength)
                        System.arraycopy(mRcvBuffer, 0, p1, 0, mRcvLength)
                        return p1
                    }
                }
            return null
        } ?: run {
            FcLog.e(TAG, "BluetoothGatt未初始化1")
            return null
        }
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, input: String) {
        bluetoothGatt?.let { gatt ->
            characteristic.setValue(hexTxt2Int(input))
            bluetoothGatt?.writeCharacteristic(characteristic)
            FcLog.e(TAG, "${characteristic.uuid.toString()},写入特征：${input}")
        } ?: run {
            FcLog.e(TAG, "BluetoothGatt未初始化2")
            return
        }
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    fun writeCharacteristicSync(
        characteristic: BluetoothGattCharacteristic,
        data: ByteArray
    ): Boolean {
        rwBusy = true
        bluetoothGatt?.let { gatt ->
            characteristic.setValue(data)
            val isOk = bluetoothGatt?.writeCharacteristic(characteristic)
            if (isOk == true)
                for (i in 0..20) {
                    SystemClock.sleep(500)
                    if (!rwBusy) {
                        return true
                    }
                }
            return isOk == true
        } ?: run {
            FcLog.e(TAG, "BluetoothGatt未初始化2")
            return false
        }
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic,
        enabled: Boolean
    ) {
        bluetoothGatt?.let { gatt ->
            gatt.setCharacteristicNotification(characteristic, enabled)
            characteristic.descriptors?.forEach { descriptor ->
                val value =
                    if (enabled) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                descriptor.value = value
                gatt.writeDescriptor(descriptor)
            }

            // 这是针对心率测量的.
//            if (UUID_HEART_RATE_MEASUREMENT == characteristic.uuid) {
////                val descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG))
//                characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG)?.let { descriptor ->
//                    val value =
//                        if (enabled) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
//                    descriptor.value = value
//                    gatt.writeDescriptor(descriptor)
//                }
//            }
        } ?: run {
            FcLog.e(TAG, "BluetoothGatt未初始化2")
        }
    }


    fun getMax_packet(): Int {
        return max_packet
    }

    fun getMtu(): Int {
        return max_packet + mtuOffset
    }
}