package com.melvinhou.rxjava.fileloader.image;

import android.graphics.Bitmap;

import com.melvinhou.kami.util.ImageUtils;
import com.melvinhou.rxjava.fileloader.NetworkCacheObservable;

import java.io.InputStream;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2017/9/10 16:42
 * <p>
 * = 分 类 说 明：网络图片的请求类
 * ================================================
 */

public class ImageNetworkCacheObservable extends NetworkCacheObservable<BitmapCacheModel,Bitmap,ImageParameter> {

    @Override
    public BitmapCacheModel getDataFromCache(ImageParameter imageParameter) {
        String key= imageParameter.getCacheKey();
        BitmapCacheModel cacheModel = new BitmapCacheModel(key);
        cacheModel.setCacheValue(getCache(imageParameter.getUrl()));
        return cacheModel;
    }

    @Override
    public Bitmap input2Cache(InputStream inputStream) {
        return ImageUtils.decodeBitmapFromStream(inputStream,-1,-1);
    }
}
