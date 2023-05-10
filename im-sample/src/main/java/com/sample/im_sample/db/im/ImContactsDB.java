package com.sample.im_sample.db.im;

import com.sample.im_sample.bean.ImContactEntity;

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
 * = 时 间：2021/6/20 17:10
 * <p>
 * = 分 类 说 明：通过room创建数据库
 * ================================================
 */
@Database(entities = {ImContactEntity.class}, version = 1 )
public abstract class ImContactsDB extends RoomDatabase {
    public abstract ImContactsDao imContactsDao();
}
