package com.melvinhou.dimension2;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2019/2/14 14:22
 * <p>
 * = 分 类 说 明：用户信息
 * ============================================================
 */
@Entity(tableName = User.TABLE_NAME)//表名
public class User {
    public static final String TABLE_NAME = "users";
    public static final String NICK_NAME = "nick_name";
    public static final String USER_ID = "user_id";

    /*基础信息*/
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = USER_ID)
    private long userId;
    private String uuid;
    private String name;
    @ColumnInfo(name = NICK_NAME)
    private String nickName;
    private String photo;

    /*资料*/
    private String phone;
    private String email;
    private String qq;
    private String weChat;
    private String blog;

    /*简介*/
    private String intro;


    public User(){

    }

    //消除不必要的构造函数
    @Ignore
    public User(long userId, String uuid, String name, String nickName,
                String photo, String phone, String email,
                String qq, String weChat, String blog, String intro) {
        this.userId = userId;
        this.uuid = uuid;
        this.name = name;
        this.nickName = nickName;
        this.photo = photo;
        this.phone = phone;
        this.email = email;
        this.qq = qq;
        this.weChat = weChat;
        this.blog = blog;
        this.intro = intro;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUuid() {
        return uuid;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getWeChat() {
        return weChat;
    }

    public void setWeChat(String weChat) {
        this.weChat = weChat;
    }

    public String getBlog() {
        return blog;
    }

    public void setBlog(String blog) {
        this.blog = blog;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }


    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", nickName='" + nickName + '\'' +
                ", photo='" + photo + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", qq='" + qq + '\'' +
                ", weChat='" + weChat + '\'' +
                ", blog='" + blog + '\'' +
                ", intro='" + intro + '\'' +
                '}';
    }
}
