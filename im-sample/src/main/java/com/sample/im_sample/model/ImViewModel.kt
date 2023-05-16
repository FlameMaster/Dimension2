package com.sample.im_sample.model

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.collection.arrayMapOf
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.melvinhou.kami.io.SharePrefUtil
import com.melvinhou.kami.util.DateUtils
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.kami.util.StringCompareUtils
import com.melvinhou.knight.NavigaionFragmentModel
import com.sample.im_sample.bean.ImChatEntity
import com.sample.im_sample.bean.ImContactEntity
import com.melvinhou.accountlibrary.bean.User
import com.melvinhou.accountlibrary.db.SqlManager
import com.melvinhou.kami.util.ResourcesUtils
import com.sample.im_sample.R
import com.sample.im_sample.db.im.ImChatDB
import com.sample.im_sample.db.im.ImContactsDB
import com.sample.im_sample.util.PinYinUtil
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.Collections


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/5/5 0005 15:52
 * <p>
 * = 分 类 说 明：即时通讯
 * ================================================
 */
class ImViewModel(application: Application) : NavigaionFragmentModel(application) {
    //数据库-联系人
    private var mContactsDb: ImContactsDB

    //数据库-聊天记录
//    private var mChatDb: ImChatDB? = null
    //聊天数据库
    private val mChatDbs = arrayMapOf<Long, ImChatDB>()

    init {
        mContactsDb = Room.databaseBuilder(
            getApplication(),
            ImContactsDB::class.java, "im_contacts.db"
        ).build()
    }

    //当前用户
    var currentUserId: Long? = null

    //软键盘显示
    var isShowKeyboard = false

    fun getChatDb(userId: Long): ImChatDB {
        var chatDB = mChatDbs.get(userId)
        if (chatDB == null) {
            chatDB = Room.databaseBuilder(
                getApplication(),
                ImChatDB::class.java, "im_chat_$userId.db"
            ).build()
            mChatDbs.put(userId, chatDB)
        }
        return chatDB
    }

    //解绑聊天数据库
    fun clearChatDbs() {
        mChatDbs.values.forEach {
            if (it.isOpen) it.close()
        }
        mChatDbs.clear()
    }

    @SuppressLint("CheckResult")
    fun getUserInfo(userId: Long, callback: (User) -> Unit) {
        Observable
            .create<User?> { emitter: ObservableEmitter<User?> ->
                try {
                    val user = SqlManager.findUser(FcUtils.getContext(), userId)
                    if (user != null)
                        emitter.onNext(user)
                } finally {
                    emitter.onComplete()
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { user: User? ->
                user?.let(callback)
            }
    }

    //获取所有联系人,livedata
    @Synchronized
    fun getAllContacts(): LiveData<List<ImContactEntity>> =
        mContactsDb.imContactsDao().allContacts

    //添加联系人
    @Synchronized
    fun addContact(entity: ImContactEntity) {
        mContactsDb.imContactsDao().add(entity)
    }

    //添加联系人
    fun addContact(userId: Long?, name: String, ip: String, port: String) {
        if (name.isEmpty() || ip.isEmpty() || port.isEmpty()) {
            FcUtils.showToast("添加失败")
            return
        }
        if (!StringCompareUtils.isIp(ip)) {
            FcUtils.showToast("ip格式不正确")
            return
        }
        val id = userId ?: createUserId();
        Thread {
            //用户
            val user = User()
            user.userId = id
            user.name = name
            user.photo = ResourcesUtils.getStringArray(R.array.photo_heads)[(id % 5).toInt()]
            SqlManager.addUser(FcUtils.getContext(), user)
            //联系人
            val entity = ImContactEntity()
            entity.ip = ip
            entity.port = port.toInt()
            entity.userId = id
            val fchar = name.toCharArray()[0]
            entity.initial = PinYinUtil.getPinyin(fchar)
            addContact(entity)
//            Log.w("","添加用户：${name},id=${id}")
        }.start()
    }

    //生成一个id
    fun createUserId(): Long {
        val id = SharePrefUtil.getLong("userNumber", 20000L)
        SharePrefUtil.saveLong("userNumber", id + 1)
        return id
    }


    //获取聊天记录,rxjava背压
    fun getChat(userId: Long, callback: (List<ImChatEntity>) -> Unit) {
        addDisposable(getChatDb(userId).chatHistoryDao()!!.all
            .map {
                Collections.reverse(it)//倒序排列
                it
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { list ->
                callback(list)
            })
    }

    //添加聊天信息，rxjava
    fun addChat(userId: Long, entity: ImChatEntity) {
        getChatDb(userId).chatHistoryDao().insert(entity)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    //添加聊天信息
    @SuppressLint("CheckResult")
    @Synchronized
    fun addChat2(userId: Long, entity: ImChatEntity) {
        getChatDb(userId).chatHistoryDao().add(entity)
    }

    //添加聊天
    fun sendChat(userId: Long, message: String) {
        val entity = ImChatEntity()
        entity.uuid = DateUtils.getCurrentTime()
        entity.userId = currentUserId ?: -1
        entity.message = message
        entity.date = DateUtils.formatDuration("yyyy-MM-dd  HH:mm:ss")
        addChat(userId, entity)
    }

    //收到聊天
    fun receiveChat(ip: String, message: String) {
        val userId = mContactsDb.imContactsDao().query(ip).userId
        if (userId > 0) {
            val entity = ImChatEntity()
            entity.uuid = DateUtils.getCurrentTime()
            entity.userId = userId
            entity.message = message
            entity.date = DateUtils.formatDuration("yyyy-MM-dd  HH:mm:ss")
            addChat2(userId, entity)
        }
    }

}