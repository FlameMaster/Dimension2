package com.melvinhou.rxjava.fileloader;

import com.melvinhou.kami.model.BaseCacheModel;
import com.melvinhou.kami.util.IOUtils;

import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
 * = 分 类 说 明：三级缓存-网络缓存
 * ================================================
 */

public abstract class NetworkCacheObservable<T extends BaseCacheModel<D>, D,I> extends BaseCacheObservable<T,I> {

    /**
     * 获取缓存
     *
     * @param url
     * @return
     */
    public D getCache(String url) {
        return downloadCache(url);
    }

    @Override
    public void putDataToCache(T image) {

    }

    /**
     * 下载文件
     *
     * @param url
     * @return
     */
    public D downloadCache(String url) {
        D d = null;
        InputStream inputStream = null;
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().get()
                    .url(url)
                    .build();
            Response response =
                    client.newCall(request).execute();
            d = input2Cache(response.body().byteStream());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) IOUtils.close(inputStream);
            return d;
        }
    }

    /*将下载的流转成缓存*/
    public abstract D input2Cache(InputStream inputStream);
}
