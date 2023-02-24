package com.melvinhou.dimension2.prespace;

import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.melvinhou.dimension2.databinding.ActivityLockBinding;
import com.melvinhou.kami.mvvm.BaseViewModel;
import com.melvinhou.kami.mvvm.BindFragment;
import com.melvinhou.kami.util.FcUtils;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2022/8/11 0011 14:16
 * <p>
 * = 分 类 说 明：隐私空间
 * ================================================
 */
public class SpacePreFragment extends BindFragment<ActivityLockBinding, BaseViewModel> {
    @Override
    protected ActivityLockBinding openViewBinding(LayoutInflater inflater, ViewGroup container) {
        return ActivityLockBinding.inflate(inflater, container, false);
    }

    @Override
    protected Class<BaseViewModel> openModelClazz() {
        return BaseViewModel.class;
    }


    @Override
    protected void initView() {
        mBinding.getRoot().setBackgroundColor(Color.GRAY);
    }

    @Override
    protected void initListener() {
        mBinding.vLock.setListener(new UnlockView.TrajectoryListener() {
            @Override
            public void Start(int p) {
            }

            @Override
            public void Move(int p, String pass) {

            }

            @Override
            public void Stop(String password) {
                verify(password);
            }
        });
    }

    //校验密码
    private void verify(String password) {
        String myPasswprd = "01258";
        Log.e("密码验证", "输入=" + password);
        if (myPasswprd.equals(password)) {
            toActivity(new Intent(getContext(), SpacePreActivity.class));
        } else {
            FcUtils.showToast("密码错误");
        }
        mBinding.vLock.cliar();
    }

    @Override
    protected void initData() {
    }
}
