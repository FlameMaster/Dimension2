package com.melvinhou.rxjava.fileloader;

import android.util.LruCache;

import com.melvinhou.kami.model.BaseCacheModel;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2017/9/9 21:09
 * <p>
 * = 分 类 说 明：三级缓存-内存缓存
 * ================================================
 */

public abstract class MemoryCacheObservable<T extends BaseCacheModel<D>,D> extends BaseCacheObservable<T,String> {

    /*最大缓存量*/
    private int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    /*缓存大小*/
    private int cacheSize = maxMemory / 4;


    private LruCache<String, D> mLruCache = new LruCache<String, D>(cacheSize) {
        @Override
        protected int sizeOf(String key, D d) {
            return getSize(d);
        }
    };

    public D getCache(String url){
        return mLruCache.get(url);
    }

    @Override
    public void putDataToCache(T t) {
        mLruCache.put(t.getCacheKey(), t.getCacheValue());
    }


    /*获取要存储的大小*/
    protected abstract int getSize(D d);
}
