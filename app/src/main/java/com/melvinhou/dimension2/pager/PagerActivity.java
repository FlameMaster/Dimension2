package com.melvinhou.dimension2.pager;

import android.content.Intent;
import android.view.MenuItem;
import android.view.View;

import com.melvinhou.dimension2.PairEntity;
import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.databinding.ActPagerBD;
import com.melvinhou.kami.model.EventMessage;
import com.melvinhou.kami.mvvm.DataBindingActivity;
import com.melvinhou.rxjava.RxBus;
import com.melvinhou.rxjava.RxBusClient;
import com.melvinhou.rxjava.RxMsgParameters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.viewpager.widget.ViewPager;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2019/12/5 17:03
 * <p>
 * = 分 类 说 明：列表切换pager
 * ============================================================
 */
public class PagerActivity extends DataBindingActivity<ActPagerBD> {

    //页面的adapter
    private PagerAdapter mPagerAdapter;

    //RxBus的接收器
    private RxBusClient mRxBusClient;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_pager;
    }

    @Override
    protected void onDestroy() {
        if (mRxBusClient != null) {
            mRxBusClient.unregister();
            mRxBusClient = null;
        }
        super.onDestroy();
    }

    @Override
    protected void initView() {
        getViewDataBinding().setTitle(getIntent().getStringExtra("title"));
        getViewDataBinding().container.setOffscreenPageLimit(8);

    }

    @Override
    protected void initListener() {
        bindRxBus();

    }

    @Override
    protected void initData() {

    }


    /**
     * 初始化pager
     *
     * @param pagers
     */
    public void initPager(List<BasePager> pagers) {
        if (pagers == null) return;
        else if (pagers.size() < 2)
            getViewDataBinding().indicator.setVisibility(View.GONE);

        //适配器初始化
        getViewDataBinding().container.removeAllViews();
        mPagerAdapter = new PagerAdapter(pagers);
        getViewDataBinding().indicator.setupWithViewPager(getViewDataBinding().container);//指示器绑定
        getViewDataBinding().container.setAdapter(mPagerAdapter);//适配器
        getViewDataBinding().container.setOffscreenPageLimit(8);//缓存页数，0无效

        //手动选择初始化页面
        getViewDataBinding().container.setCurrentItem(0);
        getViewDataBinding().container.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int nowPosition = -1;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position != nowPosition && positionOffset == 0 && positionOffsetPixels == 0) {
                    //调用生命周期
                    if (mPagerAdapter != null)
                        mPagerAdapter.getPager(position).onShow(position);
                    nowPosition = position;
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    @Override
    protected int upBarMenuID() {
        return getIntent().getIntExtra("menuId", -1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mPagerAdapter != null) {
            int position = getViewDataBinding().container.getCurrentItem();
            if (mPagerAdapter.getPager(position).onMenuItemClick(item.getItemId()))
                return true;
            else return super.onOptionsItemSelected(item);
        } else
            return super.onOptionsItemSelected(item);
    }

    @Override
    public void back() {
        if (mPagerAdapter != null) {
            int position = getViewDataBinding().container.getCurrentItem();
            if (!mPagerAdapter.getPager(position).back())
                super.back();
        } else
            super.back();
    }


    /**
     * 注册绑定rxbus
     */
    private void bindRxBus() {
        mRxBusClient = new RxBusClient(getClass().getName()) {
            @Override
            protected void onEvent(int type, String message, Object data) {
                PagerActivity.this.onEvent(type, message, data);
            }
        };
        //告诉别人我这里初始化了
        RxBus.get().post(new EventMessage(getClass().getName()
                + RxMsgParameters.ACTIVITY_LAUNCHED));
    }

    /**
     * 事件处理
     *
     * @param type    类型
     * @param message 信息
     * @param data    数据
     */
    public void onEvent(@EventMessage.EventType int type, String message, Object data) {
        if (type == EventMessage.EventType.ASSIGN
                && message.contains(getClass().getName())) {
            if (message.contains(RxMsgParameters.ACTIVITY_LAUNCH)) {//打开新页面
                if (data instanceof Intent) {
                    Intent intent = (Intent) data;
                    toActivity(intent);
                } else if (data instanceof PairEntity) {
                    PairEntity entity = (PairEntity) data;
                    toActivity((View) entity.getKey(), (Intent) entity.getValue());
                }
            } else if (message.contains(RxMsgParameters.ACTIVITY_FINISH)) {//关闭页面
                close();
            } else if (message.contains(RxMsgParameters.DATA_REFRESH)) {//刷新数据
                showLoadingView();
                //页面初始化
            } else if (message.contains(RxMsgParameters.Pager.PAGER_INIT)) {
                List<BasePager> listPagers = null;
                if (data instanceof List) {
                    listPagers = (List<BasePager>) data;
                } else if (data != null && data.getClass().isArray()) {
                    BasePager[] pagers = (BasePager[]) data;
                    //listPagers = new ArrayList<>(Arrays.asList(array));
                    listPagers = new ArrayList<>(pagers.length);
                    Collections.addAll(listPagers, pagers);
                    //java9:List.of(array)
                }
                //不直接调用init是为了防止数据丢失
                if (listPagers != null)
                    initPager(listPagers);
            }
        }
    }
}
