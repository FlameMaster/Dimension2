package com.melvinhou.dimension2.launch

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.melvinhou.dimension2.MainActivity
import com.melvinhou.dimension2.R
import com.melvinhou.dimension2.databinding.FragmentAdvertBinding
import com.melvinhou.dimension2.utils.KeyConstant
import com.melvinhou.kami.io.SharePrefUtil
import com.melvinhou.kami.tool.AssetsUtil
import com.melvinhou.kami.util.DimenUtils
import com.melvinhou.knight.KindFragment
import com.melvinhou.knight.loadImage


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/6/6 0006 17:03
 * <p>
 * = 分 类 说 明：广告页
 * ================================================
 */
class AdvertFragment : KindFragment<FragmentAdvertBinding, LaunchViewModel>() {
    override val _ModelClazz: Class<LaunchViewModel>
        get() = LaunchViewModel::class.java

    override fun openViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentAdvertBinding = FragmentAdvertBinding.inflate(inflater, container, false)




    //倒计时
    private var downTimer: CountDownTimer? = null

    override fun initView() {
        mBinding.getRoot().visibility = View.INVISIBLE
        mBinding.getRoot().post {
            startAnim()
        }
        //间隔
        mModel.WindowInsets.observe(this) { insets ->
            mBinding.root.setPadding(0, 0, 0, insets.bottom)
            val lp = mBinding.btSkip.layoutParams as? ConstraintLayout.LayoutParams
            lp?.let {
                it.topMargin = insets.top + DimenUtils.dp2px(10)
                mBinding.btSkip.layoutParams = it
            }
        }
    }

    override fun initListener() {
        mBinding.btSkip.setOnClickListener {
            onNext()
        }
    }

    override fun initData() {
        //加载广告图
        mModel.loadAdvertImage {
            //加载广告图
            mBinding.container.loadImage(it)
        }
        //倒计时
        downTimer = object : CountDownTimer(4000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                mBinding.btSkip.text = "跳过 | ${millisUntilFinished / 1000}"
            }

            override fun onFinish() {
                onNext()
            }
        }.start()
    }


    /**
     * 揭露动画
     */
    private fun startAnim() {
        mBinding.root.visibility = View.VISIBLE
        val width = mBinding.root.width
        val height = mBinding.root.height
        //斜边
        val viewHypotenuse =
            Math.hypot(width.toDouble(), height.toDouble())
        val anim = ViewAnimationUtils.createCircularReveal(
            mBinding.root, width / 2, height / 2, 0f, viewHypotenuse.toFloat()
        )
        anim.duration = 500
        anim.start()
    }

    /**
     * 下一步操作
     */
    private fun onNext() {
        downTimer?.cancel()
        //首次进入会进引导页
        val isFirstLaunch = SharePrefUtil.getBoolean(KeyConstant.APP_FIRST_LAUNCH, true)
        if (isFirstLaunch){
            mModel.toFragment(R.id.action_advert2guide)
        }else{
            toActivity<MainActivity>()
            requireActivity().finish()
        }
    }
}