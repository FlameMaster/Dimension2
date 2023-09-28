package com.melvinhou.`fun`.device

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.annotation.RequiresPermission
import com.melvinhou.kami.io.FcLog
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/9/15 0015 13:53
 * <p>
 * = 分 类 说 明：
 * ================================================
 */

private const val TAG = "FcBluetoothService"

// 定义在服务和UI之间传输消息时使用的几个常量.
// Message types sent from the BluetoothChatService Handler
const val MESSAGE_STATE_CHANGE = 1
const val MESSAGE_READ = 2
const val MESSAGE_WRITE = 3
const val MESSAGE_DEVICE_NAME = 4
const val MESSAGE_TOAST = 5
const val DEVICE_NAME = "拜拜风尘子的蓝牙"
const val TOAST = "toast"
const val DEFAULT_UUID = "00001101-0000-1000-8000-00805F9B34FB"//串口模块连接

// ... (根据需要在这里添加其他消息类型.)
class FcBluetoothService(
    private val context: Context,
    // 从蓝牙服务获取信息的处理器
    private val handler: Handler
) {

    // Constants that indicate the current connection state
    val STATE_NONE = 0 // we're doing nothing
    val STATE_LISTEN = 1 // now listening for incoming connections
    val STATE_CONNECTING = 2 // now initiating an outgoing connection
    val STATE_CONNECTED = 3 // now connected to a remote device


    // Name for the SDP record when creating server socket
    private val NAME_SECURE = "BluetoothChatSecure"
    private val NAME_INSECURE = "BluetoothChatInsecure"

    // Unique UUID for this application
    //00007212-0000-1000-8000-00805f9b34fb
    private var MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var mAdapter: BluetoothAdapter? = null
    private var mSecureAcceptThread: AcceptThread? = null
    private var mInsecureAcceptThread: AcceptThread? = null
    private var mConnectThread: ConnectThread? = null
    private var mConnectedThread: ConnectedThread? = null
    private var mState: Int? = null
    private var mNewState: Int? = null


    init {
        mAdapter = BluetoothAdapter.getDefaultAdapter()
        mState = STATE_NONE
    }


    /**
     * Update UI title according to the current state of the chat connection
     */
    @Synchronized
    private fun updateUserInterfaceTitle() {
        mState = getState()
        FcLog.w(
            TAG,
            "updateUserInterfaceTitle() $mNewState -> $mState"
        )
        mNewState = mState

        // Give the new state to the Handler so the UI Activity can update
        handler.obtainMessage(MESSAGE_STATE_CHANGE, mNewState ?: -1, -1).sendToTarget()
    }

    /**
     * Return the current connection state.
     */
    @Synchronized
    fun getState(): Int {
        return mState!!
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    @Synchronized
    fun start() {
        FcLog.w(TAG, "start")

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }

        // Cancel any thread currently running a connection
        mConnectedThread?.cancel()
        mConnectedThread = null

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = AcceptThread(true)
            mSecureAcceptThread!!.start()
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = AcceptThread(false)
            mInsecureAcceptThread!!.start()
        }
        // Update UI title
        updateUserInterfaceTitle()
    }

    /**
     * Stop all threads
     */
    @Synchronized
    fun stop() {
        FcLog.w(TAG, "stop")
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread!!.cancel()
            mSecureAcceptThread = null
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread!!.cancel()
            mInsecureAcceptThread = null
        }
        mState = STATE_NONE
        // Update UI title
        updateUserInterfaceTitle()
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread.write
     */
    fun write(out: ByteArray?) {
        // Create temporary object
        var r: ConnectedThread
        // Synchronize a copy of the ConnectedThread
        synchronized(this) {
            if (mState != STATE_CONNECTED) return
            r = mConnectedThread!!
        }
        // Perform the write unsynchronized
        r.write(out)
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    @Synchronized
    fun connect(device: BluetoothDevice, secure: Boolean, uuid: UUID?) {
        FcLog.w(TAG, "connect to: $device")
//        ParcelUuid(UUID(0x123abcL, -1L)
        if (uuid!=null){
            MY_UUID_SECURE = uuid
            MY_UUID_INSECURE = uuid
        }
        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            mConnectThread?.cancel()
            mConnectThread = null
        }

        // Cancel any thread currently running a connection
        mConnectedThread?.cancel()
        mConnectedThread = null

        // Start the thread to connect with the given device
        mConnectThread = ConnectThread(device, secure)
        mConnectThread!!.start()
        // Update UI title
        updateUserInterfaceTitle()
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    @Synchronized
    @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
    fun connected(socket: BluetoothSocket?, device: BluetoothDevice, socketType: String) {
        FcLog.w(TAG, "connected, Socket Type:$socketType")

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }

        // Cancel any thread currently running a connection
        mConnectedThread?.cancel()
        mConnectedThread = null

        // Cancel the accept thread because we only want to connect to one device
        mSecureAcceptThread?.cancel()
        mSecureAcceptThread = null
        mInsecureAcceptThread?.cancel()
        mInsecureAcceptThread = null

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = ConnectedThread(socket!!, socketType)
        mConnectedThread!!.start()

        // Send the name of the connected device back to the UI Activity
        val msg: Message = handler.obtainMessage(MESSAGE_DEVICE_NAME)
        val bundle = Bundle()
        bundle.putString(DEVICE_NAME, device.name)
        msg.data = bundle
        handler.sendMessage(msg)
        // Update UI title
        updateUserInterfaceTitle()
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private fun connectionFailed() {
        // Send a failure message back to the Activity
        val msg: Message = handler.obtainMessage(MESSAGE_TOAST)
        val bundle = Bundle()
        bundle.putString(TOAST, "Unable to connect device")
        msg.data = bundle
        handler.sendMessage(msg)
        mState = STATE_NONE
        // Update UI title
        updateUserInterfaceTitle()

        // Start the service over to restart listening mode
        this@FcBluetoothService.start()
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private fun connectionLost() {
        // Send a failure message back to the Activity
        val msg: Message = handler.obtainMessage(MESSAGE_TOAST)
        val bundle = Bundle()
        bundle.putString(TOAST, "Device connection was lost")
        msg.data = bundle
        handler.sendMessage(msg)
        mState = STATE_NONE
        // Update UI title
        updateUserInterfaceTitle()

        // Start the service over to restart listening mode
        this@FcBluetoothService.start()
    }


    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    inner class AcceptThread(secure: Boolean) : Thread() {
        // The local server socket
        private val mmServerSocket: BluetoothServerSocket?
        private val mSocketType: String

        init {
            var tmp: BluetoothServerSocket? = null
            mSocketType = if (secure) "Secure" else "Insecure"

            // Create a new listening server socket
            try {
                tmp = if (secure) {
                    mAdapter?.listenUsingRfcommWithServiceRecord(NAME_SECURE, MY_UUID_SECURE)
                } else {
                    mAdapter?.listenUsingInsecureRfcommWithServiceRecord(
                        NAME_INSECURE,
                        MY_UUID_INSECURE
                    )
                }
            } catch (e: IOException) {
                FcLog.w(TAG, "Socket Type: " + mSocketType + "listen() failed", e)
            }
            mmServerSocket = tmp
            mState = STATE_LISTEN
        }

        @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
        override fun run() {
            FcLog.w(
                TAG, "Socket Type: " + mSocketType +
                        "BEGIN mAcceptThread" + this
            )
            name = "AcceptThread$mSocketType"
            var socket: BluetoothSocket? = null

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                socket = try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    mmServerSocket!!.accept()
                } catch (e: IOException) {
                    FcLog.w(TAG, "Socket Type: " + mSocketType + "accept() failed", e)
                    break
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized(this@FcBluetoothService) {
                        when (mState) {
                            STATE_LISTEN, STATE_CONNECTING -> {
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.remoteDevice, mSocketType)
                            }
                            STATE_NONE, STATE_CONNECTED -> {
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close()
                                } catch (e: IOException) {
                                    FcLog.w(TAG, "Could not close unwanted socket", e)
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
            FcLog.w(TAG, "END mAcceptThread, socket Type: $mSocketType")
        }

        fun cancel() {
            FcLog.w(TAG, "Socket Type" + mSocketType + "cancel " + this)
            try {
                mmServerSocket!!.close()
            } catch (e: IOException) {
                FcLog.w(TAG, "Socket Type" + mSocketType + "close() of server failed", e)
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    inner class ConnectThread(private val mmDevice: BluetoothDevice, secure: Boolean) : Thread() {
        private val mmSocket: BluetoothSocket?
        private val mSocketType: String

        init {
            var tmp: BluetoothSocket? = null
            mSocketType = if (secure) "安全的" else "不安全的"

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = if (secure) {
                    mmDevice.createRfcommSocketToServiceRecord(MY_UUID_SECURE)
                } else {
                    mmDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE)
                }
            } catch (e: IOException) {
                FcLog.w(TAG, "Socket Type: " + mSocketType + "create() failed", e)
            }
            mmSocket = tmp
            //通过反射建立连接
//            val clazz = tmp!!.remoteDevice.javaClass
//            val paramTypes = arrayOf<Class<*>>(Integer.TYPE)
//            val m = clazz.getMethod("createRfcommSocket", *paramTypes)
//            mmSocket = m.invoke(tmp.remoteDevice, Integer.valueOf(1)) as BluetoothSocket

            mState = STATE_CONNECTING
        }

        //        @RequiresPermission(value = "android.permission.BLUETOOTH_SCAN")
        @RequiresPermission(value = "android.permission.BLUETOOTH_CONNECT")
        override fun run() {
            FcLog.w(TAG, "BEGIN mConnectThread SocketType:$mSocketType")
            name = "ConnectThread$mSocketType"

            // Always cancel discovery because it will slow down a connection
            mAdapter?.cancelDiscovery()

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket!!.connect()
            } catch (e: IOException) {
                e.printStackTrace()
                // Close the socket
                try {
                    mmSocket!!.close()
                } catch (e2: IOException) {
                    FcLog.w(
                        TAG,
                        "unable to close() " + mSocketType + " socket during connection failure",
                        e2
                    )
                }
                connectionFailed()
                return
            }

            // Reset the ConnectThread because we're done
            synchronized(this@FcBluetoothService) { mConnectThread = null }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType)
        }

        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: IOException) {
                FcLog.w(TAG, "close() of connect $mSocketType socket failed", e)
            }
        }
    }


    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    inner class ConnectedThread(socket: BluetoothSocket, socketType: String) :
        Thread() {
        private val mmSocket: BluetoothSocket
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?

        init {
            FcLog.w(TAG, "create ConnectedThread: $socketType")
            mmSocket = socket
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.inputStream
                tmpOut = socket.outputStream
            } catch (e: IOException) {
                FcLog.w(TAG, "temp sockets not created", e)
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
            mState = STATE_CONNECTED
        }

        override fun run() {
            FcLog.w(
                TAG, "BEGIN mConnectedThread"
            )
            val buffer = ByteArray(1024)
            var bytes: Int

            // Keep listening to the InputStream while connected
            while (mState == STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream!!.read(buffer)

                    // Send the obtained bytes to the UI Activity
                    handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                        .sendToTarget()
                } catch (e: IOException) {
                    FcLog.w(TAG, "disconnected", e)
                    connectionLost()
                    break
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        fun write(buffer: ByteArray?) {
            try {
                mmOutStream!!.write(buffer)

                // Share the sent message back to the UI Activity
                handler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer)
                    .sendToTarget()
            } catch (e: IOException) {
                FcLog.w(TAG, "Exception during write", e)
            }
        }

        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                FcLog.w(TAG, "close() of connect socket failed", e)
            }
        }
    }
}