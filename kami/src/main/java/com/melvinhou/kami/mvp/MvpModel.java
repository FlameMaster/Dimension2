package com.melvinhou.kami.mvp;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2019/11/28 10:32
 * <p>
 * = 分 类 说 明：mvp-m
 * ============================================================
 */
public  interface MvpModel<P extends MvpPresenter>{
    P getPresenter();
    void setPresenter(P presenter);



    /**
     * 加载网络数据数据
     */
     void loadNetWorkData();
}
