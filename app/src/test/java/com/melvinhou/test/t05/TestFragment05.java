package com.melvinhou.test.t05;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.databinding.FragmentTest05Binding;
import com.melvinhou.kami.adapter.RecyclerAdapter;
import com.melvinhou.kami.adapter.RecyclerHolder;
import com.melvinhou.kami.mvvm.BaseViewModel;
import com.melvinhou.kami.mvvm.BindFragment;

import java.util.ArrayList;
import java.util.List;

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
 * = 分 类 说 明：
 * ================================================
 */
public class TestFragment05 extends BindFragment<FragmentTest05Binding, BaseViewModel> {

    @Override
    protected FragmentTest05Binding openViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentTest05Binding.inflate(getLayoutInflater());
    }

    @Override
    protected Class<BaseViewModel> openModelClazz() {
        return BaseViewModel.class;
    }


    @Override
    protected void initView() {
        mBinding.barRoot.title.setText("卡片切换");

    }

    @Override
    protected void initListener() {


        RecyclerAdapter adapter = new RecyclerAdapter<String, RecyclerHolder>() {
            @Override
            public void bindData(RecyclerHolder viewHolder, int position, String data) {
                TextView tv = viewHolder.itemView.findViewById(R.id.item_title);
                tv.setText("现在位置" + data);
            }

            @Override
            public int getItemLayoutId(int viewType) {
                return R.layout.item_test05;
            }

            @Override
            protected RecyclerHolder onCreate(View view, int viewType) {
                return new RecyclerHolder(view);
            }
        };
        mBinding.listView.setLayoutManager(new CardLayoutManager(3,0.5f));
        mBinding.listView.setAdapter(adapter);
        //添加数据
        List<String> list = new ArrayList<>();
        for (int i =0;i<20;i++){
            list.add(String.valueOf(i));
        }
        adapter.addDatas(list);
        adapter.notifyDataSetChanged();

        //
        new CardSnapHelper(3).attachToRecyclerView(mBinding.listView);
    }

    @Override
    protected void initData() {

    }


}
