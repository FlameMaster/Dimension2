package com.melvinhou.accountlibrary.db;

import com.melvinhou.accountlibrary.bean.User;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

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
public interface UserDao {

    //查全体
    @Query("select * from "+ User.TABLE_NAME)
    List<User> getAll();

    //查id
    @Query("select * from "+User.TABLE_NAME+" where " + User.USER_ID + "=:userId")
    User findUser(long userId);


    //插入
    @Insert(onConflict = OnConflictStrategy.REPLACE)//若数据库中已存在，则将其替换
    void addUser(User user);

    @Insert
    void addUsers(User... users);

    //删除
    @Delete
    void deleteUser(User user);

    //更新
    @Update
    void updateUser(User user);

}
