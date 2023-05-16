package com.sample.im_sample.udp

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.melvinhou.kami.tool.ThreadManager
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/5/11 0011 9:33
 * <p>
 * = 分 类 说 明：udp-chat-server
 * ================================================
 */
class ImUdpServer {

    private val TAG = ImUdpServer::class.java.name

    private var mSocket: DatagramSocket? = null
    private val bytes = ByteArray(1024 * 1024)
    val status = MutableLiveData(0)//状态

    fun start(port: Int, callback: (String?, String) -> Unit) {
        if (mSocket != null) return
        ThreadManager.getThreadPool().execute {
            try {
                val packet = DatagramPacket(bytes, bytes.size)
                //实例并监听端口
                mSocket = DatagramSocket(port)
                callback(null, "服务端已启动")
                status.postValue(1)
                do {
                    Log.w(TAG, "Server等待消息中...");
                    mSocket?.receive(packet)
                    val ip = packet.address.hostAddress
                    val msg = String(packet.data, 0, packet.length)
                    Log.w(TAG, "Server获取到来自($ip)的信息:$msg");
                    callback(ip, msg)
                } while (mSocket?.isClosed == false)
            } catch (e: IOException) {
                e.printStackTrace()
                callback(null, "启动失败，请稍后再试")
                stop()
            }
        }
    }

    fun stop() {
        mSocket?.close()
        mSocket = null;
        status.postValue(0)
    }

    fun send(address: InetAddress, port: Int, text: String) {
        ThreadManager.getThreadPool().execute {
            try {
                val bytes = text.toByteArray()
                val packet = DatagramPacket(bytes, bytes.size, address, port)
                mSocket?.send(packet)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}