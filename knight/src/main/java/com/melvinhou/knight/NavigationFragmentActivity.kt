package com.melvinhou.knight

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.annotation.NavigationRes
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.melvinhou.kami.databinding.ActivityListBinding
import com.melvinhou.kami.mvvm.BaseViewModel
import com.melvinhou.kami.mvvm.BindActivity
import com.melvinhou.knight.databinding.ActivityFragmentContainerBinding


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/2/6 0006 14:43
 * <p>
 * = 分 类 说 明：通用的Fragment导航容器
 * ================================================
 */
abstract class NavigationFragmentActivity<M : NavigaionFragmentModel> :
    KindActivity<ActivityFragmentContainerBinding, M>() {

    override val _ViewBinding: ActivityFragmentContainerBinding
        get() = ActivityFragmentContainerBinding.inflate(layoutInflater)

    @get:NavigationRes
    protected abstract val _navigationRes: Int

    private lateinit var navController: NavController
    private var mControllerCompat: WindowInsetsControllerCompat? = null


    override fun onNavigateUp(): Boolean {
        val navUp = navController.navigateUp()
        if (!navUp) finish()
        return navUp
    }

    override fun onSupportNavigateUp(): Boolean {
        return onNavigateUp()
    }

    override fun backward() {
        onNavigateUp()
    }

    override fun initView() {
        mControllerCompat = WindowCompat.getInsetsController(window, mBinding.root)
        navController = findNavController(R.id.nav_host_fragment)
        //设置管理资源
        val navGraph = navController.navInflater.inflate(_navigationRes)
        //设置启动页
        val startId = intent.getIntExtra("start", -1)
        if (startId > 0) {
            navGraph.setStartDestination(startId)
        }
        navController.setGraph(navGraph, null)
    }

    override fun initData() {

        //进度条
        mModel.isRequest.observe(this) {
            if (it) {
                showProcess(null)
            } else {
                hideProcess()
            }
        }

        //页面切换
        mModel.page.observe(this) {
            if (it.pageId < 0) {
                if (it.pageId < -1) {
                    val num = it.pageId / -1
                    for (i in 0 until num) {
                        backward()
                    }
                } else backward()
            } else {
                navController.navigate(it.pageId, it.pageArgs)
            }
        }
    }


    /**
     * 系统按键
     *
     * @param keyCode
     * @param event
     * @return
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        //使用Fragment的返回
        if (keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0) {
            val fragment = supportFragmentManager.primaryNavigationFragment
            val view = fragment?.view?.findViewById<View>(R.id.back)
            if (view != null) {
                view.performClick()
            } else {
                backward()
            }
            return true
        }
        return false
    }

}