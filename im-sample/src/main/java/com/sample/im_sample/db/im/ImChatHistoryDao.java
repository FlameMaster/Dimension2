package com.sample.im_sample.db.im;

import com.sample.im_sample.bean.ImChatEntity;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
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
 * = 时 间：2021/6/20 21:09
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
@Dao
public interface ImChatHistoryDao {

    @Query("select * from " + ImChatEntity.TABLE_NAME)
    Flowable<List<ImChatEntity>> getAll();


    //插入,若数据库中已存在，则将其替换
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(ImChatEntity message);


    //删除
    @Delete
    void delete(ImChatEntity message);
}
