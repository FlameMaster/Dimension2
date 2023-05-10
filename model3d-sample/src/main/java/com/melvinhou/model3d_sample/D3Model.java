package com.melvinhou.model3d_sample;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;

import com.melvinhou.kami.bean.FcEntity;
import com.melvinhou.kami.mvvm.BaseViewModel;
import com.melvinhou.kami.net.RequestCallback;
import com.melvinhou.kami.tool.AssetsUtil;
import com.melvinhou.model3d_sample.api.AssetsService;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import de.javagl.obj.Obj;
import io.reactivex.Observable;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/6/20 19:43
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class D3Model extends BaseViewModel {

    public D3Model(@NonNull Application application) {
        super(application);
    }

    MutableLiveData<List<D3Entity>> list = new MutableLiveData<>();
    MutableLiveData<Obj> obj = new MutableLiveData<>();


    @SuppressLint("CheckResult")
    void loadListData() {
        AssetsUtil.loadData(
                        "sample_media_list.json",
                        D3Entity.class, ArrayList.class)
                .subscribe(data -> {
                    Log.e("获取数据", "长度=${data?.data?.size ?: -1}");
                    list.postValue((List<D3Entity>) data.getData());
                });
    }

    /**
     * 加载列表
     */
    void  loadList() {
        Observable<FcEntity<ArrayList<D3Entity>>> observable = AssetsService.instance.Api().getD3List();
        requestData(observable, new RequestCallback<ArrayList<D3Entity>>() {
            @Override
            public void onSuceess(ArrayList<D3Entity> data) {
                list.postValue(data);
            }
        });
    }
}
