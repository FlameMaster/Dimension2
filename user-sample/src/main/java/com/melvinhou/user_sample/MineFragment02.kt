package com.melvinhou.user_sample

import android.app.PendingIntent
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.get
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.melvinhou.kami.util.FcUtils
import com.melvinhou.knight.KfcUtils
import com.melvinhou.knight.KindFragment
import com.melvinhou.user_sample.account.AccountActivity
import com.melvinhou.user_sample.databinding.FragmentMine02Binding
import com.melvinhou.user_sample.help.HelpActivity
import com.melvinhou.user_sample.setting.SettingActivity
import com.melvinhou.user_sample.user.UserActivity
import com.melvinhou.userlibrary.UserUtils
import com.melvinhou.userlibrary.bean.User
import com.melvinhou.userlibrary.setOnLoginClickListener


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/7/18 0018 11:55
 * <p>
 * = 分 类 说 明：我的页面2
 * ================================================
 */
class MineFragment02 : KindFragment<FragmentMine02Binding, UserModel>() {
    override val _ModelClazz: Class<UserModel>
        get() = UserModel::class.java

    override fun openViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMine02Binding = FragmentMine02Binding.inflate(inflater, container, false)

    var isStart = true
    override fun initListener() {
        arrayOf(
            mBinding.inUser.ivUser,
            mBinding.inUser.llUser,
            mBinding.inUser.tvMore,
        ).setOnLoginClickListener {
            toActivity<UserActivity>()
        }
        mBinding.inAccount.root.setOnLoginClickListener {
            toActivity<AccountActivity>()
        }

        mBinding.glFuns.get(3).setOnLoginClickListener {
            val bundle = bundleOf("start" to R.id.nav_help_center)
            toActivity<HelpActivity>(bundle)
        }
        mBinding.glFuns.get(4).setOnLoginClickListener {
            val bundle = bundleOf("start" to R.id.nav_abuot_us)
            toActivity<SettingActivity>(bundle)
        }
        mBinding.glFuns.get(5).setOnLoginClickListener {
            toActivity<SettingActivity>()
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun initData() {
        mModel.info.observe(this) {
            onSuccess(it)
        }

    }

    private fun loadData() {
        mModel.loadUserInfo()
    }

    /**
     * 获取数据成功
     */
    private fun onSuccess(data: User) {
        //显示
        val isLogin = UserUtils.isLogin()
        mBinding.inUser.tvMobile.isVisible = isLogin
        mBinding.inUser.ivVipState.isVisible = isLogin
        mBinding.inUser.ivUser.let {
            Glide.with(FcUtils.getContext())
                .load(data.photo)
                .apply(
                    RequestOptions()
                        .override(it.width, it.height)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                )
                .into(it)
        }
        mBinding.inUser.tvUser.text = if (isLogin) data.nickName else "登录/注册"
        mBinding.inUser.tvMobile.text = KfcUtils.getMaskMobile(data.phone)
    }
}