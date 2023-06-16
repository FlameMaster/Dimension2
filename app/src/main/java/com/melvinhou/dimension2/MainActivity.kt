package com.melvinhou.dimension2

import android.view.Gravity
import android.view.MenuItem
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.melvinhou.dimension2.databinding.ActivityMainBinding
import com.melvinhou.kami.mvvm.BaseViewModel
import com.melvinhou.kami.mvvm.BindActivity


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/3/17 0017 9:37
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
class MainActivity : BindActivity<ActivityMainBinding, BaseViewModel>() {
    override fun openViewBinding(): ActivityMainBinding =
        ActivityMainBinding.inflate(layoutInflater)

    override fun openModelClazz(): Class<BaseViewModel> =
        BaseViewModel::class.java


    private lateinit var mNavController: NavController
    private lateinit var mNavView: BottomNavigationView

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                mBinding.drawerLayout.openDrawer(Gravity.LEFT, true)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun initView() {
        //添加一个toolbar，阻止底部导航栏setTitle报错
        setSupportActionBar(mBinding.bar)
        //显示标题
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        val drawable = DrawerArrowDrawable(baseContext)
        supportActionBar?.setHomeAsUpIndicator(drawable);
        supportActionBar?.setDisplayShowHomeEnabled(true)

        //
        mNavController = findNavController(R.id.nav_host_fragment)
        mNavView = mBinding.btnNavView
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_media,
                R.id.navigation_function
            )
        )
        setupActionBarWithNavController(mNavController, appBarConfiguration)
        mNavView.setupWithNavController(mNavController)
        mBinding.navView.setupWithNavController(mNavController)
        //切换时保留原始色彩
//        mNavView.itemIconTintList = null
    }


    override fun initListener() {
        //自定义跳转
        mNavView.setOnItemSelectedListener {
            mNavController.navigate(it.itemId)
            true
        }
        //监听，切换状态栏颜色
        mNavController.addOnDestinationChangedListener { _, destination, _ ->

            val isblank = when (destination.id) {
//                R.id.navigation_home -> false
                else -> true
            }
            WindowCompat.getInsetsController(window, mBinding.root).let {
                it.isAppearanceLightStatusBars = isblank
                it.isAppearanceLightNavigationBars = true
            }
        }
    }

    override fun initData() {

    }

}