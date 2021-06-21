package com.melvinhou.dimension2.function.messenger;

import com.melvinhou.kami.util.FcUtils;

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
 * = 时 间：2021/6/20 17:55
 * <p>
 * = 分 类 说 明：实例化ImDB应该使用单例模式
 * ================================================
 */
class ImSqlManager {
    private static ImDB mDb;
    private static ImFriendDB mFriendDBDb;

    public static ImDB getIntance() {
        if (mDb == null) {
            mDb = Room.databaseBuilder(FcUtils.getContext(),
                    ImDB.class,"im.db").build();
        }
        return mDb;
    }

    /**
     * 对应好友的数据库
     * @param userId
     * @return
     */
    public static ImFriendDB getFriendIntance(long userId) {
        if (mFriendDBDb == null) {
            mFriendDBDb = Room.databaseBuilder(FcUtils.getContext(),
                    ImFriendDB.class,"im"+userId+".db").build();
        }
        return mFriendDBDb;
    }

    //新增
    public static synchronized void addImFriend(ImFriendEntity entity) {
        getIntance().imFriendDao().add(entity);
    }

    //查询
    public static synchronized ImFriendEntity getImFriend(long userId) {
        return getIntance().imFriendDao().query(userId);
    }

    //查询
    public static synchronized ImFriendEntity getImFriend(String ip) {
        return getIntance().imFriendDao().query(ip);
    }

    //删除
    public static synchronized void deleteImFriend(long userId) {
        getIntance().imFriendDao().delete(userId);
    }

    //修改
    public static synchronized void updateImFriend(ImFriendEntity entity) {
        getIntance().imFriendDao().update(entity);
    }

    //获取所有信息
    public static synchronized List<ImFriendEntity> getImFriends() {
        return getIntance().imFriendDao().getAll();
    }


}
