package com.melvinhou.kami.mvvm;

import android.app.Application;
import android.util.Log;

import com.melvinhou.kami.bean.PageInfo;
import com.melvinhou.kami.net.BaseEntity;
import com.melvinhou.kami.net.HttpCallBack;
import com.melvinhou.kami.net.RequestCallback;
import com.melvinhou.kami.net.RequestState;
import com.melvinhou.kami.net.ResultState;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2022/8/11 0011 15:30
 * <p>
 * = 分 类 说 明：mvvm-vm
 * ================================================
 */
public class BaseViewModel extends AndroidViewModel {
    public BaseViewModel(@NonNull Application application) {
        super(application);
        register();
    }

//***********************************状态管理*********************************************//

    //网络访问状态
    protected MutableLiveData<Integer> state = new MutableLiveData<>();
    //提交状态
    public MutableLiveData<Boolean> isRequest = new MutableLiveData<>(false);

    @RequestState
    public int getState() {
        return state.getValue();
    }

    public void updateState(@RequestState int state) {
        this.state.setValue(state);
        isRequest.postValue(state == RequestState.RUNNING);
    }


//***********************************网络请求*********************************************//

    //管理进程的
    private CompositeDisposable mDisposable = new CompositeDisposable();

    //添加进管理
    protected void addDisposable(Disposable disposable){
        mDisposable.add(disposable);
    }

    //注册初始化
    public void register() {
        mDisposable = new CompositeDisposable();
        updateState(RequestState.READY);
    }


    @Override
    protected void onCleared() {
        //注销
        if (mDisposable != null) {
//            mDisposable.dispose();
            mDisposable.clear();
            mDisposable = null;
        }

    }

    /**
     * 网络请求
     *
     * @param observable 请求数据
     * @param callback   回调
     * @param <T>        返回数据的类型
     */
    public <T, E extends BaseEntity<T>> void requestData(@NonNull Observable<E> observable, RequestCallback<T> callback) {
        if (mDisposable == null) register();
        updateState(RequestState.RUNNING);
        observable
                .compose(superCall())
                .subscribe(new HttpCallBack<T>() {
                    @Override
                    protected void onSuccees(T data) {
                        updateState(ResultState.SUCCESS);
                        callback.onSuceess(data);
                    }

                    @Override
                    protected void onFailure(@ResultState int state, String message) {
                        updateState(state);
                        callback.onFailure(state, message);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                        super.onSubscribe(d);
                        addDisposable(d);
                    }
                });
    }

    /**
     * 返回参数拦截
     * @return
     * @param <T>
     */
    private <T extends BaseEntity> ObservableTransformer<T, T> superCall() {
        return upstream -> upstream
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(entity -> {
                    Log.w("BaseViewModel——",
                            "返回数据：code=" + entity.getCode() + "\ndata=" + entity.getData());
                    return entity;
                });
    }
}
