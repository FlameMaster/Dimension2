package com.melvinhou.kami.mvvm;

import android.app.Application;

import com.melvinhou.kami.net.BaseEntity;
import com.melvinhou.kami.net.HttpCallBack;
import com.melvinhou.kami.net.RequestCallback;
import com.melvinhou.kami.net.RequestState;
import com.melvinhou.kami.net.ResultState;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

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
    }


//***********************************状态管理*********************************************//

    //网络访问状态
    protected MutableLiveData<Integer> state = new MutableLiveData<>();
    @RequestState
    public int getState() {
        return state.getValue();
    }
    public void updateState(@RequestState int state){
        this.state.setValue(state);
    }


//***********************************网络请求*********************************************//

    //管理进程的
    private CompositeDisposable mDisposable = new CompositeDisposable();

    //注册初始化
    public void register() {
        mDisposable = new CompositeDisposable();
        updateState(RequestState.READY);
    }

    //注销
    public void cancel() {
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
    public <T,E extends BaseEntity<T>> void requestData(@NonNull Observable<E> observable, RequestCallback<T> callback) {
        if (mDisposable == null) register();
        updateState(RequestState.RUNNING);
        observable.subscribe(new HttpCallBack<T>() {
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
                mDisposable.add(d);
            }
        });
    }
}
