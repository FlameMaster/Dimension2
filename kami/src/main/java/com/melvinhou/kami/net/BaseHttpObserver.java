package com.melvinhou.kami.net;

import android.accounts.NetworkErrorException;

import com.melvinhou.kami.model.BaseEntity;
import com.melvinhou.kami.util.FcUtils;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2017/9/3 15:45
 * <p>
 * = 分 类 说 明：网络请求的观察者
 * ================================================
 */

public abstract class BaseHttpObserver<D, E extends BaseEntity<D>> implements Observer<E> {

    /*用于解除订阅*/
    @Override
    public void onSubscribe(Disposable d) {
        onRequestStart();
    }

    /*观察者接收到通知,进行相关操作*/
    @Override
    public void onNext(E entity) {
        onRequestEnd();
        if (entity.isSuccess()) {
            try {
                onSuccees(entity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                onCodeError(entity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*事件队列异常*/
    @Override
    public void onError(Throwable e) {
        onRequestEnd();
        try {
            if (e instanceof ConnectException
                    || e instanceof TimeoutException//连接超时
                    || e instanceof NetworkErrorException//网络异常
                    || e instanceof UnknownHostException) {//找不到域名
                onFailure(e, true);
            } else {
                onFailure(e, false);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /*事件队列完结时调用该方法，onComplete() 和 onError() 二者也是互斥的，即在队列中调用了其中一个，就不应该再调用另一个*/
    @Override
    public void onComplete() {
    }

    /**
     * 返回成功
     *
     * @param entity
     * @throws Exception
     */
    protected abstract void onSuccees(E entity) throws Exception;

    /**
     * 返回成功了,但是code错误
     *
     * @param entity
     * @throws Exception
     */
    protected void onCodeError(E entity) {
        FcUtils.showToast(entity.getMessage());
        //添加一个code异常
        onFailure(new CodeException(entity.getCode(), entity.getMessage()), false);
    }

    /**
     * 返回失败
     *
     * @param e
     * @param isNetWorkError 是否是网络错误
     * @throws Exception
     */
    protected abstract void onFailure(Throwable e, boolean isNetWorkError);

    /*网络请求开始的操作*/
    protected void onRequestStart() {

    }

    /*网络请求结束的操作*/
    protected void onRequestEnd() {

    }

}