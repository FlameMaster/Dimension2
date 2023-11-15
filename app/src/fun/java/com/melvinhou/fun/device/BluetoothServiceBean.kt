package com.melvinhou.`fun`.device

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/10/9 0009 13:33
 * <p>
 * = 分 类 说 明：蓝牙特征封装
 * ================================================
 */
class BluetoothServiceBean {
    var isTop = false
    var parent: BluetoothGattService?= null
    var chlid: BluetoothGattCharacteristic?= null
    var parentName: String?= null
    var chlidName: String?= null



    companion object{
        //服务名
        fun getGattServiceName(uuid: String): String {
            return when(uuid){
                "00001801-0000-1000-8000-00805f9b34fb"->"通用属性规范"
                "00001800-0000-1000-8000-00805f9b34fb"->"通用接入规范"
                else->"UNKNOWN_SERVICE"
            }
        }

        //特征名
        fun getGattCharacteristicName(uuid: String,parentUuid: String): String {
            return when(uuid){
                "00002a00-0000-1000-8000-00805f9b34fb"->"设备名称"
                "00002a01-0000-1000-8000-00805f9b34fb"->"设备外观"
                else->"UNKNOWN_CHARACTERISTIC"
            }
        }
    }
}