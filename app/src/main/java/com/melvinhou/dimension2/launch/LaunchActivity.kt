package com.melvinhou.dimension2.launch

import androidx.core.graphics.Insets
import com.melvinhou.dimension2.R
import com.melvinhou.knight.NavigationFragmentActivity


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/6/6 0006 10:19
 * <p>
 * = 分 类 说 明：启动页
 * ================================================
 */
class LaunchActivity : NavigationFragmentActivity<LaunchViewModel>() {
    override val _ModelClazz: Class<LaunchViewModel>
        get() = LaunchViewModel::class.java
    override val _navigationRes: Int
        get() = R.navigation.launch_navigation


    override fun onWindowInsetsChange(insets: Insets?) {
        super.onWindowInsetsChange(insets)
        mModel.WindowInsets.postValue(insets)
    }

    override fun onNavigateUp(): Boolean {
        finish()
        return true
    }

}