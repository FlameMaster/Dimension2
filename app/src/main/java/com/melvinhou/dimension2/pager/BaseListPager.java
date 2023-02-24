package com.melvinhou.dimension2.pager;

import android.view.LayoutInflater;

import com.melvinhou.dimension2.R;
import com.melvinhou.dimension2.databinding.ItemMoreBD;
import com.melvinhou.dimension2.databinding.VpListBD;
import com.melvinhou.kami.adapter.DataBindingHolder;
import com.melvinhou.kami.adapter.DataBindingRecyclerAdapter;
import com.melvinhou.kami.net.RequestState;
import com.melvinhou.kami.util.FcUtils;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2019/12/5 17:49
 * <p>
 * = 分 类 说 明：
 * ============================================================
 */
public abstract class BaseListPager<D, H extends DataBindingHolder> extends BasePager<VpListBD> {


    /*页码和每页长度*/
    private int mPage, mSize;
    /*是否在加载中*/
    private boolean isLoading;
    /*适配器*/
    private ListAdapter mAdapter;

    /*尾布局*/
    private ItemMoreBD mLoadMoreBinding;


    /*初始化必要属性*/
    public BaseListPager() {
        setPage(1);
        setSize(10);
        isLoading = false;
    }

/////////////////////////////////////////初始化/////////////////////////////////////////////////////

    @Override
    public int getLayoutID() {
        return R.layout.vp_list;
    }

    @Override
    public void onCreate(int position) {
        super.onCreate(position);
        initList();
        //刷新按钮
        getBinding().loading.refresh.setOnClickListener(v -> refreshData(true));
    }

    /**
     * 初始化list条目
     */
    private void initList() {
        mAdapter = new ListAdapter();
        getBinding().list.setLayoutManager(getLayoutManager());
        getBinding().list.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this::onItemClick);
    }

    @Override
    public void onShow(int position) {
            loadData();
    }

    /////////////////////////////////////////工具/////////////////////////////////////////////////////

    public int getPage() {
        return mPage;
    }

    public void setPage(int page) {
        this.mPage = page;
    }

    public int getSize() {
        return mSize;
    }

    public void setSize(int size) {
        this.mSize = size;
    }

    protected void updateLoadingState(boolean isLoading) {
        this.isLoading = isLoading;
    }

    public ListAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * 布局管理器
     *
     * @return
     */
    protected RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(FcUtils.getContext(), LinearLayoutManager.VERTICAL, false);
    }

    /**
     * 添加尾部布局
     */
    protected void addTailLayout() {
        if (mLoadMoreBinding == null) {
            mLoadMoreBinding = DataBindingUtil.inflate(LayoutInflater.from(
                    FcUtils.getContext()), R.layout.item_loadmore, null, false);
            mLoadMoreBinding.setState(RequestState.RUNNING);
        }
        //显示加载更多布局
        if (getAdapter().getTailSize() <= 0)
            getAdapter().addTailBinding(mLoadMoreBinding);
    }

/////////////////////////////////////////条目处理/////////////////////////////////////////////////////

    /**
     * 获取页面持有
     *
     * @param binding
     * @param viewType
     * @return
     */
    protected abstract H getHolder(ViewDataBinding binding, int viewType);

    /**
     * 条目布局id
     *
     * @param viewType
     * @return
     */
    protected abstract int getItemLayoutID(int viewType);

    /**
     * 特殊条目
     *
     * @param viewHolder
     * @param position
     * @param itemViewType
     */
    protected void onCustomItemBind(DataBindingHolder viewHolder, int position, int itemViewType) {

    }

    /**
     * 条目数据变化
     *
     * @param viewHolder
     * @param realPosition
     * @param data
     */
    protected abstract void onItemBind(H viewHolder, int realPosition, D data);

    /*条目点击处理*/
    public void onItemClick(H viewHolder, int position, D data) {

    }

    @Override
    public void loadData() {
        if (isLoading) return;
        if (getAdapter().getDatas().size() <= 0) {
            isLoading = true;
            updateRequestState(RequestState.RUNNING);
            addTailLayout();
            setPage(1);
            loadData(mSize, mPage++);
        }
    }

    /**
     * 加载数据
     *
     * @param size 长度
     * @param page 页数
     */
    public abstract void loadData(int size, int page);

    /**
     * 加载更多
     */
    public void loadMore() {
        if (isLoading) return;
        //加载更多布局出现的时候开始加载更多
        updataTailState(RequestState.RUNNING);
        loadData(mSize, mPage++);
    }

    @Override
    public void refreshData(boolean isShowLoad) {
        if (isShowLoad) {
            getAdapter().clearData();
            setPage(1);
            loadData();
        }
    }

    @Override
    public void updateRequestState(@RequestState int state) {
        getBinding().setState(state);
    }

    /**
     * 更新加载更多的状态
     *
     * @param state
     */
    public void updataTailState(@RequestState int state) {
        if (mLoadMoreBinding == null) return;
        //改变状态
        mLoadMoreBinding.setState(state);
    }


//////////////////////////////////////////////////////////////////////////////////////////////

    /*配合listpager的条目管理器*/
    public class ListAdapter extends DataBindingRecyclerAdapter<D, H> {

        @Override
        public void bindData(H viewHolder, int RealPosition, D data) {
            onItemBind(viewHolder, RealPosition, data);
            if (getTailSize() < 1
                    && RealPosition == getItemCount() - getHeaderSize() - getTailSize() - 1) {
//                loadMore();
            }
        }

        @Override
        protected void bindCustomData(DataBindingHolder viewHolder, int position, int itemViewType) {
            onCustomItemBind(viewHolder, position, itemViewType);
            if (position == getItemCount() - 1 && getDatas().size() > 0) {
                loadMore();
            }
        }

        @Override
        public int getItemLayoutId(int viewType) {
            return getItemLayoutID(viewType);
        }

        @Override
        protected H onCreate(ViewDataBinding binding, int viewType) {
            return getHolder(binding, viewType);
        }
    }
}
