package com.melvinhou.model3d_sample.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import com.melvinhou.kami.util.FcUtils;
import com.melvinhou.kami.util.ResourcesUtils;
import com.melvinhou.model3d_sample.D3Activity;
import com.melvinhou.model3d_sample.D3Config;
import com.melvinhou.model3d_sample.R;
import com.melvinhou.rxjava.rxbus.RxBus;
import com.melvinhou.rxjava.rxbus.RxBusMessage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.javagl.obj.Mtl;
import de.javagl.obj.MtlReader;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjFace;
import de.javagl.obj.ObjGroup;
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
 * = 时 间：2022/7/11 0011 13:22
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class D3ObjGroup implements D3Object {

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

    //不同的解析器
    private List<D3Object> objs = new ArrayList<>();

    //不同的解析器，0,1
    private int mType = 0;
    private String mDirectoryPath;
    private boolean isAssetsFile = false;

    public D3ObjGroup(int type, String objPath, String objName) {
        mType = type;
        objPath = "ar/keqing/keqing.obj";
//        objPath = "ar/models/redcar.obj";
        try {
            // 加载模型文件
            Obj obj = ObjReader.read(getFile(objPath, objName));
            obj = ObjUtils.convertToRenderable(obj);
            load(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private D3Config mD3Config;

    public D3ObjGroup(int type, Obj obj, D3Config config) {
        mType = type;
        mD3Config = config;
        try {
            load(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public D3ObjGroup(int type, String directoryPath, Obj obj, D3Config config) {
        mType = type;
        mDirectoryPath = directoryPath;
        isAssetsFile = !directoryPath.contains(ResourcesUtils.getString(R.string.app_name));
        mD3Config = config;
        try {
            load(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load(Obj obj) throws IOException {
        List<Mtl> mtls = new ArrayList<>();
        for (String mtlName : obj.getMtlFileNames()) {
            mtls.addAll(MtlReader.read(getFile(mDirectoryPath, mtlName)));
        }

        for (Mtl mtl : mtls) {
            String groupName = mtl.getName();
            ObjGroup group = obj.getMaterialGroup(groupName);
            if (group == null) continue;
            String mapPath = mtl.getMapKd();
            if (TextUtils.isEmpty(mapPath)) {
                continue;
            }
            Bitmap bitmap = loadBitmap(mapPath);

            //
            float[] vertices = new float[group.getNumFaces() * 3 * 3];
            float[] normals = new float[group.getNumFaces() * 3 * 3];
            float[] texCoords = new float[group.getNumFaces() * 3 * 2];
            int vNum = 0, nNum = 0, tNum = 0;
            for (int i = 0; i < group.getNumFaces(); i++) {
                ObjFace face = group.getFace(i);
                for (int j = 0; j < face.getNumVertices(); j++) {
                    vertices[vNum++] =
                            obj.getVertex(face.getVertexIndex(j)).get(0);
                    vertices[vNum++] =
                            obj.getVertex(face.getVertexIndex(j)).get(1);
                    vertices[vNum++] =
                            obj.getVertex(face.getVertexIndex(j)).get(2);

                    if (obj.getNumNormals() > 0) {
                        normals[nNum++] =
                                obj.getNormal(face.getNormalIndex(j)).get(0);
                        normals[nNum++] =
                                obj.getNormal(face.getNormalIndex(j)).get(1);
                        normals[nNum++] =
                                obj.getNormal(face.getNormalIndex(j)).get(2);
                    }

                    texCoords[tNum++] =
                            obj.getTexCoord(face.getTexCoordIndex(j)).get(0);
                    texCoords[tNum++] =
                            obj.getTexCoord(face.getTexCoordIndex(j)).get(1);
                }
            }
            D3Object d3Object;
            if (mType == 1) {
                d3Object = new D3Player1Obj(groupName, vertices, normals, texCoords, bitmap, mD3Config);
            } else {
                d3Object = new D3DomeObj(groupName, vertices, normals, texCoords, bitmap, mD3Config);
            }
            objs.add(d3Object);
        }
        //通知ui变更
        RxBus.instance().post(RxBusMessage.Builder
                .instance(RxBusMessage.CommonType.DATA_REFRESH)
                .client(D3Activity.class.getName())
                .attach(null)
                .build());

    }

    /**
     * 加载图片
     *
     * @param fileName
     * @return
     * @throws IOException
     */
    private Bitmap loadBitmap(String fileName) throws IOException {
        Bitmap bitmap = null;
        bitmap = mLruCache.get(fileName);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeStream(getFile(mDirectoryPath, fileName));
            mLruCache.put(fileName, bitmap);
        }
        return bitmap;
    }


    /**
     * 加载文件
     *
     * @param path
     * @param fileName
     * @return
     * @throws IOException
     */
    private InputStream getFile(String path, String fileName) throws IOException {
        String filePath = path + File.separator + fileName;
        if (isAssetsFile) {
            return getAssetsIs(filePath);
        }
        Uri uri = Uri.fromFile(new File(filePath));
        return FcUtils.getContext().getContentResolver().openInputStream(uri);
    }


    /**
     * 加载资源文件
     *
     * @param path
     * @return
     * @throws IOException
     */
    private InputStream getAssetsIs(String path) throws IOException {
        Log.e("加载资源文件", "文件：" + path);
        RxBus.instance().post(RxBusMessage.Builder
                .instance(RxBusMessage.CommonType.DATA_REFRESH)
                .client(D3Activity.class.getName())
                .attach("加载：" + path)
                .build());
        return FcUtils.getContext().getAssets().open(path);
    }


    @Override
    public void onDraw(float[] mMatrix, float[] mvpMatrix) {
        for (D3Object obj : objs) {
            obj.onDraw(mMatrix, mvpMatrix);
        }
    }

}
