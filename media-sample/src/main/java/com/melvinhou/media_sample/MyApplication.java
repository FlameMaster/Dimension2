package com.melvinhou.media_sample;

import com.melvinhou.kami.BaseApplication;
import com.melvinhou.kami.BaseException;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/3/30 0:53
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class MyApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected BaseException getException() {
        return new BaseException();
    }
}
