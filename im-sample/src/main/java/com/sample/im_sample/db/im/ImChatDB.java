package com.sample.im_sample.db.im;

import com.sample.im_sample.bean.ImChatEntity;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/6/20 21:06
 * <p>
 * = 分 类 说 明：即时通讯对应好友的数据库
 * ================================================
 */
@Database(entities = {ImChatEntity.class}, version = 1 )
public abstract class ImChatDB extends RoomDatabase {
    public abstract ImChatHistoryDao chatHistoryDao();
}
