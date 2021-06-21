package com.melvinhou.kami.mvp;

import android.content.Intent;

import com.melvinhou.kami.model.EventMessage;
import com.melvinhou.kami.net.EmptyState;
import com.melvinhou.rxjava.RxBus;
import com.melvinhou.rxjava.RxBusClient;
import com.melvinhou.rxjava.RxMsgParameters;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2018/12/8 14:37
 * <p>
 * = 分 类 说 明：实现mvp-p中需要实现的方法
 * ============================================================
 */
public abstract class BasePresenter<V extends MvpView,M extends MvpModel> implements LifecycleObserver, MvpPresenter<V,M> {

    //mvp
    private V mView;
    private M mModel;

    /*RxBus的接收器*/
    private RxBusClient mRxBusClient;

    public BasePresenter(V view, M model) {
        mView = view;
        mModel = model;
        model.setPresenter(this);
    }

    @Override
    public V getView() {
        return mView;
    }

    @Override
    public M getModel() {
        return mModel;
    }

    @Override
    public LifecycleObserver getLifecycleObserver() {
        return this;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    protected void onCreate() {
        bindRxBus();
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    protected void onResume() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    protected void onStart() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    protected void onStop() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    protected void onPause() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        if (mRxBusClient != null) {
            mRxBusClient.unregister();
            mRxBusClient = null;
        }
    }









    /*注册绑定rxbus*/
    private void bindRxBus() {
        mRxBusClient = new RxBusClient(getView().getClass().getName()) {
            @Override
            protected void onEvent(int type, String message, Object data) {
                if (type == EventMessage.EventType.ALL) {
                    BasePresenter.this.onEvent(type, message, data);
                } else if (type == EventMessage.EventType.ASSIGN
                        && message.contains(getView().getClass().getName())) {
                    BasePresenter.this.onEvent(type, message, data);
                }
            }
        };
        //告诉别人我这里初始化了
        RxBus.get().post(new EventMessage(getView().getClass().getName()
                + RxMsgParameters.ACTIVITY_LAUNCHED));
    }

    /**
     * 全局消息处理
     *
     * @param type
     * @param message
     * @param data
     */
    private void onGlobalEvent(@EventMessage.EventType int type, String message, Object data) {
        if (message.contains(RxMsgParameters.NETWORK_CHANGE_LINK))//网络状态改变
            changeNetworkState((boolean) data);
    }

    /**
     * rxbus消息传递接收处理
     *
     * @param type
     * @param message
     * @param data
     */
    public void onEvent(@EventMessage.EventType int type, String message, Object data) {
        if (type == EventMessage.EventType.ASSIGN
                && message.contains(getView().getClass().getName())) {

            if (message.contains(RxMsgParameters.ACTIVITY_LAUNCH)
                    && data instanceof Intent) {//打开新页面
                Intent intent = (Intent) data;
                getView().startActivity(intent);
            } else if (message.contains(RxMsgParameters.ACTIVITY_FINISH)) {//关闭页面
                getView().close();
            } else if (message.contains(RxMsgParameters.DATA_REFRESH))//刷新数据
                refreshData();
        }
    }






    /**
     * 刷新数据
     */
    protected void refreshData() {

    }

    /**
     * 网络状态改变
     *
     * @param isLink
     */
    public void changeNetworkState(boolean isLink) {

    }

    @Override
    public void startLoading(String message) {
        getView().showLoadingView(true);
        getView().changeLoadingState(EmptyState.PROGRESS, message);
    }


    @Override
    public void endLoading(@EmptyState int code,String message) {
        getView().changeLoadingState(code, null);//加载改变状态
        if (code == EmptyState.NORMAL)
            getView().hideLoadingView();//正常时隐藏加载
    }
}
