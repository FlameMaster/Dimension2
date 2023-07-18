package com.melvinhou.user_sample.login

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.melvinhou.kami.bean.PageInfo
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.knight.KindFragment
import com.melvinhou.user_sample.R
import com.melvinhou.user_sample.databinding.FragmentUserPasswordRetrieveBinding


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
 * = 分 类 说 明：用户-密码找回
 * ================================================
 */
class UserPasswordRetrieveFragment :
    KindFragment<FragmentUserPasswordRetrieveBinding, LoginModel>() {

    override val _ModelClazz: Class<LoginModel>
        get() = LoginModel::class.java

    override fun openViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentUserPasswordRetrieveBinding =
        FragmentUserPasswordRetrieveBinding.inflate(inflater, container, false)


    //倒计时
    private var timer: CountDownTimer? = null

    override fun backward() {
        mModel.page.postValue(PageInfo(-1))//返回上一页
    }

    override fun initView() {
        mBinding.barRoot.title.text = "忘记密码"
        mBinding.btSubmit.isEnabled = true
    }

    override fun initListener() {
        mBinding.tvCodeGet.setOnClickListener {
            getAuthCode()
        }
        mBinding.ivMobileCleal.setOnClickListener {
            mBinding.etMobile.text.clear()
        }
        mBinding.ivPasswordCleal.setOnClickListener {
            mBinding.etPassword.text.clear()
        }
        mBinding.btSubmit.setOnClickListener {
            changePassword()
        }
    }

    override fun initData() {

        mBinding.etMobile.addTextChangedListener {
            val length = it?.length ?: 0
            mBinding.ivMobileCleal.visibility = if (length > 0) View.VISIBLE else View.GONE
        }
        mBinding.etPassword.addTextChangedListener {
            val length = it?.length ?: 0
            mBinding.ivPasswordCleal.visibility = if (length > 0) View.VISIBLE else View.GONE
        }
    }

    //获取验证码
    private fun getAuthCode() {
        val mobile = mBinding.etMobile.text.toString()
        if (mModel.isRequest.value == true) {
            FcUtils.showToast("正在提交中")
            return
        } else if (mModel.checkParameter(mobile = mobile)) {
            time()
            mModel.sendCode(1, "86", mobile)
        }
    }

    //修改密码
    private fun changePassword() {
        if (mModel.isRequest.value == true) {
            FcUtils.showToast("正在提交中")
            return
        }
        val mobile = mBinding.etMobile.text.toString()
        val authCode = mBinding.etCode.text.toString()
        val password = mBinding.etPassword.text.toString()
        if (mModel.checkParameter(mobile = mobile, authCode = authCode, password = password)) {
            mModel.changePassword("86", mobile, authCode, password) {
                FcUtils.showToast("密码修改成功")
                backward()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        timer?.cancel()
    }

    //倒计时
    fun time() {
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

}