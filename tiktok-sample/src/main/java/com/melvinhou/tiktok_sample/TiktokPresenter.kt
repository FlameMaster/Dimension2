package com.melvinhou.tiktok_sample

import androidx.lifecycle.ViewModelProvider
import com.melvinhou.kami.mvp.BasePresenter
import com.melvinhou.tiktok_sample.bean.TiktokEntity


/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2023/5/16 0016 17:35
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
 class TiktokPresenter(v: TiktokCotract.View) : BasePresenter<TiktokCotract.View, TiktokCotract.Model>(v),
    TiktokCotract.Presenter {
    override fun openModel(provider: ViewModelProvider): TiktokCotract.Model = provider[TiktokModel::class.java]

    override fun onCreate() {
        model.loadList{
            view.addItems(true,it)
        }
    }
}