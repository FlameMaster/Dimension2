package com.melvinhou.`fun`.device.ota

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import com.melvinhou.`fun`.device.FcBluetoothLeService
import com.melvinhou.`fun`.device.ota.callback.IProgress
import com.melvinhou.kami.io.FcLog
import java.io.File
import java.util.*


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/10/20 0020 11:45
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
object OTAHelper {
    private var stopFlag = false
    private var progress: IProgress? = null

    //通过服务控制
    private var bluetoothService: FcBluetoothLeService? = null
    private var mOTA: BluetoothGattCharacteristic? = null
    private var currentImageInfo: CurrentImageInfo? = null//硬件信息


    /**
     * 获取当前硬件信息
     * @return CurrentImageInfo 硬件信息
     * @throws Exception
     */
    @Throws(java.lang.Exception::class)
    fun getCurrentImageInfo() {
        FcLog.d("try-->getCurrentImageInfo")
        val imageInfoCommand = CommandUtil.getImageInfoCommand()
        val response = writeAndRead(imageInfoCommand, imageInfoCommand.size, 0)
        currentImageInfo = ParseUtil.parseImageFromResponse(response)
    }

    /**
     * OTA升级
     */
    @SuppressLint("MissingPermission")
    fun startOTAUpdate(
        eraseAddr: Int,
        file: File,
        progress: IProgress
    ) {
        if (currentImageInfo == null) return
        stopFlag = false
        this.progress = progress
        //HEX文件需从调用getHexFileEraseAddr(File)方法获取擦除地址
        var startAddr = 0
        if (file.name.endsWith("hex")) {
            startAddr = FileParseUtil.parseHexFileStartAddr(file)
        }
        FcLog.d("current chip type:" + currentImageInfo!!.chipType.toString())
        //image信息
        FcLog.d("start update use file: " + file.absolutePath)

        //读取文件
        FcLog.d("读取文件");
        var byteBuffer =
            FileParseUtil.parseHexFile(file)
        FcLog.d("byteBuffer  capacity: " + byteBuffer.capacity())
        val total = byteBuffer.capacity()
        FcLog.d("total size: $total")
        //读取文件的offset
        FcLog.d("解析文件")
        val blockSize = currentImageInfo!!.getBlockSize();
        FcLog.d("blockSize: $blockSize")
        //v1.2--修改擦除块的计算方式
        val nBlocks: Int =
            (total + (blockSize - 1)) / blockSize
        FcLog.d("erase nBlocks: " + (nBlocks and 0xffff))


        progress?.onInformation(String.format(Locale.getDefault(), "erase address 0x%x", startAddr))
        progress?.onEraseStart()
        //开始擦除
        FcLog.d("start erase... ")
        FcLog.d("startAddr: $startAddr")
        FcLog.d("nBlocks: $nBlocks")
        val eraseCommand = CommandUtil.getEraseCommand(startAddr, nBlocks);
        val bytes = writeAndRead(eraseCommand, eraseCommand.size, 1000);
        if (!ParseUtil.parseEraseResponse(bytes)) {
            FcLog.d("erase fail!")
            progress?.onError("erase fail!")
            return
        } else {
            FcLog.d("erase success!")
            progress?.onEraseFinish()
        }
        progress?.onProgramStart()
        val realBuffer = byteBuffer.array()
        //开始编程
        try {
            CommandUtil.updateAddressBase(currentImageInfo!!.chipType)
            bluetoothService?.getMtu()?.let { CommandUtil.updateMTU(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        FcLog.d("start program... ")
        var offset = 0
        while (offset < realBuffer.size) {
            if (checkStopFlag()) {
                return
            }
            //有效数据的长度
            val programmeLength = CommandUtil.getProgrammeLength2(realBuffer, offset)
            val programmeCommand =
                CommandUtil.getProgrammeCommand2(offset + startAddr, realBuffer, offset)
            if (!write(programmeCommand, programmeCommand.size)) {
                progress?.onError("program fail!")
                return
            }
            offset += programmeLength
            FcLog.d("progress: " + offset + "/" + realBuffer.size)
            progress?.onProgramProgress(offset, realBuffer.size)
        }
        FcLog.d("program complete! ")
        progress?.onProgramFinish()
        //开始校验
        progress?.onVerifyStart();
        FcLog.d("start verify... ")
        var vIndex = 0
        while (vIndex < realBuffer.size) {
            if (checkStopFlag()) {
                return
            }
            val verifyLength = CommandUtil.getVerifyLength2(realBuffer, vIndex)
            val verifyCommand =
                CommandUtil.getVerifyCommand2(vIndex + startAddr, realBuffer, vIndex)
            if (!write(verifyCommand, verifyCommand.size)) {
                progress?.onError("verify fail!")
                return
            }
            vIndex += verifyLength
            FcLog.d("progress: " + vIndex + "/" + realBuffer.size)
            progress?.onVerifyProgress(vIndex, realBuffer.size)
        }
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        val bytes1: ByteArray? = read()
        if (!ParseUtil.parseVerifyResponse(bytes1)) {
            FcLog.d("---->verify fail!")
            progress?.onError("verify fail!")
            return
        }
        FcLog.d("verify complete! ")
        progress?.onVerifyFinish()
        //结束
        FcLog.d("start ending... ")
        val endCommand = CommandUtil.getEndCommand()
        if (!write(endCommand, endCommand.size)) {
            progress?.onError("ending fail!")
            return
        } else {
            FcLog.d("ending success!")
            progress?.onEnd()
        }
    }


    private fun checkStopFlag(): Boolean {
        if (stopFlag) {
            progress?.onCancel()
            return true
        }
        return false
    }


    @SuppressLint("MissingPermission")
    fun read(): ByteArray? {
        return bluetoothService?.readCharacteristicSync(mOTA!!)
    }

    @SuppressLint("MissingPermission")
    fun write(
        data: ByteArray,
        length: Int
    ): Boolean {
        FcLog.d("try write :" + FormatUtil.bytesToHexString(data))
        var total = 0
        if (data.size == 0 || length == 0) {
            return false
        }
        val packetLen: Int = bluetoothService!!.getMax_packet()
        val fullCount = Math.min(length, data.size) / packetLen
        for (i in 0 until fullCount) {
            val tmp = ByteArray(packetLen)
            System.arraycopy(data, i * packetLen, tmp, 0, packetLen)
            if (!bluetoothService!!.writeCharacteristicSync(mOTA!!, tmp)) {
                return true
            }
            total += tmp.size
            if (i == fullCount - 1 && data.size % packetLen == 0) {
                break
            }
        }
        val res = Math.min(length, data.size) % packetLen
        if (res != 0) {
            val tmp = ByteArray(res)
            System.arraycopy(data, fullCount * packetLen, tmp, 0, tmp.size)
            if (!bluetoothService!!.writeCharacteristicSync(mOTA!!, tmp)) {
                return true
            }
            //LogUtil.d("final write "+tmp.length);
            total += tmp.size
        }

        return total == data.size
    }


    @SuppressLint("MissingPermission")
    fun writeAndRead(
        data: ByteArray?,
        len: Int,
        sleepTime: Int
    ): ByteArray? {
        if (!write(data!!, len)) {
            throw Exception("write fail")
        }
        if (sleepTime > 0) {
            Thread.sleep(sleepTime.toLong())
        }
        return read()
    }

}