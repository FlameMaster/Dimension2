package com.melvinhou.kami.net;

import com.melvinhou.kami.util.FcUtils;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/2/22 0022 10:53
 * <p>
 * = 分 类 说 明：网络请求
 * ================================================
 */
public abstract class  RequestCallback<D> {
    public abstract void  onSuceess(D data);
    public void onFailure(@ResultState int code, String message){
        if (code == ResultState.RELOGIN) {
            FcUtils.showToast(message);
        } else if (code != ResultState.CONVERT_ERROR) {
            FcUtils.showToast(message);
        }
    }
}
