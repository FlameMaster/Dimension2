package com.melvinhou.opengllibrary.d3.entity;


import android.graphics.Bitmap;

import java.io.IOException;

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
 * = 时 间：2023/5/25 0025 16:30
 * <p>
 * = 分 类 说 明：模型解析类
 * ================================================
 */
public interface D3Object {
    /**
     *
     * @param obj 模型对象
     * @param group 组件分组
     * @param texture 纹理
     */
    void loadData(Obj obj, ObjGroup group, Bitmap texture) throws IOException;

    /**
     *
     * @param mMatrix
     * @param mvpMatrix
     */
    void doDraw(float[] mMatrix, float[] mvpMatrix);

    void claer();
}
