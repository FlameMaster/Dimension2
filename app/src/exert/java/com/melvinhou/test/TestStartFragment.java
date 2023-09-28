package com.melvinhou.test;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.databinding.FragmentTestStartBinding;
import com.melvinhou.kami.adapter.RecyclerAdapter;
import com.melvinhou.kami.adapter.RecyclerHolder;
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
 * = 时 间：2020/7/16 2:39
 * <p>
 * = 分 类 说 明：测试的首页
 * ================================================
 */
public class TestStartFragment extends BindFragment<FragmentTestStartBinding, BaseViewModel> {

    @Override
    protected FragmentTestStartBinding openViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentTestStartBinding.inflate(getLayoutInflater());
    }

    @Override
    protected Class<BaseViewModel> openModelClazz() {
        return BaseViewModel.class;
    }

    @Override
    protected void initView() {
        mBinding.barRoot.title.setText("实验室");
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("使用说明").append("\n");
        buffer.append("01.左侧划出菜单抽屉").append("\n");
        buffer.append("02.选择对应页面").append("\n");
        mBinding.tvExplain.setText(buffer);
    }


}
