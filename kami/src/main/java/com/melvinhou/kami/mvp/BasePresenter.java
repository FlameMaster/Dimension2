package com.melvinhou.kami.mvp;

import android.content.Intent;

import com.melvinhou.kami.mvp.interfaces.MvpModel;
import com.melvinhou.kami.mvp.interfaces.MvpPresenter;
import com.melvinhou.kami.mvp.interfaces.MvpView;
import com.melvinhou.kami.net.RequestState;
import com.melvinhou.kami.net.ResultState;
import com.melvinhou.rxjava.rxbus.RxBus;
import com.melvinhou.rxjava.rxbus.RxBusClient;
import com.melvinhou.rxjava.rxbus.RxBusMessage;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ViewModelProvider;

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
public abstract class BasePresenter<V extends MvpView, M extends MvpModel> implements LifecycleEventObserver, MvpPresenter<V, M> {

    //mvp
    private V mView;
    private M mModel;

    /*RxBus的接收器*/
    private RxBusClient mRxBusClient;

    public BasePresenter(V view) {
        mView = view;
        mModel = openModel(view.getViewModelProvider());
        mModel.setPresenter(this);
    }

    protected abstract M openModel(ViewModelProvider provider);

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


    /**
     * 刷新数据
     */
    protected void refreshData(boolean isActive) {

    }

    /**
     * 网络状态改变
     *
     * @param isLink
     */
    public void changeNetworkState(boolean isLink) {

    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        switch (event) {
            case ON_CREATE:
                onCreate();
                if (openRxBus()) bindRxBus();
                break;
            case ON_START:
                break;
            case ON_RESUME:
                onResume();
                refreshData(false);
                break;
            case ON_PAUSE:
                onPause();
                break;
            case ON_STOP:
                break;
            case ON_DESTROY:
                if (mRxBusClient != null) {
                    mRxBusClient.cancel();
                    mRxBusClient = null;
                }
                onClose();
                break;
        }
    }

    protected  void onCreate(){}
    protected  void onResume(){}
    protected  void onPause(){}
    protected  void onClose(){}



//=================================RxBus=========================================//

    /**
     * 是否启用RxBus
     *
     * @return
     */
    protected boolean openRxBus() {
        return true;
    }


    /**
     * 注册绑定rxbus
     */
    private void bindRxBus() {
        mRxBusClient = new RxBusClient(getRxBusClientId()) {
            @Override
            protected void onEvent(@NonNull String eventType, Object attach) {
                BasePresenter.this.onEvent(eventType, attach);
            }

            @Override
            protected void onGlobalEvent(@NonNull String eventType, Object attach) {
                BasePresenter.this.onGlobalEvent(eventType, attach);
            }
        };
        //告诉别人我这里初始化了
        RxBus.instance().post(RxBusMessage.Builder
                .instance(RxBusMessage.CommonType.ACTIVITY_LAUNCHED)
                .client(RxBusMessage.OFFSCREEN_CLIENT_DEFAULT)
                .build());
    }

    /**
     * rxbus的客户端id，默认使用全局的
     *
     * @return
     */
    protected int getRxBusClientId() {
        return RxBusClient.getClientId(getClass().getName());
    }

    /**
     * 全局消息处理
     *
     * @param type
     * @param attach
     */
    private void onGlobalEvent(@NonNull String type, Object attach) {
        if (RxBusMessage.CommonType.NETWORK_CHANGE_LINK.equals(type)
                && attach instanceof Boolean)//网络状态改变
            changeNetworkState((boolean) attach);
    }

    /**
     * rxbus消息传递接收处理
     *
     * @param type
     * @param attach
     */
    public void onEvent(@NonNull String type, Object attach) {
        //打开新页面
        if (RxBusMessage.CommonType.ACTIVITY_LAUNCH.equals(type)
                && attach instanceof Intent) {
            Intent intent = (Intent) attach;
            getView().toActivity(intent);
        }
        //关闭页面
        if (RxBusMessage.CommonType.ACTIVITY_FINISH.equals(type)) {
            getView().close();
        }
        //刷新数据
        if (RxBusMessage.CommonType.DATA_REFRESH.equals(type)) {
            refreshData(true);
        }
    }
}
