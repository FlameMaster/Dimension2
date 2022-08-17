package com.melvinhou.dimension2.media.video;

import android.text.TextUtils;

import com.jeffmony.videocache.VideoProxyCacheManager;
import com.jeffmony.videocache.listener.IVideoCacheListener;
import com.jeffmony.videocache.utils.LogUtils;
import com.jeffmony.videocache.utils.ProxyCacheUtils;
import com.jeffmony.videocache.utils.StorageUtils;
import com.melvinhou.kami.util.FcUtils;

import java.io.File;
import java.util.Map;

import androidx.annotation.NonNull;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2022/7/22 0022 17:15
 * <p>
 * = 分 类 说 明：JeffVideoCache的帮助类
 * ================================================
 */
public class LocalProxyVideoHelper {

    private static LocalProxyVideoHelper helper;


    public static LocalProxyVideoHelper getInstance() {
        if (helper == null)
            helper = new LocalProxyVideoHelper();
        return helper;
    }


    private LocalProxyVideoHelper() {
        //初始化缓存器
        File saveFile = StorageUtils.getVideoFileDir(FcUtils.getContext());
        if (!saveFile.exists()) {
            saveFile.mkdir();
        }
        VideoProxyCacheManager.Builder builder = new VideoProxyCacheManager.Builder().
                setFilePath(saveFile.getAbsolutePath()).    //缓存存储位置
                setConnTimeOut(60 * 1000).                  //网络连接超时
                setReadTimeOut(60 * 1000).                  //网络读超时
                setExpireTime(2 * 24 * 60 * 60 * 1000).     //2天的过期时间
                setMaxCacheSize(2 * 1024 * 1024 * 1024);    //2G的存储上限
        VideoProxyCacheManager.getInstance().initProxyConfig(builder.build());
    }


    private String mVideoUrl;


    public void startRequestVideoInfo(String videoUrl, @NonNull IVideoCacheListener listener) {
        mVideoUrl = videoUrl;
        //设置缓存监听
        if (listener != null)
            VideoProxyCacheManager.getInstance().addCacheListener(mVideoUrl, listener);
        //设置正在播放链接
        VideoProxyCacheManager.getInstance().setPlayingUrlMd5(ProxyCacheUtils.computeMD5(videoUrl));
        //发起缓存请求
        VideoProxyCacheManager.getInstance().startRequestVideoInfo(videoUrl, null, null);
    }

    public void pauseLocalProxyTask() {
        if (TextUtils.isEmpty(mVideoUrl))return;
        //暂停缓存任务
        VideoProxyCacheManager.getInstance().pauseCacheTask(mVideoUrl);
    }

    public void resumeLocalProxyTask() {
        if (TextUtils.isEmpty(mVideoUrl))return;
        //恢复缓存任务
        VideoProxyCacheManager.getInstance().resumeCacheTask(mVideoUrl);
    }

    public void seekToCachePosition(float percent) {
        if (TextUtils.isEmpty(mVideoUrl))return;
        VideoProxyCacheManager.getInstance().seekToCacheTaskFromClient(mVideoUrl, percent);
    }

    public void releaseLocalProxyResources() {
        if (TextUtils.isEmpty(mVideoUrl))return;
        // 释放缓存任务
        VideoProxyCacheManager.getInstance().stopCacheTask(mVideoUrl);   //停止视频缓存任务
        VideoProxyCacheManager.getInstance().releaseProxyReleases(mVideoUrl);
    }
}
