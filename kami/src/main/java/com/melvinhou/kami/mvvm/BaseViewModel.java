package com.melvinhou.kami.mvvm;


import android.util.Log;

import com.melvinhou.kami.model.BaseEntity;
import com.melvinhou.kami.model.StateModel;
import com.melvinhou.kami.net.BaseHttpObserver;
import com.melvinhou.kami.net.EmptyState;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2017/5/24 14:16
 * <p>
 * = 分 类 说 明：处理视图和数据的交互
 * ================================================
 */
public abstract class BaseViewModel<D,E extends BaseEntity<D>> extends BaseHttpObserver<D,E> {


    private StateModel mStateModel;

    public StateModel getStateModel() {
        return mStateModel;
    }

    /**
     * 初始化
     * @param emptyState 加载状态
     */
    public void initViewModel(@EmptyState int emptyState){
        mStateModel = new StateModel();
        mStateModel.setEmptyState(emptyState);
        dataBinding();
        initListener();
        initData();
    }


    /*数据绑定*/
    protected abstract void dataBinding();

    /*初始化监听*/
    protected abstract void initListener();

    /*初始化数据*/
    protected abstract void initData();

    /*刷新数据*/
    protected  void updateData(D data){

    }

    protected void onSuccees(E entity){
        mStateModel.setEmptyState(EmptyState.NORMAL);
        updateData(entity.getData());
    }

    protected void onFailure(Throwable e, boolean isNetWorkError){
        //判断是否是网络错误
        if (isNetWorkError){
            //提醒用户重新连接网络
            mStateModel.setEmptyState(EmptyState.NET_ERROR);
        }else {
            //这tm就等死吧
            mStateModel.setEmptyState(EmptyState.EMPTY);
        }
        Log.e("onFailure*****",mStateModel.getEmptyState()+"/"+isNetWorkError+"/"+e.getMessage());
    }
}
