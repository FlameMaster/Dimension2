package com.melvinhou.dimension2.media.tiktok;

import com.melvinhou.dimension2.Comment;
import com.melvinhou.kami.mvp.interfaces.MvpModel;
import com.melvinhou.kami.mvp.interfaces.MvpPresenter;
import com.melvinhou.kami.mvp.interfaces.MvpView;

import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

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
    public interface View extends MvpView<TiktokCotract.Presenter> {
        //适配器
        TikTokAdapter getListAdapter();

        void setCurrentData(TiktokEntity entity);
        //条目是否允许滑动
        void switchItemCanTouchState(boolean value);

        //打开关闭抽屉
        void closeCommentDrawer();
        void openCommentDrawer();

        //更新点赞数
        void updateStarCount(int starCount);
        //点赞动画
        void showStarAnimation(float x, float y);

        //清空添加评论
        void clearCommentItems();
        void jionHigCommentItem(Comment comment);
        void jionSubCommentItem(Comment.SubComment comment);
    }

    /**
     * mvp-p
     */
    public interface Presenter extends MvpPresenter<TiktokCotract.View,TiktokCotract.Model> {
        //更新数据
        void updateDatas(List<TiktokEntity> datas);
        void updateCurrentData(TiktokEntity entity);
        void updateCommentList(List<Comment> comments);
        //切换抽屉状态
        void onChangeUnfoldState(boolean isOpen);
        //关闭打开抽屉
        void closeCommentDrawer(android.view.View view);
        void openCommentDrawer(android.view.View view);
        //是否可以滑动list
        boolean canScrollVertically(boolean defaultValue);

        //监听列表滚动
        RecyclerView.OnScrollListener getListScrollListener();
        //监听视频点击
        TikTokAdapter.OnItemDoubleClickListener getItemDoubleClickListener();

        void backward();
    }

    /**
     * mvp-m
     */
    public interface Model extends MvpModel<TiktokCotract.Presenter> {
        //整体数据
        MutableLiveData<List<TiktokEntity>> getListDatas();
    }
}
