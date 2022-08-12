package com.melvinhou.kami.mvvm;

import android.app.Application;

import com.melvinhou.kami.util.IOUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
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
 * = 分 类 说 明：
 * ================================================
 */
public class BaseModel extends AndroidViewModel {
    public BaseModel(@NonNull Application application) {
        super(application);
    }

    MutableLiveData<Boolean> isRequest = new MutableLiveData();
    //管理进程的
    private CompositeDisposable mDisposable = new CompositeDisposable();


    public void register() {
        mDisposable = new CompositeDisposable();
    }

    public void unRegister() {
        if (mDisposable != null) {
            mDisposable.clear();
//        mDisposable?.dispose()
            mDisposable = null;
        }
    }

    public void requestData(Observable observable) {
        if (mDisposable == null) register();
        isRequest.postValue(true);
        observable.subscribe(new Observer() {
            @Override
            public void onSubscribe(Disposable disposable) {
                mDisposable.add(disposable);
            }

            @Override
            public void onNext(Object o) {
                try {
//                    callback.onSuceess(t)
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                try {
//                    callback.onError()
//                    if (!StringUtils.isSpace(e?.message)) {
//                        LogUtil.e(e?.message)
//                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onComplete() {
                isRequest.postValue(false);
            }
        });
    }
}
