package com.melvinhou.tiktok_sample;

import com.melvinhou.kami.lucas.CallBack;
import com.melvinhou.kami.mvp.interfaces.MvpModel;
import com.melvinhou.kami.mvp.interfaces.MvpPresenter;
import com.melvinhou.kami.mvp.interfaces.MvpView;
import com.melvinhou.tiktok_sample.bean.Comment;
import com.melvinhou.tiktok_sample.bean.TiktokEntity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/4/25 18:54
 * <p>
 * = 分 类 说 明：玩玩mvp模式
 * ================================================
 */
public class TiktokCotract {

    /**
     * mvp-v
     */
    public interface View extends MvpView<Presenter> {
        void addItems(boolean isRefresh, @NotNull ArrayList<TiktokEntity> items);
    }

    /**
     * mvp-p
     */
    public interface Presenter extends MvpPresenter<View, Model> {
    }

    /**
     * mvp-m
     */
    public interface Model extends MvpModel<Presenter> {
        void loadList(CallBack<ArrayList<TiktokEntity>> callback);
    }
}
