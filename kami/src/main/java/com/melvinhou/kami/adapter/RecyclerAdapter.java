package com.melvinhou.kami.adapter;

import android.view.View;
import android.view.ViewGroup;

import com.melvinhou.kami.util.FcUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2015/12/28 18:11
 * <p>
 * = 分 类 说 明：可添加头布局和设置点击事件的recyclerview
 * ================================================
 */
public abstract class RecyclerAdapter<T, VH extends RecyclerHolder> extends RecyclerView.Adapter<RecyclerHolder> {

    /*正常布局*/
    public static final int TYPE_NORMAL = -1;

    //——————————————————————————————————————————————————————————————————————————————————————//

    /*数据集合*/
    private List<T> mDatas;
    /*头条目集合*/
    private List<View> mHeaderViews;
    /*尾条目集合*/
    private List<View> mTailViews;
    /*条目点击事件*/
    private OnItemClickListener mListener;

    public RecyclerAdapter() {
        mDatas = new ArrayList<>();
        mHeaderViews = new ArrayList<>();
//        mInsertViews = new SparseArray<>();
        mTailViews = new ArrayList<>();
    }

    /*设置条目点击事件*/
    public void setOnItemClickListener(OnItemClickListener<T, VH> li) {
        mListener = li;
    }

//////////////////////////////////////普通布局///////////////////////////////////////////////////////

    /* 根据位置返回保存的数据*/
    public T getData(int position) {
        return mDatas.get(position);
    }

    /*返回所有数据的集合*/
    public List<T> getDatas() {
        return mDatas;
    }

    /*添加数据*/
    public void addDatas(List datas) {

        if (datas == null || datas.size() <= 0) return;

        int start = getHeaderSize() + getDatas().size();
        mDatas.addAll(datas);//添加数据
        //执行item动画
        if (getDatas().size() == datas.size()) notifyDataSetChanged();
        else notifyItemRangeInserted(start, datas.size());
//        notifyDataSetChanged();
    }

    /*添加数据*/
    public void addData(T data) {
        if (data == null) return;
        int start = getHeaderSize() + getDatas().size();
        mDatas.add(data);
        //执行item动画
        notifyItemInserted(start);
//        notifyItemInserted(start);
    }

    /*添加数据到前排*/
    public void addTopData(T data) {
        if (data == null) return;
        mDatas.add(0, data);
        notifyItemInserted(getHeaderSize());
    }


    /*删除一个*/
    public void removedData(int position) {
        if (position >= 0 && position < getDatas().size()) {
            getDatas().remove(position);
            notifyItemRemoved(getHeaderSize() + position);
        }
    }

    /*数据长度*/
    public int getDatasSize() {
        return getDatas().size();
    }

//////////////////////////////////////头部布局///////////////////////////////////////////////////////

    /**
     * 添加头布局
     * <p>
     * 每次添加都会把上一次的头挤下去
     */
    public void addHeaderView(View View) {
        if (View == null) return;
        mHeaderViews.add(0, View);
        notifyItemInserted(0);
    }

    /**
     * @return 头布局数量
     */
    public int getHeaderSize() {
        return mHeaderViews.size();
    }

    /**
     * 删除顶部头布局
     */
    public void removedTopHeader() {
        mHeaderViews.remove(0);
        notifyItemRemoved(0);
    }

//////////////////////////////////////尾部布局///////////////////////////////////////////////////////

    /**
     * 添加尾布局
     * <p>
     * 每次添加都会把上一次的尾挤上去
     */
    public void addTailView(View View) {
        if (View == null) return;
        mTailViews.add(View);
        //执行item动画
//        滚动中不能调用
        notifyItemInserted(getHeaderSize() + getDatas().size() + getTailSize());
    }

    /**
     * 删除一个尾布局
     *
     * @param position 删除的尾布局位置
     */
    public void removedTail(int position) {
        mTailViews.remove(position);
        notifyItemRemoved(getHeaderSize() + getDatas().size() + position);
    }

    /**
     * @return 头布局数量
     */
    public int getTailSize() {
        return mTailViews.size();
    }

    /*获取尾布局*/
    public View getTailView(int position) {
        if (position < getTailSize() && position >= 0)
            return mTailViews.get(position);
        return null;
    }

    //////////////////////////////////////删除数据///////////////////////////////////////////////////////

    /**
     * 删除所有数据
     */
    public void clearDatas() {

        int size = getDatasSize() + getHeaderSize() + getTailSize();
        mHeaderViews.clear();
        mTailViews.clear();
        clearData();
        //执行item动画
        notifyItemRangeRemoved(0, size);
    }

    /*直接删除Data所有数据*/
    public void clearData() {
        int size = getDatasSize();
        mDatas.clear();
        //执行item动画
        notifyItemRangeRemoved(getHeaderSize(), size);
    }

////////////////——————————————————上面是对数据的操作，下面是对ui的操作————————————————————////////////////
////////////////——————————————————上面是对数据的操作，下面是对ui的操作————————————————————////////////////
////////////////——————————————————上面是对数据的操作，下面是对ui的操作————————————————————////////////////
////////////////——————————————————上面是对数据的操作，下面是对ui的操作————————————————————////////////////
////////////////——————————————————上面是对数据的操作，下面是对ui的操作————————————————————////////////////
////////////////——————————————————上面是对数据的操作，下面是对ui的操作————————————————————////////////////


    /**
     * 根据位置获取镶嵌view
     *
     * @param position
     * @return
     */
    public View getCustomView(int position) {
        //头
        if (position < mHeaderViews.size())
            return mHeaderViews.get(position);
            //尾
        else if (position >= mHeaderViews.size() + mDatas.size())
            return mTailViews.get(position - mHeaderViews.size() - mDatas.size());
        //镶嵌布局
        return null;
    }

    /**
     * 根据位置判断条目类型
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {

        //没有镶嵌布局时返回正常布局类型-1
        if (mHeaderViews.size() <= 0 && mTailViews.size() <= 0) return TYPE_NORMAL;

        //镶嵌布局返回所在位置
        if (position < mHeaderViews.size()
                || position >= mHeaderViews.size() + mDatas.size())
            return position;

        //默认布局
        return TYPE_NORMAL;
    }

    /**
     * 初始化条目
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public RecyclerHolder onCreateViewHolder(ViewGroup parent, final int viewType) {

        //判断条目类型是否是普通类型
        if (viewType != TYPE_NORMAL)
            return onCustomCreate(getCustomView(viewType), viewType);

        View view = View.inflate(FcUtils.getContext(), getItemLayoutId(viewType), null);

        return onCreate(view, viewType);
    }

    /* 初始化对应条目数据*/
    @Override
    public void onBindViewHolder(final RecyclerHolder viewHolder, int position) {
        //判断布局类型执行相关超作
        if (getItemViewType(position) != TYPE_NORMAL) {
            bindCustomData(viewHolder, position, getItemViewType(position));
        } else {
            final int pos = getRealPosition(viewHolder);
            final T data = mDatas.get(pos);
            bindData((VH) viewHolder, pos, data);

            if (mListener != null) {
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onItemClick(viewHolder, pos, data);
                    }
                });
            }
        }
    }

    /*使Grid布局时头布局独占一行*/
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        //判断是否是grid布局
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    //非普通布局独占整行
                    return getItemViewType(position) != TYPE_NORMAL ? gridManager.getSpanCount() : 1;
                }
            });
        }
    }

    /*使用瀑布流时使头布局独占一行*/
    @Override
    public void onViewAttachedToWindow(RecyclerHolder holder) {
        super.onViewAttachedToWindow(holder);
        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams//判断布局类型是瀑布流
                && (getHeaderSize() > 0 | getTailSize() > 0)) {//判断是否包含头布局和尾布局
            StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
            boolean fullSpan = false;
            //头布局
            if (holder.getLayoutPosition() < getHeaderSize())
                fullSpan = true;
            //尾布局
            if (holder.getLayoutPosition() >= getHeaderSize() + getDatasSize())
                fullSpan = true;
            p.setFullSpan(fullSpan);
        }
    }

    /**
     * 获取当前条目位置,此功能只对普通条目有效，镶嵌条目不计算
     *
     * @param holder
     * @return
     */
    public int getRealPosition(RecyclerHolder holder) {
        int position = holder.getLayoutPosition();
        //减去头布局数量
        int realPosition = position - getHeaderSize();
        return realPosition;
    }

    /*返回总共的条目数*/
    @Override
    public int getItemCount() {
        return mDatas.size() + mHeaderViews.size() + mTailViews.size();
    }


////////////////————————————————————————子类去实现的一些方法——————————————————————————////////////////


    /* 对条目数据初始化*/
    public abstract void bindData(VH viewHolder, int position, T data);

    /*对特殊条目的初始化*/
    protected void bindCustomData(RecyclerHolder viewHolder, int position, int itemViewType) {
    }

    /*布局引用*/
    public abstract int getItemLayoutId(int viewType);

    /*初始化*/
    protected abstract VH onCreate(View View, int viewType);

    /*可以被继承的特殊条目初始化*/
    public RecyclerHolder onCustomCreate(View insertView, int viewType) {
        return new RecyclerHolder(insertView);
    }


    /*条目点击事件接口*/
    public interface OnItemClickListener<T, VH extends RecyclerHolder> {
        void onItemClick(VH viewHolder, int position, T data);
    }
}
