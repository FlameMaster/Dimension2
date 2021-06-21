package com.melvinhou.kami.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

import static com.melvinhou.kami.model.EventMessage.EventType.ALL;
import static com.melvinhou.kami.model.EventMessage.EventType.ASSIGN;

/**
 * ===========================================================
 * = 作 者：风 尘
 * <p>
 * = 版 权 所 有：7416064@qq.com
 * <p>
 * = 地 点：中 国 北 京 市 朝 阳 区
 * <p>
 * = 时 间：2018/12/4 09:36
 * <p>
 * = 分 类 说 明：rxbus使用的消息对象
 * ============================================================
 */
public class EventMessage {

    @IntDef({ALL,ASSIGN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EventType {

        /*全局接收*/
        int ALL = 0;

        /*指定*/
        int ASSIGN = 1;
    }


    private String message;
    private Object data;
    private int type;

    public EventMessage(String message) {
        this(EventType.ALL,message);
    }

    public EventMessage(@EventType int type, String message) {
        this(type,message,null);
    }

    public EventMessage(String message, Object data) {
        this(EventType.ALL,message,data);
    }

    public EventMessage(@EventType int type, String message, Object data) {
        this.type =type;
        this.message = message;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
