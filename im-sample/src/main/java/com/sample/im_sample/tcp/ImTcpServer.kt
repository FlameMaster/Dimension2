package com.sample.im_sample.tcp

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.melvinhou.kami.tool.ThreadManager
import com.melvinhou.kami.util.FcUtils
import java.io.*
import java.net.ServerSocket
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
 * = 分 类 说 明：tcp-chat-server
 * ================================================
 */
class ImTcpServer {

    private val TAG = ImTcpServer::class.java.name

    private var mServerSocket: ServerSocket? = null
    private var mSocket: Socket? = null
    private var mInStream: InputStream? = null
    private var mOutStream: OutputStream? = null
    val status = MutableLiveData(0)//状态

    fun start(port: Int, callback: (Int,String) -> Unit) {
        ThreadManager.getThreadPool().execute {
            try {
                mServerSocket = ServerSocket(port)
                status.postValue(2)
                callback(0, "等待连接中...")
                //等待客户端的连接，Accept会阻塞，直到建立连接
                mSocket = mServerSocket?.accept()
                status.postValue(1)
                callback(0, "客户端已连接")
                //输入和输出
                mOutStream = mSocket?.getOutputStream()
                mInStream = mSocket?.getInputStream()
                //消息循环
                val reader = DataInputStream(mInStream)
                while (mInStream != null) {
                    Log.w(TAG, "Server等待消息中...");
                    // 读取数据
                    val msg = reader.readUTF()
                    Log.w(TAG, "Server获取到信息:$msg");
                    callback(2,msg)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                callback(0,"启动失败，请稍后再试")
                stop()
            }
        }
    }


    fun stop() {
        mSocket?.apply {
            try {
                shutdownInput()
                shutdownOutput()
            }catch (e:IOException){
                e.printStackTrace()
            }
            close()
        }
        mServerSocket?.close()
        //置空
        mOutStream = null
        mInStream = null
        mSocket = null
        mServerSocket = null
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