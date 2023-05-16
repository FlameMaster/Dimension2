package com.sample.im_sample.udp

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.melvinhou.kami.tool.ThreadManager
import java.io.IOException
import java.io.InterruptedIOException
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
 * = 时 间：2023/5/11 0011 9:54
 * <p>
 * = 分 类 说 明：udp-chat-client
 * ================================================
 */
class ImUdpClient {
    private val TAG = ImUdpClient::class.java.name

    private var mSocket: DatagramSocket? = null
    private val responseBytes = ByteArray(1024 * 1024)
    val status = MutableLiveData(0)//状态

    fun connent(ip:String,port: Int,callback: ( String) -> Unit){
        ThreadManager.getThreadPool().execute{
            try {
                mSocket = DatagramSocket()
                //连接服务端，也可以不连，直接在packet中设置
                val address = InetAddress.getByName(ip)
                mSocket?.connect(address, port)
                status.postValue(1)
                callback( "客户端已启动")
                //附加:发送后响应
//                val responsePacket = DatagramPacket(responseBytes, responseBytes.size)
//                mSocket?.receive(responsePacket)
//                do {
//                    Log.w(TAG, "Client等待消息中...");
//                    mSocket?.receive(responsePacket)
//                    val serverIp = responsePacket.address.hostAddress
//                    val responseMsg = String(responsePacket.data, 0, responsePacket.length)
//                    Log.w(TAG, "Client获取到来自($serverIp)的信息:$responseMsg");
//                    callback(serverIp, responseMsg)
//                } while (mSocket?.isClosed == false)
            }catch (e:IOException){
                e.printStackTrace()
                callback("启动失败，请稍后再试")
                close()
            }
        }
    }

    fun close(){
        mSocket?.close()
        mSocket = null;
        status.postValue(0)
    }

    fun send(text:String){
        ThreadManager.getThreadPool().execute{
            try {
                val bytes = text.toByteArray()
                val packet = DatagramPacket(bytes, bytes.size)
//                packet.setData(bytes,0, bytes.size)
                mSocket?.send(packet)
            }catch (e:IOException){
                e.printStackTrace()
            }
        }
    }
}