package com.melvinhou.dimension2.ui.home;

import android.content.Intent;
import android.view.View;

import com.bumptech.glide.Glide;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.ar.d3.D3ListActivity;
import com.melvinhou.dimension2.databinding.FgtHomeBD;
import com.melvinhou.dimension2.game.GameLaunchPager;
import com.melvinhou.dimension2.pager.BasePager;
import com.melvinhou.dimension2.pager.PagerActivity;
import com.melvinhou.dimension2.test.TestActivity;
import com.melvinhou.dimension2.web.WebActivity;
import com.melvinhou.kami.mvvm.DataBindingFragment;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.rxjava.rxbus.RxBus;
import com.melvinhou.rxjava.rxbus.RxBusClient;
import com.melvinhou.rxjava.rxbus.RxBusMessage;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2020/7/1 0:0
 * <p>
 * = 分 类 说 明：主页
 * ================================================
 */
public class HomeFragment extends DataBindingFragment<FgtHomeBD> {

    private HomeViewModel homeViewModel;

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_home;
    }

    @Override
    protected void initView() {
    }

    @Override
    protected void initListener() {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
//        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                getViewDataBinding().textHome.setText(s);
//            }
//        });

        getViewDataBinding().test.setOnClickListener(this::toTest);
        getViewDataBinding().web.setOnClickListener(this::toWeb);
        getViewDataBinding().ar.setOnClickListener(this::toAR);
        getViewDataBinding().games.setOnClickListener(this::toGameList);

    }

    @Override
    protected void initData() {
        getViewDataBinding().setModel(homeViewModel);
//        getViewDataBinding().setBannerUrl("https://b-ssl.duitang.com/uploads/item/201801/08/20180108181057_VSyJF.gif");
        Glide
                .with(FcUtils.getContext())
                .load("https://b-ssl.duitang.com/uploads/item/201801/08/20180108181057_VSyJF.gif")
                .into(getViewDataBinding().banner);
    }


    public void toTest(View view){
        Intent intent =new Intent(FcUtils.getContext(), TestActivity.class);
        startActivity(intent);
    }
    public void toWeb(View view){
        Intent intent =new Intent(FcUtils.getContext(), WebActivity.class);
        intent.putExtra("title","微软中国");
        intent.putExtra("url","https://cn.bing.com/");
        startActivity(intent);

    }
    public void toAR(View view){
        Intent intent =new Intent(FcUtils.getContext(), D3ListActivity.class);
        startActivity(intent);
    }

    private void openPagerActivity(String title, BasePager[] pagers) {
        Intent intent = new Intent(FcUtils.getContext(), PagerActivity.class);
        intent.putExtra("title", title);

        new RxBusClient(RxBusClient.getClientId(getClass().getName())) {
            @Override
            protected void onEvent(@NonNull String eventType, Object attach) {
                //发送
                RxBus.instance().post(RxBusMessage.Builder
                        .instance(":{Pager}init")
                        .client(getClass().getName())
                        .build());
                cancel();
            }
        };
        toActivity(intent);
    }

    private void toGameList(View view) {
        openPagerActivity("游戏", new BasePager[]{new GameLaunchPager()});

    }


}
