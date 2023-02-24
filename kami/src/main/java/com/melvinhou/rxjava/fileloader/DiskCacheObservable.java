package com.melvinhou.rxjava.fileloader;

import com.jakewharton.disklrucache.DiskLruCache;
import com.melvinhou.kami.util.AppUtils;
import com.melvinhou.kami.io.FileUtils;
import com.melvinhou.kami.io.IOUtils;
import com.melvinhou.kami.util.StringUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import androidx.annotation.NonNull;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

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
 * = 分 类 说 明：三级缓存-本地缓存
 * ================================================
 */

public abstract class DiskCacheObservable<T extends BaseCacheModel<D>, D> extends BaseCacheObservable<T,String> {

    /*本地缓存管理*/
    private DiskLruCache mDiskLruCache;
    /*设置本地缓存大小*/
    private final int maxSize = 300 * 1024 * 1024;

    public DiskCacheObservable(String cacheFile) {
        initDiskLruCache(cacheFile);
    }

    @Override
    public void putDataToCache(final T t) {
        //由于网络读取需要在子线程中执行
        Observable.create(new ObservableOnSubscribe<T>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<T> e) throws Exception {
                putDataToDiskLruCache(t.getCacheKey(), t.getCacheValue());
            }
        }).compose(IOUtils.<T>setThread())
        .subscribe();
    }

    public void initDiskLruCache(String cacheFile) {
        File cacheDir = FileUtils.getDiskCacheDir(cacheFile);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        int versionCode = AppUtils.getVersion();
        try {
            //这里需要注意参数二：缓存版本号，只要不同版本号，缓存都会被清除，重新使用新的
            mDiskLruCache = DiskLruCache.open(cacheDir, versionCode, 1, maxSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取文件缓存
     *
     * @param cacheKey
     * @return
     */
    public D getCache(String cacheKey) {
        D d = null;
        FileDescriptor fileDescriptor = null;
        FileInputStream fileInputStream = null;
        try {
            final String key = StringUtils.md5(cacheKey);
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
            if (snapshot != null) {
                fileInputStream = (FileInputStream) snapshot.getInputStream(0);
                fileDescriptor = fileInputStream.getFD();
            }
            if (fileDescriptor != null) {
                d = decodeStream(fileInputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(fileInputStream);
            return d;
        }
    }

    /**
     * 根据获取的输入流获取数据
     * @param fileInputStream
     * @return
     */
    protected abstract D decodeStream(FileInputStream fileInputStream);

    /**
     * 缓存文件数据
     *
     * @param cachekey   缓存的键
     * @param cacheValue 缓存的值
     */
    private void putDataToDiskLruCache(String cachekey, D cacheValue) {
        try {
            String key = StringUtils.md5(cachekey);
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);
            if (editor != null) {
                OutputStream outputStream = editor.newOutputStream(0);
                //保存到本地
                boolean isSuccess = keepCacheToStream(cacheValue, outputStream);
                if (isSuccess) {
                    editor.commit();//保存成功
                } else {
                    editor.abort();//保存失败
                }
                mDiskLruCache.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 把缓存保存到本地
     *
     * @param cacheValue
     * @param outputStream
     * @return
     */
    public abstract boolean keepCacheToStream(D cacheValue, OutputStream outputStream);

}
