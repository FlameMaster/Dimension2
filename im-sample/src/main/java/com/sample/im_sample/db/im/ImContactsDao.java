package com.sample.im_sample.db.im;

import com.sample.im_sample.bean.ImContactEntity;
import com.melvinhou.accountlibrary.bean.User;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import io.reactivex.Completable;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/6/20 17:34
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
@Dao
public interface ImContactsDao {

    //查全体
//    @Query("select * from ImContactEntity")
//    List<ImContactEntity> getAll();

    @Query("select * from ImContactEntity")
    List<ImContactEntity> getAll();

    @Query("select * from ImContactEntity")
    LiveData<List<ImContactEntity>> getAllContacts();

    //查id
    @Query("select * from ImContactEntity where "+ User.USER_ID+"=:userId")
    ImContactEntity query(long userId);

    //查id
    @Query("select * from ImContactEntity where ip=:ip")
    ImContactEntity query(String ip);


    //插入
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(ImContactEntity imContactEntity);

    //插入
    @Insert
    void add(ImContactEntity imContactEntity);

    @Insert
    void insert(ImContactEntity... imContactEntities);

    //删除
    @Query("delete from ImContactEntity where "+ User.USER_ID+"=:userId")
    void delete(long userId);

    //更新
    @Update
    void update(ImContactEntity imContactEntity);

}
