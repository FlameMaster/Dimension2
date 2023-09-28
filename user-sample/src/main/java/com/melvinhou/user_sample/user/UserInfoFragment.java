package com.melvinhou.user_sample.user;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.melvinhou.kami.mvvm.BindFragment;
import com.melvinhou.user_sample.UserModel;
import com.melvinhou.user_sample.databinding.FragmentUserInfoBinding;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/6/25 0025 15:13
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class UserInfoFragment extends BindFragment<FragmentUserInfoBinding, UserModel> {
    @Override
    protected FragmentUserInfoBinding openViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentUserInfoBinding.inflate(getLayoutInflater());
    }

    @Override
    protected Class<UserModel> openModelClazz() {
        return UserModel.class;
    }

    @Override
    protected void initView() {
    }
}
