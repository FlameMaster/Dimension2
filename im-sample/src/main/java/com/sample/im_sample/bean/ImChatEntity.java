package com.sample.im_sample.bean;

import com.melvinhou.userlibrary.bean.User;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：melvinhou@163.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2021/6/16 22:16
 * <p>
 * = 分 类 说 明：
 * ================================================
 */
@Entity(tableName = ImChatEntity.TABLE_NAME)
public class ImChatEntity {

    public static final String TABLE_NAME = "chat_history";

    @PrimaryKey(autoGenerate = true)
    private long uuid;
    private String message;
    private String date;
    @ColumnInfo(name = User.USER_ID)
    private long userId;

    public ImChatEntity(){

    }

    @Ignore
    public ImChatEntity(long uuid, String message, String date, long userId){
        setUserId(uuid);
        setUserId(userId);
        setMessage(message);
        setDate(date);
    }

    public long getUuid() {
        return uuid;
    }

    public void setUuid(long uuid) {
        this.uuid = uuid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }


    @Override
    public String toString() {
        return "ImChatEntity{" +
                "uuid=" + uuid +
                ", message='" + message + '\'' +
                ", date='" + date + '\'' +
                ", userId=" + userId +
                '}';
    }
}
