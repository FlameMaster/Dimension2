package com.melvinhou.dimension2.pager;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import com.melvinhou.kami.net.RequestState;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.rxjava.rxbus.RxBus;
import com.melvinhou.rxjava.rxbus.RxBusMessage;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2019/12/5 17:33
 * <p>
 * = 分 类 说 明：pager的基类
 * ============================================================
 */
public abstract class BasePager<T extends ViewDataBinding> {

    //当前页码
    private int position;
    //标题
    private String title;
    //附件
    private Object attach;
    //绑定数据
    private T binding;


/////////////////////////////////////////初始化/////////////////////////////////////////////////////

    /**
     * 初始化页面
     * @param position
     */
    public void onCreate(int position) {
        this.position = position;
        initView();
        initData();
    }

    /**
     * 初始化布局
     */
    private void initView() {
        binding = DataBindingUtil.inflate(
                LayoutInflater.from(FcUtils.getContext()),
                getLayoutID(),
                null, false);
    }

    /**
     * 初始化数据
     */
    protected void initData(){

    }

    /**
     *  显示，每次显示都会走这个方法
     */
    public void onShow(int position){
        loadData();
    }

/////////////////////////////////////////工具/////////////////////////////////////////////////////

    /**
     * 获取页面id
     * @return
     */
    public abstract int getLayoutID();

    /**
     * 获取当前页码
     * @return
     */
    public int getPosition() {
        return position;
    }

    /**
     * 页面标题
     * @return
     */
    public String getTitle() {
        return title;
    }

    public BasePager setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * 附件
     * @return
     */
    public Object getAttach() {
        return attach;
    }

    public BasePager setAttach(Object attach) {
        this.attach = attach;
        return this;
    }

    /**
     * 获取绑定器
     * @return
     */
    public T getBinding() {
        return binding;
    }

    /**
     * 获取顶级布局
     * @return
     */
    public View getRootView() {
        return getBinding().getRoot();
    }

    /**
     * 发送跳转页面的消息
     *
     * @param intent
     */
    public void toActivity(Intent intent) {

        RxBus.instance().post(RxBusMessage.Builder
                .instance(RxBusMessage.CommonType.ACTIVITY_LAUNCH)
                .client(PagerActivity.class.getName())
                .build());
    }


/////////////////////////////////////////页面逻辑/////////////////////////////////////////////////////


    /**
     * 加载数据
     */
    public void loadData() {
        updateRequestState(RequestState.RUNNING);

    }

    /**
     * 返回键拦截
     *
     * @return 是否拦截
     */
    public boolean backward() {
        return false;
    }

    /**
     * 更新页面状态
     * @param state 状态码
     */
    public abstract void updateRequestState(@RequestState int state);

    /**
     * 页面刷新
     *
     * @param isShowLoadingView 是否显示进度条
     */
    public void onRefresh(boolean isShowLoadingView) {
        refreshData(isShowLoadingView);
    }

    /**
     * 刷新当前页面
     * @param isShowLoad 是否显示进度条
     */
    public abstract void refreshData(boolean isShowLoad);

/////////////////////////////////////////页面逻辑/////////////////////////////////////////////////////


    /**
     * appbar的条目点击事件
     * @param itemId
     */
    public  boolean onMenuItemClick(int itemId){
        return false;
    }

}
