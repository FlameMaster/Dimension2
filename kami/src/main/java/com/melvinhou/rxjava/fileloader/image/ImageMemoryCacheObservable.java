package com.melvinhou.rxjava.fileloader.image;

import android.graphics.Bitmap;

import com.melvinhou.rxjava.fileloader.MemoryCacheObservable;


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2017/9/10 16:29
 * <p>
 * = 分 类 说 明：图片的内存缓存
 * ================================================
 */

public class ImageMemoryCacheObservable extends MemoryCacheObservable<BitmapCacheModel,Bitmap> {
    @Override
    protected int getSize(Bitmap bitmap) {
            return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
    }

    @Override
    public BitmapCacheModel getDataFromCache(String key) {
        BitmapCacheModel cacheModel = new BitmapCacheModel(key);
        cacheModel.setCacheValue(getCache(key));
        return cacheModel;
    }
}
