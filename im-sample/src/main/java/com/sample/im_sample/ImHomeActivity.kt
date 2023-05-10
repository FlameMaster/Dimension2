package com.sample.im_sample

import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.knight.KindActivity
import com.melvinhou.knight.NavigationFragmentActivity
import com.sample.im_sample.databinding.ActivityImHomeBinding
import com.sample.im_sample.model.ImViewModel


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
 * = 分 类 说 明：即时通讯相关
 * ================================================
 */
class ImHomeActivity : NavigationFragmentActivity<ImViewModel>() {
    override val _ModelClazz: Class<ImViewModel>
        get() = ImViewModel::class.java
    override val _navigationRes: Int
        get() = R.navigation.navigation_im

    //底部距离
    private var initialBottom = -1

    override fun onWindowInsetsChange(insets: Insets) {
        super.onWindowInsetsChange(insets)
        val bottom = insets.bottom
        if (initialBottom < 0) initialBottom = bottom//初始化
        mModel.isShowKeyboard = bottom > initialBottom
        //软键盘
        mBinding.root.setPadding(0, 0, 0, bottom)
    }
}