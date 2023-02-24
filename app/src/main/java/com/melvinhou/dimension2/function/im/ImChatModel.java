package com.melvinhou.dimension2.function.im;

import android.app.Application;

import com.melvinhou.kami.util.DateUtils;
import com.melvinhou.kami.util.StringUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
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
 * = 时 间：2021/6/20 19:43
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
public class ImChatModel extends AndroidViewModel {

    public ImChatModel(@NonNull Application application) {
        super(application);
    }

    public void onCreate(long userId) {
        mDataSource = new LocalFriendDataSource(ImSqlManager.getFriendIntance(userId).chatHistoryDao());
    }


    /**
     * 生成一个聊天消息对象
     * @param userId
     * @param message
     * @return
     */
    ImChatMessageEntity createChatMessage(long userId, String message) {
        ImChatMessageEntity entity = new ImChatMessageEntity();
        long date = DateUtils.getCurrentTime();
        entity.setUuid(date);
        entity.setUserId(userId);
        entity.setMessage(message);
        entity.setDate(DateUtils.formatDuration(date, "yyyyMMdd_HHmmss"));
        return entity;
    }

    /**
     * DataSource 接口
     */
    private LocalFriendDataSource mDataSource;

//    private List<ImChatMessageEntity> chatMessages;


    /**
     * 从数据库中读取所有
     * <p>
     * 由于数据库中数据量可能很大，可能会因为背压导致内存溢出
     * 故采用 Flowable 模式，取代 Observable
     *
     * @return
     */
    public Flowable<List<ImChatMessageEntity>> getAllChatHistory() {
        return mDataSource.getAllChatHistory();
    }

    /**
     * 更新/添加 数据
     *
     * @return
     */
    public Completable putChatMessage(ImChatMessageEntity entity) {
        return mDataSource.insertOrUpdateChatHistory(entity);
    }

    /**
     * 删除聊天记录
     *
     * @param uuid
     */
    public void deleteChatMessage(long uuid) {
//        mDataSource.deleteChatHistory(uuid);
    }
}
