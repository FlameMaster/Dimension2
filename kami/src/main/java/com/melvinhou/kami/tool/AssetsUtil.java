package com.melvinhou.kami.tool;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.melvinhou.kami.bean.FcEntity;
import com.melvinhou.kami.net.BaseEntity;
import com.melvinhou.kami.util.FcUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

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

    public static <T> Observable<FcEntity<T>> loadData(String filePath, Type... elementClass) {
        return Observable.create((ObservableOnSubscribe<FcEntity<T>>) emitter -> {
                    Gson gson = new Gson();
                    String json = readText(filePath);
                    TypeToken typeToken =null;
                    for (Type rawType : elementClass){
                        if (typeToken!=null){
                            Type type = typeToken.getType();
                            typeToken = TypeToken.getParameterized(rawType, type);
                        }else {
                            typeToken = TypeToken.get(rawType);
                        }
                    }
                    typeToken = TypeToken.getParameterized(FcEntity.class, typeToken.getType());
                    FcEntity<T> entity = gson.fromJson(json, typeToken.getType());
                    emitter.onNext(entity);
                    emitter.onComplete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 读取assets下的txt文件，返回utf-8 String
     *
     * @param filePath 包括后缀
     * @return
     */
    public static String readText(String filePath) {
        String text = null;
        try {
            InputStream is = FcUtils.getContext().getAssets().open(filePath);
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
