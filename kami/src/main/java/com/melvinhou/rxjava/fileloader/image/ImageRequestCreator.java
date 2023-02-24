package com.melvinhou.rxjava.fileloader.image;

import com.melvinhou.kami.io.FileUtils;
import com.melvinhou.rxjava.fileloader.RequestCreator;

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
 * = 分 类 说 明：图片的三级缓存管理员
 * ================================================
 */

public class ImageRequestCreator extends RequestCreator {
    @Override
    public ImageMemoryCacheObservable getMemoryCacheObservable() {
        return new ImageMemoryCacheObservable();
    }

    @Override
    public ImageDiskCacheObservable getDiskCacheObservable() {
        return new ImageDiskCacheObservable(FileUtils.TYPE_PATH_IMAGE);
    }

    @Override
    public ImageNetworkCacheObservable getNetworkCacheObservable() {
        return new ImageNetworkCacheObservable();
    }
}
