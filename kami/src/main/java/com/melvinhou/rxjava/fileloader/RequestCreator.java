package com.melvinhou.rxjava.fileloader;


import com.melvinhou.kami.model.BaseCacheModel;

import androidx.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2017/9/9 21:08
 * <p>
 * = 分 类 说 明：三级缓存管理员
 * ================================================
 */

public abstract class RequestCreator<D extends BaseCacheModel, N,
        MC extends MemoryCacheObservable, DC extends DiskCacheObservable
        , NC extends NetworkCacheObservable> {

    /*内存缓存*/
    public MC memoryCacheObservable;
    /*本地缓存*/
    public DC diskCacheObservable;
    /*网络缓存*/
    public NC networkCacheObservable;


    public abstract MC getMemoryCacheObservable();

    public abstract DC getDiskCacheObservable();

    public abstract NC getNetworkCacheObservable();

    public RequestCreator() {
        memoryCacheObservable = getMemoryCacheObservable();
        diskCacheObservable = getDiskCacheObservable();
        networkCacheObservable = getNetworkCacheObservable();
    }

    /*内存中获取*/
    public Observable<D> getFromMemory(String key) {
        return memoryCacheObservable.getCacheObservable(key)
                .filter(new Predicate<D>() {
                    @Override
                    public boolean test(@NonNull D date) {
                        return date.getCacheValue() != null;
                    }
                });
    }

    /*本地获取*/
    public Observable<D> getFromDisk(String key) {
        return diskCacheObservable
                .getCacheObservable(key)
                .filter(new Predicate<D>() {
                    @Override
                    public boolean test(@NonNull D date) {
                        return date.getCacheValue() != null;
                    }
                }).doOnNext(new Consumer<D>() {
                    @Override
                    public void accept(@NonNull D date) {
                        //缓存内存
                        memoryCacheObservable.putDataToCache(date);
                    }
                });
    }

    /*从网络加载*/
    public Observable<D> getFromNetwork(N n) {
        return networkCacheObservable
                .getCacheObservable(n)
                .filter(new Predicate<D>() {
                    @Override
                    public boolean test(@NonNull D date) {
                        return date.getCacheValue() != null;
                    }
                })
                .doOnNext(new Consumer<D>() {
                    @Override
                    public void accept(@NonNull D date) {
                        //缓存文件和内存
                        diskCacheObservable.putDataToCache(date);
                        memoryCacheObservable.putDataToCache(date);
                    }
                });
    }
}
