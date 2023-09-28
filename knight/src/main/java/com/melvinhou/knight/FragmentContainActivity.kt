package com.melvinhou.knight

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.core.graphics.Insets
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.melvinhou.kami.mvvm.BaseViewModel
import com.melvinhou.kami.mvvm.BindActivity
import com.melvinhou.knight.databinding.ActivityFragmentContainBinding


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/3/24 0024 9:11
 * <p>
 * = 分 类 说 明：直接使用fragment的容器
 * ================================================
 */
class FragmentContainActivity : BindActivity<ActivityFragmentContainBinding, BaseViewModel>() {

    override fun openViewBinding(): ActivityFragmentContainBinding =
        ActivityFragmentContainBinding.inflate(layoutInflater)

    override fun openModelClazz(): Class<BaseViewModel> = BaseViewModel::class.java

//    override fun initWindowUI() {
//        //沉浸状态栏
//        WindowCompat.setDecorFitsSystemWindows(window, false)
//        //状态栏和导航栏颜色
//        val statusColor = Color.TRANSPARENT
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            window.statusBarColor = statusColor
//            window.navigationBarColor = Color.WHITE
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            window.navigationBarDividerColor = statusColor
//        }
//        val isBlack = true
//        //设置沉浸后专栏栏和导航字体的颜色，
//        WindowCompat.getInsetsController(window, mBinding.root).let {
//            it?.isAppearanceLightStatusBars = isBlack
//            it?.isAppearanceLightNavigationBars = isBlack
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //判断参数
        val fragment = intent.getSerializableExtra("fragment")
        if (savedInstanceState == null && fragment is Class<*>) {
            val clazz = fragment as? Class<Fragment>
            clazz?.let {
                supportFragmentManager.beginTransaction()
                    .replace(mBinding.container.id, it.newInstance())
                    .commitNow()
            }
        }

    }

    override fun backward() {
        super.backward()
    }

    override fun onWindowInsetsChange(insets: Insets) {
        super.onWindowInsetsChange(insets)
        val bundle = bundleOf("left" to insets.left,"top" to insets.top,"right" to insets.right,"bottom" to insets.bottom,)
        supportFragmentManager.fragments[0].arguments = bundle
    }

}