package com.melvinhou.rxjava;

import android.util.Log;

import com.melvinhou.kami.model.EventMessage;
import com.melvinhou.kami.util.StringUtils;

import org.reactivestreams.Subscription;

import io.reactivex.FlowableSubscriber;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2019/1/10 11:12
 * <p>
 * = 分 类 说 明：rxbus使用的帮助类:接收装置
 * ============================================================
 */
public abstract class RxBusClient {

    /*订阅管理*/
    private Subscription mSubscription;
    /*指定内容*/
    private String mAssignName;
    /*指定事件*/
    private String mEvent;

    /*接收全局*/
    public RxBusClient() {
        this(null);
    }

    /*指定内容*/
    public RxBusClient(String assignName) {
        this(assignName, null);
    }

    /*专属事件注册*/
    public RxBusClient(String assignName, String event) {
        mAssignName = assignName;
        mEvent = assignName;
        bindingRxBus();
    }

    /*需要执行的方法*/
    protected abstract void onEvent(int type, String message, Object data);

    /*绑定一个接收器*/
    private void bindingRxBus() {
        //注册一个远程事件
        RxBus.get().toFlowable(EventMessage.class)
                .map(message -> {
                    Log.e("RxBus事件判断", "type：" + message.getType() + "/message：" + message.getMessage());
                    //全局接收的类型
                    if (message.getType() == EventMessage.EventType.ALL)
                        return message;
                        //判断是否包含指定内容
                    else if (message.getType() == EventMessage.EventType.ASSIGN
                            && StringUtils.noNull(mAssignName)
                            && message.getMessage().contains(mAssignName))
                        return message;
                    return new EventMessage("null");
                })
                .subscribe(new FlowableSubscriber<EventMessage>() {

                    @Override
                    public void onSubscribe(Subscription s) {
                        mSubscription = s;
                        //先接收一个事件
                        s.request(1);
                    }

                    @Override
                    public void onNext(EventMessage event) {
                        try {
                            if (StringUtils.noNull(event.getMessage()))
                                onEvent(event.getType(), event.getMessage(), event.getData());
                        } catch (Exception e) {
                            Log.e("RxBusNext", e.getMessage());
                            e.printStackTrace();
                        } finally {
                            //执行完接收下一个
                            if (mSubscription != null) mSubscription.request(1);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e("RxBusError", t.getMessage());
                        t.printStackTrace();
                        //错误重新绑定
                        bindingRxBus();
                    }

                    @Override
                    public void onComplete() {
                        Log.e("RxBusComplete", "你不可能看到我的");
                        /*结束*/
                        unBindingRxBus();
                    }
                });
    }

    public void unregister(){
        unBindingRxBus();
        mAssignName=null;
        mEvent=null;
    }


    /*删除对他的使用*/
    private void unBindingRxBus() {
        if (mSubscription != null) mSubscription.cancel();
    }
}
