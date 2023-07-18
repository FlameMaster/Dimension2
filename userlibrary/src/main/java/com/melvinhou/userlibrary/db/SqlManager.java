package com.melvinhou.userlibrary.db;

import android.content.Context;

import com.melvinhou.userlibrary.bean.User;

import java.util.List;

import androidx.room.Room;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/6/20 18:47
 * <p>
 * = 分 类 说 明：全局的数据库访问
 * ================================================
 */
public class SqlManager {
    private static ImSampleDB mDb;
    public static ImSampleDB getIntance(Context context){
        if (mDb == null) {
            mDb = Room.databaseBuilder(context,
                    ImSampleDB.class,
                    "dimension2.db").build();
        }
        return mDb;
    }
    //新增
    public static synchronized void addUser(Context context, User user) {
        getIntance(context).userDao().addUser(user);
    }
    //新增
    public static synchronized void addUsers(Context context, User... users) {
        getIntance(context).userDao().addUsers(users);
    }
    //查询
    public static synchronized User findUser(Context context, long userId) {
        return getIntance(context).userDao().findUser(userId);
    }
    //删除
    public static synchronized void deleteUser(Context context, User user) {
        getIntance(context).userDao().deleteUser(user);
    }
    //修改
    public static synchronized void updateUser(Context context, User user) {
        getIntance(context).userDao().updateUser(user);
    }
    //获取用户信息
    public static synchronized List<User> getAllUsers(Context context) {
        return getIntance(context).userDao().getAll();
    }

    //手机号查询
    public static synchronized User findUserByPhone(Context context, String phone) {
        return getIntance(context).userDao().findUserByPhone(phone);
    }
}
