package com.melvinhou.rxjava.rxbus;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2018/12/3 18:34
 * <p>
 * = 分 类 说 明：支持背压的通讯器
 * ============================================================
 */
public class RxBus {

    //唯一的实例
    private static final RxBus BUS = new RxBus();

    public static RxBus instance() {
        return BUS;
    }



    private final FlowableProcessor<RxBusMessage> mBus;

//    AsyncProcessor
//    不论何时订阅，都只发射最后一个数据，如果因为异常而终止，不会释放任何数据，但是会向 Observer 传递一个异常通知。
//
//    BehaviorProcessor
//    发射订阅之前的一个数据和订阅之后的全部数据。如果订阅之前没有值，可以使用默认值。
//
//    PublishProcessor
//    从哪里订阅就从哪里发射数据。
//
//    ReplayProcessor
//    无论何时订阅，都发射所有的数据。
//
//    SerializedProcessor
//    其它 Processor 不要在多线程上发射数据，如果确实要在多线程上使用，用这个 Processor 封装，可以保证在一个时刻只在一个线程上执行。
//
//    UnicastProcessor


    //    只能有一个观察者。
    private RxBus() {
        // toSerialized method made bus thread safe
        mBus = PublishProcessor.<RxBusMessage>create().toSerialized();
    }

    public void post(RxBusMessage message) {
        mBus.onNext(message);
    }

    public <T> Flowable<T> toFlowable(Class<T> tClass) {
        return mBus.ofType(tClass);
    }

    public Flowable<RxBusMessage> toFlowable() {
        return mBus;
    }

    public boolean hasSubscribers() {
        return mBus.hasSubscribers();
    }
}
