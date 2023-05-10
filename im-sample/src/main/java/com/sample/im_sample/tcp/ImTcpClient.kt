package com.sample.im_sample.tcp

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.melvinhou.kami.tool.ThreadManager
import com.melvinhou.kami.util.FcUtils
import java.io.*
import java.net.Socket


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/5/10 0010 15:13
 * <p>
 * = 分 类 说 明：tcp-chat-client
 * ================================================
 */
class ImTcpClient {

    private val TAG = ImTcpClient::class.java.name

    private var mSocket: Socket? = null
    private var mInStream: InputStream? = null
    private var mOutStream: OutputStream? = null
    val status = MutableLiveData(0)//状态

    fun connent(host: String, port: Int, callback: (Int, String) -> Unit) {
        ThreadManager.getThreadPool().execute {
            try {
                callback(0, "正在连接中...")
                //连接服务端
                mSocket = Socket(host, port)
                status.postValue(1)
                callback(0, "服务器连接成功")
                //输入和输出
                mOutStream = mSocket?.getOutputStream()
                mInStream = mSocket?.getInputStream()
                //消息循环
                val reader = DataInputStream(mInStream)
                while (mInStream != null) {
                    Log.w(TAG, "Client等待消息中...");
                    // 读取数据
                    val msg = reader.readUTF()
                    Log.w(TAG, "Client获取到信息:$msg");
                    callback(2, msg)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                callback(0, "连接失败，请稍后再试")
                close()
            }
        }
    }


    fun close() {
        mSocket?.apply {
            try {
                shutdownInput()
                shutdownOutput()
            }catch (e:IOException){
                e.printStackTrace()
            }
            close()
        }
        //置空
        mOutStream = null
        mInStream = null
        mSocket = null
        status.postValue(0)
    }

    fun send(text: String) {
        if (mSocket?.isClosed == false)
            ThreadManager.getThreadPool().execute {
                try {
                    val writer = DataOutputStream(mOutStream)
                    writer.writeUTF(text)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
    }

}