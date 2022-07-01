package com.melvinhou.dimension2.function.messenger;

import com.melvinhou.dimension2.user.User;

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
public interface ImFriendDao {

    //查全体
//    @Query("select * from ImFriendEntity")
//    List<ImFriendEntity> getAll();

    @Query("select * from ImFriendEntity")
    List<ImFriendEntity> getAll();

    @Query("select * from ImFriendEntity")
    LiveData<List<ImFriendEntity>> getAllFriends();

    //查id
    @Query("select * from ImFriendEntity where "+ User.USER_ID+"=:userId")
    ImFriendEntity query(long userId);

    //查id
    @Query("select * from ImFriendEntity where ip=:ip")
    ImFriendEntity query(String ip);


    //插入
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insert(ImFriendEntity imFriendEntity);

    //插入
    @Insert
    void add(ImFriendEntity imFriendEntity);

    @Insert
    void insert(ImFriendEntity... imFriendEntities);

    //删除
    @Query("delete from ImFriendEntity where "+ User.USER_ID+"=:userId")
    void delete(long userId);

    //更新
    @Update
    void update(ImFriendEntity imFriendEntity);

}
