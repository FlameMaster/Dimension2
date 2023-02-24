package com.melvinhou.rxjava.fileloader;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2017/9/10 15:07
 * <p>
 * = 分 类 说 明：缓存的基类
 * ================================================
 */

public abstract class BaseCacheModel<T> {

    /*缓存的key*/
    private String key;
    /*缓存的值*/
    private T value;

    protected BaseCacheModel(String key){
        this.key = key;
    }

    /*要缓存的键*/
    public String getCacheKey(){
        return key;
    }
    /*要缓存的值*/
    public T getCacheValue(){
        return value;
    }
    /*设置缓存*/
    public void setCacheValue(T value){
        this.value= value;
    }

}
