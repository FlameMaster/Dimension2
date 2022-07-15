package com.melvinhou.dimension2.function.im;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/6/20 20:06
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class LocalFriendDataSource {

    private final ImChatHistoryDao mChatHistoryDao;

    public LocalFriendDataSource(ImChatHistoryDao dao) {
        this.mChatHistoryDao = dao;
    }


    /**
     * 从数据库中读取信息
     * 由于读取速率可能 远大于 观察者处理速率，故使用背压 Flowable 模式
     */
    public Flowable<List<ImChatMessageEntity>> getAllChatHistory(){
        return mChatHistoryDao.getAll();
    }


    /**
     * 将数据写入数据库中
     * 如果数据已经存在则进行更新
     * Completable 可以看作是 RxJava 的 Runnale 接口
     * 但他只能调用 onComplete 和 onError 方法，不能进行 map、flatMap 等操作
     */
    public Completable insertOrUpdateChatHistory(ImChatMessageEntity entity){
        return mChatHistoryDao.insert(entity);
    }


    /**
     * 删除聊天记录
     */
    public void  deleteChatHistory(ImChatMessageEntity message){
        mChatHistoryDao.delete(message);
    }
}
