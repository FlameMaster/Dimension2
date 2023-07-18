package com.melvinhou.user_sample

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.util.Log
import androidx.core.graphics.Insets
import androidx.lifecycle.MutableLiveData
import com.melvinhou.kami.io.IOUtils
import com.melvinhou.kami.io.SharePrefUtil
import com.melvinhou.kami.mvvm.BaseViewModel
import com.melvinhou.kami.net.RequestCallback
import com.melvinhou.kami.tool.AssetsUtil
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.knight.NavigaionFragmentModel
import com.melvinhou.user_sample.net.ApiModel
import com.melvinhou.userlibrary.bean.User
import com.melvinhou.userlibrary.db.SqlManager
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/6/6 0006 15:21
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
class UserNativeModel {


    /**
     * 加载用户信息
     */
    @SuppressLint("CheckResult")
    fun loadUserInfo(id: Long, callback: (User?) -> Unit) {
        if (id < 0) {
            return
        }
        Observable
            .create { emitter: ObservableEmitter<User> ->
                var user: User? = null
                try {
                    user = SqlManager.findUser(FcUtils.getContext(), id)
                } finally {
                    user?.let { emitter.onNext(it) }
                    emitter.onComplete()
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { data: User ->
                callback(data)
            }
    }

}