package com.melvinhou.dimension2.game;

import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;

import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.databinding.ItemGameLaunchBD;
import com.melvinhou.dimension2.game.klotski.GameKlotskiActivity;
import com.melvinhou.dimension2.game.poker.GamePokerActivity;
import com.melvinhou.dimension2.pager.BaseListPager;
import com.melvinhou.kami.adapter.BindingHolder;
import com.melvinhou.kami.net.EmptyState;
import com.melvinhou.kami.util.FcUtils;

import java.util.Collections;

import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/4/26 19:56
 * <p>
 * = 分 类 说 明：游戏列表
 * ================================================
 */
public class GameLaunchPager extends BaseListPager<GameLaunchItem, GameLaunchPager.Holder> {


    /*触摸帮助类 */
    private ItemTouchHelper mItemTouchHelper;
    /*拖拽监听*/
    private final SimpleItemTouchHelperCallback.OnStartDragListener mDragStartListener
            = new SimpleItemTouchHelperCallback.OnStartDragListener() {
        @Override
        public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
            mItemTouchHelper.startDrag(viewHolder);
        }
    };

    @Override
    public void onCreate(int position) {
        super.onCreate(position);

        //初始化拖拽监听
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(
                new SimpleItemTouchHelperCallback.ItemTouchHelperAdapter() {
                    @Override
                    public boolean onItemMove(int fromPosition, int toPosition) {
                        Collections.swap(getAdapter().getDatas(), fromPosition, toPosition);
                        getAdapter().notifyItemMoved(fromPosition, toPosition);
                        return true;
                    }

                    @Override
                    public void onItemDismiss(int position) {
                        getAdapter().getDatas().remove(position);
                        getAdapter().notifyItemRemoved(position);
                    }
                });
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(getBinding().list);
    }

    @Override
    protected Holder getHolder(ViewDataBinding binding, int viewType) {
        return new Holder((ItemGameLaunchBD) binding);
    }

    @Override
    protected int getItemLayoutID(int viewType) {
        return R.layout.item_game_launch;
    }

    @Override
    protected void onItemBind(Holder viewHolder, int realPosition, GameLaunchItem data) {
        //不设置这个的话，需要长按才能拖动
        if (viewHolder==null)
        viewHolder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(viewHolder);
                }
                return false;
            }
        });
        viewHolder.updata(data);
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManager() {
        return new GridLayoutManager(FcUtils.getContext(), 3);
    }

    @Override
    public void loadData(int size, int page) {
        updataEmptyState(EmptyState.NORMAL, null);
        updataTailState(EmptyState.USER_DEFINED, null);
        getAdapter().addData(new GameLaunchItem("FC斗地主", null));
        getAdapter().addData(new GameLaunchItem("华容道FC", null));
        getAdapter().addData(new GameLaunchItem("风尘的冒险", null));
    }

    @Override
    protected void updateLoadingState(boolean isLoading) {

    }

    @Override
    public void updataEmptyState(int emptyState, String message) {

    }

    @Override
    public void onItemClick(Holder viewHolder, int position, GameLaunchItem data) {
        Intent intent = null;
        if (data.getName().equals("FC斗地主"))
            intent = new Intent(FcUtils.getContext(), GamePokerActivity.class);
        if (data.getName().equals("华容道FC"))
            intent = new Intent(FcUtils.getContext(), GameKlotskiActivity.class);
        if (intent != null)
            toActivity(intent);
    }

    class Holder extends BindingHolder<ItemGameLaunchBD> {

        public Holder(ItemGameLaunchBD binding) {
            super(binding);
        }

        public void updata(GameLaunchItem data) {
            getBinding().title.setText(data.getName());
        }
    }
}
