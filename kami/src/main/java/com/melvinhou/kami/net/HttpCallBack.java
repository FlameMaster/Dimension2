package com.melvinhou.kami.net;

import android.accounts.NetworkErrorException;
import android.util.Log;

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
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/2/22 0022 11:18
 * <p>
 * = 分 类 说 明：网络请求返回
 * ================================================
 */
public abstract class HttpCallBack<D> implements Observer<BaseEntity<D>> {

    private static final String TAG = HttpCallBack.class.getSimpleName();

    /**用于解除订阅*/
    @Override
    public void onSubscribe(Disposable d) {
        onRequestStart();
    }

    @Override
    public void onComplete() {
        onRequestEnd();
    }

    /**
     * 事件队列
     */
    @Override
    public void onNext(BaseEntity<D> entity) {
        if (entity.isSuccess()) {
            try {
                onSuccees(entity.getData());
            } catch (Exception e) {
                Log.e(TAG, "数据解析：" + e.getMessage());
                e.printStackTrace();
                onFailure(ResultState.CONVERT_ERROR, "数据解析错误");
            }
        } else {
            Log.e(TAG, "状态码错误：" + entity.getCode() + "/" + entity.getMessage());
            onFailure(ResultState.FAILED, entity.getMessage());
        }
    }

    /**
     * 事件队列异常
     */
    @Override
    public void onError(Throwable throwable) {
        try {
            Log.e(TAG, "网络请求：" + throwable.getMessage());
            if (throwable instanceof ConnectException
                    || throwable instanceof TimeoutException//连接超时
                    || throwable instanceof NetworkErrorException//网络异常
                    || throwable instanceof UnknownHostException) {//找不到域名
                onFailure(ResultState.NETWORK_ERROR, "网络错误，请稍后重试");
            } else {
                onFailure(ResultState.FAILED, "请求异常");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 返回成功
     *
     * @throws Exception
     */
    protected abstract void onSuccees(D data) throws Exception;

    /**
     * 返回失败
     *
     * @param code
     * @param message
     * @throws Exception
     */
    protected void onFailure(@ResultState int state, String message) {
        if (state == ResultState.RELOGIN) {
            FcUtils.showToast(message);
        } else if (state != ResultState.CONVERT_ERROR) {
            FcUtils.showToast(message);
        }
    }


    /**
     * 网络请求开始的操作
     */
    protected void onRequestStart() {

    }

    /**
     * 网络请求结束的操作
     */
    protected void onRequestEnd() {

    }
}
