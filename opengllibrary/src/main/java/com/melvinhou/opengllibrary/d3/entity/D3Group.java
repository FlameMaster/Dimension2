package com.melvinhou.opengllibrary.d3.entity;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.melvinhou.opengllibrary.utils.ObjHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.javagl.obj.Mtl;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjGroup;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/5/25 0025 11:05
 * <p>
 * = 分 类 说 明：简单的模型解析组
 * ================================================
 */
public class D3Group<T extends D3Object> {
    //不同的解析器
    private List<D3Object> objs = new ArrayList<>();
    private Class<T> objClass;

    public D3Group(Class<T> tClass){
        objClass = tClass;
    }

    public void load(String objPath, String objName) throws Exception {
        List<Mtl> mtls = new ArrayList<>();
        //加载obj文件
        Obj obj = ObjHelper.instance().loadObj(objPath, objName);
        if (obj == null) {
            throw new IOException("obj文件未找到");
        }
        //加载mtl文件
        for (String mtlName : obj.getMtlFileNames()) {
            mtls.addAll(ObjHelper.instance().loadMtl(objPath, mtlName));
        }
        if (mtls.size() == 0) {
            throw new IOException("mtl文件未找到");
        }
        //加载所有材质
        for (Mtl mtl : mtls) {
            String groupName = mtl.getName();
            ObjGroup group = obj.getMaterialGroup(groupName);
            if (group == null) continue;

            String mapName = mtl.getMapKd();
            if (TextUtils.isEmpty(mapName)) {
                continue;
            }
            Bitmap texture = ObjHelper.instance().loadTexture(objPath, mapName);

            //分组对象
            D3Object d3Object = creatObj();
            if (d3Object == null) return;
            objs.add(d3Object);
            d3Object.loadData(obj, group, texture);
        }

    }

    private T creatObj() {
        try {
            return objClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void doDraw(float[] mMatrix, float[] mvpMatrix) {
        for (D3Object obj : objs) {
            obj.doDraw(mMatrix, mvpMatrix);
        }
    }
}
