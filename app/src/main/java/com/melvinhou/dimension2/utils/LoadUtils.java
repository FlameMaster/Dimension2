package com.melvinhou.dimension2.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.melvinhou.dimension2.CYEntity;
import com.melvinhou.kami.util.FcUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2019/2/15 17:02
 * <p>
 * = 分 类 说 明：加载数据的工具类
 * ============================================================
 */
public class LoadUtils {
    /*本地assets*/
    public final static int SOURCE_ASSETS = 0;
    /*本地内存*/
    public final static int SOURCE_NATIVE = 2;
    /*本地内存卡*/
    public final static int SOURCE_NATIVE_SD = 3;
    /*网络*/
    public final static int SOURCE_NET = 5;

    /**
     * 获取数据
     *
     * @param <T>
     * @param dataSource
     * @param dataPath
     * @return
     */
    public static <T> T getData(int dataSource, String dataPath,TypeToken typeToken) {
        Gson gson = new Gson();
        CYEntity<T> entity = gson.fromJson(
                getDataText(dataSource, dataPath),
                typeToken.getType());
        return entity.getData();
    }

    /**
     * 获取数据说需字符串
     *
     * @param dataSource 来源
     * @param dataPath   路径
     * @return
     */
    public static String getDataText(int dataSource, String dataPath) {
        String text = null;
        if (dataSource == SOURCE_ASSETS)
            text = readAssetsTxt(dataPath);

        return text;
    }

    /**
     * 读取assets下的txt文件，返回utf-8 String
     *
     * @param fileName 不包括后缀
     * @return
     */
    public static String readAssetsTxt(String fileName) {
        String text = null;
        try {
            InputStream is = FcUtils.getContext().getAssets().open(fileName + ".json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            text = new String(buffer, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return text;
        }
    }

    /**
     * 读取assets下的txt文件，返回utf-8 String
     *
     * @param fileName 不包括后缀
     * @return
     */
    public static String readAssetsTxt(String fileName,String unit) {
        String text = null;
        try {
            InputStream is = FcUtils.getContext().getAssets().open(fileName + unit);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            text = new String(buffer, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return text;
        }
    }
}
