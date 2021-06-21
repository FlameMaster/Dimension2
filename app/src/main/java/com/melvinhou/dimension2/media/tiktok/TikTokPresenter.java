package com.melvinhou.dimension2.media.tiktok;

import android.view.View;

import com.melvinhou.dimension2.Comment;
import com.melvinhou.kami.mvp.BasePresenter;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/4/25 18:59
 * <p>
 * = 分 类 说 明：抖音页面
 * ================================================
 */
public class TikTokPresenter extends BasePresenter<TiktokCotract.View, TiktokCotract.Model>
        implements TiktokCotract.Presenter {

    //抽屉状态
    boolean isOpenCommentDrawer = false;

    public TikTokPresenter(TiktokCotract.View view, TiktokCotract.Model model) {
        super(view, model);
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        getModel().loadNetWorkData();
    }

    @Override
    public void startLoading(String message) {
        getModel().getListDatas().observe(getView().getLifecycleOwner(), new Observer<List<TiktokEntity>>() {
            @Override
            public void onChanged(List<TiktokEntity> tiktokEntities) {
                updateDatas(tiktokEntities);
            }
        });
    }

    @Override
    public void endLoading(int code, String message) {

    }

    @Override
    public void updateDatas(List<TiktokEntity> datas) {
        getView().getListAdapter().addDatas(datas);
        updateCurrentData(datas.get(0));
    }

    @Override
    public void updateCurrentData(TiktokEntity entity) {
        getView().setCurrentData(entity);
        updateCommentList(entity.getComments());
    }

    @Override
    public void updateCommentList(List<Comment> comments) {
        getView().clearCommentItems();
        for (Comment comment : comments) {
            getView().jionHigCommentItem(comment);
            for (Comment.SubComment subComment : comment.getSubComments()) {
                getView().jionSubCommentItem(subComment);
            }
        }
    }

    @Override
    public void onChangeUnfoldState(boolean isOpen) {

        if (isOpen != isOpenCommentDrawer) {
            boolean value = !isOpen;
            getView().switchItemCanTouchState(value);
        }
        isOpenCommentDrawer = isOpen;
    }

    @Override
    public void closeCommentDrawer(View view) {
        if (isOpenCommentDrawer) {
            getView().closeCommentDrawer();
        }
    }

    @Override
    public void openCommentDrawer(View view) {
        getView().openCommentDrawer();
    }

    @Override
    public boolean canScrollVertically(boolean defaultValue) {
        if (isOpenCommentDrawer) return false;
        else return defaultValue;
    }

    @Override
    public RecyclerView.OnScrollListener getListScrollListener() {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //播放视频
                    View view = recyclerView.getChildAt(0);
                    int position = recyclerView.getLayoutManager().getPosition(view);
                    TiktokEntity entity = getModel().getListDatas().getValue().get(position);
                    updateCurrentData(entity);
                    TikTokHolder holder = (TikTokHolder) recyclerView.getChildViewHolder(view);
                    holder.play();
                }
            }
        };
    }

    @Override
    public TikTokAdapter.OnItemDoubleClickListener getItemDoubleClickListener() {
        return new TikTokAdapter.OnItemDoubleClickListener() {
            @Override
            public void onItemSingleClick(TikTokHolder viewHolder, int position, TiktokEntity data) {
                viewHolder.switchVideoState();
            }

            @Override
            public void onItemDoubleClick(TikTokHolder viewHolder, int position, TiktokEntity data, float x, float y) {
                data.setStarCount(data.getStarCount() + 1);
                getView().updateStarCount(data.getStarCount());
                getView().showStarAnimation(x, y);
            }
        };
    }

    @Override
    public void back() {
        if (isOpenCommentDrawer) getView().closeCommentDrawer();
        else getView().close();
    }
}
