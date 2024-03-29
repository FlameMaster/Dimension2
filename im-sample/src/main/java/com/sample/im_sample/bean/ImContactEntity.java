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
 * = 时 间：2021/6/20 17:16
 * <p>
 * = 分 类 说 明：im好友数据库的表
 * ================================================
 */
@Entity
public class ImContactEntity {

    private String ip;//ip
    private int port;//端口号

    @PrimaryKey
    @ColumnInfo(name = User.USER_ID)
    private long userId;//用户id
    @ColumnInfo(name = "unread_count")
    private int unreadCount;//未读数

    private char initial;//首字母

    public ImContactEntity() {
    }

    @Ignore
    public ImContactEntity(long userId, String ip, int port) {
        this(userId,ip,port,'#');
    }

    @Ignore
    public ImContactEntity(long userId, String ip, int port, char initial) {
        this(userId,ip,port,initial,0);
    }

    @Ignore
    public ImContactEntity(long userId, String ip, int port, char initial, int unreadCount) {
        setUserId(userId);
        setIp(ip);
        setPort(port);
        setInitial(initial);
        setUnreadCount(unreadCount);
    }


    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public char getInitial() {
        return initial;
    }

    public void setInitial(char initial) {
        this.initial = initial;
    }

    @Override
    public String toString() {
        return "ImFriendsEntity{" +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", userId='" + userId + '\'' +
                ", initial='" + initial + '\'' +
                ", unreadCount=" + unreadCount +
                '}';
    }
}
