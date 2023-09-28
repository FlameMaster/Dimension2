package com.melvinhou.test.t04;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.os.SystemClock;
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
import com.melvinhou.kami.view.wiget.NestedSwipeLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
                TextView tv = viewHolder.itemView.findViewById(R.id.tv_title);
                tv.setText("位置：" + data);
            }

            @Override
            public int getItemLayoutId(int viewType) {
                return R.layout.item_test04;
            }

            @Override
            protected RecyclerHolder onCreate(View view, int viewType) {
                return new RecyclerHolder(view);
            }
        };
        mBinding.listView.setLayoutManager(new LinearLayoutManager(getContext()));
        mBinding.listView.setAdapter(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new CallBack());
        itemTouchHelper.attachToRecyclerView(mBinding.listView);
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
                    SystemClock.sleep(1000);
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


    class CallBack extends ItemTouchHelper.Callback {
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
//            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;//拖拽标记
            int swipeFlags = ItemTouchHelper.START;//滑动标记
            return makeMovementFlags(0, swipeFlags);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            //viewHolder 当前拖拽
            //target 目标位置
            return false;
        }

        //条目选中的状态
        @Override
        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            /**
             * call max distance start onSwiped call
             */
            //滑动距离满
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                ViewGroup viewGroup = (ViewGroup) viewHolder.itemView;
                View view = viewGroup.getChildAt(1);
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                if (Math.abs(dX) <= layoutParams.width) {
                    viewHolder.itemView.scrollTo((int) -dX, 0);
                }
            }
        }
    }
}
