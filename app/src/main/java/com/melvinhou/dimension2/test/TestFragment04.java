package com.melvinhou.dimension2.test;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.databinding.FragmentTest04Binding;
import com.melvinhou.kami.adapter.RecyclerAdapter;
import com.melvinhou.kami.adapter.RecyclerHolder;
import com.melvinhou.kami.io.IOUtils;
import com.melvinhou.kami.mvvm.BaseViewModel;
import com.melvinhou.kami.mvvm.BindFragment;
import com.melvinhou.kami.util.FcUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;

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
public class TestFragment04 extends BindFragment<FragmentTest04Binding, BaseViewModel> {

    @Override
    protected FragmentTest04Binding openViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentTest04Binding.inflate(getLayoutInflater());
    }

    @Override
    protected Class<BaseViewModel> openModelClazz() {
        return BaseViewModel.class;
    }


    private RecyclerAdapter adapter;
    private int page = 1;//页码
    private boolean isMoreHas = true;//是否有更多

    @Override
    protected void initView() {

        adapter = new RecyclerAdapter<String, RecyclerHolder>() {
            @Override
            public void bindData(RecyclerHolder viewHolder, int position, String data) {
                TextView tv = viewHolder.itemView.findViewById(R.id.user_name);
                tv.setText("位置：" + data);
            }

            @Override
            public int getItemLayoutId(int viewType) {
                return R.layout.item_im_friend;
            }

            @Override
            protected RecyclerHolder onCreate(View view, int viewType) {
                return new RecyclerHolder(view);
            }
        };
        mBinding.listView.setLayoutManager(new LinearLayoutManager(getContext()));
        mBinding.listView.setAdapter(adapter);
    }

    @Override
    protected void initListener() {
        mBinding.root.setSwipeListener(new NestedSwipeLayout.SwipeListener() {
            @Override
            public void onRefresh() {
                FcUtils.showToast("刷新数据~");
                page = 1;
                loadData();
            }

            @Override
            public void onContinue() {
                if (isMoreHas) {
                    FcUtils.showToast(page + "/加载更多~");
                    page++;
                    loadData();
                }
            }
        });
    }

    @Override
    protected void initData() {
        page = 1;
        loadData();
    }

    @SuppressLint("CheckResult")
    private void loadData() {
        Observable.create((ObservableOnSubscribe<List<String>>) emitter -> {
                    List<String> list = loadData(page, 10);
                    emitter.onNext(list);
                    emitter.onComplete();
                })
                .compose(IOUtils.setThread())
                .subscribe(list -> {
                    if (page == 1) {
                        mBinding.root.finishTop();
                        adapter.clearData();
                        isMoreHas = true;
                    }
                    adapter.addDatas(list);
                    if (list == null || list.isEmpty()) {
                        isMoreHas = false;
                    }
                });
    }

    private List<String> loadData(int p, int size) {
        List<String> list = new ArrayList<>();
        if (p < 4) {
            for (int i = 0; i < size; i++)
                list.add(String.valueOf(p * size + i));
        }
        return list;
    }

}
