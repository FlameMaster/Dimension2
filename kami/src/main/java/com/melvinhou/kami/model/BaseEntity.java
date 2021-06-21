package com.melvinhou.kami.model;

import com.google.gson.annotations.SerializedName;

/**
 * ===============================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2017/9/3 15:02
 * <p>
 * = 分 类 说 明：网络实体类
 * ================================================
 */

public interface BaseEntity<D>{

    /*获取数据模型*/
    D getData();
    /*是否成功登陆*/
    boolean isSuccess();
    /*获取返回码*/
    int getCode();
    /*获取返回信息*/
    @SerializedName(value = "message", alternate = {"msg"})
    String getMessage();
    /*获取时间*/
    long getDate();
}
