package com.melvinhou.`fun`.device

import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.nfc.NfcAdapter.EXTRA_DATA
import android.os.Binder
import android.os.IBinder
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
 * = 分 类 说 明：
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


    private var connectionState = STATE_DISCONNECTED

    private val bluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {

        //连接状态改变时回调，连接成功后使用gatt.discoverService()发现连接设备的服务，当断开连接是应使用gatt.close（）释放连接
        @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
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
            } else if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, null)
            }
        }

        //获取设备的服务，如果服务获取失败，可认为连接是失败
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, null)
            } else {
                FcLog.w(TAG, "onServicesDiscovered 收到: $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //读取出characteristic的value值
                val value = String(characteristic.getValue()).trim().replace(" ", "");
                FcLog.i(TAG, "=====>读取到 value =" + value);
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
            }
        }

        //读操作回调
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //读取出characteristic的value值
                val value = String(characteristic.getValue()).trim().replace(" ", "");
                FcLog.i(TAG, "=====>读取到 value =" + value);
            }
        }

        //写操作回调
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
        }

        //通知数据更新时回调
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            //读取出characteristic的value值
            val value = String(characteristic.getValue()).trim().replace(" ", "");
            FcLog.i(TAG, "=====>读取到 value =" + value);
            //此处为ascii表字符，需转换为十进制ascii值
            //再将十进制ascii值，转换为十六进制
            val stringBuffer = StringBuffer("[")
            value.forEach {
                stringBuffer.append(changeAsciiTo16(it)).append(",")
            }
            FcLog.e(TAG, "参数=${stringBuffer.append("]")}")
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
        }

        private fun changeAsciiTo16(a: Char): String? {
//            FcLog.i(TAG, "change from a =$a")
            var value = ""
            val `val` = a.code
//            FcLog.i(TAG, "change to 10进制ASCII值 val =$`val`")
            //ascii值到
            value = Integer.toHexString(`val`).uppercase(Locale.getDefault())
//            FcLog.i(TAG, "change to 16进制字符串 value =$value")
            return value
        }

        //蓝牙信号强度改变回调
        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
        }

        //蓝牙发送、接收的长度改变回调
        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
        }

    }

    private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic?) {
        val intent = Intent(action)

        // 这是对心率测量配置文件的特殊处理. 根据概要文件规范执行数据解析.
        if (characteristic != null)
            when (characteristic.uuid) {
            else -> {
                val flag = characteristic.properties
                val format = when (flag and 0x01) {
                    0x01 -> {
                        FcLog.d(TAG, "心率格式UINT16.")
                        BluetoothGattCharacteristic.FORMAT_UINT16
                    }
                    else -> {
                        FcLog.d(TAG, "心率格式UINT8.")
                        BluetoothGattCharacteristic.FORMAT_UINT8
                    }
                }
                val t1 = characteristic.getIntValue(format, 1)
                val t2 = characteristic.getIntValue(format, 2)
                val t3 = characteristic.getIntValue(format, 5)
                val t4 = characteristic.getIntValue(format, 6)
                val p1 = characteristic.getIntValue(format, 7)
                val p2 = characteristic.getIntValue(format, 8)
                FcLog.w(TAG, "测量温度：${t1}.${t2},环境温度：${t3}.${t4},电量：${p1}/${p2}")
//                val heartRate = characteristic.getIntValue(format, 1)
//                FcLog.d(TAG, String.format("接收心率: %d", heartRate))
//                intent.putExtra(EXTRA_DATA, (heartRate).toString())
            }
//                else -> {
//                    // 对于所有其他配置文件，写入以HEX格式格式化的数据.
//                    val data: ByteArray? = characteristic.value
//                    if (data?.isNotEmpty() == true) {
//                        val hexString: String = data.joinToString(separator = " ") {
//                            String.format("%02X", it)
//                        }
//                        intent.putExtra(EXTRA_DATA, "$data\n$hexString")
//                    }
//                }
            }
        sendBroadcast(intent)
    }

    companion object {
        const val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
        const val ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"

        private const val STATE_DISCONNECTED = 0
        private const val STATE_CONNECTED = 2

        private val UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
//            UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
        private val CLIENT_CHARACTERISTIC_CONFIG =
            UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb")
//        UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
//            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    }

    fun getSupportedGattServices(): List<BluetoothGattService?>? {
        return bluetoothGatt?.services
    }

    fun getCharacteristic(): BluetoothGattCharacteristic? {
        val service = bluetoothGatt?.getService(UUID_HEART_RATE_MEASUREMENT)
        if (service==null){
            FcLog.e(TAG,"service is 空")
            return null
        }
        val characteristic = service.getCharacteristic(CLIENT_CHARACTERISTIC_CONFIG)
        if (characteristic==null){
            FcLog.e(TAG,"characteristic is 空")
            return null
        }
        return characteristic
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        bluetoothGatt?.let { gatt ->
            val isRed = gatt.readCharacteristic(characteristic)
            FcLog.d(TAG, "读取特征：${characteristic.uuid.toString()},isRed=${isRed}")
        } ?: run {
            FcLog.w(TAG, "BluetoothGatt未初始化1")
            return
        }
    }

    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic,
        enabled: Boolean
    ) {
        bluetoothGatt?.let { gatt ->
            gatt.setCharacteristicNotification(characteristic, enabled)
            characteristic.descriptors?.forEach {descriptor->
                val value =
                    if (enabled) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                descriptor.value = value
                gatt.writeDescriptor(descriptor)
            }

            // 这是针对心率测量的.
            if (UUID_HEART_RATE_MEASUREMENT == characteristic.uuid) {
//                val descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG))
                characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG)?.let { descriptor ->
                    val value =
                        if (enabled) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                    descriptor.value = value
                    gatt.writeDescriptor(descriptor)
                }
            }
        } ?: run {
            FcLog.e(TAG, "BluetoothGatt未初始化2")
        }
    }
}