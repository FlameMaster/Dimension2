package com.melvinhou.dimension2.launch

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.animation.addListener
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.melvinhou.dimension2.R
import com.melvinhou.dimension2.databinding.FragmentSplashBinding
import com.melvinhou.dimension2.utils.KeyConstant
import com.melvinhou.dimension2.web.WebActivity
import com.melvinhou.kami.io.SharePrefUtil
import com.melvinhou.kami.tool.UITools
import com.melvinhou.knight.KindFragment


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/6/6 0006 15:17
 * <p>
 * = 分 类 说 明：开屏页
 * ================================================
 */
class SplashFragment : KindFragment<FragmentSplashBinding, LaunchViewModel>() {
    override val _ModelClazz: Class<LaunchViewModel>
        get() = LaunchViewModel::class.java

    override fun openViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentSplashBinding = FragmentSplashBinding.inflate(inflater, container, false)


    override fun initView() {
    }

    override fun initData() {
        mBinding.ivLogo.post { startAnimat() }
    }

    private fun startAnimat() {
        val width = mBinding.root.width + mBinding.ivLogo.width
        val height = mBinding.root.height + mBinding.ivLogo.height
        val set = AnimatorSet()
        set.play(
            ObjectAnimator.ofFloat(
                mBinding.ivLogo, View.ROTATION,
                360f, 0f,
            )
        )
            .with(
                ObjectAnimator.ofFloat(
                    mBinding.ivLogo, View.TRANSLATION_X,
                    width / 2f, 0f,
                )
            )
        //弹性插值器
//        set.setInterpolator { x ->
//            if (x < 0.3535) bounce(x)
//            else if (x < 0.7408) bounce(x - 0.54719f) + 0.7f
//            else if (x < 0.9644) bounce(x - 0.8526f) + 0.9f
//            else bounce(x - 1.0435f) + 0.95f
//        }
        set.duration = 800
        set.addListener(onStart = {
            mBinding.ivLogo.isVisible = true
        }, onEnd = {
            onNext()
        })
        set.start()
    }

    //弹性计算
    fun bounce(t: Float): Float = t * t * 8;


    /**
     * 下一步操作
     */
    private fun onNext() {
        //协议判断
        val isRule = SharePrefUtil.getBoolean(KeyConstant.APP_RULE, false)
        if (!isRule) {
            showRule() {
                if (it) onNext()
                else showCheckDialog(
                    title = "使用说明",
                    message = "您未同意隐私政策和用户协议，为了保护您的隐私和权益，您将无法使用本应用。",
                    "查看协议", "退出应用"
                ) {
                    if (it) {
                        onNext()
                    } else {
                        requireActivity().finish()
                    }
                }
            }
            return
        }
        //权限判断
        val isPermission = SharePrefUtil.getBoolean(KeyConstant.APP_PERMISSION, false)
        if (isPermission)
            mModel.toFragment(R.id.navigation_advert)
        else
            mModel.toFragment(R.id.navigation_permission)
    }

    /**
     * 规则
     */
    private fun showRule(callback: (Boolean) -> Unit) {
        val dialog = UITools.createDialog(
            requireActivity(),
            R.layout.dialog_rule,
            Gravity.CENTER,
            R.style.Animation_Dialog_Center
        )
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        val tvTitle = dialog.window?.findViewById<TextView>(R.id.tv_title)
        val tvContent = dialog.window?.findViewById<TextView>(R.id.tv_content)
        val btAgree = dialog.window?.findViewById<View>(R.id.bt_agree)
        val btCancel = dialog.window?.findViewById<View>(R.id.bt_cancel)
        //支持内链
        tvContent?.movementMethod = LinkMovementMethod.getInstance()
        tvContent?.highlightColor = Color.GREEN
        btAgree?.setOnClickListener {
            SharePrefUtil.saveBoolean(KeyConstant.APP_RULE, true)
            dialog.dismiss()
            callback(true)
        }
        btCancel?.setOnClickListener {
            dialog.dismiss()
            callback(false)
        }

        //数据
        val ruleContent = getString(R.string.text_rule).replace(
            getString(R.string.rule_app_name), getString(R.string.app_name)
        )
        val spanText = SpannableString(ruleContent)
        val platformAgree = getString(R.string.title_rule_platform_agreement)
        ruleContent.indexOf(platformAgree).let {
            spanText.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        toActivity<WebActivity>(
                            bundleOf(
                                "title" to platformAgree,
                                "url" to "https://cn.bing.com/"
                            )
                        )
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.color = requireContext().getColor(R.color.red)
                        ds.isUnderlineText = false
                    }
                },
                it, it + platformAgree.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        val userAgree = getString(R.string.title_rule_user_agreement)
        ruleContent.indexOf(userAgree).let {
            spanText.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        toActivity<WebActivity>(
                            bundleOf(
                                "title" to userAgree,
                                "url" to "https://cn.bing.com/"
                            )
                        )
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.color = requireContext().getColor(R.color.red)
                        ds.isUnderlineText = false
                    }
                },
                it, it + userAgree.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        tvContent?.text = spanText
    }
}