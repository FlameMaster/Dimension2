package com.melvinhou.rxjava.fileloader;

import com.melvinhou.kami.util.IOUtils;

import androidx.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2017/9/9 20:58
 * <p>
 * = 分 类 说 明：缓存存取基类
 * ================================================
 */
public abstract class BaseCacheObservable<T,I> {

    /*获取缓存数据*/
    public Observable<T> getCacheObservable(final I key) {
        return Observable.create(new ObservableOnSubscribe<T>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<T> e){
                if (!e.isDisposed()) {
                    T data = getDataFromCache(key);
                    e.onNext(data);
                    e.onComplete();
                }
            }
        }).compose(IOUtils.<T>setThread());
    }

    /**
     * 取出缓存数据
     * @param key
     * @return
     */
    public abstract T getDataFromCache(I key);

    /**
     * 缓存数据
     * @param image
     */
    public abstract void putDataToCache(T image);
}
