package com.melvinhou.dimension2.prespace;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.databinding.FragmentSpacePreBinding;
import com.melvinhou.kami.mvvm.BaseModel;
import com.melvinhou.kami.mvvm.BindFragment;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.view.BaseFragment2;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

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
public class SpacePreFragment extends BindFragment<FragmentSpacePreBinding, BaseModel> {
    @Override
    protected FragmentSpacePreBinding openViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentSpacePreBinding.inflate(inflater,container,false);
    }

    @Override
    protected Class<BaseModel> openModelClazz() {
        return BaseModel.class;
    }



    @Override
    protected void initView() {
        mBinding.getRoot().setBackgroundColor(Color.GRAY);
        mBinding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toActivity(new Intent(getContext(),SpacePreActivity.class));
            }
        });
    }

    @Override
    protected void initData() {
    }
}
