package com.melvinhou.dimension2.web;

import android.app.Application;

import com.melvinhou.kami.mvvm.BaseViewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/6/13 0013 13:22
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class WebViewModel extends BaseViewModel {
    public WebViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<String> url = new MutableLiveData<>();

}
