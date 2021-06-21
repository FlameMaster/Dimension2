package com.melvinhou.rxjava.fileloader.image;

import android.graphics.Bitmap;

import com.melvinhou.kami.model.BitmapCacheModel;
import com.melvinhou.kami.util.ImageUtils;
import com.melvinhou.rxjava.fileloader.DiskCacheObservable;

import java.io.FileInputStream;
import java.io.OutputStream;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2017/9/10 15:32
 * <p>
 * = 分 类 说 明：图片的本地缓存
 * ================================================
 */

public class ImageDiskCacheObservable extends DiskCacheObservable<BitmapCacheModel,Bitmap> {

    public ImageDiskCacheObservable(String cacheFile) {
        super(cacheFile);
    }

    @Override
    public BitmapCacheModel getDataFromCache(String key) {
        BitmapCacheModel cacheModel = new BitmapCacheModel(key);
        cacheModel.setCacheValue(getCache(key));
        return cacheModel;
    }

    @Override
    protected Bitmap decodeStream(FileInputStream fileInputStream) {
        Bitmap bitmap = ImageUtils.decodeBitmapFromStream(fileInputStream,-1,-1);
        return bitmap;
    }

    @Override
    public boolean keepCacheToStream(Bitmap cacheValue, OutputStream outputStream) {
        return cacheValue.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
    }
}
