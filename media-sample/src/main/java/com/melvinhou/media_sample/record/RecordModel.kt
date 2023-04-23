package com.melvinhou.media_sample.record

import android.annotation.SuppressLint
import android.app.Application
import com.melvinhou.kami.io.FileUtils
import com.melvinhou.kami.mvvm.BaseViewModel
import com.melvinhou.media_sample.bean.*
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/3/20 0020 13:57
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
class RecordModel(application: Application) : BaseViewModel(application) {


    /**
     * 存储位置
     */
    fun getRecordFilesDir(): File {
        val folderFile = File(FileUtils.getAppFileDir(FileUtils.RECORD_DIR_SUFFIX))
        if (!folderFile.exists()) {
            folderFile.mkdirs()
        }
        return folderFile
    }

    /**
     * 列表加载
     */
    @SuppressLint("CheckResult")
    fun loadListData(callback: (ArrayList<File>) -> Unit) {
        Observable.create(ObservableOnSubscribe { emitter: ObservableEmitter<ArrayList<File>> ->
            val list = arrayListOf<File>()
            val dir: File = getRecordFilesDir()
            if (dir.isDirectory) { // 处理目录
                val files = dir.listFiles()
                list.addAll(files)
            }
            emitter.onNext(list)
            emitter.onComplete()
        } as ObservableOnSubscribe<ArrayList<File>>)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                callback(it)
            }
    }

}