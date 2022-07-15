package com.melvinhou.dimension2.ar.d3;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import de.javagl.obj.Obj;

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
public class D3Model extends AndroidViewModel {

    public D3Model(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<Obj> obj = new MutableLiveData<>();

}
