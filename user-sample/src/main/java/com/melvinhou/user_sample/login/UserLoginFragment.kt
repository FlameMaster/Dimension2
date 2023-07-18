package com.melvinhou.user_sample.login

import android.content.Intent
import android.graphics.Typeface
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import com.melvinhou.kami.bean.PageInfo
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.kami.util.StringCompareUtils
import com.melvinhou.knight.KindFragment
import com.melvinhou.user_sample.R
import com.melvinhou.user_sample.databinding.FragmentUserLoginBinding
import com.melvinhou.user_sample.net.HttpConstants
import com.melvinhou.userlibrary.UserUtils
import com.melvinhou.userlibrary.bean.User


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2022/10/10 0010 14:36
 * <p>
 * = 分 类 说 明：用户-登录
 * ================================================
 */
class UserLoginFragment : KindFragment<FragmentUserLoginBinding,LoginModel>() {

    override val _ModelClazz: Class<LoginModel>
        get() = LoginModel::class.java

    override fun openViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentUserLoginBinding =
        FragmentUserLoginBinding.inflate(inflater, container, false)


    //倒计时
    private var timer: CountDownTimer? = null

    override fun initView() {
        mBinding.tvRule.movementMethod = LinkMovementMethod.getInstance()
        mBinding.tvRule.text = buildRule()
    }

    /**
     * 规则
     */
    private fun buildRule(): SpannableString? {
        val ageementText = requireContext().getString(R.string.text_login_rule)
        val cusPrivacy1 = requireContext().getString(R.string.title_rule_user_agreement)
        val cusPrivacy2 = requireContext().getString(R.string.title_rule_privacy_policy)
        val checkColor: Int = requireContext().getColor(R.color.colorAccent)
        val spanStr = SpannableString(ageementText)
        if (!TextUtils.isEmpty(cusPrivacy1)) {
            val privacy1Index = ageementText.indexOf(cusPrivacy1)
            //设置文字的单击事件
            spanStr.setSpan(object : ClickableSpan() {
                override fun updateDrawState(ds: TextPaint) {
                    ds.isUnderlineText = false
                }

                override fun onClick(widget: View) {
                    toAgreementPage(HttpConstants.USER_AGREEMENT, cusPrivacy1)
                }
            }, privacy1Index, privacy1Index + cusPrivacy1.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            //设置文字的前景色
            spanStr.setSpan(
                ForegroundColorSpan(checkColor),
                privacy1Index,
                privacy1Index + cusPrivacy1.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        if (!TextUtils.isEmpty(cusPrivacy2)) {
            val privacy2Index = ageementText.lastIndexOf(cusPrivacy2)
            //设置文字的单击事件
            spanStr.setSpan(object : ClickableSpan() {
                override fun updateDrawState(ds: TextPaint) {
                    ds.isUnderlineText = false
                }

                override fun onClick(widget: View) {
                    toAgreementPage(HttpConstants.PLATFORM_AGREEMENT, cusPrivacy2)
                }
            }, privacy2Index, privacy2Index + cusPrivacy2.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            //设置文字的前景色
            spanStr.setSpan(
                ForegroundColorSpan(checkColor),
                privacy2Index,
                privacy2Index + cusPrivacy2.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return spanStr
    }

    override fun initListener() {
        mBinding.btLogin.setOnClickListener {
            userLogin()
        }
        mBinding.tvCodeGet.setOnClickListener {
            getAuthCode()
        }
        mBinding.tvLogon.setOnClickListener {
            mModel.page.postValue(PageInfo(R.id.action_user_login2register))
        }
        mBinding.tvPasswordRetrieve.setOnClickListener {
            mModel.page.postValue(PageInfo(R.id.action_user_login2passwordretrieve))
        }
        mBinding.ivMobileCleal.setOnClickListener {
            mBinding.etMobile.text.clear()
        }
        mBinding.ivPasswordCleal.setOnClickListener {
            mBinding.etPassword.text.clear()
        }

        //登录模式
        mBinding.rgLoginMode.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == mBinding.rbPassword.id)
                mModel.loginModel.postValue(1)
            else
                mModel.loginModel.postValue(0)
        }
        mBinding.cbPasswordShow.setOnCheckedChangeListener { _, isChecked ->
            mBinding.etPassword.transformationMethod = if (isChecked)
                HideReturnsTransformationMethod.getInstance()
            else PasswordTransformationMethod.getInstance()
        }

    }

    override fun initData() {
        mModel.loginModel.observe(this) {
            if (it == 1) {
                mBinding.etCode.text.clear()
                mBinding.rbPassword.typeface = Typeface.DEFAULT_BOLD
                mBinding.rbCode.typeface = Typeface.DEFAULT
                mBinding.llCode.visibility = View.GONE
                mBinding.llPassword.visibility = View.VISIBLE
                mBinding.tvPasswordMessage.visibility = View.VISIBLE
                mBinding.tvLogon.visibility = View.VISIBLE
                mBinding.tvPasswordRetrieve.visibility = View.VISIBLE
            } else {
                mBinding.etPassword.text.clear()
                mBinding.rbPassword.typeface = Typeface.DEFAULT
                mBinding.rbCode.typeface = Typeface.DEFAULT_BOLD
                mBinding.llCode.visibility = View.VISIBLE
                mBinding.llPassword.visibility = View.GONE
                mBinding.tvPasswordMessage.visibility = View.GONE
                mBinding.tvLogon.visibility = View.GONE
                mBinding.tvPasswordRetrieve.visibility = View.GONE
            }
        }

        mBinding.etMobile.addTextChangedListener {
            val length = it?.length ?: 0
            mBinding.ivMobileCleal.visibility = if (length > 0) View.VISIBLE else View.GONE
            showErrorMessage()
        }
        mBinding.etPassword.addTextChangedListener {
            val length = it?.length ?: 0
            mBinding.ivPasswordCleal.visibility = if (length > 0) View.VISIBLE else View.GONE
            showErrorMessage()
        }
    }

    override fun onPause() {
        super.onPause()
        timer?.cancel()
    }

    //获取验证码
    private fun getAuthCode() {
        val mobile = mBinding.etMobile.text.toString()
        if (mModel.isRequest.value == true) {
            FcUtils.showToast("正在提交中")
            return
        }else if (mModel.checkParameter(mobile = mobile)){
            time()
            mModel.sendCode(1, "86", mobile)
        }
    }

    //登录
    private fun userLogin() {
        if (!mBinding.cbRule.isChecked) {
            FcUtils.showToast("请阅读并同意协议")
            return
        }
        if (mModel.isRequest.value == true) {
            FcUtils.showToast("正在提交中")
            return
        }
        val mobile = mBinding.etMobile.text.toString()
        val authCode = mBinding.etCode.text.toString()
        val password = mBinding.etPassword.text.toString()
        when(mModel.loginModel.value){
            0->{//验证码登录
                if (mModel.checkParameter(mobile = mobile, authCode = authCode)){
                    mModel.loginAuthCode("86", mobile, authCode){data->
                        data?.let {
                            onSuceessLogin(it)
                        }
                    }
                }
            }
            1->{//密码登录
                if (mModel.checkParameter(mobile = mobile,password = password)){
                    mModel.loginPassword("86", mobile, password){data,message->
                        data?.let {
                            onSuceessLogin(it)
                        }
                        message?.let {
                            mBinding.tvPasswordMessage.text = it
                        }
                    }
                }
            }
        }
    }

    //登录成功
    private fun onSuceessLogin(data: User) {
        UserUtils.loginComplete(data)
        backward()
    }

    //可替换为跳转自己的webview
    private fun toAgreementPage(agreementUrl: String, title: String?) {
        if (TextUtils.isEmpty(agreementUrl)) {
            return
        }
        val intent = Intent()
        intent.setAction("web.base");
        intent.putExtras(bundleOf("title" to title,"url" to agreementUrl))
        toActivity(intent)
    }

    //计时器
    private fun time() {
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                mBinding.tvCodeGet.text =
                    getString(
                        R.string.txt_send_code,
                        (millisUntilFinished / 1000).toString()
                    )
                mBinding.tvCodeGet.isEnabled = false
            }

            override fun onFinish() {
                mBinding.tvCodeGet.text = "点击重新发送"
                mBinding.tvCodeGet.isEnabled = true
            }
        }.start()
    }

    //错误信息
    private fun showErrorMessage() {
        val mobile = mBinding.etMobile.text.toString()
        val password = mBinding.etPassword.text.toString()

        mBinding.tvPasswordMessage.text = when {
            !StringCompareUtils.isPhone(mobile) -> "手机号格式不正确"
            password.length < 6 -> "密码不足6位"
            else -> ""
        }
    }

}