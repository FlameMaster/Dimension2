package com.melvinhou.user_sample.user

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import com.melvinhou.kami.io.FcLog
import com.melvinhou.knight.NavigationFragmentActivity
import com.melvinhou.user_sample.R
import com.melvinhou.user_sample.UserModel


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/5/5 0005 15:50
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
class UserActivity : NavigationFragmentActivity<UserModel>() {
    override val _ModelClazz: Class<UserModel>
        get() = UserModel::class.java
    override val _navigationRes: Int
        get() = R.navigation.navigation_user
}