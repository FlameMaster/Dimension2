package com.melvinhou.rxjava.rxbus;

import android.text.TextUtils;
import android.util.Log;

import org.reactivestreams.Subscription;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
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

    private final static String TAG = RxBusClient.class.getName();
    //存储id
    private final static Map<String, Integer> client_ids = new ArrayMap<>();
    //设置id缓存表
    private static int cacheId = 1000;

    /**
     * 通过字符串获取一个id
     *
     * @param textId
     * @return
     */
    public static int getClientId(@NonNull String textId) {
        if (TextUtils.isEmpty(textId)) {
            throw new IllegalArgumentException(
                    "textId not be null");
        }
        if (client_ids.containsKey(textId)) {
            return client_ids.get(textId);
        }
        //存储id
        int id = cacheId++;
        client_ids.put(textId, id);
        return id;
    }

    /*订阅管理*/
    private Subscription mSubscription;
    /*指定对象的id*/
    private int mClientId;

    /*接收全局*/
    public RxBusClient() {
        this(RxBusMessage.OFFSCREEN_CLIENT_DEFAULT);
    }

    /*指定接收器*/
    /*事件注册*/
    public RxBusClient(@RxBusMessage.OffscreenClient int clientId) {
        if (mClientId < 1 && mClientId != RxBusMessage.OFFSCREEN_CLIENT_DEFAULT) {
            throw new IllegalArgumentException(
                    "client id must be OFFSCREEN_CLIENT_DEFAULT or a number > 0");
        }
        mClientId = clientId;
        bindingRxBus();
    }

    /*需要执行的方法*/
    protected abstract void onEvent(@NonNull String eventType, Object attach);

    protected void onGlobalEvent(@NonNull String eventType, Object attach) {
    }

    /*绑定一个接收器*/
    private void bindingRxBus() {
        //注册一个远程事件
        RxBus.instance()
//                .toFlowable(RxBusMessage.class)//用于指定类型，但是现在RxBus中固定了类型，所以不用
                .toFlowable()
                .map(message -> {
                    Log.e(TAG, "事件判断type：" + message.getType() + "/message：" + message.getType());
                    if (message.getClientId() == RxBusMessage.OFFSCREEN_CLIENT_DEFAULT//全局接收的类型
                            || message.getClientId() == mClientId)//指定接收器内容
                        return message;
                    return RxBusMessage.Builder
                            .instance(RxBusMessage.CommonType.DEFAULT)
                            .build();
                })
                .subscribe(new FlowableSubscriber<RxBusMessage>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        mSubscription = s;
                        //先接收一个事件
                        s.request(1);
                    }

                    @Override
                    public void onNext(RxBusMessage event) {
                        try {
                            if (RxBusMessage.CommonType.DEFAULT.equals(event.getType()))
                                Log.e(TAG, "RxBus: a default event");
                            else if (event.getClientId() == RxBusMessage.OFFSCREEN_CLIENT_DEFAULT)
                                onGlobalEvent(event.getType(), event.getAttach());
                            else if (event.getClientId() == mClientId)
                                onEvent(event.getType(), event.getAttach());
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                            e.printStackTrace();
                        } finally {
                            //执行完接收下一个
                            if (mSubscription != null) mSubscription.request(1);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e(TAG, "onError:" + t.getMessage());
                        t.printStackTrace();
                        //错误重新绑定
                        bindingRxBus();
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "onComplete:" + "你不可能看到我的");
                        /*结束*/
                        unBindingRxBus();
                    }
                });
    }

    public void cancel() {
        unBindingRxBus();
        mClientId = -10;
    }


    /*删除对他的使用*/
    private void unBindingRxBus() {
        if (mSubscription != null) mSubscription.cancel();
    }

}
