package com.melvinhou.medialibrary.video.ijk;

import com.danikula.videocache.HttpProxyCacheServer;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.StringUtils;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2022/7/21 0021 13:43
 * <p>
 * = 分 类 说 明：视频缓存帮助类
 * ================================================
 */
public class LocalVideoCacheHelper {


    private static LocalVideoCacheHelper helper;

    public static HttpProxyCacheServer getProxy() {
        return getInstance().proxy == null ? (getInstance().proxy = getInstance().newProxy()) : getInstance().proxy;
    }

    public static LocalVideoCacheHelper getInstance() {
        if (helper == null)
            helper = new LocalVideoCacheHelper();
        return helper;
    }


    //视频缓存类，保证唯一性
    private HttpProxyCacheServer proxy;

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer.Builder(FcUtils.getContext())
                .maxCacheSize(512 * 1024)// 缓存文件大小，默认值是512MB
//                .maxCacheFilesCount(20)//限制缓存中的文件总数
//                .diskUsage(new DiskUsage(){})//本地缓存
                .cacheDirectory(FcUtils.getContext().getExternalCacheDir())//缓存目录
                .fileNameGenerator(url -> StringUtils.md5(url) + ".mp4")//缓存的名称
                .build();
    }

}
