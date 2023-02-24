package com.melvinhou.kami.adapter;

import android.view.View;
import android.view.ViewGroup;

import com.melvinhou.kami.R;
import com.melvinhou.kami.bean.InsertItemEntity;
import com.melvinhou.kami.util.FcUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
 * = 时 间：2020/7/1 0:0
 * <p>
 * = 分 类 说 明：玩转adapter，为recyclerview定制
 * ================================================
 */
public abstract class RecyclerAdapter2<T, VH extends RecyclerHolder> extends RecyclerView.Adapter<RecyclerHolder> {

    //正常布局
    public static final int TYPE_NORMAL = -1;
    //镶嵌布局（位置自定义,只能替代正常布局的位置
    public static final int TYPE_INSERT = -2;
    //头部布局（永远在头部
    public static final int TYPE_HEAD = -11;
    //尾部布局（永远在尾部
    public static final int TYPE_TAIL = -12;

    //——————————————————————————————————————————————————————————————————————————————————————//

    //数据集合
    private List<T> mDatas;
    //头条目集合
    private List<Object> mHeadDatas;
    //尾条目集合
    private List<Object> mTailDatas;
    //嵌套条目集合（自定义位置
    private List<InsertItemEntity> mInsertDatas;
    //条目点击事件
    private OnItemClickListener mListener;

    public RecyclerAdapter2() {
        mDatas = new ArrayList<>();
        mHeadDatas = new ArrayList<>();
        mInsertDatas = new ArrayList<>();
        mTailDatas = new ArrayList<>();
    }

    /*设置条目点击事件*/
    public void setOnItemClickListener(OnItemClickListener<T, VH> li) {
        mListener = li;
    }

//////////////////////////////////////普通布局///////////////////////////////////////////////////////

    /**
     * 根据位置返回保存的数据
     *
     * @param position
     * @return
     */
    public T getData(int position) {
        return mDatas.get(position);
    }

    /**
     * 返回所有数据的集合
     *
     * @return
     */
    public List<T> getDatas() {
        return mDatas;
    }

    /**
     * 添加数据
     *
     * @param datas
     */
    public void addDatas(List<T> datas) {
        if (datas == null || datas.size() <= 0) return;
        int start = getHeadSize() + getDatas().size() + getInsertSize();
        mDatas.addAll(datas);//添加数据
        //执行item动画
        if (getDatas().size() == datas.size()) notifyDataSetChanged();
        else notifyItemRangeInserted(start, datas.size());
    }

    /**
     * 添加数据
     *
     * @param data
     */
    public void addData(T data) {
        if (data == null) return;
        int position = getHeadSize() + getDatas().size() + getInsertSize();
        mDatas.add(data);
        //执行item动画
        notifyItemInserted(position);
    }

    /**
     * 添加数据到前排
     *
     * @param data
     */
    public void addTopData(T data) {
        if (data == null) return;
        int position = getHeadSize();
        mDatas.add(0, data);
        if (getInsertSize() > 0)
            for (InsertItemEntity oldEntity : mInsertDatas) {
                int oldPosition = oldEntity.getPosition();
                if (oldPosition == 0)
                    position += 1;
            }
        notifyItemInserted(position);
    }


    /**
     * 删除一个
     *
     * @param position
     */
    public void removedData(int position) {
        if (position >= 0 && position < getDatas().size()) {
            getDatas().remove(position);
            int normalPosition = getHeadSize() + position;
            int insertSize = 0;
            if (getInsertSize() > 0) {
                Set<Integer> set = new HashSet<>();
                for (InsertItemEntity oldEntity : mInsertDatas)
                    set.add(oldEntity.getPosition());
                normalPosition = getNormalPosition(normalPosition, set);
            }
            notifyItemRemoved(normalPosition);
        }
    }

    /**
     * 用于计算默认条目真实位置
     *
     * @param position 默认条目位置
     * @param set      镶嵌条目位置集合
     * @return
     */
    private int getNormalPosition(int position, Set<Integer> set) {
        if (set == null) return 0;
        for (int num : set) {
            if (num <= position) {
                set.remove(num);
                return getNormalPosition(position + 1, set);
            }
        }
        return position;
    }

    /**
     * 数据长度
     *
     * @return
     */
    public int getDatasSize() {
        return getDatas().size();
    }

    /**
     * 删除Data所有数据
     */
    public void clearDatas() {
        mDatas.clear();
        notifyDataSetChanged();
    }

//////////////////////////////////////头部布局///////////////////////////////////////////////////////

    /**
     * 添加头布局
     * <p>
     * 每次添加都会把上一次的头挤下去
     */
    public void addHead(Object data) {
        if (data == null) return;
        mHeadDatas.add(0, data);
        notifyItemInserted(0);
    }

    /**
     * 删除顶部头布局
     */
    public void removedTopHead() {
        mHeadDatas.remove(0);
        notifyItemRemoved(0);
    }

    /**
     * @return 头布局数量
     */
    public int getHeadSize() {
        return mHeadDatas.size();
    }

//////////////////////////////////////尾部布局///////////////////////////////////////////////////////

    /**
     * 添加尾布局,每次添加都会把上一次的尾挤上去
     */
    public void addTail(Object data) {
        if (data == null) return;
        mTailDatas.add(data);
        //执行item动画
//        滚动中不能调用
        notifyItemInserted(getHeadSize() + getDatasSize() + getInsertSize() + getTailSize());
    }

    /**
     * 删除一个尾布局
     */
    public void removedTail() {
        int position = getHeadSize() + getDatasSize() + getInsertSize() + getTailSize() - 1;
        mTailDatas.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * @return 头布局数量
     */
    public int getTailSize() {
        return mTailDatas.size();
    }


//////////////////////////////////////镶嵌布局///////////////////////////////////////////////////////

    public void addInsert(int position, Object data, boolean isFull) {
        int realPosition = position;
        realPosition += getHeadSize();
        //后移一位
        if (getInsertSize() > 0)
            for (InsertItemEntity oldEntity : mInsertDatas) {
                int oldPosition = oldEntity.getPosition();
                if (oldPosition >= realPosition)
                    oldEntity.setPosition(oldPosition + 1);
            }
        InsertItemEntity entity = new InsertItemEntity();
        entity.setIsFull(isFull);
        entity.setPosition(realPosition);
        entity.setData(data);
        mInsertDatas.add(entity);
        //执行item动画
        notifyItemInserted(realPosition);
    }

    /**
     * 删除一个指定位置的
     *
     * @param position 不包含头部位置
     */
    public void removedInsert(int position) {
        int realPosition = position + getHeadSize();
        boolean isFind = false;
        if (getInsertSize() > 0)
            for (InsertItemEntity oldEntity : mInsertDatas) {
                int oldPosition = oldEntity.getPosition();
                if (oldPosition == realPosition) {
                    isFind = true;
                    mInsertDatas.remove(oldEntity);
                }
                if (oldPosition > realPosition)
                    oldEntity.setPosition(oldPosition - 1);
            }
        if (isFind) notifyItemRemoved(realPosition);
    }

    /**
     * @return 镶嵌布局数量
     */
    public int getInsertSize() {
        return mInsertDatas.size();
    }

    /**
     * 清空数据
     */
    public void clearAllData() {
        int size = getItemCount();
        mTailDatas.clear();
        mDatas.clear();
        mInsertDatas.clear();
        mHeadDatas.clear();
        //执行item动画
        notifyItemRangeRemoved(0, size);
    }

////////////////——————————————————上面是对数据的操作，下面是对ui的操作————————————————————////////////////
////////////////——————————————————上面是对数据的操作，下面是对ui的操作————————————————————////////////////
////////////////——————————————————上面是对数据的操作，下面是对ui的操作————————————————————////////////////
////////////////——————————————————上面是对数据的操作，下面是对ui的操作————————————————————////////////////
////////////////——————————————————上面是对数据的操作，下面是对ui的操作————————————————————////////////////
////////////////——————————————————上面是对数据的操作，下面是对ui的操作————————————————————////////////////


    /**
     * 返回总共的条目数
     *
     * @return
     */
    @Override
    public int getItemCount() {
        return mDatas.size() + mHeadDatas.size() + mTailDatas.size() + mInsertDatas.size();
    }


    /**
     * 根据位置判断条目类型
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        //头
        if (position < mHeadDatas.size()) return TYPE_HEAD;
        //尾
        if (position >= mHeadDatas.size() + mDatas.size() + mInsertDatas.size())
            return TYPE_TAIL;
        //嵌套
        if (mInsertDatas.size() > 0) {
            for (InsertItemEntity entity : mInsertDatas)
                if (entity.getPosition() == position)
                    return TYPE_INSERT;
        }
        //默认布局
        return TYPE_NORMAL;
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
        int realPosition = position - getHeadSize();
        return realPosition;
    }

    /**
     * 获取默认条目位置
     *
     * @param position 真实位置
     * @return
     */
    public int getNormalPosition(int position) {
        int normalPosition = position;
        //嵌套布局数量
        int InsertSize = 0;
        for (InsertItemEntity entity : mInsertDatas) {
            if (entity.getPosition() <= position)
                InsertSize++;
        }
        normalPosition -= getHeadSize();
        normalPosition -= InsertSize;
        return normalPosition;
    }

////////////////——————————————————ui的初始化相关————————————————————////////////////

    /**
     * 初始化条目
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public RecyclerHolder onCreateViewHolder(ViewGroup parent, final int viewType) {
        View view = View.inflate(FcUtils.getContext(), getItemLayoutId(viewType), null);
        //判断条目类型是否是普通类型
        if (viewType != TYPE_NORMAL)
            return onCustomCreate(view, viewType);
        return onCreate(view, viewType);
    }

    /**
     * 布局引用
     *
     * @param viewType
     * @return
     */
    public abstract int getItemLayoutId(int viewType);

    /**
     * 初始化普通布局
     *
     * @param View
     * @param viewType
     * @return
     */
    protected abstract VH onCreate(View View, int viewType);

    /**
     * 殊条目初始化
     *
     * @param insertView
     * @param viewType
     * @return
     */
    public RecyclerHolder onCustomCreate(View insertView, int viewType) {
        if (insertView == null)
            insertView = View.inflate(FcUtils.getContext(), R.layout.null_frame, null);
        return new RecyclerHolder(insertView);
    }

////////////////——————————————————数据初始化相关————————————————————////////////////

    /**
     * 初始化对应条目数据
     *
     * @param viewHolder
     * @param position
     */
    @Override
    public void onBindViewHolder(final RecyclerHolder viewHolder, int position) {

        int viewType = getItemViewType(position);
        //判断布局类型执行相关超作
        if (viewType != TYPE_NORMAL) {
            bindCustomData(viewHolder, position, viewType);
        } else {
            final int normalPosition = getNormalPosition(position);
            final T data = mDatas.get(normalPosition);
            bindData((VH) viewHolder, normalPosition, data);

            if (mListener != null) {
                viewHolder.itemView.setOnClickListener(v ->
                        mListener.onItemClick(viewHolder, normalPosition, data));
            }
        }
    }


    /**
     * 对条目数据初始化
     *
     * @param viewHolder
     * @param position
     * @param data
     */
    public abstract void bindData(VH viewHolder, int position, T data);

    /**
     * 对特殊条目的初始化
     *
     * @param viewHolder
     * @param position
     * @param itemViewType
     */
    protected void bindCustomData(RecyclerHolder viewHolder, int position, int itemViewType) {
    }

////////////////————————————————————————其它方法——————————————————————————////////////////


    /**
     * 使用瀑布流时使头布局独占一行
     *
     * @param holder
     */
    @Override
    public void onViewAttachedToWindow(RecyclerHolder holder) {
        super.onViewAttachedToWindow(holder);
        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams//判断布局类型是瀑布流
                && (getHeadSize() > 0 | getTailSize() > 0)) {//判断是否包含头布局和尾布局
            StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
            boolean fullSpan = false;
            int position = holder.getLayoutPosition();
            //头布局
            if (position < getHeadSize())
                fullSpan = true;
            //尾布局
            if (position >= getHeadSize() + getDatasSize() + getInsertSize())
                fullSpan = true;
            //嵌套布局
            if (getInsertSize() > 0) for (InsertItemEntity entity : mInsertDatas) {
                if (entity.getPosition() == position) {
                    fullSpan = entity.isFull();
                    break;
                }
            }
            p.setFullSpan(fullSpan);
        }
    }

    /**
     * 使Grid布局时镶嵌布局独占一行
     *
     * @param recyclerView
     */
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
                    int type = getItemViewType(position);
                    if (type == TYPE_NORMAL) return 1;
                    else if (type == TYPE_HEAD || type == TYPE_TAIL)
                        return gridManager.getSpanCount();
                    else if (getInsertSize() > 0) for (InsertItemEntity entity : mInsertDatas) {
                        if (entity.getPosition() == position) {
                            return entity.isFull() ? gridManager.getSpanCount() : 1;
                        }
                    }
                    return 1;
                }
            });
        }
    }

    /**
     * 条目点击事件接口
     *
     * @param <T>
     * @param <VH>
     */
    public interface OnItemClickListener<T, VH extends RecyclerHolder> {
        void onItemClick(VH viewHolder, int position, T data);
    }
}
