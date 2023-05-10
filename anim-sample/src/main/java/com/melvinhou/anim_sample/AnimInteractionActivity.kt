package com.melvinhou.anim_sample

import android.transition.Explode
import android.transition.Fade
import android.transition.Slide
import android.view.KeyEvent
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.melvinhou.anim_sample.databinding.ActivityAnimInactBinding
import com.melvinhou.kami.mvvm.BindActivity
import com.melvinhou.knight.NavigaionFragmentModel


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
 * = 分 类 说 明：
 * ================================================
 */
 class AnimInteractionActivity : BindActivity<ActivityAnimInactBinding, NavigaionFragmentModel>() {

    override fun openViewBinding(): ActivityAnimInactBinding =
        ActivityAnimInactBinding.inflate(layoutInflater)

    override fun openModelClazz(): Class<NavigaionFragmentModel> =
        NavigaionFragmentModel::class.java

    private lateinit var navController: NavController


    override fun onNavigateUp(): Boolean {
        val navUp = navController.navigateUp()
        if (!navUp){
            //finish()不会执行动画
            finishAfterTransition()
        }
        return navUp
    }

    override fun backward() {
        onNavigateUp()
    }

    override fun initActivity(layoutId: Int) {
        //在需要启动的 activity 中开启动画的特征
//        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.initActivity(layoutId)
    }

    override fun initView() {
        mBinding.barRoot.title.text = "交互动画2"
        navController = findNavController(R.id.nav_host_fragment)
        //explode（分解）
        //slide（滑进滑出）
        //fade（淡入淡出）
        window.enterTransition = Fade()
        window.exitTransition = Slide()
        //再次进入动画
        window.reenterTransition = Explode()
    }

    override fun initData() {
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