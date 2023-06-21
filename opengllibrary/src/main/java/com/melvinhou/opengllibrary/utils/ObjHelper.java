package com.melvinhou.opengllibrary.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.util.LruCache;

import com.melvinhou.kami.util.FcUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import de.javagl.obj.Mtl;
import de.javagl.obj.MtlReader;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/5/25 0025 11:44
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class ObjHelper {

    private static ObjHelper mObjHelper;

    public static ObjHelper instance() {
        if (mObjHelper == null) mObjHelper = new ObjHelper();
        return mObjHelper;
    }

    private final String TAG = ObjHelper.class.getName();

    //最大缓存量
    private int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    //缓存大小
    private int cacheSize = maxMemory / 2;

    private LruCache<String, Bitmap> mLruCache = new LruCache<String, Bitmap>(cacheSize) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
        }
    };

    private ObjHelper() {

    }

    //加载obj文件
    public Obj loadObj(String path, String name) throws IOException {
        Obj obj = ObjReader.read(getInputStream(path, name));
        obj = ObjUtils.convertToRenderable(obj);
        return obj;
    }

    //加载mtl文件
    public List<Mtl> loadMtl(String path, String name) throws IOException {
        List<Mtl> mtls = MtlReader.read(getInputStream(path, name));
        return mtls;
    }


    //加载贴图
    public Bitmap loadTexture(String path, String name) throws IOException {
        Bitmap bitmap = null;
        bitmap = mLruCache.get(name);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeStream(getInputStream(path, name));
            mLruCache.put(name, bitmap);
        }
        return bitmap;
    }

    //获取文件的流
    private InputStream getInputStream(String path, String name) throws IOException {
        String filePath = path + File.separator + name;
        boolean isAssets = path.contains("sample");
        if (isAssets) {
            return FcUtils.getContext().getAssets().open(filePath);
        }
        Uri uri = Uri.fromFile(new File(filePath));
        Log.e(TAG, "uri=" + uri);
        return FcUtils.getContext().getContentResolver().openInputStream(uri);
    }

    public void clear(){
        mLruCache.evictAll();
    }

}
