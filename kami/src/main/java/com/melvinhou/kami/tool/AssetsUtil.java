package com.melvinhou.kami.tool;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.melvinhou.kami.net.BaseEntity;
import com.melvinhou.kami.util.FcUtils;

import java.io.IOException;
import java.io.InputStream;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/3/20 0020 14:20
 * <p>
 * = 分 类 说 明：资源文件的工具类
 * ================================================
 */
public class AssetsUtil {

    public static <T, E extends BaseEntity<T>> Observable<E> loadData(String fileName) {
        return Observable.create((ObservableOnSubscribe<E>) emitter -> {
            Gson gson = new Gson();
            TypeToken<E> typeToken = new TypeToken<E>() {
            };
            E entity = gson.fromJson(readText(fileName), typeToken.getType());
            emitter.onNext(entity);
            emitter.onComplete();
        });
    }

    /**
     * 读取assets下的txt文件，返回utf-8 String
     *
     * @param fileName 包括后缀
     * @return
     */
    public static String readText(String fileName) {
        String text = null;
        try {
            InputStream is = FcUtils.getContext().getAssets().open(fileName);
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
