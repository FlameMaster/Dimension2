package com.melvinhou.userlibrary

import android.content.Intent
import android.view.View
import com.melvinhou.kami.io.SharePrefUtil
import com.melvinhou.kami.util.FcUtils


/**
 * 判断是否登录
 */
fun View.setOnLoginClickListener(Callback: () -> Unit) {
    setOnClickListener {
        if (isLogin()) {//已经登录
            Callback.invoke()
        }else{//需要登录
            val intent = Intent()
            intent.action = "user.login"
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            FcUtils.getContext().startActivity(intent)
        }
    }
}

/**
 * 判断是否登录
 */
fun Array<View>.setOnLoginClickListener(Callback: () -> Unit) {
    forEach {
        it.setOnLoginClickListener {
            Callback.invoke()
        }
    }
}


fun isLogin():Boolean{
    return UserUtils.isLogin()
}