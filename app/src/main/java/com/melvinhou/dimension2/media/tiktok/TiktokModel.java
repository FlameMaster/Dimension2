package com.melvinhou.dimension2.media.tiktok;

import android.annotation.SuppressLint;
import android.app.Application;

import com.google.gson.reflect.TypeToken;
import com.melvinhou.dimension2.net.AssetsFileKey;
import com.melvinhou.dimension2.CYEntity;
import com.melvinhou.dimension2.utils.LoadUtils;
import com.melvinhou.kami.mvp.BaseModel;
import com.melvinhou.kami.net.ResultState;
import com.melvinhou.kami.io.IOUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
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
 * = 时 间：2021/4/25 19:13
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class TiktokModel extends BaseModel<TiktokCotract.Presenter> implements TiktokCotract.Model {


    /*列表*/
    private MutableLiveData<List<TiktokEntity>> listDatas = new MutableLiveData<>();



    public TiktokModel(@NonNull Application application) {
        super(application);
    }

    @SuppressLint("CheckResult")
    @Override
    public void loadNetWorkData() {
        getPresenter().startLoading();
        Observable.create((ObservableOnSubscribe<ArrayList<TiktokEntity>>) emitter -> {
            ArrayList<TiktokEntity> list = LoadUtils.getData(
                    LoadUtils.SOURCE_ASSETS,//资源位置
                    AssetsFileKey.MEDIAT_TIKTOK_LIST,//资源文件
                    new TypeToken<CYEntity<ArrayList<TiktokEntity>>>() {
                    });
            emitter.onNext(list);
            emitter.onComplete();
        })
                .compose(IOUtils.setThread())
                .subscribe(list -> {
                    getListDatas().postValue(list);
                    getPresenter().endLoading(ResultState.SUCCESS);
                });
    }

    public MutableLiveData<List<TiktokEntity>> getListDatas() {
        return listDatas;
    }
}
